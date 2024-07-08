package com.weisanju.crawler.selectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.weisanju.crawler.util.JqUtil;
import net.thisptr.jackson.jq.JsonQuery;

import java.util.ArrayList;
import java.util.List;

public class JqPathSelector implements Selector {
    private final JsonQuery rule;

    public JqPathSelector(String rule) {
        this.rule = JqUtil.compile(rule);
    }

    @Override
    public String select(String text) {
        List<String> list = selectList(text);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public List<String> selectList(String text) {
        ArrayNode evaluate = JqUtil.evaluate(rule, text);

        List<String> results = new ArrayList<>();
        for (JsonNode node : evaluate) {
            results.add(node.asText());
        }

        return results;
    }
}
