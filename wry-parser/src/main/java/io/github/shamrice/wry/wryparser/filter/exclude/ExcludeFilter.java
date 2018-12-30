package io.github.shamrice.wry.wryparser.filter.exclude;

import java.util.List;

public class ExcludeFilter {

    private ExcludeFilterType excludeFilterType;
    private List<String> excludedWords;

    ExcludeFilter(ExcludeFilterType excludeFilterType, List<String> excludedWords) {
        this.excludeFilterType = excludeFilterType;
        this.excludedWords = excludedWords;
    }

    public boolean isExcludedWordInLine(String line) {

        for (String word : excludedWords) {
            if (line.contains(word)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInExcluded(String data) {
        return excludedWords.contains(data);
    }

    public ExcludeFilterType getExcludeFilterType() {
        return excludeFilterType;
    }
}
