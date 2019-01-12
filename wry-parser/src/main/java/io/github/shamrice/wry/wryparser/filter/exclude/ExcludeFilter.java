package io.github.shamrice.wry.wryparser.filter.exclude;

public interface ExcludeFilter {

    boolean isExcludedWordInLine(String line);
    boolean isInExcluded(String data);
    ExcludeFilterType getExcludeFilterType();
}
