package bridge;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;

public class Program {

    public static void main(String[] args) throws InterruptedException, IOException {
        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));

        String inwardHost = arguments.poll();
        int inwardPort = Integer.parseInt(arguments.poll());
        InetSocketAddress inwardAddress = new InetSocketAddress(inwardHost, inwardPort);

        String outwardHost = arguments.poll();
        int outwardPort = Integer.parseInt(arguments.poll());
        InetSocketAddress outwardAddress = new InetSocketAddress(outwardHost, outwardPort);

        Program program = new Program(inwardAddress, outwardAddress);
        while (true) {
            program.run();
        }
    }
    private void run() throws InterruptedException {
        try {
            accept();
        } catch (IOException e) {
            logException(e);

            Thread.sleep(100);
        }

        List<Bridge> errorBridges = new LinkedList<Bridge>();

        for (Bridge bridge : bridges) {
            try {
                forward(bridge);
            } catch (IOException e) {
                logException(e);

                errorBridges.add(bridge);

                Thread.sleep(100);
            }
        }

        for (Bridge errorBridge : errorBridges) {
            try {
                errorBridge.close();
            } catch (IOException e) {
                logException(e);

                Thread.sleep(100);
            }
        }

        try {
            count();
        } catch (IOException e) {
            logException(e);

            Thread.sleep(100);
        }
    }

    private Program(InetSocketAddress inwardAddress, InetSocketAddress outwardAddress) throws IOException {
        server = new Server(inwardAddress);

        this.inwardAddress = inwardAddress;
        this.outwardAddress = outwardAddress;

        String logPath = "Count." + startTime + ".log";
        FileOutputStream logStream = new FileOutputStream(logPath);
        this.logStream = new PrintStream(logStream);

        logLine("inward: " + inwardAddress);
        logLine("outward: " + outwardAddress);
    }
    private long startTime = System.currentTimeMillis();

    private final Server server;
    private final InetSocketAddress inwardAddress;
    private void accept() throws IOException {
        SocketChannel clientChannel = server.accept();
        if (clientChannel == null) {
            return;
        }

        SocketChannel serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(outwardAddress);

        Bridge bridge = new Bridge(serverChannel, clientChannel);
        bridges.add(bridge);
    }

    private final InetSocketAddress outwardAddress;
    private final List<Bridge> bridges = new ArrayList<Bridge>();
    private static void forward(Bridge bridge) throws IOException {
        if (!bridge.finishConnect()) {
            return;
        }

        bridge.run();
    }

    private long lastCountTime = 0;
    private final static long countInterval = 3000;
    private void count() throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCountTime < countInterval) {
            return;
        }

        logLine("bridges: " + bridges.size());

        for (Bridge bridge : bridges) {
            String channel = bridge.channel();
            if (bridge.finishConnect()) {
                String countText = bridge.count();
                logLine("[" + channel + "] " + countText);
            } else {
                logLine("[" + channel + "] not connected");
            }
        }

        lastCountTime = currentTime;
    }

    private final static SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss");
    private void logTime() {
        Date date = new Date();
        String dateText = "[" + timeFormat.format(date) + "] ";

        System.out.print(dateText);
        logStream.print(dateText);
    }

    private final PrintStream logStream;
    private void logLine(String line) {
        logTime();

        System.out.println(line);
        logStream.println(line);
    }
    private void logException(Exception exception) {
        logTime();

        exception.printStackTrace(logStream);

        System.out.println(exception.toString());
    }
}
