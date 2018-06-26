import jdk.incubator.sql2.DataSource;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.postgresql.sql2.PGConnection;

public class DbHandler {
	private DataSource ds;
	private static List<PGConnection> connections = new LinkedList<PGConnection>();
	private static final int MAX_CNT = 1;

	DbHandler(String ipAddr){
		ds = ConnectUtil.openDB(ipAddr);
		for(int i=0; i<MAX_CNT; i++)
			connections.add((PGConnection) ds.getConnection());
		//conn.connectDb();
	}

	public CompletableFuture<Integer> singleRowOperation(String sql){
		System.out.println("Query: ["+sql+"]");
		PGConnection conn = DbConnection.getConnection();
		CompletableFuture<Integer> fut = conn.<Integer>rowOperation("SELECT i as t FROM numbers1")
				.collect(CollectorUtils.singleCollector(Integer.class))
				.submit()
				.getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); System.out.println("Print inside thenApply");return s;});
	}

	public CompletableFuture<Object> countOperation(String sql) {
		PGConnection conn = DbConnection.getConnection();
		CompletableFuture<Object> fut = conn.countOperation(sql).submit().getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); System.out.println("Print inside thenApply");return s;}); 
	}

	public CompletableFuture<String> multipleRowOperation(String sql, ResponseHandler respHandlr) {
		PGConnection conn = DbConnection.getConnection();
		CompletableFuture<String> fut = conn.<String>rowOperation(sql)
				.collect(CollectorUtils.rowCollector("csv"))
				.submit()
				.getCompletionStage().toCompletableFuture();
		return fut.thenApply(s -> {respHandlr.sendResponseToClient(fut);;
									return s;});
	}
	
	static class DbConnection{
		private static int state = 0;
		public static PGConnection getConnection() {
			state++;
			if(state >= MAX_CNT)
				state = 0;
			return connections.get(state);
		}
	}
}
