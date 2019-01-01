package io.github.shamrice.wry.wryparser.sourceparser.linker;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.util.ArrayList;
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

    public Story link(Story story, List<StoryPage> storyPages) {
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


        traverseStory(story.getStoryId(), storyPages.get(pregamePageId), storyPages);


        //add pages to story.
        for (StoryPage page : storyPages) {
            if (page.getStoryPageId() == story.getStoryId()) {
                logger.info("Adding page " + page.getStoryPageId() + "-" + page.getOriginalSubName()
                        + " to story " + story.getStoryName());
                story.addPage(page);
            }
        }


        return story;
    }

    private void traverseStory(int storyId, StoryPage page, List<StoryPage> allPages) {

        // TODO : causing stack over flow. needs to be re-thunk.
        // TODO : adding check for choiceid = -1 caused spy adventure to get stuck in infinite loop
        // TODO : infinite loop is caused by story's option to choose between 2am and 4pm over and over again.
        // TODO : traversal limits on choices seems to help. needs more fine tuning..........


        for (PageChoice choice : page.getPageChoices()) {
            logger.info("Page " + page.getOriginalSubName() + " choice text= " + choice.getChoiceText()
                    + " choice dest= " + choice.getDestinationSubName());

            if (choice.getTraverseCount() < 10 && !choice.isParsed()) {

                if (!page.isParsed() && choice.getDestinationPageId() != -1) {

                    int nextPageId = choice.getDestinationPageId();

                    StoryPage destPage = allPages.get(nextPageId);

                    if (!destPage.isParsed()) {

                        choice.incrementTraverseCount();
                        choice.setParsed(true);

                        page.setSourceStoryId(storyId);
                        traverseStory(storyId, allPages.get(nextPageId), allPages);
                    }
                }

                if (choice.getDestinationPageId() == -1) {
                    page.setParsed(true);
                    page.setStatusMessage("Destination for choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + ". Dest sub=" + choice.getDestinationSubName() + " failed to parse because destination pageId = -1");
                }
            } else {
                logger.error("Exceeded maximum number of traversals through choice " + choice.getChoiceId() + "-" + choice.getChoiceText());
            }
        }

        page.setParsed(true);
        page.setStatusMessage("Finishing linking page to story");

        logger.info("Finished linking page " + page.getStoryPageId() + "-" + page.getOriginalSubName()
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
