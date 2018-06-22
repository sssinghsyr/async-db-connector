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
		System.out.println("Query: ["+sql+"]");
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
		String debug = "select * from numbers1 limit 3";
		System.out.println("debug="+escapeNonAscii(debug));
		System.out.println("sql="+escapeNonAscii(sql));
		int c = debug.compareTo(sql);
		if(c == 0)
			System.out.println("SAME");
		CompletableFuture<String> fut = conn.<String>rowOperation(sql)
				.collect(CollectorUtils.rowCollector("csv"))
				.submit()
				.getCompletionStage().toCompletableFuture();
		return fut.thenApply(s -> {QueryHandler.readyResponseIdx.add(future_key);
									ClientQueryReceiver.selector.wakeup();
									System.out.println("Print inside thenApply");
									return s;});
	}

	private static String escapeNonAscii(String str) {
		StringBuilder retStr = new StringBuilder();
		for(int i=0; i<str.length(); i++) {
			int cp = Character.codePointAt(str, i);
			int charCount = Character.charCount(cp);
			if (charCount > 1) {
				i += charCount - 1; // 2.
				if (i >= str.length()) {
					throw new IllegalArgumentException("truncated unexpectedly");
				}
			}

			if (cp < 128) {
				retStr.appendCodePoint(cp);
			} else {
				retStr.append(String.format("\\u%x", cp));
			}
		}
		return retStr.toString();
	}
}
