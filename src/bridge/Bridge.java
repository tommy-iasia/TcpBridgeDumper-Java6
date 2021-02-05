package bridge;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Bridge implements Closeable {

    public Bridge(int id, SocketChannel serverChannel, SocketChannel clientChannel, Logger logger) throws IOException {
        this.id = id;

        this.serverChannel = serverChannel;
        this.clientChannel = clientChannel;

        rightRoad = new Road(serverChannel, clientChannel, logger);
        leftRoad = new Road(clientChannel, serverChannel, logger);

        this.logger = logger;
    }
    private int id;

    private final Road rightRoad;
    private final Road leftRoad;
    public void run() throws IOException {
        if (rightRoad.registered()) {
            rightRoad.forward();
        } else {
            rightRoad.register();

            if (rightRoad.registered()) {
                logger.log("right road "
                        + serverChannel.socket().getLocalAddress()
                        + " > " + clientChannel.socket().getRemoteSocketAddress()
                        + " is established");
            }
        }

        if (leftRoad.registered()) {
            leftRoad.forward();
        } else {
            leftRoad.register();

            if (leftRoad.registered()) {
                logger.log("left road "
                        + serverChannel.socket().getLocalAddress()
                        + " > " + clientChannel.socket().getRemoteSocketAddress()
                        + " is established");
            }
        }
    }

    private final SocketChannel serverChannel;
    private final SocketChannel clientChannel;
    public boolean connected() {
        return serverChannel.isConnected()
                && clientChannel.isConnected();
    }

    private Logger logger;
    public String tag() {
        return id
                + ", " + serverChannel.socket().getLocalAddress()
                + " < " + clientChannel.socket().getRemoteSocketAddress();
    }
    public void count() {
        String text = "[" + tag() + "] "
                + "right { " + rightRoad.count() + " }, "
                + "left { " + leftRoad.count() + " }";

        boolean registered = leftRoad.registered() && rightRoad.registered();
        if (!registered) {
            text += ", not connected";
        }

        logger.log(text);
    }

    public void dumping(boolean dumping) throws IOException {
        rightRoad.dumping(dumping);
        leftRoad.dumping(dumping);
    }

    @Override
    public void close() throws IOException {
        try {
            clientChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
