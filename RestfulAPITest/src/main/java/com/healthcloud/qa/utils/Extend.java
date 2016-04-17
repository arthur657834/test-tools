package com.healthcloud.qa.utils;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONObject;

public class Extend {
	public JSONObject GetResult(String DBtype,String IP,String port,String DB,String user,String passwd) throws Exception {
	
		Connection conn = null;
		PreparedStatement pst = null;
		
		JSONObject jsonStrs = new JSONObject();
		String sql;
		// MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
		// 避免中文乱码要指定useUnicode和characterEncoding
		// 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
		// 下面语句之前就要先创建javademo数据库
		
		String url = "jdbc:"+DBtype+"://"+IP+":"+port+"/"+DB+"?"
				+ "user="+user+"&password="+passwd+"&useUnicode=true&characterEncoding=UTF8";

		try {
			// 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
			// 可以通过Class.forstringArray[i][1]把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
			switch (DBtype) {
			case "mysql":
				Class.forName("com.mysql.jdbc.Driver");
				break;
			}
			// 动态加载mysql驱动
			// or:
			// com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
			// or：
			// new com.mysql.jdbc.Driver();

			System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
				sql = "select * from student";
				// ResultSet rs = stmt.executeQuery(sql);//
				// executeQuery会返回结果的集合，否则返回空值
				pst = conn.prepareStatement(sql);
				ResultSet rs = pst.executeQuery();
				ResultSetMetaData rsd = rs.getMetaData();
				String stringArray[][] = new String[rsd.getColumnCount()][2];
				for (int i = 0; i < rsd.getColumnCount(); i++) {
//					System.out.print("java类型：" + rsd.getColumnClassName(i + 1));
//					System.out.print(" 数据库类型:" + rsd.getColumnTypeName(i + 1));
//					System.out.print(" 数据库类型:" + rsd.getColumnType(i + 1));
//					System.out.print(" 字段名称:" + rsd.getColumnName(i + 1));
//					System.out.print(" 字段长度:" + rsd.getColumnDisplaySize(i + 1));
//					System.out.println();
					// type = rsd.getColumnType(i + 1);
					stringArray[i][0] = rsd.getColumnType(i + 1) + "";

					stringArray[i][1] = rsd.getColumnName(i + 1);
				}

				while (rs.next()) {
					
					for (int i = 0; i < stringArray.length; i++) {
						String s = "";
						switch (Integer.parseInt(stringArray[i][0])) {
						case Types.BIGINT:
							s = s + rs.getLong(stringArray[i][1]);
							break;
						case Types.BOOLEAN:
							s = s + rs.getBoolean(stringArray[i][1]);
							break;
						case Types.DATE:
							s = s + rs.getDate(stringArray[i][1]);
							break;
						case Types.DOUBLE:
							s = s + rs.getDouble(stringArray[i][1]);
							break;
						case Types.FLOAT:
							s = s + rs.getFloat(stringArray[i][1]);
							break;
						case Types.INTEGER:
							s = s + rs.getInt(stringArray[i][1]);
							break;
						case Types.SMALLINT:
							s = s + rs.getInt(stringArray[i][1]);
							break;
						case Types.TIME:
							s = s + rs.getTime(stringArray[i][1]);
							break;
						case Types.TIMESTAMP:
							s = s + rs.getTimestamp(stringArray[i][1]);
							break;
						case Types.TINYINT:
							s = s + rs.getShort(stringArray[i][1]);
							break;
						case Types.VARCHAR:
							s = s + rs.getString(stringArray[i][1]);
							break;
						case Types.CHAR:
							s = s + rs.getString(stringArray[i][1]);
							break;
						case Types.NCHAR:
							s = s + rs.getNString(stringArray[i][1]);
							break;
						case Types.NVARCHAR:
							s = s + rs.getNString(stringArray[i][1]);
							break;
						case Types.BIT:
							s = s + rs.getByte(stringArray[i][1]);
							break;

						}
						System.out.println(stringArray[i][1]+" "+s);
						jsonStrs.put(stringArray[i][1], s);
					}

				}
				System.out.println("jsonObject：" + jsonStrs);
		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return jsonStrs;
		

	}

}