package io.github.shamrice.wry.wryparser.story;

import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Story {

    private int storyId;
    private String storyName;
    private boolean isParseSuccessful = false;
    private List<String> errorMessages = new ArrayList<>();
    private List<StoryPage> pages = new LinkedList<>();
    private String firstPageSubName;

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

    public boolean isParseSuccessful() {
        return isParseSuccessful;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
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
}
