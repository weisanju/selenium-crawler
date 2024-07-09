package com.weisanju.crawler.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;

import java.util.List;

public class JacksonUtil {
    static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    public static ObjectNode createObjectNode(String key, JsonNode value) {
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.set(key, value);
        return objectNode;
    }

    public static JsonNode valueToTree(Object value) {
        return objectMapper.valueToTree(value);
    }


    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }


    public static ArrayNode createArrayNodeFromNode(List<JsonNode> jsonNodes) {
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (JsonNode t : jsonNodes) {
            arrayNode.add(t);
        }
        return arrayNode;
    }


    public static <T> ArrayNode createArrayNode(List<T> list) {


        ArrayNode arrayNode = objectMapper.createArrayNode();

        for (T t : list) {
            arrayNode.add(objectMapper.valueToTree(t));
        }
        return arrayNode;
    }


    @SneakyThrows
    public static JsonNode convert(String jsonStr) {
        return objectMapper.readTree(jsonStr);
    }


    public static <T> T convert(String firstSourceText, Class<T> clazz) throws JsonProcessingException {
        return objectMapper.readValue(firstSourceText, clazz);
    }

    public static String toJsonString(JsonNode x) {
        return x.toString();
    }
}
