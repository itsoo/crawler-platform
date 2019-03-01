package com.sncfc.crawler.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sncfc.crawler.bean.UpdateTaskInfo;

public class GsonUtils {
	private final static String MAP_KEY_TASK_NODE_PATH = "taskNodePath";

	private final static String MAP_KEY_MISSION_NODE_PATH = "missionNodePath";

	private final static String MAP_KEY_FEEDBACK = "feedback";

	private final static Gson gson = new Gson();

	public final static Gson getGson() {
		return gson;
	}

	public final static String toJson(Object o) {
		return gson.toJson(o);
	}

	public final static String getTaskDesc(UpdateTaskInfo updatedTask) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(MAP_KEY_TASK_NODE_PATH, updatedTask.getTaskNodePath());
		map.put(MAP_KEY_MISSION_NODE_PATH, updatedTask.getMissionNodePath());
		map.put(MAP_KEY_FEEDBACK, String.valueOf(updatedTask.getFeedback()));

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