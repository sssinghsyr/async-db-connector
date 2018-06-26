import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseHandler {
	
	static final int MAX_T = 5;
	private static ExecutorService pool;

	ResponseHandler(SocketChannel channel) {
		this.channel = channel;
	}
	
	public static void init() {
		if(pool == null)
			pool = Executors.newFixedThreadPool(MAX_T);
	}
	
	public static void close() {
		if(pool != null)
			pool.shutdown();
	}

	private SocketChannel channel;

	public void sendResponseToClient(CompletableFuture<?> future) {
		pool.execute(new Runnable() {
			
			@Override
			public void run() {
				ByteBuffer bb;
				try {
					String result = (String) future.get();
					result += "END\n";
					bb = ByteBuffer.wrap(result.getBytes());

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
		})  ;
	}
}
