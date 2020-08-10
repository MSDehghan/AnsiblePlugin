import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class PrintJsonDocSchema {
    public static final Map<String, String> stat = new TreeMap<>();
    public static final Map<String, Long> counter = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new JsonMapper();
        Map<String, Map<String, Object>> map = mapper.readValue(PrintJsonDocSchema.class.getResource("/plugins.json"), new TypeReference<Map<String, Map<String, Object>>>() {
        });

        for (Map<String, Object> plugin : map.values()) {
            calcStat("", plugin);
        }

        for (Map.Entry<String, String> entry : stat.entrySet()) {
            System.out.println('>' + entry.getKey() + '(' + counter.get(entry.getKey()) + ')' + " : " + entry.getValue());
        }
    }

    private static void calcStat(String prefix, Map<String, Object> plugin) {
        for (Map.Entry<String, Object> field : plugin.entrySet()) {

            String key;
            if (prefix.endsWith(".return.*") || prefix.endsWith(".suboptions")) {
                return;
            } else if (prefix.endsWith(".options") || prefix.endsWith(".return")
                    || prefix.endsWith(".*.contains") || prefix.endsWith(".default")) {
                key = prefix + ".*";
            } else {
                key = prefix + '.' + field.getKey();
            }

            stat.compute(key, (k, v) -> computeTypeKey(field, v));
            counter.compute(key, (s, aLong) -> aLong == null ? 1L : aLong + 1);
            if (field.getValue() instanceof Map) {
                calcStat(key, (Map<String, Object>) field.getValue());
            }
        }
    }

    private static String computeTypeKey(Map.Entry<String, Object> field, String v) {
        String typeName = field.getValue() == null ? "null" : field.getValue().getClass().getSimpleName();
        if (v == null) return typeName;
        else if (!v.contains(typeName)) return v + " | " + typeName;
        else return v;
    }
}
