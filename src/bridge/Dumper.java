package bridge;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dumper implements Closeable {

    public Dumper() throws FileNotFoundException {
        Date time = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeText = timeFormat.format(time);

        int randomId = (int) (Math.random() * 10000);

        fileName = "Dump." + timeText + "." + randomId + ".log";

        stream = new FileOutputStream(fileName);
    }

    private final String fileName;
    public String fileName() {
        return fileName;
    }

    private final FileOutputStream stream;
    private long count = 0;
    public void dump(byte[] bytes) throws IOException {
        ByteBuffer headerBuffer = ByteBuffer.allocate(8 + 4);
        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

        long time = System.currentTimeMillis();
        headerBuffer.putLong(time);

        headerBuffer.putInt(bytes.length);

        stream.write(headerBuffer.array());

        stream.write(bytes);

        if (count++ % 10 == 100) {
            stream.flush();
        }
    }

    @Override
    public void close() throws IOException {
        stream.flush();

        stream.close();
    }
}
