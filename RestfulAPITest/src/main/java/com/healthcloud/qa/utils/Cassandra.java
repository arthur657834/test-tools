package com.healthcloud.qa.utils;

import java.util.Iterator;

import org.json.JSONArray;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class Cassandra {

	// authenticator
	public JSONArray GetResultFromCassandra(String DBtype, String IP, String port, String DB, String user,
			String passwd, String sql) {

		JSONArray s_tmp = null;
		Cluster cluster;
		// TODO Auto-generated method stub
		if (user.length() > 0 & passwd.length() > 0) {
			cluster = Cluster.builder().addContactPoint(IP).withCredentials(user, passwd).build();
		} else {
			cluster = Cluster.builder().addContactPoint(IP).build();
		}
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
