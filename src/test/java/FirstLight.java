package test.java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.sql2.util.PGCount;

import jdk.incubator.sql2.Connection;
import jdk.incubator.sql2.DataSource;
import main.java.CollectorUtils;
import test.java.testutil.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.testcontainers.containers.PostgreSQLContainer;

class FirstLight {

	public static PostgreSQLContainer postgres = DatabaseHolder.getCached();

	@BeforeAll
	public static void setup() throws InterruptedException, ExecutionException, TimeoutException {
		try (Connection conn = ConnectUtil.openDB(postgres).getConnection()) {
			conn.operation("create table emp(id int, empno int, ename varchar(10), deptno int)")
			.submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
			/*conn.operation("insert into emp(id, empno, ename, deptno) values(1, 2, 'empname', 3)")
			.submit().getCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);*/
		}
	}

	@Test
	void justAnExample() {
		System.out.println("This test method should be run");
	}

	/**
	 * create a Connection and send a SQL to the database
	 */
	@Test
	public void sqlOperation() throws InterruptedException, ExecutionException, TimeoutException{
		String TRIVIAL = "SELECT 1 as t";
		DataSource ds = ConnectUtil.openDB(postgres);
		Connection conn = ds.getConnection(t -> fail("ERROR: " + t.getMessage()));
		try (conn) {
			assertNotNull(conn);
			CompletableFuture<Integer> fut = conn.<Integer>rowOperation(TRIVIAL).collect(CollectorUtils.singleCollector(Integer.class))
			.submit()
			.getCompletionStage().toCompletableFuture();
			assertEquals(1, fut.get(10, TimeUnit.SECONDS).intValue());
		}
	}
	
	/**
	 * insert a row into to the database table
	 */
	@Test
	public void insertOperation() throws InterruptedException, ExecutionException, TimeoutException{
		String INSERT = "insert into emp(id, empno, ename, deptno) values(1, 2, 'empname', 3)";
		DataSource ds = ConnectUtil.openDB(postgres);
		Connection conn = ds.getConnection(t -> fail("ERROR: " + t.getMessage()));
		try (conn) {
			assertNotNull(conn);
			CompletableFuture<Object> fut = conn.countOperation(INSERT)
	          .submit().getCompletionStage().toCompletableFuture();
			assertEquals(1, ((PGCount)fut.get(10, TimeUnit.SECONDS)).getCount());
		}
	}
}