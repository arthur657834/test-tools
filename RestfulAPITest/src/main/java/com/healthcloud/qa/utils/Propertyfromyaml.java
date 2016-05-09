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

public class Propertyfromyaml {

	public static JSONObject propertyfromyaml(String filename, String filter) throws FileNotFoundException {

		JSONObject jsonStrs = new JSONObject();

		InputStream input = new FileInputStream(new File(filename));
		Yaml yaml = new Yaml();

		Map<String, Object> object = (Map<String, Object>) yaml.load(input);

		for (Entry<String, Object> entry : object.entrySet()) {
			String object_tmp;
			// System.out.println("key= " + entry.getKey() + " and value= " +
			// entry.getValue().toString());

			String[] tmp = null;
			String tmp_split = "},";

			tmp = entry.getValue().toString().split(tmp_split);
			for (int i = 0; i < tmp.length; i++) {

				tmp[i] = tmp[i].replace(", ", ",").replace("=", "\":\"").replace(",", "\",\"").replace("{", "{\"")
						.replace("}", "\"}").replace("\":\"{\"", "\":{\"").replace("}\"}]", "}}]").replace("},{", "},");
			}
			jsonStrs = (JSONObject) JSONObject
					.parse("{\"" + entry.getKey() + "\":" + String.join(tmp_split, tmp) + "}");

		}
		JSONArray jsonStrs_tmp = (JSONArray) jsonStrs.get("DBtype");

		for (int i = 0; i < jsonStrs_tmp.size(); i++) {

			if (jsonStrs_tmp.get(i).toString().contains(filter)) {
				jsonStrs = (JSONObject) jsonStrs_tmp.get(i);
			}
		}

		return jsonStrs.getJSONObject(filter);
	}
}
