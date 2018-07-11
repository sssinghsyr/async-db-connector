import jdk.incubator.sql2.AdbaType;
import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.SqlException;
import jdk.incubator.sql2.Transaction;
import java.util.logging.Logger;
import java.util.stream.Collector;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.postgresql.sql2.PGConnection;

public class DbHandler {
	private DataSource ds;
	private static ExecutorService pool;
	static final int MAX_T = 2;

	DbHandler(String ipAddr){
		ds = ConnectUtil.openDB(ipAddr);
		init();
	}
	
	public static void init() {
		if(pool == null)
			pool = Executors.newFixedThreadPool(MAX_T);
	}
	
	public static void close() {
		if(pool != null)
			pool.shutdown();
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
		CompletableFuture<String> fut = conn.<String>rowOperation(sql)
				.collect(CollectorUtils.rowCollector("csv"))
				.submit()
				.getCompletionStage().toCompletableFuture();
		return fut.thenApplyAsync(s -> {respHandlr.sendResponseToClient(fut);conn.close();
									return s;}, pool);
	}
}
