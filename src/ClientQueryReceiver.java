import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientQueryReceiver {
	private static String host = "127.0.0.1";
	private static int qrcv_port = 4444; //query receiver port

	public static Selector selector;

	public static void queryListner() throws Exception {  
		selector = Selector.open();
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(host, qrcv_port));
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			if (selector.select() <= 0) {
				continue;
			}
			processReadySet(selector.selectedKeys());
		}
	}

	public static void main(String[] args) throws Exception {
		queryListner();
	}

	public static void processReadySet(Set<SelectionKey> readySet) throws Exception {
		Iterator<SelectionKey> iterator = readySet.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			iterator.remove();
			if (key.isAcceptable()) {
				ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
				SocketChannel sChannel = (SocketChannel) ssChannel.accept();
				sChannel.configureBlocking(false);
				sChannel.register(key.selector(), SelectionKey.OP_READ);
			}
			if (key.isReadable()) {
				String msg = processRead(key);
				if (msg != null && msg.length() > 0) {
					// TODO Send query to the API
					QueryHandler.processQuery(msg, new ResponseHandler((SocketChannel)key.channel()));
				}
			}
		}
	}

	public static String processRead(SelectionKey key) throws Exception {
		SocketChannel sChannel = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		try {
			int bytesCount = sChannel.read(buffer);
			if (bytesCount > 0) {
				buffer.flip();
				return StandardCharsets.UTF_8.decode(buffer).toString();
			}
			if(bytesCount == -1 && !sChannel.isOpen()) {
				// Close connection
				sChannel.close();
				key.cancel();
			}
			return null;
		}catch(IOException ex) {
			System.out.println("Connection closed by the client");
		}
		return null;
	}
}