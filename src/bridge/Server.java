package bridge;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public Server(InetSocketAddress address) throws IOException {
        channel.configureBlocking(false);

        channel.socket().bind(address);

        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private final ServerSocketChannel channel = ServerSocketChannel.open();
    private final Selector selector = Selector.open();

    public SocketChannel accept() throws IOException {
        if (selector.selectNow() <= 0) {
            return null;
        }

        Set<SelectionKey> keySet = selector.selectedKeys();
        Iterator<SelectionKey> keyIterator = keySet.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();

            if (!key.isValid()) {
                continue;
            }

            if (!key.isAcceptable()) {
                continue;
            }

            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
            return channel.accept();
        }

        return null;
    }
}
