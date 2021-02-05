package bridge;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public Logger(String name, boolean console) throws FileNotFoundException {
        String logPath = name + "." + startTime + ".log";

        FileOutputStream logStream = new FileOutputStream(logPath);
        this.logStream = new PrintStream(logStream);

        this.console = console;
    }
    private final long startTime = System.currentTimeMillis();

    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private String getTimeTag() {
        Date date = new Date();
        return "[" + timeFormat.format(date) + "]";
    }

    private final PrintStream logStream;
    private final boolean console;
    public void log(String line) {
        String timeTag = getTimeTag();
        String tagLine = timeTag + " " + line;

        if (console) {
            System.out.println(tagLine);
        }

        logStream.println(tagLine);
    }
    public void log(Exception exception) {
        String timeTag = getTimeTag();

        logStream.print(timeTag + " ");
        exception.printStackTrace(logStream);

        if (console) {
            System.out.println(timeTag + " " + exception);
        }
    }
}
