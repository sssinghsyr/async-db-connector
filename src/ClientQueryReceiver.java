import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientQueryReceiver {
	private static String host = "127.0.0.1";
	private static int qrcv_port = 4444; //query receiver port
	private static int count = 0;

	public static Selector selector;
	private static Map<Integer, ResponseHandler> futures = new HashMap<Integer, ResponseHandler>();

	public static void queryListner() throws Exception {  
		selector = Selector.open();
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(host, qrcv_port));
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			if (selector.select() <= 0) {
				replyQueryResp();
				continue;
			}
			processReadySet(selector.selectedKeys());
			if(!QueryHandler.readyResponseIdx.isEmpty()) {
				replyQueryResp();
			}
		}
	}

	private static void replyQueryResp() {
		try {
			int fut_idx = QueryHandler.readyResponseIdx.poll();
			String result = QueryHandler.getRespose(futures.get(fut_idx).getFuture());
			result += "END\n";
			ByteBuffer bb = ByteBuffer.wrap(result.getBytes());
			while(bb.hasRemaining())
				futures.get(fut_idx).getChannel().write(bb);
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
					int future_key = intKeyGenerator();
					CompletableFuture<?> fut = QueryHandler.processQuery(msg, future_key);
					futures.put(future_key, new ResponseHandler((SocketChannel)key.channel(), fut));
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
	
	private static Integer intKeyGenerator() {
		count++;
		return count;
	}
}