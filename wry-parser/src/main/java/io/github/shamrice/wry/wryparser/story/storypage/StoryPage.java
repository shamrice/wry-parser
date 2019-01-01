package io.github.shamrice.wry.wryparser.story.storypage;

import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StoryPage {

    private final static Logger logger = Logger.getLogger(StoryPage.class);

    private int sourceStoryId = -1;
    private int storyPageId;
    private String originalSubName;
    private String pageText;
    private boolean isParsed = false;
    private boolean isValidPage = false;
    private boolean isGameOverPage = false;
    private boolean isWinPage = false;
    private boolean isPreGamePage = false;
    private String statusMessage;
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

    public boolean isValidPage() {
        return isValidPage;
    }

    public void setValidPage(boolean validPage) {
        isValidPage = validPage;
    }

    public void logStoryPageDetails() {

        logger.info("logStoryPageDetails :: sourceStoryId=" + sourceStoryId + " : storyPageId=" + storyPageId
                + " : originalSubName=" + originalSubName + " : pageText=" + pageText + " : isParsed="
                + isParsed + " : isValidPage=" + isValidPage + " : isGameOverPage=" + isGameOverPage
                + " : isWinPage=" + isWinPage + " : isPreGamePage=" + isPreGamePage
                + " : statusMessage=" + statusMessage);

        for (PageChoice choice : pageChoices) {
            StringBuilder sbLog = new StringBuilder("logStoryPageDetails :: PageChoices : ");
            sbLog.append("choiceId=" + choice.getChoiceId());
            sbLog.append(" : sourcePageId= " + choice.getSourcePageId());
            sbLog.append(" : destinationPageId= " + choice.getDestinationPageId());
            sbLog.append(" : destinationSubName=" + choice.getDestinationSubName());
            sbLog.append(" : choiceText=" + choice.getChoiceText());
            sbLog.append(" : isParsed=" + choice.isParsed());
            sbLog.append(" : statusMessage=" + choice.getStatusMessage());
            logger.info(sbLog.toString());
        }

    }

    public boolean isGameOverPage() {
        return isGameOverPage;
    }

    public void setGameOverPage(boolean isGameOverPage) {
        this.isGameOverPage = isGameOverPage;
    }

    public boolean isWinPage() {
        return isWinPage;
    }

    public void setWinPage(boolean isWinPage) {
        this.isWinPage = isWinPage;
    }

    public boolean isPreGamePage() {
        return isPreGamePage;
    }

    public void setPreGamePage(boolean preGamePage) {
        isPreGamePage = preGamePage;
    }
}
