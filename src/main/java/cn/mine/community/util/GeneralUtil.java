package cn.mine.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GeneralUtil {
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String md5(String key) {
        if (StringUtils.isBlank(key)) { //key == null, key == "", key仅由空格组成，以上三种情况视为空
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    public static String getJsonString(int code, String msg, Map<String, Object> attributes) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        jsonObject.put("msg", msg);
        if (attributes != null) {
            Set<String> keySet = attributes.keySet();
            for (String key : keySet) {
                jsonObject.put(key, attributes.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    public static String getJsonString(int code, String msg) {
        return getJsonString(code, msg, null);
    }

    public static String getJsonString(int code) {
        return getJsonString(code, null, null);
    }
}
