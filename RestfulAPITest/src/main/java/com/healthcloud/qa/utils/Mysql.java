package com.healthcloud.qa.utils;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class Mysql {
	List<String> list = new ArrayList<String>();

	public static String date2TimeStamp(String date_str, String format) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			return String.valueOf(sdf.parse(date_str).getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public JSONArray GetResultFromMysql(String DBtype, String IP, String port, String DB, String user, String passwd,
			String sql) throws Exception {
		String s_tmp = "";
		JSONArray jsonArray = new JSONArray();
		Connection conn = null;
		PreparedStatement pst = null;

		// MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
		// 避免中文乱码要指定useUnicode和characterEncoding
		// 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
		// 下面语句之前就要先创建javademo数据库

		String url = "jdbc:" + DBtype + "://" + IP + ":" + port + "/" + DB + "?" + "user=" + user + "&password="
				+ passwd + "&useUnicode=true&characterEncoding=UTF8";
		// System.out.println("url:"+url);
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

			// System.out.println("成功加载MySQL驱动程序");
			// 一个Connection代表一个数据库连接
			conn = DriverManager.getConnection(url);
			// ResultSet rs = stmt.executeQuery(sql);//
			// executeQuery会返回结果的集合，否则返回空值
			// System.out.println("sql:" + sql);
			String[] Column_tmp = null;
			if (sql.contains("   from")) {
				Column_tmp = sql.split("   from");
			} else if (sql.contains("  from")) {
				Column_tmp = sql.split("  from");
			} else {
				Column_tmp = sql.split(" from");
			}
			JSONObject Column_object = new JSONObject();
			Column_tmp = Column_tmp[0].replace("SELECT", "select").replace("Select", "select").replace("select  ", "")
					.replace("select ", "").replace("select", "").replace(", ", ",").replace(",  ", ",").split(",");
			// Column_tmp=sql.split("from").toString().replace("select",
			// "").split(",");
			for (int j = 0; j < Column_tmp.length; j++) {
				// System.out.println("Column_tmp:" + Column_tmp[j]);
				if (Column_tmp[j].contains(" ")) {
					String[] Column_tmp2 = Column_tmp[j].split(" ");
					Column_object.put(Column_tmp2[0], Column_tmp2[1]);
				}
			}
			// System.out.println("-------------------------" +
			// Column_object.toString());

			pst = conn.prepareStatement(sql);
			ResultSet rs = pst.executeQuery();
			ResultSetMetaData rsd = rs.getMetaData();
			String stringArray[][] = new String[rsd.getColumnCount()][2];
			for (int i = 0; i < rsd.getColumnCount(); i++) {
				// System.out.println("java类型：" + rsd.getColumnClassName(i +
				// 1));
				// System.out.println(" 数据库类型:" + rsd.getColumnTypeName(i + 1));
				// System.out.println(" 数据库类型:" + rsd.getColumnType(i + 1));
				// System.out.println(" 字段名称:" + rsd.getColumnName(i + 1));
				// System.out.println(" 字段长度:" + rsd.getColumnDisplaySize(i +
				// 1));
				// System.out.println();
				// type = rsd.getColumnType(i + 1);
				stringArray[i][0] = rsd.getColumnType(i + 1) + "";

				stringArray[i][1] = rsd.getColumnName(i + 1);
				if (Column_object.has(stringArray[i][1])) {
					stringArray[i][1] = Column_object.get(stringArray[i][1]).toString();
				}
				// System.out.println("stringArray[i][1]:" + stringArray[i][1]);
				// Thread.sleep(100000);
			}

			while (rs.next()) {
				JSONObject jsonStrs = new JSONObject();
				for (int i = 0; i < stringArray.length; i++) {
					String s = "";
					switch (Integer.parseInt(stringArray[i][0])) {
					case Types.BIGINT:
						s = s + rs.getLong(stringArray[i][1]);
						list.add(stringArray[i][1]);
						break;
					case Types.BOOLEAN:
						s = s + rs.getBoolean(stringArray[i][1]);
						break;
					case Types.DATE:
						s = s + rs.getDate(stringArray[i][1]);
						break;
					case Types.DOUBLE:
						s = s + rs.getDouble(stringArray[i][1]);
						list.add(stringArray[i][1]);
						break;
					case Types.FLOAT:
						s = s + rs.getFloat(stringArray[i][1]);
						list.add(stringArray[i][1]);
						break;
					case Types.INTEGER:
						s = s + rs.getInt(stringArray[i][1]);
						list.add(stringArray[i][1]);
						break;
					case Types.SMALLINT:
						s = s + rs.getInt(stringArray[i][1]);
						list.add(stringArray[i][1]);
						break;
					case Types.TIME:
						s = s + rs.getTime(stringArray[i][1]);
						break;
					case Types.TIMESTAMP:
						s = s + rs.getTimestamp(stringArray[i][1]);
						s = date2TimeStamp(s, "yyyy-MM-dd HH:mm:ss");
						list.add(stringArray[i][1]);
						break;
					case Types.TINYINT:
						s = s + rs.getShort(stringArray[i][1]);
						list.add(stringArray[i][1]);
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
					// System.out.println(stringArray[i][1] + " " + s);
					jsonStrs.put(stringArray[i][1], s);
				}
				s_tmp = s_tmp + jsonStrs.toString();
				jsonArray.put(jsonStrs);
			}
			// System.out.println("jsonObject：" + jsonStrs);
			// System.out.println("jsonArray：" + jsonArray);

			s_tmp = "[" + s_tmp.replaceAll("\\}\\{", "},{") + "]";
			// System.out.println("s_tmp：[" + s_tmp.replaceAll("\\}\\{",
			// "},{")+"]");

		} catch (SQLException e) {
			System.out.println("MySQL操作错误");
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		return jsonArray;
		// return s_tmp;

	}
	public List<String> getregex() {
		return list;
	}

}