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
			           a[0] += column+"="+v.get(column, Object.class);
			           a[0] += ",";
			        }
			        a[0] += '\n';
			      },
			      (a, b) -> null,
			      a -> a[0]);
	}
}