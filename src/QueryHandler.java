import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class QueryHandler {
	public static Queue<Integer> readyResponseIdx = new ConcurrentLinkedQueue<Integer>();
	private static DbHandler dh = new DbHandler("10.176.16.231");
	
	public static CompletableFuture<?> processQuery(String sql, int future_key) throws Exception {
		CompletableFuture<?> fut = null;
		if(sql.contains("INSERT") || sql.contains("insert") || sql.contains("UPDATE") || sql.contains("update"))
			fut = dh.countOperation(sql);
		else if(sql.contains("SELECT") || sql.contains("select"))
			fut = dh.multipleRowOperation(sql, future_key);
		else
			throw new Exception("Unhandled Query!");
		return fut;
	}
	
	public static String getRespose(Future<?> fut) throws InterruptedException, ExecutionException {
		return (String) fut.get();
	}
}
