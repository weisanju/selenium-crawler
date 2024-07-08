package com.weisanju.crawler.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.SneakyThrows;
import net.thisptr.jackson.jq.*;
import net.thisptr.jackson.jq.exception.JsonQueryException;
import net.thisptr.jackson.jq.module.loaders.BuiltinModuleLoader;

public class JqUtil {

    static Scope root;

    static {
        root = initScope();
    }

    @SneakyThrows
    public static JsonQuery compile(String jsonQuery) {
        return JsonQuery.compile(jsonQuery, Version.LATEST);
    }

    public static ArrayNode evaluate(JsonQuery jsonQuery, String json) {
        ArrayNode arrayNode = JacksonUtil.createArrayNode();
        try {
            jsonQuery.apply(root, JacksonUtil.valueToTree(json), arrayNode::add);
            return arrayNode;
        } catch (JsonQueryException e) {
            throw new RuntimeException(e);
        }
    }


    private static Scope initScope() {
        // First of all, you have to prepare a Scope which s a container of built-in/user-defined functions and variables.
        Scope rootScope = Scope.newEmptyScope();

        // Use BuiltinFunctionLoader to load built-in functions from the classpath.
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, rootScope);

        rootScope.setModuleLoader(BuiltinModuleLoader.getInstance());

        return rootScope;
    }
}
