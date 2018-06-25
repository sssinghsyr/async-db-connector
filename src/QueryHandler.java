import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class QueryHandler {
	private static DbHandler dh = new DbHandler("10.176.16.231");
	
	public static CompletableFuture<?> processQuery(String sql, ResponseHandler respHandl) throws Exception {
		CompletableFuture<?> fut = null;
		if(sql.contains("INSERT") || sql.contains("insert") || sql.contains("UPDATE") || sql.contains("update"))
			fut = dh.countOperation(sql);
		else if(sql.contains("SELECT") || sql.contains("select"))
			fut = dh.multipleRowOperation(sql, respHandl);
		else
			throw new Exception("Unhandled Query!");
		return fut;
	}
}
