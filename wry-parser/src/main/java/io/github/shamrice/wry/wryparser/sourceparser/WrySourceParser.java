package io.github.shamrice.wry.wryparser.sourceparser;

import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.sourceparser.linker.StoryLinker;
import io.github.shamrice.wry.wryparser.sourceparser.validate.PageValidator;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;

public class WrySourceParser {

    private static final Logger logger = Logger.getLogger(WrySourceParser.class);

    private File wrySourceFile;
    private List<ExcludeFilter> excludeFilters;
    private boolean failOnErrors = false;

    private List<Story> storyData = new ArrayList<>();
    private Map<String, List<String>> rawSubData = new HashMap<>();
    private List<String> failedStoryPagesSubNames = new ArrayList<>();


    public WrySourceParser(List<ExcludeFilter> excludeFilters, File wrySourceFile) {
        this.wrySourceFile = wrySourceFile;
        this.excludeFilters = excludeFilters;
    }

    public WrySourceParser(List<ExcludeFilter> excludeFilters, File wrySourceFile, boolean forceContinueOnErrors) {
        this(excludeFilters, wrySourceFile);
        this.failOnErrors = !forceContinueOnErrors;
    }

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

    // TODO : this should be refactored to be simpler.
    private List<StoryPage> generatePages() {

        int pageId = 0;
        List<StoryPage> storyPages = new ArrayList<>();

        for (String subName : rawSubData.keySet()) {

            ExcludeFilter subNameExcludeFilter = getExcludeFilter(ExcludeFilterType.STORY_PAGE_SUB_NAMES);

            if (subNameExcludeFilter != null && !subNameExcludeFilter.isInExcluded(subName)) {

                List<String> rawSubLineData = rawSubData.get(subName);
                if (rawSubLineData != null) {

                    PageType pageType = PageValidator.getPageType(rawSubLineData);

                    if (pageType == PageType.MULTI_PAGE) {

                        //multi pages require extra parsing to separate individual pages out.

                        //TODO : this will require more thinking as it causes the linker to fail.
                        //TODO : choice goes to multipage sub name, first generated page must match original sub name
                        //TODO : following pages can be original sub name + page number?

                        logger.debug("Generating pages for multi page sub " + subName);

                        List<StoryPage> generatedPages = generatePagesFromMultiPage(subName, rawSubLineData, pageId);
                        storyPages.addAll(generatedPages);
                        pageId += rawSubLineData.size();

                    } else {

                        String pageStoryText = getPageStoryText(rawSubLineData);

                        StoryPage storyPage = new StoryPage(pageId, subName, pageStoryText);
                        storyPage.setPageType(pageType);

                        logger.debug("Sub " + subName + " pageType = " + pageType.name());

                        switch (pageType) {

                            case REGULAR_PAGE:

                                List<PageChoice> pageChoices = getChoicesForSub(rawSubLineData);

                                for (PageChoice choice : pageChoices) {
                                    choice.setSourcePageId(pageId);
                                    choice.setStatusMessage("Choice finished parsing");
                                }

                                storyPage.setPageChoices(pageChoices);
                                break;

                            case PREGAME_PAGE:
                            case WIN_PAGE:
                            case GAMEOVER_PAGE:
                            case PASS_THROUGH_PAGE:
                                String destinationSub = null;

                                if (pageType == PageType.PREGAME_PAGE || pageType == PageType.PASS_THROUGH_PAGE) {
                                    destinationSub = getDestinationSubOnSpecialPage(rawSubLineData);
                                } else {
                                    //win or game over screen
                                    destinationSub = TITLE_SCREEN_DEST_NAME;
                                }

                                PageChoice pregameChoice = new PageChoice(1, "Next Screen", destinationSub);
                                pregameChoice.setSourcePageId(pageId);

                                pregameChoice.setStatusMessage("Special page choice finished parsing");

                                logger.info("Special page choice parsing finished for sub " + subName
                                        + " destination sub = " + destinationSub);

                                storyPage.addPageChoice(pregameChoice);
                                break;

                            default:
                                logger.error("Failed to find page type for " + subName + " of type " + pageType.name());
                                if (failOnErrors) {
                                    logger.error("Fail on errors is set. Ending run.");
                                    System.exit(-9);
                                }
                        }

                        storyPage.setStatusMessage("Ready for story linking");
                        storyPages.add(storyPage);

                        pageId++;

                    }
                } else {
                    failedStoryPagesSubNames.add(subName);
                    logger.info("generatePages :: SubName : " + subName + " is not a valid story page. Skipping");
                }
            }
        }


        for (StoryPage storyPage : storyPages) {
            /* TODO : not sure how i feel about that idea below...
            for (PageChoice choice : storyPage.getPageChoices()) {
                for (StoryPage page : storyPages) {
                    if (choice.getDestinationSubName().equals(page.getOriginalSubName())
                            && page.getPageType() == PageType.MULTI_PAGE) {
                        //TODO : set the destination sub of the choice to the actual first page?
                    }
                }
            }
            */
            storyPage.logStoryPageDetails("WrySourceParser::generatePages");
        }

        failedStoryPagesSubNames.forEach( name -> logger.error("Failed to parse story page with sub name : " + name));
        logger.error("Total failed parsed subs: " + failedStoryPagesSubNames.size());

        if (failOnErrors && failedStoryPagesSubNames.size() > 0) {
            logger.error("Flag set to fail on errors. Run stopping.");
            System.exit(-1);
        }

        return storyPages;

    }

    private void linkDestinationPagesToChices(List<StoryPage> unlinkedStories) {
        StoryLinker linker = new StoryLinker(failOnErrors);
        linker.linkDestinationPageIdsToChoices(unlinkedStories);
    }

    private void linkStories(List<StoryPage> unlinkedStoryPages) {

        StoryLinker linker = new StoryLinker(failOnErrors);

        for (Story story : storyData) {
            linker.link(story, unlinkedStoryPages);

            if (story.isParseSuccessful()) {
                logger.info("Successfully parsed story " + story.getStoryId() + "-" + story.getStoryName());
            } else if (!story.isParseSuccessful() && failOnErrors) {
                logger.info("Failed parsed story " + story.getStoryId() + "-" + story.getStoryName());
                System.exit(-1);
            }
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
            if (id < 10) {
                pageChoices.add(new PageChoice(id, choicesText.get(id), choiceDestinations.get(id)));
            }
        }

        return pageChoices;
    }

    private String getDestinationSubOnSpecialPage(List<String> rawSubData) {

        int index = rawSubData.indexOf("END SUB") - 1;

        while (index > 0) {
            String possibleDest = rawSubData.get(index);
            if (!possibleDest.isEmpty()) {
                logger.info("Special page screen destination = " + possibleDest);
                return possibleDest;
            } else {
                index--;
            }
        }

        logger.error("Unable to find destination sub on special screen. Returning null.");
        if (failOnErrors) {
            logger.error("Fail on error flag is set. Ending run.");
            System.exit(-2);
        }
        return null;
    }

    private String getPageStoryText(List<String> rawSubLineData) {

        StringBuilder pageStoryText = new StringBuilder();

        for (String line : rawSubLineData) {
            if (line.startsWith(PRINT_COMMAND)) {
                String trimmedLine = LineTrimmer.trimPrintCommandsAndSpaces(line);

                if (!trimmedLine.isEmpty() && !trimmedLine.matches(CHOICE_REGEX_PATTERN)) {
                    pageStoryText.append(trimmedLine);
                }
            }
        }

        return pageStoryText.toString();
    }

    private List<StoryPage> generatePagesFromMultiPage(String originalSubName, List<String> rawSubLineData,
                                                       int currentPageId) {

        List<StoryPage> generatedPages = new ArrayList<>();
        Map<String, List<String>> rawPages = new HashMap<>();

        //add placeholder for original sub name
        rawPages.put(originalSubName, new ArrayList<>());

        boolean isFirstLine = true;
        boolean isEndOfPage = false;
        String pageName = originalSubName;
        List<String> tempPageData = new ArrayList<>();

        ExcludeFilter cmdExcludeFilter = getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

        //iterate through subs line data and break it into multiple pages
        for (String line : rawSubLineData) {

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(line)) {

                if (!isFirstLine && line.contains(":")) {
                    isEndOfPage = true;
                }

                if (isEndOfPage) {
                    logger.info("Adding page " + pageName + " from multi-page " + originalSubName);
                    rawPages.put(pageName, tempPageData);

                    tempPageData = new ArrayList<>();
                    isEndOfPage = false;
                }

                if (line.contains(":")) {
                    pageName = line.split(":")[0];
                }

                isFirstLine = false;
                tempPageData.add(line);
            }
        }

        logger.info("Multi-page sub " + originalSubName + " contains " + rawPages.size() + " pages.");

        //add found pages and generate choice information for each
        //TODO : need to generate choices for pass through pages!!
        for (String rawPageName : rawPages.keySet()) {
            List<String> rawPage = rawPages.get(rawPageName);

            for (String data : rawPage) {
                logger.debug("Multi-page sub: " + originalSubName + " :: rawPageName: " + rawPageName + " :: data=" + data);
            }

            //TODO : this is a weird work around for a special case of e4l1 should go to e4l1s as a destination...
            if (rawPage.size() == 0) {
                StoryPage storyPage = new StoryPage(currentPageId, rawPageName, "Pass through");
                storyPage.setPageType(PageType.PASS_THROUGH_PAGE);

                List<PageChoice> pageChoices = new ArrayList<>();
                PageChoice pageChoice = new PageChoice(1, "Continue", "e4l1s");
                pageChoices.add(pageChoice);

                storyPage.setPageChoices(pageChoices);
                generatedPages.add(storyPage);

            } else {

                String storyText = getPageStoryText(rawPage);

                StoryPage storyPage = new StoryPage(currentPageId, rawPageName, storyText);
                storyPage.setPageType(PageType.REGULAR_PAGE);

                storyPage.setPageChoices(getChoicesForMultiPageSub(rawPage));

                generatedPages.add(storyPage);
            }
            currentPageId++;
        }

        for (StoryPage page : generatedPages) {
            logger.info("Multi-page " + originalSubName + " generated page " + page.getStoryPageId() + "-"
                    + page.getOriginalSubName() + " :: " + page.getPageType().name() + " :: " + page.getPageText());
        }

        return generatedPages;
    }

    private List<PageChoice> getChoicesForMultiPageSub(List<String> rawSubLineData) {
        Map<Integer, String> choiceDestinations = new HashMap<>();
        Map<Integer, String> choicesText = new HashMap<>();

        for (String currentLine : rawSubLineData) {

            ExcludeFilter cmdExcludeFilter = getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);

                //get choices area of story text
                if (currentLine.matches(CHOICE_REGEX_PATTERN)) {
                    String[] idAndText = currentLine.split("\\)");

                    logger.debug("getChoicesForSub :: idAndText[0] = " + idAndText[0]);
                    logger.debug("getChoicesForSub :: idAndText[1] = " + idAndText[1]);

                    int id = Integer.parseInt(idAndText[0]);
                    String text = idAndText[1].trim();

                    choicesText.put(id, text);
                }

                //get choice destinations from if statements
                if (currentLine.contains("IF") && currentLine.contains("THEN GOTO")) {

                    logger.debug("Multi-sub choice line: " + currentLine);
                    String condensedLine = currentLine.replace(" ", "");
                    logger.debug("Multi-sub condensed line: " + condensedLine);

                    try {
                        int choiceId = Integer.parseInt(condensedLine.substring(
                                condensedLine.indexOf("=") + 1,
                                condensedLine.indexOf("=") + 2)); //TODO : major ew

                        String destinationLabel = condensedLine.substring(
                                condensedLine.lastIndexOf("GOTO") + 4);

                        logger.debug("Multi-sub choice dest=" + choiceId + " :: dest label=" + destinationLabel);

                        choiceDestinations.put(choiceId, destinationLabel);
                    } catch (Exception ex) {
                        logger.error("Multi-sub failed: ", ex);
                        if (failOnErrors) {
                            System.exit(-9);
                        }
                    }
                }
            }
        }

        List<PageChoice> pageChoices = new LinkedList<>();

        try {
            for (int id : choiceDestinations.keySet()) {
                if (id < 10) {
                    logger.info("Adding multi-sub choice " + id + "-" + choicesText.get(id) + " :: dest="
                            + choiceDestinations.get(id));
                    pageChoices.add(new PageChoice(id, choicesText.get(id), choiceDestinations.get(id)));
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to add multi-sub choices", ex);
            if (failOnErrors) {
                System.exit(-1);
            }
        }

        return pageChoices;
    }

}
