package de.xab.porter.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.xab.porter.api.exception.PorterException;

public class Jsons {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T fromJson(String content, Class<T> clazz) {
        T t;
        try {
            t = mapper.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new PorterException("json deserialization failed", e);
        }
        return t;
    }

    public static String toJson(Object obj) {
        String json;
        try {
            json = mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PorterException("json serialization failed", e);
        }
        return json;
    }
}
