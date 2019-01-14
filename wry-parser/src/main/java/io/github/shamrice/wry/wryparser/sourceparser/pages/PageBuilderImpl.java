package io.github.shamrice.wry.wryparser.sourceparser.pages;

import io.github.shamrice.wry.wryparser.configuration.Configuration;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.sourceparser.choices.ChoiceParser;
import io.github.shamrice.wry.wryparser.sourceparser.pages.validate.PageValidator;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ExitCodeConstants.BUILD_PAGES_FAILED;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.ExitCodeConstants.GET_DESTINATION_SUB_ON_SPECIAL_PAGE_FAILED;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.QBasicCommandConstants.*;

public class PageBuilderImpl implements PageBuilder {

    private static final Logger logger = Logger.getLogger(PageBuilderImpl.class);

    private Map<String, List<String>> rawSubData;

    public PageBuilderImpl(Map<String, List<String>> rawSubData) {
        this.rawSubData = rawSubData;
    }

    // TODO : this should be refactored to be simpler.
    @Override
    public List<StoryPage> build() {

        int pageId = 0;
        List<StoryPage> storyPages = new ArrayList<>();
        List<String> failedStoryPagesSubNames = new ArrayList<>();

        for (String subName : rawSubData.keySet()) {

            ExcludeFilter subNameExcludeFilter = Configuration.getInstance().getExcludeFilter(ExcludeFilterType.STORY_PAGE_SUB_NAMES);

            if (subNameExcludeFilter != null && !subNameExcludeFilter.isInExcluded(subName)) {

                List<String> rawSubLineData = rawSubData.get(subName);
                if (rawSubLineData != null) {

                    PageType pageType = PageValidator.getPageType(rawSubLineData);

                    if (pageType == PageType.MULTI_PAGE) {

                        //multi pages require extra parsing to separate individual pages out.
                        logger.debug("Generating pages for multi page sub " + subName);

                        List<StoryPage> generatedPages = generatePagesFromMultiPage(subName, rawSubLineData, pageId);
                        storyPages.addAll(generatedPages);
                        pageId += generatedPages.size();

                    } else {

                        String pageStoryText = getPageStoryText(rawSubLineData);

                        StoryPage storyPage = new StoryPage(pageId, subName, pageStoryText);
                        storyPage.setPageType(pageType);

                        logger.debug("Sub " + subName + " pageType = " + pageType.name());

                        switch (pageType) {

                            case REGULAR_PAGE:

                                ChoiceParser choiceParser = new ChoiceParser();
                                List<PageChoice> pageChoices = choiceParser.getChoicesForSub(rawSubLineData);

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
                                if (!Configuration.getInstance().isForceContinueOnErrors()) {
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
            storyPage.logStoryPageDetails("WrySourceParser::generatePages");
        }

        failedStoryPagesSubNames.forEach( name -> logger.error("Failed to parse story page with sub name : " + name));
        logger.error("Total failed parsed subs: " + failedStoryPagesSubNames.size());

        if (!Configuration.getInstance().isForceContinueOnErrors() && failedStoryPagesSubNames.size() > 0) {
            logger.error("Flag set to fail on errors. Run stopping.");
            System.exit(BUILD_PAGES_FAILED);
        }

        return storyPages;

    }


    private List<StoryPage> generatePagesFromMultiPage(String originalSubName, List<String> rawSubLineData,
                                                       int currentPageId) {

        List<StoryPage> generatedPages = new ArrayList<>();
        Map<String, List<String>> rawPages = new HashMap<>();

        //add placeholder for original sub name
        rawPages.put(originalSubName, new ArrayList<>());

        boolean isFirstLine = true;
        boolean isFirstPage = true;
        boolean isEndOfPage = false;
        String pageName = originalSubName;
        String firstPageName = originalSubName;
        List<String> tempPageData = new ArrayList<>();

        ExcludeFilter cmdExcludeFilter = Configuration.getInstance().getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

        //iterate through subs line data and break it into multiple pages
        for (String line : rawSubLineData) {

            if ((!isFirstLine && line.contains(":")) || line.contains(END_SUB_COMMAND)) {
                isEndOfPage = true;
            }

            if (isEndOfPage) {

                if (isFirstPage) {
                    firstPageName = pageName;
                }

                logger.info("Adding page " + pageName + " from multi-page " + originalSubName);
                rawPages.put(pageName, tempPageData);

                tempPageData = new ArrayList<>();
                isEndOfPage = false;
                isFirstPage = false;
            }

            if (line.contains(LABEL_TERMINATOR)) {
                pageName = line.split(LABEL_TERMINATOR)[0];
            }

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(line)) {
                isFirstLine = false;
            }

            tempPageData.add(line);

        }

        logger.info("Multi-page sub " + originalSubName + " contains " + rawPages.size() + " pages.");

        //add found pages and generate choice information for each
        for (String rawPageName : rawPages.keySet()) {
            List<String> rawPage = rawPages.get(rawPageName);
            List<String> rawPageFiltered = new ArrayList<>();

            //filter out BASIC commands from raw sub data.
            for (String data : rawPage) {
                logger.debug("Multi-page sub: " + originalSubName + " :: rawPageName: " + rawPageName
                        + " :: data=" + data);

                if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(data)) {
                    rawPageFiltered.add(data);
                }
            }

            PageType pageType = PageValidator.getPageType(rawPage);

            logger.debug("**SEARCH*** rawPageFilteredSize = " + rawPageFiltered.size() + " for " + rawPageName +
                    " first page name = " + firstPageName);

            //TODO : this is a weird work around for a special case of e4l1 should go to e4l1s as a destination...
            if (rawPageFiltered.size() == 0) {
                StoryPage storyPage = new StoryPage(currentPageId, rawPageName, "Pass through");
                storyPage.setPageType(PageType.PASS_THROUGH_PAGE);

                List<PageChoice> pageChoices = new ArrayList<>();
                PageChoice pageChoice = new PageChoice(1, "Continue", firstPageName);
                pageChoices.add(pageChoice);

                storyPage.setPageChoices(pageChoices);
                generatedPages.add(storyPage);

                logger.info("**SEARCH*** rawPageFilteredSize = " + rawPageFiltered.size() + " for " + rawPageName +
                        " first page name = " + firstPageName);

            } else {

                String storyText = getPageStoryText(rawPageFiltered);

                StoryPage storyPage = new StoryPage(currentPageId, rawPageName, storyText);
                storyPage.setPageType(pageType);

                switch (pageType) {

                    case REGULAR_PAGE:
                    case MULTI_PAGE:
                        ChoiceParser choiceParser = new ChoiceParser();
                        storyPage.setPageChoices(choiceParser.getChoicesForMultiPageSub(rawPageFiltered));
                        break;

                    case PREGAME_PAGE:
                    case WIN_PAGE:
                    case GAMEOVER_PAGE:
                    case PASS_THROUGH_PAGE:
                        String destinationSub = null;

                        if (pageType == PageType.PREGAME_PAGE || pageType == PageType.PASS_THROUGH_PAGE) {
                            destinationSub = getDestinationSubOnSpecialPage(rawPageFiltered);
                        } else {
                            //win or game over screen
                            destinationSub = TITLE_SCREEN_DEST_NAME;
                        }

                        PageChoice pregameChoice = new PageChoice(1, "Next Screen", destinationSub);
                        pregameChoice.setSourcePageId(currentPageId);

                        pregameChoice.setStatusMessage("Special page choice finished parsing");

                        logger.info("Special page choice parsing finished for sub " + rawPageName
                                + " destination sub = " + destinationSub);

                        storyPage.addPageChoice(pregameChoice);
                        break;

                    default:
                        logger.error("Failed to find page type for " + rawPageName + " of type " + pageType.name());
                        if (!Configuration.getInstance().isForceContinueOnErrors()) {
                            logger.error("Fail on errors is set. Ending run.");
                            System.exit(-9);
                        }

                }

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


    private String getDestinationSubOnSpecialPage(List<String> rawSubData) {

        int index = rawSubData.size() - 1;

        while (index > 0) {
            String possibleDest = rawSubData.get(index);

            if (!possibleDest.isEmpty() && !possibleDest.equals(END_SUB_COMMAND)) {
                logger.info("Special page screen destination = " + possibleDest);

                //remove GOTO statement if exists.
                if (possibleDest.contains(GOTO_COMMAND)) {
                    possibleDest = possibleDest.replace(GOTO_COMMAND, "");
                }
                possibleDest = possibleDest.trim();

                return possibleDest;
            } else {
                index--;
            }
        }

        logger.error("Unable to find destination sub on special screen. Returning null.");
        if (!Configuration.getInstance().isForceContinueOnErrors()) {
            logger.error("Fail on error flag is set. Ending run.");
            System.exit(GET_DESTINATION_SUB_ON_SPECIAL_PAGE_FAILED);
        }
        return null;
    }
}
