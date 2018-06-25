import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ResponseHandler {

	ResponseHandler(SocketChannel channel) {
		this.channel = channel;
	}

	private SocketChannel channel;

	public void sendResponseToClient(CompletableFuture<?> future) {
		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				ByteBuffer bb;
				try {
					String result = (String) future.get();
					//System.out.println("Inside sendResposeToClient: "+result);
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
		th.start();
	}
}
