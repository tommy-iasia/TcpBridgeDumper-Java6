package bridge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Road {

    public Road(SocketChannel readChannel, SocketChannel writeChannel) throws IOException {
        readChannel.register(selector, SelectionKey.OP_READ);
        writeChannel.register(selector, SelectionKey.OP_WRITE);
    }
    private final Selector selector = Selector.open();

    private final ByteBuffer buffer = ByteBuffer.allocate(5 * 1024 * 1024);
    public void run() throws IOException {
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
                    int read = channel.read(buffer);
                    readCounter.add(read);
                }
            } else if (key.isWritable()) {
                if (buffer.position() > 0) {
                    buffer.flip();
                    int write = channel.write(buffer);
                    writeCounter.add(write);
                    buffer.compact();
                }
            }
        }
    }

    private final Counter readCounter = new Counter();
    private final Counter writeCounter = new Counter();

    public String count() {
        return "read " + readCounter + ", "
                + "write " + writeCounter + ", "
                + "buffer " + buffer.position() + "B";
    }
}
