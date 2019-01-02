package io.github.shamrice.wry.wryparser.game;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;

import java.util.List;
import java.util.Scanner;

public class GameRunner {

    private List<Story> storyList;

    public GameRunner(List<Story> storyList) {
        this.storyList = storyList;
    }

    public void run() {
        System.out.println("\n\nSelect a story:");
        for (Story story : storyList) {
            System.out.println(" " + story.getStoryId() + ") " + story.getStoryName());
        }
        System.out.println("99) Quit");

        int choice = -1;

        while (choice < 1) {
            Scanner reader = new Scanner(System.in);
            choice = reader.nextInt();

        }

        if (choice != 99)  {
            for (Story story : storyList) {
                if (story.getStoryId() == choice) {
                    playStory(story);
                }
            }
        }

    }

    private void playStory(Story story) {
        System.out.println("Playing story: " + story.getStoryName());

        StoryPage currentPage = null;

        for (StoryPage page : story.getPages()) {
            page.logStoryPageDetails();

            if (page.isPreGamePage()) {
                currentPage = page;
            }
        }

        if (currentPage != null) {
            while (!currentPage.isWinPage() && !currentPage.isGameOverPage()) {

                System.out.println(currentPage.getStoryPageId() + "-" + currentPage.getOriginalSubName());
                System.out.println(currentPage.getPageText());
                System.out.println("\n");
                for (PageChoice choice : currentPage.getPageChoices()) {
                    System.out.println(choice.getChoiceId() + ") " + choice.getChoiceText());
                }
                System.out.println("\nSelection: ");

                int inputChoice = -1;

                while (inputChoice < 1) {
                    Scanner reader = new Scanner(System.in);
                    inputChoice = reader.nextInt();

                }

                int destinationPageId = -1;
                for (PageChoice choice : currentPage.getPageChoices()) {
                    if (choice.getChoiceId() == inputChoice) {
                        destinationPageId = choice.getDestinationPageId();
                    }
                }

                for (StoryPage page : story.getPages()) {
                    if (page.getStoryPageId() == destinationPageId) {
                        currentPage = page;
                    }
                }
            }
        }

        run();

    }
}
