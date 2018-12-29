package io.github.shamrice.wry.wryparser.story.storypage.PageChoice;

public class PageChoice {

    private int choiceId;
    private int sourcePageId;
    private int destinationPageId = -1;
    private String destinationSubName;
    private String choiceText;
    private boolean isParsed = false;
    private String statusMessage;

    public PageChoice(int choiceId, int sourcePageId, String choiceText, String destinationSubName) {
        this.choiceId = choiceId;
        this.sourcePageId = sourcePageId;
        this.choiceText = choiceText;
        this.destinationSubName = destinationSubName;
    }


    public int getChoiceId() {
        return choiceId;
    }

    public int getSourcePageId() {
        return sourcePageId;
    }

    public String getDestinationSubName() {
        return destinationSubName;
    }

    public String getChoiceText() {
        return choiceText;
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

    public int getDestinationPageId() {
        return destinationPageId;
    }

    public void setDestinationPageId(int destinationPageId) {
        this.destinationPageId = destinationPageId;
    }
}