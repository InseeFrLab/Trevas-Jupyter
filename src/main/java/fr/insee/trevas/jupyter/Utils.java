package fr.insee.trevas.jupyter;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static String getURL(String path) {
        return path.split("\\?")[0];
    }

    public static Map<String, String> getQueryMap(String path) {
        if (path == null || !path.contains("\\?")) {
            return Map.of();
        }
        String[] params = path.split("\\?")[1].split("&");
        Map<String, String> map = new HashMap<>();

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }
}
