import java.util.stream.Collector;
import jdk.incubator.sql2.Result;

public class CollectorUtils {
	public static<T> Collector<Result.Row, T[], T> singleCollector(Class<T> clazz) {
		return Collector.of(
				() -> (T[])new Object[1],
				(a, r) -> a[0] = r.get("t", clazz), // hardcode the name of the column here
				(l, r) -> null,
				a -> a[0]);
	}

	public static Collector<Integer, Integer[], Integer> summingCollector() {
		return Collector.of(
				() -> new Integer[] {0},
				(a, r) -> a[0] += r,
				(l, r) -> null,
				a -> a[0]);
	}

	/* Ignoring format type as of now
	 * Will incorporate other format to return each rows values
	 * */
	public static Collector<Result.Row, String[], String> rowCollector(String format) {
		return Collector.of(
			      () -> new String[] {""},
			      (a, v) -> {
			        for(String column : v.getIdentifiers()) {
			           //System.out.println("Column="+column);
			           a[0] += column+"="+v.get(column, Object.class);
			           a[0] += ",";
			        }
			        a[0] += '\n';
			        //System.out.println("Result from accumulator:"+a[0]);
			      },
			      (a, b) -> {/*System.out.println("Result from combiner:"+a[0]);*/ return null;},
			      a -> {/*System.out.println("Result from finisher:"+a[0]);*/return a[0];});
	}
	
//	/* Ignoring format type as of now
//	 * Will incorporate other format to return each rows values
//	 * */
//	public static Collector<Result.Row, String, String> rowCollector(String format) {
//		return Collector.of(
//			      () -> {System.out.println("Result from Supplier");return new String("start");},
//			      (a, v) -> {
//			    	check(v);
//			        for(String column : v.getIdentifiers()) {
//			           System.out.println("Column="+column);
//			           a += v.get(column, Object.class);
//			           a+=",";
//			        }
//			        System.out.println("Result from accumulator:"+a);
//			      },
//			      (a, b) -> {System.out.println("Result from combiner:"+a); return null;},
//			      a -> {System.out.println("Result from finisher:"+a);return a;});
//	}
	
	public static void check(Result.Row r) {
		System.out.println(r.toString());
	}
}