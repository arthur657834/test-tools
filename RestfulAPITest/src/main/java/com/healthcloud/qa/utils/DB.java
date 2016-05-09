package com.healthcloud.qa.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;

import com.alibaba.fastjson.JSONObject;

public class DB {
	public String GetResult(String Datatype, JSONObject Parameter, String sql) {
		String s_tmp_final = null;
		JSONArray s_tmp = null;

		String passwd = null;
		String user = null;
		String port = null;
		String DB = null;
		String IP = null;
		String SqlCharacter = null;
		String UrlSplit = null;

		if (Parameter.containsKey("passwd")) {
			passwd = (String) Parameter.get("passwd");
		}

		if (Parameter.containsKey("user")) {
			user = (String) Parameter.get("user");
		}

		if (Parameter.containsKey("port")) {
			port = (String) Parameter.get("port");
		}

		if (Parameter.containsKey("DB")) {
			DB = (String) Parameter.get("DB");
		}

		if (Parameter.containsKey("IP")) {
			IP = (String) Parameter.get("IP");
		}
		if (Parameter.containsKey("SqlCharacter")) {
			SqlCharacter = (String) Parameter.get("SqlCharacter");
		}
		if (Parameter.containsKey("UrlSplit")) {
			UrlSplit = (String) Parameter.get("UrlSplit");
		}

		try {
			// 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
			// 可以通过Class.forstringArray[i][1]把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以

			switch (Datatype) {
			case "mysql":
				Mysql DB_Mysql = new Mysql();
				List<String> list_regx=DB_Mysql.getregex();
				s_tmp = DB_Mysql.GetResultFromMysql(Datatype, IP, port, DB, user, passwd, sql);
				s_tmp_final = s_tmp.toString();
				s_tmp_final =GetResult(list_regx, s_tmp_final);
				System.out.println("s_tmp_final--------------------:"+s_tmp_final);
				break;
			case "cassandra":
				Cassandra DB_Cassandra = new Cassandra();
				s_tmp = DB_Cassandra.GetResultFromCassandra(Datatype, IP, port, DB, user, passwd, sql);
				s_tmp_final = s_tmp.toString();
				break;
			case "yaml":
				// Thread.sleep(1000000);
				YamlSql DB_yaml = new YamlSql();
				s_tmp_final = DB_yaml.GetResultFromYaml(Datatype, IP, port, DB, user, passwd, sql);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("-------------------------------------");
		System.out.println("s_tmp_final:" + s_tmp_final);
		return s_tmp_final;
	}
	public String GetResult(List list_tmp, String result) {
		for (int i = 0; i < list_tmp.size(); i++) {

			String regex = "(\"" + list_tmp.get(i) + "\":)\"(\\d+)\"";
			// String str =
			// "{\"name\":\"tomcat\",\"className\":\"中间件\",\"id\":\"1\"}";

			Pattern pat = Pattern.compile(regex);
//			System.out.println("regex:" + regex);
			Matcher matcher = pat.matcher(result);
			while (matcher.find()) {
				String temp = null;
//				System.out.println(i + ":" + matcher.group(0));
//				System.out.println(i + ":" + matcher.group(1));
//				System.out.println(i + ":" + matcher.group(2));
				temp = result.substring(matcher.start(), matcher.end());
//				System.out.println("tmp:" + temp + " " + matcher.start() + " " + matcher.end());
				result = result.replaceAll(temp, matcher.group(1) + matcher.group(2));
				matcher = pat.matcher(result);
//				System.out.println("inwhile:" + result);
			}

		}
		return result;
	}
}