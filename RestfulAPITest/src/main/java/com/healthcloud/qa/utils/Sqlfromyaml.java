package com.healthcloud.qa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sound.midi.SysexMessage;

import junit.framework.TestCase;

import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;

public class Sqlfromyaml {

	public static JSONArray sqlfromyaml(String filename) throws FileNotFoundException {

		
		JSONArray jsonStrs = new JSONArray();
		String jsonStrs_tmp2 = "";
		String[] tmp = null;
		InputStream input = new FileInputStream(new File(filename));
		Yaml yaml = new Yaml();

		Map<String, Object> object = (Map<String, Object>) yaml.load(input);

		for (Entry<String, Object> entry : object.entrySet()) {
			JSONObject jsonStrs_tmp = new JSONObject();
//			System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue().toString());
			if (entry.getValue().toString().contains(", sql_complex")) {
				tmp = entry.getValue().toString().split(", sql_complex");
				for (int i = 0; i < tmp.length; i++) {

					tmp[i] = tmp[i].replace("=", "\":\"").replace("{", "{\"").replace("}", "\"}")
							.replace("]\"}", "]}").replace("\"[{\"", "[{\"");
//					System.out.println(i + ":" + tmp[i]);
				}
//				System.out.println("jsonStrs_tmp:" + "{\"" + entry.getKey() + "\":" + String.join("\",\"sql_complex", tmp) + "}");
				jsonStrs_tmp = (JSONObject) JSONObject
						.parse("{\"" + entry.getKey() + "\":" + String.join("\",\"sql_complex", tmp) + "}");
//				System.out.println("jsonStrs_tmp:" + jsonStrs_tmp);
				jsonStrs_tmp2=jsonStrs_tmp2+jsonStrs_tmp+",";
			} else {
				jsonStrs_tmp = (JSONObject) JSONObject.parse("{\"" + entry.getKey() + "\":"
						+ entry.getValue().toString().replace("=", "\":\"").replace("{", "{\"").replace("}", "\"}")
						+ "}");
//				System.out.println("jsonStrs_tmp:" + jsonStrs_tmp);
				jsonStrs_tmp2=jsonStrs_tmp2+jsonStrs_tmp+",";
			}

		}
		jsonStrs=JSONArray.parseArray("["+jsonStrs_tmp2.substring(0,jsonStrs_tmp2.length()-1)+"]");
		return jsonStrs;

	}
}
