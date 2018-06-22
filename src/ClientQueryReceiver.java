import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ClientQueryReceiver {
	static String host = "127.0.0.1";
	static int qrcv_port = 4444; //query receiver port

	public static Selector selector;
	private static List<SocketChannel> channels = new ArrayList<SocketChannel>();

	public static void queryListner() throws Exception {  
		selector = Selector.open();
		ServerSocketChannel ssChannel = ServerSocketChannel.open();
		ssChannel.configureBlocking(false);
		ssChannel.socket().bind(new InetSocketAddress(host, qrcv_port));
		ssChannel.register(selector, SelectionKey.OP_ACCEPT);
		while (true) {
			if (selector.select() <= 0) {
				// selector has been woken up 
				// Message passing will be needed to determine index of response ready
				//int idx = QueryHandler.readyResponseIdx.poll();
				replyQueryResp(0);
				continue;
			}
			processReadySet(selector.selectedKeys());
		}
	}

	private static void replyQueryResp(int idx) {
		try {
			String result = QueryHandler.getRespose();
			SocketChannel channel = channels.get(0);
			result += "END\n";
			ByteBuffer bb = ByteBuffer.wrap(result.getBytes());
			while(bb.hasRemaining())
				channel.write(bb);
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
				channels.add(sChannel);
			}
			if (key.isReadable()) {
				String msg = processRead(key);
				if (msg != null && msg.length() > 0) {
					System.out.println("Received Query: "+ msg);
					// TODO Send query to the API
					QueryHandler.processQuery(msg);
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
				//return new String(buffer.array());
				//return new String( buffer.array(), Charset.forName("UTF-8") );
				return StandardCharsets.UTF_8.decode(buffer).toString();
			}
			if(bytesCount == -1 && !sChannel.isOpen()) {
				// Close connection
				sChannel.close();
				key.cancel();
				channels.remove(sChannel);
			}
			return null;
		}catch(IOException ex) {
			System.out.println("Connection closed by the server");
		}
		return null;
	}
}