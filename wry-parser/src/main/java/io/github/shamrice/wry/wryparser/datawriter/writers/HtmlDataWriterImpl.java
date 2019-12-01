package io.github.shamrice.wry.wryparser.datawriter.writers;

import io.github.shamrice.wry.wryparser.datawriter.WryDataWriter;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;
import org.omg.CosNaming._NamingContextExtStub;
import sun.jvm.hotspot.debugger.linux.LinuxDebugger;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HtmlDataWriterImpl extends WryDataWriter {

    private static final Logger logger = Logger.getLogger(HtmlDataWriterImpl.class);

    private static final String STORY_PATTERN = "%d";
    private static final String PAGE_PATTERN = "%03d";
    private static final String CHOICE_PATTERN = "%d";

    private static final String FILE_NAME_EXTENSION = ".html";
    private static final String STORY_START_FILE_NAME_PATTERN = STORY_PATTERN + "_start" + FILE_NAME_EXTENSION;
    private static final String FILE_NAME_PATTERN = STORY_PATTERN + PAGE_PATTERN + FILE_NAME_EXTENSION;

    private static final String FILE_HEADER = "<HTML><HEAD><TITLE>Wry On the Web</TITLE></HEAD><BODY>";
    private static final String FILE_FOOTER = "</BODY></HTML>";

    @Override
    public void writeDataFiles(List<Story> storyList) {
        logger.info("Writing data files for HTML using output dir: " + outputDir);

        //create episode select page
        String startFileContents = FILE_HEADER + "<P>Please select an episode:</p><ul>";
        for (Story story : storyList) {
            startFileContents += "<li><a href=\"" + String.format(STORY_START_FILE_NAME_PATTERN, story.getStoryId())
                    + "\">" + story.getStoryName() + "</a></li>";
        }
        startFileContents += FILE_FOOTER;
        writeFile(this.outputDir + "/index.html", startFileContents);

        //create story pages.
        for (Story story : storyList) {
            for (StoryPage storyPage : story.getPages()) {
                if (storyPage.getPageType().equals(PageType.PREGAME_PAGE)) {
                    String storyStartFile = this.outputDir + "/" + STORY_START_FILE_NAME_PATTERN;
                    storyStartFile = String.format(storyStartFile, storyPage.getSourceStoryId());

                    int destinationPage = storyPage.getPageChoices().get(0).getDestinationPageId();
                    String destinationUrl = String.format(FILE_NAME_PATTERN, storyPage.getSourceStoryId(), destinationPage);

                    String fileContents = FILE_HEADER + "<P>" + storyPage.getPageText()
                            + "</P><BR /><a href=\"" + destinationUrl + "\">Click here to begin.</a><br />"
                            + FILE_FOOTER;
                    writeFile(storyStartFile, fileContents);
                } else {

                    String currentPageFile = String.format(this.outputDir + "/" + FILE_NAME_PATTERN, storyPage.getSourceStoryId(), storyPage.getStoryPageId());
                    String fileContents = FILE_HEADER + "<P>" + storyPage.getPageText().replace("\r", "<br />") + "</P><BR /><UL>";

                    for (PageChoice choice : storyPage.getPageChoices()) {
                        String destinationUrl;
                        String choiceText = choice.getChoiceText();
                        if (choice.getDestinationSubName().equalsIgnoreCase("gameover")) {
                            destinationUrl = "index.html";
                            choiceText = "Game over. Click here to continue.";
                        } else {
                            destinationUrl = String.format(FILE_NAME_PATTERN, storyPage.getSourceStoryId(), choice.getDestinationPageId());
                        }
                        fileContents += "<li><a href=\"" + destinationUrl + "\">" + choiceText + "</a></li>";
                    }

                    fileContents += "</ul><br />" + FILE_FOOTER;
                    writeFile(currentPageFile, fileContents);
                }
            }
        }
    }

    private void writeFile(String filename, String fileContents) {
        logger.info("Writing output file: " + filename + " :: contents: " + fileContents);
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(fileContents);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            logger.error("Error creating output file: " + filename + " :: " + ex.getMessage(), ex);
        }
    }
}
