package io.github.shamrice.wry.wryparser.story;

import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class Story {

    private static final Logger logger = LogManager.getLogger(Story.class);

    private int storyId;
    private String storyName;
    private String firstPageSubName;
    private List<StoryPage> pages = new LinkedList<>();

    public Story(int storyId, String storyName) {
        this.storyId = storyId;
        this.storyName = storyName;
    }

    public int getStoryId() {
        return storyId;
    }

    public String getStoryName() {
        return storyName;
    }

    public List<StoryPage> getPages() {
        return pages;
    }

    public void addPage(StoryPage page) {
        this.pages.add(page);
    }

    public String getFirstPageSubName() {
        return firstPageSubName;
    }

    public void setFirstPageSubName(String firstPageSubName) {
        this.firstPageSubName = firstPageSubName;
    }

    public boolean isParseSuccessful() {

        int failedParsedChoices = 0;
        int failedParsedPages = 0;

        for (StoryPage page : pages) {

            if (!page.isParsed()) {
                logger.error("Failed to parse page " + page.getStoryPageId() + "-" + page.getOriginalSubName()
                        + " :: " + page.getStatusMessage() + " :: " + page.getPageText());
                failedParsedPages++;
            }

            for (PageChoice choice : page.getPageChoices()) {
                if (!choice.isParsed()) {
                    logger.error("Failed to parse choice " + choice.getChoiceId() + "-" + choice.getChoiceText()
                            + " for page " + page.getOriginalSubName() + " destination PageId = " + choice.getDestinationPageId()
                            + " destination Sub name = " + choice.getDestinationSubName());
                    failedParsedChoices++;
                }
            }
        }

        if (failedParsedPages > 0) {
            logger.error("Number of pages failed to be parsed and linked to story " + storyId
                    + "-" + storyName + " = " + failedParsedPages);
        } else {
            logger.info("No failed or unparsed pages for story " + storyId + "-" + storyName);
        }

        if (failedParsedChoices > 0) {
            logger.error("Number of choices failed to be parsed and linked to story " + storyId
                    + "-" + storyName + " = " + failedParsedChoices);
        } else {
            logger.info("No failed or unparsed choices for story " + storyId + "-" + storyName);
        }

        return failedParsedChoices == 0 && failedParsedPages == 0;
    }
}
