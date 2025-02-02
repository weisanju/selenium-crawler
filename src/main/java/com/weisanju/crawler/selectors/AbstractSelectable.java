package com.weisanju.crawler.selectors;



import java.util.ArrayList;
import java.util.List;

/**
 * @author code4crafer@gmail.com
 * @since 0.5.2
 */
public abstract class AbstractSelectable implements Selectable {

    protected abstract List<String> getSourceTexts();

    protected Selectable select(Selector selector, List<String> strings) {
        List<String> results = new ArrayList<>();
        for (String string : strings) {
            String result = selector.select(string);
            if (result != null) {
                results.add(result);
            }
        }
        return new PlainText(results);
    }

    protected Selectable selectList(Selector selector, List<String> strings) {
        List<String> results = new ArrayList<String>();
        for (String string : strings) {
            List<String> result = selector.selectList(string);
            results.addAll(result);
        }
        return new PlainText(results);
    }

    @Override
    public List<String> all() {
        return getSourceTexts();
    }

    @Override
    public String get() {
        List<String> all = all();
        if (all!=null && !all.isEmpty()) {
            return all.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Selectable select(Selector selector) {
        return select(selector, getSourceTexts());
    }

    @Override
    public Selectable selectList(Selector selector) {
        return selectList(selector, getSourceTexts());
    }

    @Override
    public Selectable regex(String regex) {
        RegexSelector regexSelector = Selectors.regex(regex);
        return selectList(regexSelector, getSourceTexts());
    }

    @Override
    public Selectable regex(String regex, int group) {
        RegexSelector regexSelector = Selectors.regex(regex, group);
        return selectList(regexSelector, getSourceTexts());
    }

    @Override
    public Selectable replace(String regex, String replacement) {
        ReplaceSelector replaceSelector = new ReplaceSelector(regex,replacement);
        return select(replaceSelector, getSourceTexts());
    }


    @Override
    public String toString() {
        return get();
    }

    @Override
    public boolean match() {
        return getSourceTexts() != null && !getSourceTexts().isEmpty();
    }
}
