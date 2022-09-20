package io.github.shamrice.wry.wryparser.sourceparser.linker;

import io.github.shamrice.wry.wryparser.configuration.Configuration;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ExitCodeConstants.*;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;

public class StoryLinkerImpl implements StoryLinker {

    private static final Logger logger = LogManager.getLogger(StoryLinkerImpl.class);

    @Override
    public void linkDestinationPageIdsToChoices(List<StoryPage> storyPages) {

        logger.info("Linking choice destination page ids to choices.");
        int choicesLinked = 0;
        int numChoices = 0;

        for (StoryPage page : storyPages) {

            //set choices destination page id for the current page.
            for (PageChoice choice : page.getPageChoices()) {
                if (choice.getDestinationSubName().equals(TITLE_SCREEN_DEST_NAME)) {
                    choice.setDestinationPageId(TITLE_SCREEN_PAGE_ID);
                } else {
                    for (StoryPage page2 : storyPages) {
                        if (choice.getDestinationSubName().equals(page2.getOriginalSubName())) {
                            logger.info("Found destination pageId " + page2.getStoryPageId()
                                    + "-" + page2.getOriginalSubName() + " "
                                    + " for choice " + choice.getChoiceText());

                            choice.setDestinationPageId(page2.getStoryPageId());
                            choice.setStatusMessage("Finished linking dest page id to choice");
                            choicesLinked++;
                        }
                    }
                    numChoices++;
                }
            }
        }

        logger.info("Linked " + choicesLinked + " destination pages for " + numChoices + " choices");
        logFailedChoiceLinks(storyPages);
    }

    @Override
    public void link(Story story, List<StoryPage> storyPages) {
        logger.info("Linking pages to story : " + story.getStoryName());

        int pregamePageId = PAGE_NOT_FOUND_ID;

        //set pregame start
        for (StoryPage page : storyPages) {

            //set source story id for pregame page.
            if (page.getOriginalSubName().equals(story.getFirstPageSubName())
                    && page.getPageType() == PageType.PREGAME_PAGE) {
                logger.info("Found pregame page " + story.getFirstPageSubName() + " for story " + story.getStoryName()
                        + " page sub name = " + page.getOriginalSubName() + " id= " + page.getStoryPageId());

                page.setSourceStoryId(story.getStoryId());
                pregamePageId = page.getStoryPageId();

            }
        }

        //recursive method that will traverse through all pages in story.
        traverseStory(story.getStoryId(), storyPages.get(pregamePageId), storyPages);

        //add pages to story.
        for (StoryPage page : storyPages) {
            if (page.getSourceStoryId() == story.getStoryId()) {
                logger.info("Adding page " + page.getStoryPageId() + "-" + page.getOriginalSubName()
                        + " to story " + story.getStoryName());
                story.addPage(page);
            }
        }

    }

    private void traverseStory(int storyId, StoryPage currentPage, List<StoryPage> allPages) {

        currentPage.setSourceStoryId(storyId);
        currentPage.setParsed(true);
        currentPage.setStatusMessage("Finishing linking page to story");

        for (PageChoice choice : currentPage.getPageChoices()) {
            logger.info("Page " + currentPage.getOriginalSubName() + " choice text= " + choice.getChoiceText()
                    + " choice dest= " + choice.getDestinationSubName() + " choice destid= " + choice.getDestinationPageId());

            choice.incrementTraversalCount();
            choice.setParsed(true);

            if (choice.getTraversalCount() < Configuration.getInstance().getTraversalLinkLimit()) {

                int nextPageId = choice.getDestinationPageId();

                if (nextPageId != PAGE_NOT_FOUND_ID && nextPageId != TITLE_SCREEN_PAGE_ID) {

                    try {

                        StoryPage nextPage = allPages.get(nextPageId);
                        traverseStory(storyId, nextPage, allPages);

                    } catch (IndexOutOfBoundsException ex) {

                        logger.error("currentPage " + currentPage.getOriginalSubName() + " destination is " +
                                choice.getDestinationSubName() + " with id " + choice.getDestinationPageId() +
                                " so skipping further story traversal.", ex);
                        if (!Configuration.getInstance().isForceContinueOnErrors()) {
                            logger.error("Fail on error flag is set so ending run.");
                            System.exit(TRAVERSE_STORY_INDEX_OUT_OF_BOUNDS);
                        }
                    }

                } else if (nextPageId == PAGE_NOT_FOUND_ID) {


                    currentPage.setStatusMessage("Destination for choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + ". Dest sub=" + choice.getDestinationSubName() + " failed to parse because destination pageId = -1");

                    logger.error("Destination for choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + ". Dest sub=" + choice.getDestinationSubName() + " failed to parse because destination pageId = "
                            + nextPageId);
                    if (!Configuration.getInstance().isForceContinueOnErrors()) {
                        logger.error("Fail on error flag is set so ending run.");
                        System.exit(TRAVERSE_STORY_PAGE_NOT_FOUND);
                    }
                }
            } else {
                logger.warn("Exceeded maximum number of traversals through choice " + choice.getChoiceId() + "-" + choice.getChoiceText());
            }
        }

        logger.info("Finished linking page " + currentPage.getStoryPageId() + "-" + currentPage.getOriginalSubName()
                + " to story Id " + storyId);
    }

    private void logFailedChoiceLinks(List<StoryPage> storyPages) {
        boolean isFailure = false;

        for (StoryPage page : storyPages) {
            for (PageChoice choice : page.getPageChoices()) {
                if (choice.getDestinationPageId() == PAGE_NOT_FOUND_ID) {
                    logger.error("Failed to link destination pageId for choice - " + choice.getChoiceId()
                            + "-" + choice.getChoiceText() + " on page " + page.getStoryPageId()
                            + "-" + page.getOriginalSubName() + ". Attempted destination sub for choice = "
                            + choice.getDestinationSubName());
                    isFailure = true;
                }
            }
        }

        if (!Configuration.getInstance().isForceContinueOnErrors() && isFailure) {
            logger.error("Linking choices failed and fail on error flag is set. Ending run.");
            System.exit(LOG_FAILED_CHOICE_LINKS);
        }
    }

}
