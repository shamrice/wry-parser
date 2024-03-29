package io.github.shamrice.wry.wryparser.sourceparser;

import io.github.shamrice.wry.wryparser.configuration.Configuration;
import io.github.shamrice.wry.wryparser.sourceparser.linker.StoryLinker;
import io.github.shamrice.wry.wryparser.sourceparser.linker.StoryLinkerImpl;
import io.github.shamrice.wry.wryparser.sourceparser.pages.PageBuilderImpl;
import io.github.shamrice.wry.wryparser.sourceparser.stories.StoryBuilderImpl;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ExitCodeConstants.LINK_STORIES_FAILED;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.QBasicCommandConstants.*;

public class WrySourceParser {

    private static final Logger logger = LogManager.getLogger(WrySourceParser.class);

    private Map<String, List<String>> rawSubData = new HashMap<>();

    public List<String> getSubDisplayData(String subName) {

        List<String> rawSubDataSelected = rawSubData.get(subName);
        List<String> rawPrintDataToReturn = new ArrayList<>();

        if (rawSubDataSelected != null) {
            for (String lineData : rawSubDataSelected) {
                if (lineData.startsWith(PRINT_COMMAND)) {
                    rawPrintDataToReturn.add(lineData);
                }
            }
        }

        return rawPrintDataToReturn;
    }

    public List<Story> run() throws IOException {

        Instant startInstant = Instant.now();

        logger.info("Populating Raw Subroutine Data.");
        populateRawSubData();

        logger.info("Generating Story Data.");
        List<Story> storyData = new StoryBuilderImpl(this.rawSubData).build();

        logger.info("Generating Story Pages.");
        List<StoryPage> unlinkedStoryPages = new PageBuilderImpl(this.rawSubData).build();

        logger.info("Linking destination pageIds to Story Page Choices");
        linkDestinationPagesToChoices(unlinkedStoryPages);

        logger.info("Linking pages into stories.");
        linkStories(storyData, unlinkedStoryPages);

        Instant endInstant = Instant.now();

        long duration = Duration.between(startInstant, endInstant).toMillis();
        logger.info("***** Total run duration: " + duration + "ms. *****");

        return storyData;
    }

    private void populateRawSubData() throws IOException {

        File wrySourceFile = Configuration.getInstance().getWrySourceFile();

        if (wrySourceFile.canRead()) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(wrySourceFile));

            String currentLine;
            boolean isSub = false;
            String subName = "";
            List<String> rawLineData = new ArrayList<>();

            while ((currentLine = bufferedReader.readLine()) != null) {

                if (currentLine.contains(SUB_COMMAND) && !currentLine.equals(END_SUB_COMMAND)
                        && !currentLine.contains(GOSUB_COMMAND) && !currentLine.contains("()")) {

                    isSub = true;
                    subName = currentLine.split("\\ ")[1];
                    rawLineData = new ArrayList<>();
                }

                if (isSub) {
                    rawLineData.add(currentLine);
                }

                if (currentLine.equals(END_SUB_COMMAND)) {
                    logger.debug("Finished getting raw sub data - END SUB found for sub " + subName);
                    isSub = false;
                    rawSubData.put(subName, rawLineData);
                }
            }

        }
    }

    private void linkDestinationPagesToChoices(List<StoryPage> unlinkedStories) {
        StoryLinker linker = new StoryLinkerImpl();
        linker.linkDestinationPageIdsToChoices(unlinkedStories);
    }

    private void linkStories(List<Story> storyData, List<StoryPage> unlinkedStoryPages) {

        StoryLinker linker = new StoryLinkerImpl();

        for (Story story : storyData) {
            linker.link(story, unlinkedStoryPages);

            if (story.isParseSuccessful()) {
                logger.info("Successfully parsed story " + story.getStoryId() + "-" + story.getStoryName());
            } else if (!story.isParseSuccessful() && !Configuration.getInstance().isForceContinueOnErrors()) {
                logger.info("Failed parsed story " + story.getStoryId() + "-" + story.getStoryName());
                System.exit(LINK_STORIES_FAILED);
            }
        }
    }

}
