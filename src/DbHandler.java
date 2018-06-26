import jdk.incubator.sql2.DataSource;
import java.util.concurrent.CompletableFuture;
import org.postgresql.sql2.PGConnection;

public class DbHandler {
	private DataSource ds;
	private PGConnection conn;

	DbHandler(String ipAddr){
		ds = ConnectUtil.openDB(ipAddr);
		conn = (PGConnection) ds.getConnection();
		//conn.connectDb();
	}

	public CompletableFuture<Integer> singleRowOperation(String sql){
		CompletableFuture<Integer> fut = conn.<Integer>rowOperation("SELECT i as t FROM numbers1")
				.collect(CollectorUtils.singleCollector(Integer.class))
				.submit()
				.getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); System.out.println("Print inside thenApply");return s;});
	}

	public CompletableFuture<Object> countOperation(String sql) {
		CompletableFuture<Object> fut = conn.countOperation(sql).submit().getCompletionStage().toCompletableFuture();
		fut.exceptionally(ex -> {ClientQueryReceiver.selector.wakeup(); ex.printStackTrace(); return 0;});
		return fut.thenApply(s -> {ClientQueryReceiver.selector.wakeup(); System.out.println("Print inside thenApply");return s;}); 
	}

	public CompletableFuture<String> multipleRowOperation(String sql, int future_key) {
		CompletableFuture<String> fut = conn.<String>rowOperation(sql)
				.collect(CollectorUtils.rowCollector("csv"))
				.submit()
				.getCompletionStage().toCompletableFuture();
		return fut.thenApply(s -> {QueryHandler.readyResponseIdx.add(future_key);
									ClientQueryReceiver.selector.wakeup();
									return s;});
	}
}
