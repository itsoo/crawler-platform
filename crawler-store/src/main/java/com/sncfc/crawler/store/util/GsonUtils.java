package com.sncfc.crawler.store.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GsonUtils {
	public final static String MAP_KEY_SELF_IP = "selfIp";

	public final static String MAP_KEY_SELF_TU_PATH = "taskUnitPath";

	private final static Gson gson = new Gson();

	public final static Gson getGson() {
		return gson;
	}

	public final static String toJson(Object o) {
		return gson.toJson(o);
	}

	public final static String getSelfNodeValue(String selfIp,
			String selfMessionNodePath) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(MAP_KEY_SELF_IP, selfIp);
		map.put(MAP_KEY_SELF_TU_PATH, selfMessionNodePath);

		return toJson(map);
	}

	public final static <T> T get(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public final static Map<String, String> getMap(String str) {
		return gson.fromJson(str, new TypeToken<Map<String, String>>() {
		}.getType());
	}

	public final static List<Map<String, String>> getMapList(String str) {
		return gson.fromJson(str, new TypeToken<List<Map<String, String>>>() {
		}.getType());
	}

	public final static List<String> getStringList(String str) {
		return gson.fromJson(str, new TypeToken<List<String>>() {
		}.getType());
	}
}