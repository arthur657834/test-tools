package com.healthcloud.qa.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONObject;

public class Property {
	public static JSONObject Property() {
		JSONObject jsonStrs = new JSONObject();
		Properties prop = new Properties();
		try {

			// 读取属性文件a.properties
			InputStream in = new BufferedInputStream(
					new FileInputStream("a.properties"));
			prop.load(in); /// 加载属性列表
			Iterator<String> it = prop.stringPropertyNames().iterator();
			while (it.hasNext()) {
				String key = it.next();
				jsonStrs.put(key, prop.getProperty(key));
			}
			in.close();
		} catch (Exception e) {
			System.out.println(e);
		}
		return jsonStrs;
	}
}