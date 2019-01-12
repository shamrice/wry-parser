package io.github.shamrice.wry.wryparser.filter.exclude;

import java.util.List;

public class ExcludeFilterImpl implements ExcludeFilter {

    private ExcludeFilterType excludeFilterType;
    private List<String> excludedWords;

    public ExcludeFilterImpl(ExcludeFilterType excludeFilterType, List<String> excludedWords) {
        this.excludeFilterType = excludeFilterType;
        this.excludedWords = excludedWords;
    }

    @Override
    public boolean isExcludedWordInLine(String line) {

        for (String word : excludedWords) {
            if (line.contains(word)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInExcluded(String data) {
        return excludedWords.contains(data);
    }

    @Override
    public ExcludeFilterType getExcludeFilterType() {
        return excludeFilterType;
    }
}
