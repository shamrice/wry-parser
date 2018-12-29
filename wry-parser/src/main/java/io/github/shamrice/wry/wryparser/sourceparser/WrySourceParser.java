package io.github.shamrice.wry.wryparser.sourceparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.sourceparser.validate.PageValidator;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrySourceParser {

    private final static Logger logger = Logger.getLogger(WrySourceParser.class);

    private final static String STORY_REGEX_PATTERN = "[0-9].*";
    private final static String STORY_SUB_NAME = "gameselect";

    private File wrySourceFile;
    private ExcludeFilter excludeFilter;
    private List<Story> storyData = new ArrayList<>();
    private Map<String, List<String>> rawSubData = new HashMap<>();

    public WrySourceParser(ExcludeFilter excludeFilter, File wrySourceFile) {
        this.wrySourceFile = wrySourceFile;
        this.excludeFilter = excludeFilter;
    }

    public void populateRawSubData() throws IOException {
        if (wrySourceFile.canRead()) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(wrySourceFile));

            String currentLine;
            boolean isSub = false;
            String subName = "";
            List<String> rawLineData = new ArrayList<>();

            while ((currentLine = bufferedReader.readLine()) != null) {

                if (currentLine.contains("SUB") && !currentLine.equals("END SUB") && !currentLine.contains("GOSUB")
                    && !currentLine.contains("()")) {

                    logger.debug("Found sub line: " + currentLine);
                    isSub = true;
                    subName = currentLine.split("\\ ")[1];
                    rawLineData = new ArrayList<>();

                }

                if (isSub) {
                    logger.debug("Subname = " + subName + " Adding current line " + currentLine);
                    rawLineData.add(currentLine);
                }

                if (currentLine.equals("END SUB")) {
                    logger.debug("END SUB found for sub " + subName);
                    isSub = false;
                    rawSubData.put(subName, rawLineData);
                }
            }

            for (String name : rawSubData.keySet()) {
                for (String data : rawSubData.get(name)) {
                    logger.debug(name + " :: " + data);
                }
            }
        }
    }

    public void generateStories() {

        List<String> rawStorySubData = rawSubData.get(STORY_SUB_NAME);
        List<String> validContents = new ArrayList<>();

        //get story names and ids
        for (String currentLine : rawStorySubData) {

            if (!excludeFilter.isLineExcluded(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);
                logger.debug("STORY SUB :: " + currentLine);

                if (currentLine.matches(STORY_REGEX_PATTERN)) {
                    validContents.add(currentLine);
                }
            }
        }

        //get story choice destination subs
        Map<Integer, String> choiceDestination = getChoiceDestinationsForSub(rawStorySubData);

        //build stories object with found data
        for (String validContent : validContents) {
            logger.info("generateStories :: Valid content found: " + validContent);

            String[] idAndName = validContent.split("\\) ");

            logger.debug("generateStories :: idAndName[0] = " + idAndName[0]);
            logger.debug("generateStories :: idAndName[1] = " + idAndName[1]);

            int id = Integer.parseInt(idAndName[0]);
            String name = idAndName[1];

            Story story = new Story(id, name);
            story.setFirstPageSubName(choiceDestination.get(id));
            this.storyData.add(story);

            logger.info("generateStories :: Added story id " + id + " : " + name + " to storyData. First sub name= "
                    + story.getFirstPageSubName());
        }

    }

    public void generatePages() {

        //TODO : need to also differentiate pages like "buysold" which are intermediate pages for info that do not
        //TODO : cause game over and return player to previous chocie.

        for (String subName : rawSubData.keySet()) {
            if (PageValidator.isValidPage(rawSubData.get(subName))) {

                logger.debug("generatePages :: SubName : " + subName + " is valid story page. Processing.");

                for (String lineData : rawSubData.get(subName)) {

                }
            } else if (PageValidator.isGameOverScreen(rawSubData.get(subName))) {
                logger.info("generatePages :: SubName : " + subName + " is a Game Over screen.");
            } else {
                logger.info("generatePages :: SubName : " + subName + " is not a valid story page.");
            }
        }

    }

    private Map<Integer, String> getChoiceDestinationsForSub(List<String> rawSubLineData) {
        Map<Integer, String> choiceDestinations = new HashMap<>();

        boolean isNextLineChoiceDestination = false;
        int choiceId = -1;

        for (String currentLine : rawSubLineData) {

            if (!excludeFilter.isLineExcluded(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);

                //get destination sub for each switch choice
                if (currentLine.contains("CASE") && !currentLine.contains("SELECT")
                        && !currentLine.contains("ELSE")) {
                    isNextLineChoiceDestination = true;
                    choiceId = Integer.parseInt(currentLine.split("\\ ")[1]); //TODO : ewww...
                }

                if (isNextLineChoiceDestination && !currentLine.contains("CASE")) {

                    logger.debug("getChoiceDestinationsForSub :: Potential destination SUB = " + currentLine
                            + " choice =" + choiceId);

                    choiceDestinations.put(choiceId, currentLine);
                    isNextLineChoiceDestination = false;
                    choiceId = -1;
                }
            }
        }

        return choiceDestinations;
    }


}
