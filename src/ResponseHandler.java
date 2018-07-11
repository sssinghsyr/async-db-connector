import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ResponseHandler {

	static final int TXN_CNT = 6005;
	private int idx;

	ResponseHandler(SocketChannel channel, int idx) {
		this.channel = channel;
		this.idx = idx;

	}

	private SocketChannel channel;

	public void sendResponseToClient(CompletableFuture<?> future) {
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
			double end = System.nanoTime() / 1e3;
			System.out.println("Throughput for "+TXN_CNT+" connections = "+(TXN_CNT/((end-ClientQueryReceiver.start)/1e6))+" req/sec");
		}
	}
}
