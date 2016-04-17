package com.healthcloud.qa.utils;

import java.util.Iterator;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class Cassandra {

	// authenticator
	public String GetResultFromCassandra(String DBtype, String IP, String port, String DB, String user, String passwd,
			String sql) {

		String s_tmp = "";

		// TODO Auto-generated method stub
		Cluster cluster = Cluster.builder().addContactPoint(IP).withCredentials(user, passwd).build();
		Session session = cluster.connect(DB);

		String cql = sql;

		ResultSet result = session.execute(cql);

		Iterator<Row> iterator = result.iterator();
		while (iterator.hasNext()) {
			Row row = iterator.next();
			row.getInt(0);

		}
		session.close();
		return s_tmp;

	}

}
