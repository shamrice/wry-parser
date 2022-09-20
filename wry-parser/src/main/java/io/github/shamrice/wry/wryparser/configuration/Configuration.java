package io.github.shamrice.wry.wryparser.configuration;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private File wrySourceFile;
    private List<ExcludeFilter> excludeFilterList;
    private int traversalLinkLimit = 10;
    private boolean forceContinueOnErrors = false;
    private boolean runGame = false;

    private static Configuration instance = null;

    private Configuration() {}

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public void setValues(File wrySourceFile, List<ExcludeFilter> excludeFilterList, int traversalLinkLimit,
                          boolean forceContinueOnErrors, boolean runGame) {
        this.wrySourceFile = wrySourceFile;
        this.excludeFilterList = excludeFilterList;
        this.traversalLinkLimit = traversalLinkLimit;
        this.forceContinueOnErrors = forceContinueOnErrors;
        this.runGame = runGame;

        logger.info("Set configuration values: wrySourceFile: " + wrySourceFile.getName() + " :: exclude filter list size: "
                + excludeFilterList.size() + " :: traversalLinkLimit = " + traversalLinkLimit + " :: forContinueOnErrors: "
                + forceContinueOnErrors + " :: runGame: " + runGame);
    }

    public File getWrySourceFile() {
        return wrySourceFile;
    }

    public ExcludeFilter getExcludeFilter(ExcludeFilterType excludeFilterType) {
        for (ExcludeFilter excludeFilter : excludeFilterList) {
            if (excludeFilter.getExcludeFilterType() == excludeFilterType) {
                return excludeFilter;
            }
        }

        return null;
    }

    public int getTraversalLinkLimit() {
        return traversalLinkLimit;
    }

    public boolean isForceContinueOnErrors() {
        return forceContinueOnErrors;
    }

    public boolean isRunGame() {
        return runGame;
    }
}
