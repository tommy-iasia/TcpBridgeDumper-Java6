package bridge;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.*;

public class Program {

    public static void main(String[] args) throws InterruptedException, IOException {
        Properties properties = new Properties();

        FileInputStream file = new FileInputStream("Program.properties");
        properties.load(file);

        file.close();

        String inwardHost = properties.getProperty("InwardHost");
        int inwardPort = Integer.parseInt(properties.getProperty("InwardPort"));
        InetSocketAddress inwardAddress = new InetSocketAddress(inwardHost, inwardPort);

        String outwardHost = properties.getProperty("OutwardHost");
        int outwardPort = Integer.parseInt(properties.getProperty("OutwardPort"));
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
            logger.log(e);

            Thread.sleep(100);
        }

        List<Bridge> errorBridges = new LinkedList<Bridge>();
        for (Bridge bridge : bridges) {
            try {
                bridge.run();
            } catch (IOException e) {
                logger.log(e);

                errorBridges.add(bridge);
                Thread.sleep(100);
            }
        }

        drop(errorBridges);

        try {
            count();
        } catch (IOException e) {
            logger.log(e);

            Thread.sleep(100);
        }

        dropDisconnects();

        try {
            dumping();
        } catch (IOException e) {
            logger.log(e);

            Thread.sleep(10);
        }
    }

    private Program(InetSocketAddress inwardAddress, InetSocketAddress outwardAddress) throws IOException {
        server = new Server(inwardAddress);

        this.inwardAddress = inwardAddress;
        this.outwardAddress = outwardAddress;

        logger = new Logger("Program", true);

        logger.log("inward: " + inwardAddress);
        logger.log("outward: " + outwardAddress);
    }

    private final Server server;
    private final InetSocketAddress inwardAddress;
    private final InetSocketAddress outwardAddress;
    private int count = 0;
    private void accept() throws IOException {
        SocketChannel clientChannel = server.accept();
        if (clientChannel == null) {
            return;
        }

        Socket clientSocket = clientChannel.socket();
        InetAddress clientLocalAddress = clientSocket.getLocalAddress();
        SocketAddress clientRemoteAddress = clientSocket.getRemoteSocketAddress();
        logger.log("client received from " + clientLocalAddress + " from " + clientRemoteAddress);

        clientChannel.configureBlocking(false);

        SocketChannel serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.connect(outwardAddress);

        Bridge bridge = new Bridge(count++, serverChannel, clientChannel, logger);
        bridges.add(bridge);
    }

    private final List<Bridge> bridges = new ArrayList<Bridge>();

    private long lastCountTime = 0;
    private final static long countInterval = 5000;
    private void count() throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCountTime < countInterval) {
            return;
        }

        logger.log("bridges: " + bridges.size());

        for (Bridge bridge : bridges) {
            bridge.count();
        }

        lastCountTime = currentTime;
    }

    private void dropDisconnects() {
        List<Bridge> disconnects = new LinkedList<Bridge>();

        for (Bridge bridge : bridges) {
            if (!bridge.connected()) {
                disconnects.add(bridge);
            }
        }

        drop(disconnects);
    }
    private void drop(List<Bridge> bridges) {
        for (Bridge bridge : bridges) {
            try {
                logger.log("drop bridge: " + bridge.tag());

                bridge.close();
            } catch (IOException e) {
                logger.log(e);
            }

            this.bridges.remove(bridge);
        }
    }

    private final Logger logger;

    private void dumping() throws IOException {
        while (System.in.available() > 0) {
            int read = System.in.read();
            switch (read) {
                case '1':
                    logger.log("dumping turned on");
                    dumping(true);
                    break;

                case '0':
                    logger.log("dumping turned off");
                    dumping(false);
                    break;

                case '\r':
                case '\n': break;

                default:
                    logger.log("unrecognized input " + read);
                    break;
            }
        }
    }
    private void dumping(boolean dumping) {
        for (Bridge bridge : bridges) {
            try {
                logger.log(
                        "turn " + (dumping ? "on" : "off") + " dumping: "
                        + bridge.tag());

                bridge.dumping(dumping);
            } catch (IOException e) {
                logger.log(e);
            }
        }
    }
}
