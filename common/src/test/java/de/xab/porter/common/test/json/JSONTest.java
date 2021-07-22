package de.xab.porter.common.test.json;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xab.porter.common.util.Jsons;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * tests for JSON utils
 */
public class JSONTest {
    private final String json = "{\"field2\":\"field2\",\"field3\":0}";
    private final String jsonList = "[{\"field2\":\"field2\",\"field3\":0}]";
    private final String jsonSet = "[{\"field2\":\"field2\",\"field3\":0}]";
    private final String jsonMap = "{\"key\":{\"field2\":\"field2\",\"field3\":0}}";
    private Entity entity;

    @BeforeEach
    public void before() {
        entity = new Entity();
        entity.setField2("field2");
    }

    @Test
    public void testObjectSerialization() {
        String json = Jsons.toJson(entity);
        assertEquals(this.json, json);
    }

    @Test
    public void testObjectDeserialization() {
        Entity fromJson = Jsons.fromJson(this.json, Entity.class);
        assertEquals(fromJson.getField1(), entity.getField1());
        assertEquals(fromJson.getField2(), entity.getField2());
        assertEquals(fromJson.getField3(), entity.getField3());
    }

    @Test
    public void testListSerialization() {
        List<Entity> entities = new ArrayList<>();
        entities.add(entity);
        String json = Jsons.toJson(entities);
        assertEquals(json, jsonList);
    }

    @Test
    public void testListDeserialization() {
        List<Entity> entities = Jsons.fromJson(jsonList, new TypeReference<>() {
        });
        Entity entity = entities.get(0);
        assertEquals(entity.getField1(), this.entity.getField1());
        assertEquals(entity.getField2(), this.entity.getField2());
        assertEquals(entity.getField3(), this.entity.getField3());
    }

    @Test
    public void testSetSerialization() {
        Set<Entity> entities = new HashSet<>();
        entities.add(entity);
        String json = Jsons.toJson(entities);
        assertEquals(json, jsonSet);
    }

    @Test
    public void testSetDeserialization() {
        Set<Entity> entities = Jsons.fromJson(jsonSet, new TypeReference<>() {
        });
        Entity entity = (Entity) entities.toArray()[0];
        assertEquals(entity.getField1(), this.entity.getField1());
        assertEquals(entity.getField2(), this.entity.getField2());
        assertEquals(entity.getField3(), this.entity.getField3());
    }

    @Test
    public void testMapSerialization() {
        Map<String, Entity> entities = new HashMap<>();
        entities.put("key", entity);
        String json = Jsons.toJson(entities);
        assertEquals(json, jsonMap);
    }

    @Test
    public void testMapDeserialization() {
        Map<String, Entity> entities = Jsons.fromJson(jsonMap, new TypeReference<>() {
        });
        Entity entity = entities.get("key");
        assertEquals(entity.getField1(), this.entity.getField1());
        assertEquals(entity.getField2(), this.entity.getField2());
        assertEquals(entity.getField3(), this.entity.getField3());
    }
}
