package io.github.shamrice.wry.wryparser.filter.exclude;

import java.util.List;

public class ExcludeFilter {

    List<String> excludedWords;

    ExcludeFilter(List<String> excludedWords) {

        this.excludedWords = excludedWords;
    }

    public boolean isLineExcluded(String line) {

        for (String word : excludedWords) {
            if (line.contains(word)) {
                return true;
            }
        }

        return false;
    }
}
