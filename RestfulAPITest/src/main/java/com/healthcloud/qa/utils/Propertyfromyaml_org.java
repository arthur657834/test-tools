package com.healthcloud.qa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.TestCase;

import org.yaml.snakeyaml.Yaml;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;

public class Propertyfromyaml_org {

	public static JSONObject Propertyfromyaml(String DBType) throws FileNotFoundException {
		// public static void main(String[] args) throws FileNotFoundException{
		JSONObject jsonStrs = new JSONObject();
		InputStream input = new FileInputStream(new File("test.yml"));
		Yaml yaml = new Yaml();
		Map<String, Object> object = (Map<String, Object>) yaml.load(input);

		System.out.println("object:" + object);
		System.out.println("---------------------------");
		for (Entry<String, Object> entry : object.entrySet()) {
			System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
		}

		// Set<String> entry= object.keySet();
		// System.out.println("key:" + entry);

		// Collection<Object> entry2= object.values();
		// System.out.println("key:" + entry2);

		if (object.containsKey(DBType)) {
			System.out.println("DBType:" + object.get(DBType));
			String jsonStrs_tmp = object.get(DBType).toString().replace(" ", "").replace("=", "\":\"")
					.replace(",", "\",\"").replace("{", "{\"").replace("}", "\"}");
			System.out.println("jsonStrs:" + jsonStrs_tmp);
			jsonStrs = (JSONObject) JSONObject.parse(jsonStrs_tmp);
			System.out.println("tmp:" + jsonStrs.get("user"));
		}
		return jsonStrs;
	}

}