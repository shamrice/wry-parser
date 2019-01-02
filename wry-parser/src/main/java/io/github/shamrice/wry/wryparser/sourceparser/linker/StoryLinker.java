package io.github.shamrice.wry.wryparser.sourceparser.linker;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.util.List;

public class StoryLinker {

    private final static Logger logger = Logger.getLogger(StoryLinker.class);


    public void linkDestinationPageIdsToChoices(List<StoryPage> storyPages) {

        logger.info("Linking choice destination page ids to choices.");
        int choicesLinked = 0;
        int numChoices = 0;

        for (StoryPage page : storyPages) {

            //set choices destination page id for the current page.
            for (PageChoice choice : page.getPageChoices()) {
                for (StoryPage page2 : storyPages) {
                    if (choice.getDestinationSubName().equals(page2.getOriginalSubName())) {
                        logger.info("Found destination pageId " + page2.getStoryPageId()
                                + "-" + page2.getOriginalSubName() + " "
                                + " for choice " + choice.getChoiceText());

                        choice.setDestinationPageId(page2.getStoryPageId());
                        //choice.setParsed(true);
                        choice.setStatusMessage("Finished linking and parsing choice");
                        choicesLinked++;
                    }
                }
                numChoices++;
            }
        }

        logger.info("Linked " + choicesLinked + " destination pages for " + numChoices + " choices");
        logFailedChoiceLinks(storyPages);

    }

    public void link(Story story, List<StoryPage> storyPages) {
        logger.info("Linking pages to story : " + story.getStoryName());

        int pregamePageId = -1;

        //set pregame start
        for (StoryPage page : storyPages) {

            //set source story id for pregame page.
            if (page.getOriginalSubName().equals(story.getFirstPageSubName()) && page.isPreGamePage()) {
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

        for (StoryPage page : storyPages) {
            for (PageChoice choice : page.getPageChoices()) {
                if (!choice.isParsed()) {
                    logger.error("Failed to parse choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + " for page " + page.getOriginalSubName());
                }
            }
        }

    }

    private void traverseStory(int storyId, StoryPage currentPage, List<StoryPage> allPages) {

        // TODO : seems to mostly work now. Need to fix failed parsed pages as well as gameover / win screens.

        for (PageChoice choice : currentPage.getPageChoices()) {
            logger.info("Page " + currentPage.getOriginalSubName() + " choice text= " + choice.getChoiceText()
                    + " choice dest= " + choice.getDestinationSubName());

            if (choice.getTraverseCount() < 10 && !choice.isParsed()) {

                if (choice.getDestinationPageId() != -1) {

                    int nextPageId = choice.getDestinationPageId();

                    StoryPage nextPage = allPages.get(nextPageId);

                    choice.incrementTraverseCount();
                    choice.setParsed(true);

                    currentPage.setSourceStoryId(storyId);

                    traverseStory(storyId, nextPage, allPages);

                }

                if (choice.getDestinationPageId() == -1) {
                    currentPage.setParsed(true);
                    currentPage.setStatusMessage("Destination for choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + ". Dest sub=" + choice.getDestinationSubName() + " failed to parse because destination pageId = -1");
                }
            } else {
                logger.error("Exceeded maximum number of traversals through choice " + choice.getChoiceId() + "-" + choice.getChoiceText());
            }
        }

        currentPage.setParsed(true);
        currentPage.setStatusMessage("Finishing linking page to story");

        logger.info("Finished linking page " + currentPage.getStoryPageId() + "-" + currentPage.getOriginalSubName()
                + " to story Id " + storyId);
    }

    private void logFailedChoiceLinks(List<StoryPage> storyPages) {
        for (StoryPage page : storyPages) {
            for (PageChoice choice : page.getPageChoices()) {
                if (choice.getDestinationPageId() == -1) {
                    logger.error("Failed to link destination pageId for choice - " + choice.getChoiceId()
                            + "-" + choice.getChoiceText() + " on page " + page.getStoryPageId()
                            + "-" + page.getOriginalSubName() + ". Attempted destination sub for choice = "
                            + choice.getDestinationSubName());
                }
            }
        }
    }

}
