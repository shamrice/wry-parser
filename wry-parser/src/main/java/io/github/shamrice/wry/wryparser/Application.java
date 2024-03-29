package io.github.shamrice.wry.wryparser;

import io.github.shamrice.wry.wryparser.configuration.Configuration;
import io.github.shamrice.wry.wryparser.datawriter.WryDataWriterFactory;
import io.github.shamrice.wry.wryparser.datawriter.WryDataWriter;
import io.github.shamrice.wry.wryparser.datawriter.datatypes.OutputDataTypes;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.builder.ExcludeFilterBuilder;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.game.GameRunner;
import io.github.shamrice.wry.wryparser.sourceparser.WrySourceParser;
import io.github.shamrice.wry.wryparser.story.Story;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.TITLE_SCREEN_SUB_NAME;

public class Application implements Callable<Void> {

    private static final Logger logger = LogManager.getLogger(Application.class);

    @CommandLine.Option(names = { "-s", "--source" },
            paramLabel = "SOURCE",
            description = "Wry Source file.",
            required = true)
    private File wrySourceFile;

    @CommandLine.Option(names = { "-xc", "--exclude-commands" },
            paramLabel = "EXCLUDE_COMMAND_FILE",
            description = "File with list of BASIC command words to exclude during parsing of Wry Source",
            required = true)
    private File excludeCommandFile;

    @CommandLine.Option(names = { "-xs", "--exclude-subs" },
            paramLabel = "EXCLUDE_SUB_NAME_FILE",
            description = "File with list of sub names to exclude from story pages.")
    private File excludeSubNamesFile;

    @CommandLine.Option(names = { "-o", "--output-dir"},
            description = "Output directory for data files.")
    private String outputDataDir;

    @CommandLine.Option(names = { "--type", "--output-type"},
            paramLabel = "WRY_COBOL, HTML",
            description = "Output data file type")
    private OutputDataTypes outputDataType;

    @CommandLine.Option(names = { "-p", "--play"},
            description = "Run debug game in interactive mode after parsing of source file.")
    private boolean runGame = false;

    @CommandLine.Option(names = { "-f", "--force"},
            description = "Force parsing to continue even if there are failures encountered.")
    private boolean forceContinueOnErrors = false;

    @CommandLine.Option(names = { "-t", "--traversal-limit"},
            description = "Max traversal limit linker can pass through a story page.")
    private int traversalLimit = 10;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays help.")
    boolean isHelpRequested = false;

    public static void main(String[] args) {

        logger.info(" ----------------------------------------");
        logger.info("|  Wry Source Parser Application - 2019  |");
        logger.info("|  Created by: Erik Eriksen              |");
        logger.info(" ----------------------------------------");

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
            List<ExcludeFilter> excludeFilters = new ArrayList<>();

            logger.info("Creating exclude filters.");
            excludeFilters.add(ExcludeFilterBuilder.build(ExcludeFilterType.BASIC_COMMANDS, excludeCommandFile));
            excludeFilters.add(ExcludeFilterBuilder.build(ExcludeFilterType.STORY_PAGE_SUB_NAMES, excludeSubNamesFile));

            Configuration.getInstance().setValues(wrySourceFile, excludeFilters, traversalLimit, forceContinueOnErrors, runGame);

            logger.info("Creating WrySourceParser.");
            WrySourceParser wrySourceParser = new WrySourceParser();

            List<Story> parsedStories = wrySourceParser.run();

            if (parsedStories != null && runGame) {

                GameRunner gameRunner = new GameRunner(
                        wrySourceParser.getSubDisplayData(TITLE_SCREEN_SUB_NAME),
                        parsedStories
                );

                gameRunner.run();
            }

            if (parsedStories != null && outputDataType != null) {

                WryDataWriter wryDataWriter = new WryDataWriterFactory(outputDataDir).makeDataWriter(outputDataType);
                if (wryDataWriter != null) {
                    wryDataWriter.writeDataFiles(parsedStories);
                } else {
                    logger.error("Unable to create Wry data writer. Returned object was null.");
                }
            }


        } catch (Exception ex) {
           logger.error(ex.getMessage(), ex);
        }

        logger.info("Run complete.");

        return null;
    }
}
