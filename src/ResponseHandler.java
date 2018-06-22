import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;

public class ResponseHandler {
	public CompletableFuture<?> getFuture() {
		return future;
	}
	public SocketChannel getChannel() {
		return channel;
	}
	ResponseHandler(SocketChannel channel, CompletableFuture<?> future) {
		this.channel = channel;
		this.future = future;
	}
	
	private CompletableFuture<?> future;
	private SocketChannel channel;
}
