package com.healthcloud.qa.utils;

public class DB {
	public String GetResult(String DBtype, String IP, String port, String DB, String user, String passwd, String sql){
		String s_tmp = null;
		System.out.println("DBtype:"+DBtype);
		try {
			// 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
			// 可以通过Class.forstringArray[i][1]把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
			switch (DBtype) {
			case "mysql":
				Mysql DB_Mysql=new Mysql();
				s_tmp=DB_Mysql.GetResultFromMysql(DBtype, IP, port, DB, user, passwd, sql);
				break;
			case "cassandra":
				Cassandra DB_Cassandra=new Cassandra();
				s_tmp=DB_Cassandra.GetResultFromCassandra(DBtype, IP, port, DB, user, passwd, sql);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		System.out.println("-------------------------------------");
		System.out.println("s_tmp:"+s_tmp);
		return s_tmp;
	}

}