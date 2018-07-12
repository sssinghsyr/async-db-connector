package main.java;

import jdk.incubator.sql2.AdbaConnectionProperty;
import jdk.incubator.sql2.DataSource;
import jdk.incubator.sql2.DataSourceFactory;

public class ConnectUtil {
	public static DataSource openDB(String ipAddr) {
		return DataSourceFactory.forName("org.postgresql.sql2.PGDataSourceFactory")
				.builder()
				.url("jdbc:postgresql://" + ipAddr + ":" + "5432" +
						"/" + "postgres")
				.username("postgres")
				.password("postgres")
				.connectionProperty(AdbaConnectionProperty.TRANSACTION_ISOLATION,
						AdbaConnectionProperty.TransactionIsolation.REPEATABLE_READ)
				.build(); // Create thread from PGDataSource, assign connection to that data source
	}
}