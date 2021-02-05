package bridge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Road {

    public Road(SocketChannel readChannel, SocketChannel writeChannel, Logger logger) throws IOException {
        this.readChannel = readChannel;
        this.writeChannel = writeChannel;

        this.logger = logger;
    }
    private SocketChannel readChannel;
    private SocketChannel writeChannel;

    private final Selector selector = Selector.open();
    private boolean readRegistered = false;
    private boolean writeRegistered = false;
    public void register() throws IOException {
        if (!readRegistered) {
            if (readChannel.finishConnect()) {
                readChannel.register(selector, SelectionKey.OP_READ);
                readRegistered = true;
            }
        }

        if (!writeRegistered) {
            if (writeChannel.finishConnect()) {
                writeChannel.register(selector, SelectionKey.OP_WRITE);
                writeRegistered = true;
            }
        }
    }
    public boolean registered() {
        return readRegistered && writeRegistered;
    }

    private final ByteBuffer buffer = ByteBuffer.allocate(5 * 1024 * 1024);
    public void forward() throws IOException {
        if (selector.selectNow() < 0) {
            return;
        }

        Set<SelectionKey> keySet = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();

            if (!key.isValid()) {
                continue;
            }

            SocketChannel channel = (SocketChannel) key.channel();

            if (key.isReadable()) {
                if (buffer.remaining() > 0) {
                    long readStart = System.nanoTime();
                    int readLength = channel.read(buffer);
                    long readEnd = System.nanoTime();

                    long readTime = readEnd - readStart;
                    if (readLength > 0) {
                        readCounter.content(readLength, readTime);

                        if (readDumper != null) {
                            dump(buffer, readLength, readDumper);
                        }
                    } else {
                        readCounter.zero(readTime);
                    }
                }
            } else if (key.isWritable()) {
                if (buffer.position() > 0) {
                    buffer.flip();

                    long writeStart = System.nanoTime();
                    int writeLength = channel.write(buffer);
                    long writeEnd = System.nanoTime();

                    long writeTime = writeEnd - writeStart;
                    if (writeLength > 0) {
                        writeCounter.content(writeLength, writeTime);

                        if (writeDumper != null) {
                            dump(buffer, writeLength, writeDumper);
                        }
                    } else {
                        writeCounter.zero(writeTime);
                    }
                    buffer.compact();
                }
            }
        }
    }

    private final Logger logger;
    public void dumping(boolean dumping) throws IOException {
        if (readDumper != null) {
            logger.log("close dumper: " + readDumper.fileName());
            readDumper.close();
            readDumper = null;

            logger.log("close dumper: " + writeDumper.fileName());
            writeDumper.close();
            writeDumper = null;
        }

        if (dumping) {
            readDumper = new Dumper();
            logger.log("read dumper created: " + readDumper.fileName());

            writeDumper = new Dumper();
            logger.log("write dumper created: " + writeDumper.fileName());
        }
    }
    private Dumper readDumper = null;
    private Dumper writeDumper = null;
    private static void dump(ByteBuffer buffer, int count, Dumper dumper) throws IOException {
        buffer.position(buffer.position() - count);

        byte[] bytes = new byte[count];
        buffer.get(bytes);

        dumper.dump(bytes);
    }

    private final Counter readCounter = new Counter();
    private final Counter writeCounter = new Counter();
    public String count() {
        int position = buffer.position();

        String output = "read { " + readCounter + " }, "
                + "write { " + writeCounter + "}, "
                + "buffer " + position / 1024 / 1024 + "MB / " + position / 1024 + "KB / " + position + "B";

        if (readDumper != null) {
            output += ", dump read";
        }

        if (writeDumper != null) {
            output += ", dump write";
        }

        return output;
    }
}
