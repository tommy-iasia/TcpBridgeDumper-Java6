package bridge;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Bridge implements Closeable {

    public Bridge(SocketChannel serverChannel, SocketChannel clientChannel) throws IOException {
        this.serverChannel = serverChannel;
        this.clientChannel = clientChannel;

        rightRoad = new Road(serverChannel, clientChannel);
        leftRoad = new Road(clientChannel, serverChannel);
    }

    private final SocketChannel serverChannel;
    public boolean finishConnect() throws IOException {
        return serverChannel.finishConnect();
    }

    private final SocketChannel clientChannel;

    private final Road rightRoad;
    private final Road leftRoad;
    public void run() throws IOException {
        rightRoad.run();
        leftRoad.run();
    }

    public String channel() {
        return serverChannel.socket().getLocalAddress()
                + " < " + clientChannel.socket().getRemoteSocketAddress();
    }
    public String count() {
        return "right {" + rightRoad.count() + "}, "
                + "left {" + leftRoad.count() + "}";
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
