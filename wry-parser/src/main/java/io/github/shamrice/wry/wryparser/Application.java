package io.github.shamrice.wry.wryparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterBuilder;
import io.github.shamrice.wry.wryparser.sourceparser.WrySourceParser;
import org.apache.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

public class Application implements Callable<Void> {

    private static final Logger logger = Logger.getLogger(Application.class);

    @CommandLine.Option(names = { "-s", "--source" },
            paramLabel = "SOURCE",
            description = "Wry Source file.",
            required = true)
    private File wrySourceFile;

    @CommandLine.Option(names = { "-x", "--exclude-file" },
            paramLabel = "EXCLUDE_FILE",
            description = "File with list of words to exclude during parsing of Wry Source")
    private File excludeFile;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays help.")
    boolean isHelpRequested = false;

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder("Wry parser started with args: ");
        for (String arg : args) {
            sb.append(arg);
            sb.append(" ");
        }
        logger.info(sb.toString());

        CommandLine.call(new Application(), args);
    }

    @Override
    public Void call() {

        try {
            logger.info("Creating exclude filter.");
            ExcludeFilter excludeFilter = ExcludeFilterBuilder.build(excludeFile);

            logger.info("Creating WrySourceParser.");
            WrySourceParser wrySourceParser = new WrySourceParser(excludeFilter, wrySourceFile);

            logger.info("Populating Raw Subroutine Data.");
            wrySourceParser.populateRawSubData();

            logger.info("Generating Story Data.");
            wrySourceParser.generateStories();

            logger.info("Generating Story Pages.");
            wrySourceParser.generatePages();

        } catch (Exception ex) {
           logger.error(ex);
        }

        logger.info("Run complete.");

        return null;
    }
}
