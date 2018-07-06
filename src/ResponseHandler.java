import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseHandler {

	static final int MAX_T = 5;
	static final int TXN_CNT = 7000;
	private static ExecutorService pool;
	private int idx;
	//private long start;

	ResponseHandler(SocketChannel channel, int idx) {
		this.channel = channel;
		this.idx = idx;
		//System.out.println("RspnsHdlr for "+idx);
		//this.start = System.nanoTime();
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
					
					channel.close();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(idx == TXN_CNT) {
					//System.out.printf("End curr time %.1f us%n", System.nanoTime() / 1e3);
					double end = System.nanoTime() / 1e3;
					
					System.out.println("Throughput for "+TXN_CNT+" connections = "+(TXN_CNT/((end-ClientQueryReceiver.start)/1e6))+" req/sec");
				}
				/*long end = System.nanoTime();
				long err = System.nanoTime() - end;
				long time = end - start - err;
				System.out.printf("Execution time %.1f us%n", time / 1e3);*/
			}
		})  ;
	}
}
