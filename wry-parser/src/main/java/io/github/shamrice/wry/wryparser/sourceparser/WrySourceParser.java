package io.github.shamrice.wry.wryparser.sourceparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.sourceparser.linker.StoryLinker;
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

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;

public class WrySourceParser {

    private final static Logger logger = Logger.getLogger(WrySourceParser.class);

    private File wrySourceFile;

    private List<ExcludeFilter> excludeFilters;
    private List<Story> storyData = new ArrayList<>();
    private Map<String, List<String>> rawSubData = new HashMap<>();
    private List<String> failedStoryPagesSubNames = new ArrayList<>();


    public WrySourceParser(List<ExcludeFilter> excludeFilters, File wrySourceFile) {
        this.wrySourceFile = wrySourceFile;
        this.excludeFilters = excludeFilters;
    }

    public List<String> getSubDisplayData(String subName) {

        List<String> rawSubDataSelected = rawSubData.get(subName);
        List<String> rawPrintDataToReturn = new ArrayList<>();

        if (rawSubDataSelected != null) {
            ExcludeFilter cmdExcludeFilter = getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);
            if (cmdExcludeFilter != null) {
                for (String lineData : rawSubDataSelected) {
                    if (!cmdExcludeFilter.isExcludedWordInLine(lineData)) {
                        rawPrintDataToReturn.add(lineData);
                    }
                }
            }
        }

        return rawPrintDataToReturn;
    }

    public List<Story> run() throws IOException {
        logger.info("Populating Raw Subroutine Data.");
        populateRawSubData();

        logger.info("Generating Story Data.");
        generateStories();

        logger.info("Generating Story Pages.");
        List<StoryPage> unlinkedStoryPages = generatePages();

        logger.info("Linking destination pageIds to Story Page Choices");
        linkDestinationPagesToChices(unlinkedStoryPages);

        logger.info("Linking pages into stories.");
        linkStories(unlinkedStoryPages);

        return storyData;
    }

    private void populateRawSubData() throws IOException {
        if (wrySourceFile.canRead()) {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(wrySourceFile));

            String currentLine;
            boolean isSub = false;
            String subName = "";
            List<String> rawLineData = new ArrayList<>();

            while ((currentLine = bufferedReader.readLine()) != null) {

                if (currentLine.contains("SUB") && !currentLine.equals("END SUB") && !currentLine.contains("GOSUB")
                    && !currentLine.contains("()")) {

                    //logger.debug("Found sub line: " + currentLine);
                    isSub = true;
                    subName = currentLine.split("\\ ")[1];
                    rawLineData = new ArrayList<>();

                }

                if (isSub) {
                    rawLineData.add(currentLine);
                }

                if (currentLine.equals("END SUB")) {
                    logger.debug("Finished getting raw sub data - END SUB found for sub " + subName);
                    isSub = false;
                    rawSubData.put(subName, rawLineData);
                }
            }

        }
    }

    private void generateStories() {

        /*
            There are two story choice screens. The unlocked one that has the destination for episode 4
            lacks the pregame screens and the locked one lacks the link to the pregame of episode 4. To get
            around this, we have to comb through both of these subs to find the pregame screen destinations
        */

        List<String> rawStorySubDataLocked = rawSubData.get(STORY_SUB_NAME1);
        List<String> rawStorySubDataUnlocked = rawSubData.get(STORY_SUB_NAME2);

        List<PageChoice> storyChoicesLocked = getChoicesForSub(rawStorySubDataLocked);
        List<PageChoice> storyChoicesUnlocked = getChoicesForSub(rawStorySubDataUnlocked);

        storyChoicesLocked.addAll(storyChoicesUnlocked);

        for (PageChoice storyChoice : storyChoicesLocked) {

            if (PageValidator.isPreGameScreen(storyChoice.getDestinationSubName())) {

                String storyChoiceText = storyChoice.getChoiceText();
                storyChoiceText = storyChoiceText.replace("-UNLOCKED-", "");

                Story story = new Story(storyChoice.getChoiceId(), storyChoiceText);
                story.setFirstPageSubName(storyChoice.getDestinationSubName());
                this.storyData.add(story);

                logger.info("generateStories :: Added story id " + story.getStoryId() + " : "
                        + story.getStoryName() + " to storyData. First sub name= "
                        + story.getFirstPageSubName());
            }
        }

    }

    private List<StoryPage> generatePages() {

        //TODO : need to also differentiate pages like "buysold" which are intermediate pages for info that do not
        //TODO : cause gameover and return player to previous choice.

        int pageId = 0;
        List<StoryPage> storyPages = new ArrayList<>();

        for (String subName : rawSubData.keySet()) {

            ExcludeFilter subNameExcludeFilter = getExcludeFilter(ExcludeFilterType.STORY_PAGE_SUB_NAMES);

            if (subNameExcludeFilter != null && !subNameExcludeFilter.isInExcluded(subName)) {

                List<String> rawSubLineData = rawSubData.get(subName);
                boolean isValidPage = PageValidator.isValidPage(rawSubLineData);
                boolean isGameOverPage = PageValidator.isGameOverScreen(rawSubLineData);
                boolean isWinPage = PageValidator.isWinningScreen(rawSubLineData);
                boolean isPreGamePage = PageValidator.isPreGameScreen(rawSubLineData);

                if (rawSubLineData != null && (isPreGamePage || isValidPage || isGameOverPage || isWinPage)) {

                    logger.debug("generatePages :: SubName : " + subName + " is valid story page. Processing.");

                    String pageStoryText = getPageStoryText(rawSubLineData);

                    StoryPage storyPage = new StoryPage(pageId, subName, pageStoryText);
                    storyPage.setValidPage(isValidPage);
                    storyPage.setGameOverPage(isGameOverPage);
                    storyPage.setWinPage(isWinPage);
                    storyPage.setPreGamePage(isPreGamePage);

                    // populate choices for regular story page.
                    if (!isGameOverPage && !isWinPage && !isPreGamePage) {
                        List<PageChoice> pageChoices = getChoicesForSub(rawSubLineData);

                        for (PageChoice choice : pageChoices) {
                            choice.setSourcePageId(pageId);
                            choice.setStatusMessage("Choice finished parsing");
                        }

                        storyPage.setPageChoices(pageChoices);

                    //populate choices for pregame, win and game over pages.
                    } else {

                        String destinationSub = null;

                        if (isPreGamePage) {
                            destinationSub = getDestinationSubOnPreGame(rawSubLineData);
                        } else {
                            //win or game over screen
                            destinationSub = TITLE_SCREEN_DEST_NAME;
                        }

                        PageChoice pregameChoice = new PageChoice(1, "Next Screen", destinationSub);
                        pregameChoice.setSourcePageId(pageId);
                        pregameChoice.setStatusMessage("Pregame choice finished parsing");

                        storyPage.addPageChoice(pregameChoice);

                    }

                    storyPage.setStatusMessage("Ready for story linking");
                    storyPages.add(storyPage);

                    pageId++;

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

        return storyPages;

    }

    private void linkDestinationPagesToChices(List<StoryPage> unlinkedStories) {
        StoryLinker linker = new StoryLinker();
        linker.linkDestinationPageIdsToChoices(unlinkedStories);
    }

    private void linkStories(List<StoryPage> unlinkedStoryPages) {

        StoryLinker linker = new StoryLinker();

        for (Story story : storyData) {
            linker.link(story, unlinkedStoryPages);
        }

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

    private String getDestinationSubOnPreGame(List<String> rawSubData) {

        int index = rawSubData.indexOf("END SUB") - 1;

        while (index > 0) {
            String possibleDest = rawSubData.get(index);
            if (!possibleDest.isEmpty()) {
                logger.info("Pregame screen destination = " + possibleDest);
                return possibleDest;
            } else {
                index--;
            }
        }

        logger.error("Unable to find destination sub on pregame screen. Returning null.");
        return null;
    }

    private String getPageStoryText(List<String> rawSubLineData) {

        StringBuilder pageStoryText = new StringBuilder();

        for (String line : rawSubLineData) {
            if (line.startsWith("PRINT")) {
                String trimmedLine = LineTrimmer.trimPrintCommandsAndSpaces(line);

                if (!trimmedLine.isEmpty() && !trimmedLine.matches(CHOICE_REGEX_PATTERN)) {
                    pageStoryText.append(trimmedLine);
                }
            }
        }

        return pageStoryText.toString();
    }


}
