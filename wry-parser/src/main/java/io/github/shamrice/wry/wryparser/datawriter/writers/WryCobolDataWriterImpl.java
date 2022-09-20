package io.github.shamrice.wry.wryparser.datawriter.writers;

import io.github.shamrice.wry.wryparser.datawriter.WryDataWriter;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class WryCobolDataWriterImpl extends WryDataWriter {

    private static final Logger logger = LogManager.getLogger(WryCobolDataWriterImpl.class);

    private static final String STORY_START_FILENAME = "story-start.dat";
    private static final String STORY_FILENAME = "story.dat";
    private static final String STORY_TEXT_FILENAME = "story-text.dat";
    private static final String STORY_CHOICE_FILENAME = "story-choice.dat";

    private static final String STORY_ID_PLACEHOLDER = "%d";
    private static final String PAGE_ID_PLACEHOLDER = "%03d";
    private static final String DESTINATION_PAGE_ID_PLACEHOLDER = "%03d";
    private static final String CORRECT_PAGE_ID_PLACEHOLDER = "%03d";
    private static final String CHOICE_ID_PLACEHOLDER = "%d";
    private static final String CHOICE_NUM_PLACEHOLDER = "%d";
    private static final String CORRECT_CHOICE_ID_PLACEHOLDER = "%d";
    private static final String STORY_TEXT_PLACEHOLDER = "%s";
    private static final String CHOICE_TEXT_PLACEHOLDER = "%s";

    private static final String STORY_START_FILE_FORMAT = STORY_ID_PLACEHOLDER + PAGE_ID_PLACEHOLDER;

    private static final String STORY_FILE_FORMAT = STORY_ID_PLACEHOLDER + PAGE_ID_PLACEHOLDER
            + CORRECT_PAGE_ID_PLACEHOLDER + CORRECT_CHOICE_ID_PLACEHOLDER;

    private static final String STORY_TEXT_FILE_FORMAT = STORY_ID_PLACEHOLDER + PAGE_ID_PLACEHOLDER
            + STORY_TEXT_PLACEHOLDER;

    private static final String STORY_CHOICE_FILE_FORMAT = STORY_ID_PLACEHOLDER + PAGE_ID_PLACEHOLDER
            + DESTINATION_PAGE_ID_PLACEHOLDER + CHOICE_ID_PLACEHOLDER + CHOICE_NUM_PLACEHOLDER + ") "
            + CHOICE_TEXT_PLACEHOLDER;

    @Override
    public void writeDataFiles(List<Story> storyList) {
        logger.info("Writing data files for Wry Cobol using output dir: " + outputDir);

        String storyStartFile = outputDir + "/" + STORY_START_FILENAME;
        String storyOutputFile = outputDir + "/" + STORY_FILENAME;
        String storyTextFile = outputDir + "/" + STORY_TEXT_FILENAME;
        String storyChoiceFile = outputDir + "/" + STORY_CHOICE_FILENAME;

        List<String> storyStartLineData = new LinkedList<>();
        List<String> storyFileLineData = new LinkedList<>();
        List<String> storyTextFileLineData = new LinkedList<>();
        List<String> storyChoiceFileLineData = new LinkedList<>();

        //TODO : current has "correct choice" as final line data. should be destination page id instead.
        //TODO : needs correcting in wry-cobol as well.
        //get data
        for (Story story : storyList) {
            for (StoryPage storyPage : story.getPages()) {

                //if pregame page for story. Add to story start data.
                if (storyPage.getPageType() == PageType.PREGAME_PAGE) {
                    String storyStart = String.format(
                            STORY_START_FILE_FORMAT,
                            story.getStoryId(),
                            storyPage.getStoryPageId()
                    );
                    storyStartLineData.add(storyStart);
                }

                //get page data
                //COBOL does not use \n. Data must be in one line in DAT file as well.
                String storyPageText = storyPage.getPageText().replaceAll("\\n", " ");

                String storyTextLineData = String.format(
                        STORY_TEXT_FILE_FORMAT,
                        story.getStoryId(),
                        storyPage.getStoryPageId(),
                        storyPageText
                );
                storyTextFileLineData.add(storyTextLineData);
                logger.info("Story-text file data adding: " + storyTextLineData);

                for (PageChoice choice : storyPage.getPageChoices()) {

                    //get story linking data
                    String storyLineData = String.format(
                            STORY_FILE_FORMAT,
                            story.getStoryId(),
                            storyPage.getStoryPageId(),
                            choice.getDestinationPageId(),
                            choice.getChoiceId()
                    );
                    storyFileLineData.add(storyLineData);
                    logger.info("Story file data adding: " + storyLineData);

                    //get choice data
                    String storyChoiceLineData = String.format(
                            STORY_CHOICE_FILE_FORMAT,
                            story.getStoryId(),
                            storyPage.getStoryPageId(),
                            choice.getDestinationPageId(),
                            choice.getChoiceId(),
                            choice.getChoiceId(),
                            choice.getChoiceText()
                    );
                    storyChoiceFileLineData.add(storyChoiceLineData);
                    logger.info("Story choice file adding: " + storyChoiceLineData);

                }

            }
        }

        writeFile(storyStartFile, storyStartLineData);
        writeFile(storyOutputFile, storyFileLineData);
        writeFile(storyTextFile, storyTextFileLineData);
        writeFile(storyChoiceFile, storyChoiceFileLineData);
    }

    private void writeFile(String filename, List<String> lineData) {

        logger.info("Start writing line data to output file : " + filename);
        //write data to file.
        try {
            FileWriter fileWriter = new FileWriter(filename);
            for (String line : lineData) {
                fileWriter.write(line + "\n");
                fileWriter.flush();
            }
            fileWriter.close();;
        } catch (IOException ex) {
            logger.error(ex);
        }
        logger.info("Completed writing line data to output file : " + filename);

    }

}
