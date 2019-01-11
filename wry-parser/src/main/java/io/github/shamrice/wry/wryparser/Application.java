package io.github.shamrice.wry.wryparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterBuilder;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.game.GameRunner;
import io.github.shamrice.wry.wryparser.sourceparser.WrySourceParser;
import io.github.shamrice.wry.wryparser.story.Story;
import org.apache.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.TITLE_SCREEN_SUB_NAME;

public class Application implements Callable<Void> {

    private static final Logger logger = Logger.getLogger(Application.class);

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

    @CommandLine.Option(names = { "-p", "--play"},
            description = "Run debug game in interactive mode after parsing of source file.")
    private boolean runGame = false;

    @CommandLine.Option(names = { "-f", "--force"},
            description = "Force parsing to continue even if there are failures encountered.")
    boolean forceContinueOnErrors = false;

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

            logger.info("Creating WrySourceParser.");
            WrySourceParser wrySourceParser = new WrySourceParser(excludeFilters, wrySourceFile, forceContinueOnErrors);

            List<Story> parsedStories = wrySourceParser.run();

            if (parsedStories != null && runGame) {

                GameRunner gameRunner = new GameRunner(
                        wrySourceParser.getSubDisplayData(TITLE_SCREEN_SUB_NAME),
                        parsedStories
                );

                gameRunner.run();
            }


        } catch (Exception ex) {
           logger.error(ex);
        }

        logger.info("Run complete.");

        return null;
    }
}
