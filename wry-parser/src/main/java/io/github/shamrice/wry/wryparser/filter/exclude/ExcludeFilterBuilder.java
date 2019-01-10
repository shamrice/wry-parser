package io.github.shamrice.wry.wryparser.filter.exclude;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcludeFilterBuilder {

    private static final Logger logger = Logger.getLogger(ExcludeFilterBuilder.class);

    public static ExcludeFilter build(ExcludeFilterType excludeFilterType, File excludeFilterFile) throws IOException {

        List<String> excludedWords = new ArrayList<>();

        if (excludeFilterFile != null && excludeFilterFile.canRead()) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(excludeFilterFile));
            String currentLine;

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (!currentLine.isEmpty()) {
                    excludedWords.add(currentLine.trim());
                }
            }
        } else {
            logger.error("Exclude file supplied for " + excludeFilterType.name() + " does not exist. No entries added.");
        }

        logger.info(excludeFilterType.name() + " Excluded Words:");
        for (String word : excludedWords) {
            logger.info(word);
        }
        return new ExcludeFilter(excludeFilterType, excludedWords);

    }
}
