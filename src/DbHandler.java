import jdk.incubator.sql2.DataSource;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.postgresql.sql2.PGConnection;

public class DbHandler {
	private DataSource ds;

	DbHandler(String ipAddr){
		ds = ConnectUtil.openDB(ipAddr);
	}

	public CompletableFuture<Integer> singleRowOperation(String sql){
		PGConnection conn = (PGConnection) ds.getConnection();
		CompletableFuture<Integer> fut = conn.<Integer>rowOperation(sql)
				.collect(CollectorUtils.singleCollector(Integer.class))
				.submit()
				.getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); return s;});
	}

	public CompletableFuture<Object> countOperation(String sql) {
		PGConnection conn = (PGConnection) ds.getConnection();
		CompletableFuture<Object> fut = conn.countOperation(sql).submit().getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); return s;}); 
	}

	public CompletableFuture<String> multipleRowOperation(String sql, ResponseHandler respHandlr) {
		PGConnection conn = (PGConnection) ds.getConnection();
	    conn.connectDb();
		CompletableFuture<String> fut = conn.<String>rowOperation(sql)
				.collect(CollectorUtils.rowCollector("csv"))
				.submit()
				.getCompletionStage().toCompletableFuture();
		return fut.thenApply(s -> {respHandlr.sendResponseToClient(fut);conn.close();
									return s;});
	}
}
