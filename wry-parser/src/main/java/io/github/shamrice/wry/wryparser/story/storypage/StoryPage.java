package io.github.shamrice.wry.wryparser.story.storypage;

import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;

import java.util.LinkedList;
import java.util.List;

public class StoryPage {

    private int sourceStoryId;
    private int storyPageId;
    private String originalSubName;
    private String pageText;
    private boolean isParsed = false;
    private boolean isValidPage = false;
    private String statusMessage;
    private List<PageChoice> pageChoices = new LinkedList<>();

    public StoryPage(int sourceStoryId, int storyPageId, String originalSubName, String pageText) {
        this.sourceStoryId = sourceStoryId;
        this.storyPageId = storyPageId;
        this.originalSubName = originalSubName;
        this.pageText = pageText;
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
}
