package io.github.shamrice.wry.wryparser.filter.exclude;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ExcludeFilterBuilder {

    private final static Logger logger = Logger.getLogger(ExcludeFilterBuilder.class);

    public static ExcludeFilter build(File excludeFilterFile) throws IOException {

        List<String> excludedWords = new ArrayList<>();

        if (excludeFilterFile != null && excludeFilterFile.canRead()) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(excludeFilterFile));
            String currentLine;

            while ((currentLine = bufferedReader.readLine()) != null) {
                if (!currentLine.isEmpty()) {
                    excludedWords.add(currentLine);
                }
            }
        }

        logger.info("Excluded Words:");
        for (String word : excludedWords) {
            logger.info(word);
        }
        return new ExcludeFilter(excludedWords);

    }
}
