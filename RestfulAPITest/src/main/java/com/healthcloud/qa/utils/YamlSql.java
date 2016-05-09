package com.healthcloud.qa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Hello world!
 *
 */
public class YamlSql {
	public String GetResultFromYaml(String DBType, String IP, String port, String DB, String user, String passwd,
			String filename) throws FileNotFoundException {
		List<String> list_regx = null;
		List<String> list_regx2 = null;
		List<String> list_regx3 = null;
		String resual_final = "";

		JSONArray sqlfromyaml = Sqlfromyaml.sqlfromyaml(filename);
		for (int i = 0; i < sqlfromyaml.size(); i++) {
			JSONObject sqlfromyaml_tmp = sqlfromyaml.getJSONObject(i);
			for (Entry<String, Object> entry : sqlfromyaml_tmp.entrySet()) {
				System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());

				if (!sqlfromyaml_tmp.toString().contains("sql_complex")) {

					String sql = (String) sqlfromyaml_tmp.getJSONObject(entry.getKey()).get("sql");
					// System.out.println("sql:" +
					// sqlfromyaml_tmp.getJSONObject(entry.getKey()).get("sql"));

					Mysql DB_Mysql = new Mysql();
					list_regx = DB_Mysql.getregex();
					try {
						org.json.JSONArray s_tmp = null;
						if (filename.contains("mysql")) {
							s_tmp = DB_Mysql.GetResultFromMysql("mysql", IP, port, DB, user, passwd, sql);
						}

						// System.out.println("s_tmp.getJSONObject(0):"+s_tmp.getJSONObject(0));
						resual_final = "\"" + resual_final + entry.getKey() + "\":"
								+ s_tmp.getJSONObject(0).get(entry.getKey());
						resual_final = GetResult(list_regx, resual_final);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					String s_tmp2 = null;
					String sql = (String) sqlfromyaml.getJSONObject(i).getJSONObject(entry.getKey()).get("sql");
					Mysql DB_Mysql = new Mysql();
					list_regx2 = DB_Mysql.getregex();
					try {
						org.json.JSONArray s_tmp = null;
						if (filename.contains("mysql")) {
							s_tmp = DB_Mysql.GetResultFromMysql("mysql", IP, port, DB, user, passwd, sql);

						}
						JSONArray s_json_arry_tmp = JSONArray.parseArray(s_tmp.toString());
						for (int j = 0; j < s_json_arry_tmp.size(); j++) {
							// System.out.println("s_json_arry_tmp:" +
							// s_json_arry_tmp.toString());
							// System.out.println(j+" res_type_id:" +
							// s_json_arry_tmp.getJSONObject(j).get("res_type_id"));
							// System.out.println("hostname:" +
							// s_json_arry_tmp.getJSONObject(j).get("hostname"));
							JSONArray sql_complex_json_arry = sqlfromyaml.getJSONObject(i).getJSONObject(entry.getKey())
									.getJSONArray("sql_complex");
							String sql_complex = "";
							String sql_complex_json_arry_tmp_2 = "";
							String sql_complex_json_arry_tmp_3 = "";
							for (int x = 0; x < sql_complex_json_arry.size(); x++) {
								if (sql_complex_json_arry.getJSONObject(x).toString().contains("sql")) {
									sql_complex = (String) sql_complex_json_arry.getJSONObject(x).get("sql");
								}
								if (sql_complex_json_arry.getJSONObject(x).toString().contains("parameter")) {
									sql_complex_json_arry_tmp_2 = (String) sql_complex_json_arry.getJSONObject(x)
											.get("parameter");
								}
								if (sql_complex_json_arry.getJSONObject(x).toString().contains("name")) {
									sql_complex_json_arry_tmp_3 = (String) sql_complex_json_arry.getJSONObject(x)
											.get("name");
								}
							}

							String[] sql_complex_json_arry_tmp = new String[1];
							if (sql_complex_json_arry_tmp_2.contains(",")) {
								sql_complex_json_arry_tmp = sql_complex_json_arry_tmp_2.split(",");
								// System.out.println(
								// "sql_complex_json_arry_tmp.length:" +
								// sql_complex_json_arry_tmp.length);
							} else {
								// sql_complex_json_arry_tmp=Arrays.copyOf(sql_complex_json_arry_tmp,
								// sql_complex_json_arry_tmp.length+1);
								// sql_complex_json_arry_tmp[sql_complex_json_arry_tmp.length-1]=sql_complex_json_arry_tmp_2;
								sql_complex_json_arry_tmp[sql_complex_json_arry_tmp.length
										- 1] = sql_complex_json_arry_tmp_2;
							}

							String sql_complex_tmp = "";
							for (int y = 0; y < sql_complex_json_arry_tmp.length; y++) {
								// System.out.println("sql_complex_json_arry_tmp[y]:"
								// + sql_complex_json_arry_tmp[y]);
								if (0 == sql_complex_tmp.length()) {
									sql_complex_tmp = sql_complex_tmp + sql_complex_json_arry_tmp[0] + "="
											+ s_json_arry_tmp.getJSONObject(j).get(sql_complex_json_arry_tmp[1]);
								} else {
									sql_complex_tmp = sql_complex_tmp + " and " + sql_complex_json_arry_tmp[0] + "="
											+ s_json_arry_tmp.getJSONObject(j).get(sql_complex_json_arry_tmp[1]);
								}
							}

							// System.out.println("sql_complex:" + sql_complex +
							// " where " + sql_complex_tmp);
							String sql_2 = sql_complex + " where " + sql_complex_tmp;
							Mysql DB_Mysql_2 = new Mysql();
							list_regx3 = DB_Mysql_2.getregex();
							org.json.JSONArray s_tmp_2 = null;

							try {
								if (filename.contains("mysql")) {
									s_tmp_2 = DB_Mysql_2.GetResultFromMysql("mysql", IP, port, DB, user, passwd, sql_2);
									System.out.println("s_tmp2:" + s_tmp2);
								}

							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							s_tmp.getJSONObject(j).put(sql_complex_json_arry_tmp_3,
									GetResult(list_regx3, s_tmp_2.toString().substring(1, s_tmp_2.toString().length() - 1)));
							s_tmp.getJSONObject(j).remove(sql_complex_json_arry_tmp[1]);
							// System.out.println(j + ":" +
							// s_tmp.getJSONObject(j));
						}

						resual_final = entry.getKey() + "\":" + s_tmp.toString().replace("\"null\"", "\"\"") + ",\""
								+ resual_final;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		resual_final = GetResult(list_regx, resual_final);

		resual_final = GetResult(list_regx2, resual_final);

		System.out.println("resual_final:" + "{"
				+ resual_final.replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}") + "}");

		return "{" + resual_final.replace("\\\"", "\"").replace("\"{", "{").replace("}\"", "}") + "}";
	}

	public String GetResult(List list_tmp, String result) {
		for (int i = 0; i < list_tmp.size(); i++) {

			String regex = "(\"" + list_tmp.get(i) + "\":)\"(\\d+)\"";
			// String str =
			// "{\"name\":\"tomcat\",\"className\":\"中间件\",\"id\":\"1\"}";

			Pattern pat = Pattern.compile(regex);
			// System.out.println("regex:" + regex);
			Matcher matcher = pat.matcher(result);
			while (matcher.find()) {
				String temp = null;
				// System.out.println(i + ":" + matcher.group(0));
				// System.out.println(i + ":" + matcher.group(1));
				// System.out.println(i + ":" + matcher.group(2));
				temp = result.substring(matcher.start(), matcher.end());
				// System.out.println("tmp:" + temp + " " + matcher.start() + "
				// " + matcher.end());
				result = result.replaceAll(temp, matcher.group(1) + matcher.group(2));
				matcher = pat.matcher(result);
				// System.out.println("inwhile:" + result);
			}

		}
		return result;
	}
}
