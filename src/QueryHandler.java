import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class QueryHandler {
	public static Queue<Integer> readyResponseIdx = new ConcurrentLinkedQueue<Integer>();
	private static DbHandler dh = new DbHandler("10.176.16.231");
	private static Future<?> fut;
	
	public static void processQuery(String sql) {
		if(sql.contains("INSERT") || sql.contains("insert") || sql.contains("UPDATE") || sql.contains("update"))
			fut = dh.countOperation(sql);
		else if(sql.contains("SELECT") || sql.contains("select"))
			fut = dh.multipleRowOperation(sql);
		else
			System.out.println("Query not handled!");
	}
	
	public static String getRespose() throws InterruptedException, ExecutionException {
		return (String) fut.get();
	}
}
