package de.xab.porter.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.xab.porter.api.exception.PorterException;

/**
 * json utils
 */
public final class Jsons {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private Jsons() {
    }

    public static <T> T fromJson(String content, Class<T> clazz) {
        final T t;
        try {
            t = MAPPER.readValue(content, clazz);
        } catch (JsonProcessingException e) {
            throw new PorterException("json deserialization failed", e);
        }
        return t;
    }

    public static String toJson(Object obj) {
        final String json;
        try {
            json = MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new PorterException("json serialization failed", e);
        }
        return json;
    }
}
