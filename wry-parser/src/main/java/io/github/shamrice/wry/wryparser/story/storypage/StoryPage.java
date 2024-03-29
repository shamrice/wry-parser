package io.github.shamrice.wry.wryparser.story.storypage;

import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class StoryPage {

    private static final Logger logger = LogManager.getLogger(StoryPage.class);

    private boolean isParsed = false;
    private int sourceStoryId = -1;
    private int storyPageId;
    private String originalSubName;
    private String pageText;
    private String statusMessage;
    private PageType pageType = PageType.NOT_SET;
    private List<PageChoice> pageChoices = new ArrayList<>();

    public StoryPage(int storyPageId, String originalSubName, String pageText) {
        this.storyPageId = storyPageId;
        this.originalSubName = originalSubName;
        this.pageText = pageText;
    }

    public void setSourceStoryId(int sourceStoryId) {
        this.sourceStoryId = sourceStoryId;
    }

    public int getSourceStoryId() {
        return sourceStoryId;
    }

    public int getStoryPageId() {
        return storyPageId;
    }

    public String getOriginalSubName() {
        return originalSubName;
    }

    public String getPageText() {
        return pageText;
    }

    public boolean isParsed() {
        return isParsed;
    }

    public void setParsed(boolean parsed) {
        isParsed = parsed;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<PageChoice> getPageChoices() {
        return pageChoices;
    }

    public void setPageChoices(List<PageChoice> pageChoices) {
        this.pageChoices = pageChoices;
    }

    public void addPageChoice(PageChoice pageChoice) {
        this.pageChoices.add(pageChoice);
    }

    public void logStoryPageDetails(String sourceMethod) {

        logger.info("logStoryPageDetails :: sourceMethod :: " + sourceMethod
                + " sourceStoryId=" + sourceStoryId + " : storyPageId=" + storyPageId
                + " : originalSubName=" + originalSubName + " : pageText=" + pageText + " : isParsed="
                + isParsed + " : pageType=" + pageType.name()
                + " : statusMessage=" + statusMessage);

        for (PageChoice choice : pageChoices) {
            StringBuilder sbLog = new StringBuilder("logStoryPageDetails :: PageChoices : ");
            sbLog.append("sourceMethod :: " + sourceMethod);
            sbLog.append(" : choiceForSubName= " + originalSubName);
            sbLog.append(" : choiceId=" + choice.getChoiceId());
            sbLog.append(" : sourcePageId= " + choice.getSourcePageId());
            sbLog.append(" : destinationPageId= " + choice.getDestinationPageId());
            sbLog.append(" : destinationSubName=" + choice.getDestinationSubName());
            sbLog.append(" : choiceText=" + choice.getChoiceText());
            sbLog.append(" : isParsed=" + choice.isParsed());
            sbLog.append(" : statusMessage=" + choice.getStatusMessage());
            logger.info(sbLog.toString());
        }
    }

    public PageType getPageType() {
        return pageType;
    }

    public void setPageType(PageType pageType) {
        this.pageType = pageType;
    }
}
