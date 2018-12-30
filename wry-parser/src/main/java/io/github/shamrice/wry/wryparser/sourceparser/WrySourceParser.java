package io.github.shamrice.wry.wryparser.sourceparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.sourceparser.validate.PageValidator;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class WrySourceParser {

    private final static Logger logger = Logger.getLogger(WrySourceParser.class);

    private final static String CHOICE_REGEX_PATTERN = "^[0-9].*\\).*";
    private final static String STORY_SUB_NAME = "gameselect";

    private File wrySourceFile;

    private List<ExcludeFilter> excludeFilters;
    private List<Story> storyData = new ArrayList<>();
    private Map<String, List<String>> rawSubData = new HashMap<>();
    private List<String> failedStoryPagesSubNames = new ArrayList<>();


    public WrySourceParser(List<ExcludeFilter> excludeFilters, File wrySourceFile) {
        this.wrySourceFile = wrySourceFile;
        this.excludeFilters = excludeFilters;
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
        List<PageChoice> storyChoices = getChoicesForSub(rawStorySubData);

        for (PageChoice storyChoice : storyChoices) {

            Story story = new Story(storyChoice.getChoiceId(), storyChoice.getChoiceText());
            story.setFirstPageSubName(storyChoice.getDestinationSubName());
            this.storyData.add(story);

            logger.info("generateStories :: Added story id " + story.getStoryId() + " : "
                    + story.getStoryName() + " to storyData. First sub name= "
                    + story.getFirstPageSubName());
        }

    }

    public void generatePages() {

        //TODO : need to also differentiate pages like "buysold" which are intermediate pages for info that do not
        //TODO : cause game over and return player to previous choice.

        int pageId = 0;
        List<StoryPage> storyPages = new LinkedList<>();

        for (String subName : rawSubData.keySet()) {

            ExcludeFilter subNameExcludeFilter = getExcludeFilter(ExcludeFilterType.STORY_PAGE_SUB_NAMES);

            if (subNameExcludeFilter != null && !subNameExcludeFilter.isInExcluded(subName)) {

                List<String> rawSubLineData = rawSubData.get(subName);

                if (rawSubLineData != null && PageValidator.isValidPage(rawSubLineData)) {

                    logger.debug("generatePages :: SubName : " + subName + " is valid story page. Processing.");

                    String pageStoryText = getPageStoryText(rawSubLineData);
                    List<PageChoice> pageChoices = getChoicesForSub(rawSubLineData);

                    for (PageChoice choice : pageChoices) {
                        choice.setSourcePageId(pageId);
                    }

                    StoryPage storyPage = new StoryPage(pageId, subName, pageStoryText);
                    storyPage.setPageChoices(pageChoices);

                    storyPages.add(storyPage);

                    pageId++;

                } else if (PageValidator.isGameOverScreen(rawSubLineData)) {
                    logger.info("generatePages :: SubName : " + subName + " is a Game Over screen.");
                } else {
                    failedStoryPagesSubNames.add(subName);
                    logger.info("generatePages :: SubName : " + subName + " is not a valid story page. Skipping");
                }
            }
        }

        for (StoryPage storyPage : storyPages) {
            storyPage.logStoryPageDetails();
        }

        failedStoryPagesSubNames.forEach( name -> logger.error("Failed to parse story page with sub name : " + name));
        logger.error("Total failed parsed subs: " + failedStoryPagesSubNames.size());


    }

    private ExcludeFilter getExcludeFilter(ExcludeFilterType excludeFilterType) {
        for (ExcludeFilter filter : excludeFilters) {
            if (filter.getExcludeFilterType() == excludeFilterType) {
                return filter;
            }
        }

        return null;
    }

    private List<PageChoice> getChoicesForSub(List<String> rawSubLineData) {
        Map<Integer, String> choiceDestinations = new HashMap<>();
        Map<Integer, String> choicesText = new HashMap<>();

        boolean isNextLineChoiceDestination = false;
        int choiceId = -1;

        for (String currentLine : rawSubLineData) {

            ExcludeFilter cmdExcludeFilter = getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);

                //if choices area of story text
                if (currentLine.matches(CHOICE_REGEX_PATTERN)) {
                    String[] idAndText = currentLine.split("\\) ");

                    logger.debug("getChoicesForSub :: idAndText[0] = " + idAndText[0]);
                    logger.debug("getChoicesForSub :: idAndText[1] = " + idAndText[1]);

                    int id = Integer.parseInt(idAndText[0]);
                    String text = idAndText[1];

                    choicesText.put(id, text);
                }

                //get destination sub for each switch choice
                if (currentLine.contains("CASE") && !currentLine.contains("SELECT")
                        && !currentLine.contains("ELSE")) {
                    isNextLineChoiceDestination = true;
                    choiceId = Integer.parseInt(currentLine.split("\\ ")[1]); //TODO : ewww...
                }

                if (isNextLineChoiceDestination && !currentLine.contains("CASE")) {

                    logger.debug("getChoiceDestinationsForSub :: Potential destination SUB = " + currentLine
                            + " choice = " + choiceId);

                    choiceDestinations.put(choiceId, currentLine);
                    isNextLineChoiceDestination = false;
                    choiceId = -1;
                }
            }
        }

        List<PageChoice> pageChoices = new LinkedList<>();

        for (int id : choiceDestinations.keySet()) {
            pageChoices.add(new PageChoice(id, choicesText.get(id), choiceDestinations.get(id)));
        }

        return pageChoices;
    }

    private String getPageStoryText(List<String> rawSubLineData) {
        String pageStoryText = "";

        return pageStoryText;
    }


}
