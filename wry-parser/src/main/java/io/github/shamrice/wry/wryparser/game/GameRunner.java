package io.github.shamrice.wry.wryparser.game;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Scanner;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.PAGE_NOT_FOUND_ID;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.TITLE_SCREEN_PAGE_ID;

public class GameRunner {

    private final static Logger logger = Logger.getLogger(GameRunner.class);

    private List<Story> storyList;

    public GameRunner(List<Story> storyList) {
        this.storyList = storyList;
    }

    public void displayTitleScreen(List<String> titleScreenData) {
        for (String line : titleScreenData) {
            line = line.replace("PRINT", "");
            line = line.replace("\"", "");
            System.out.println(line);
        }
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

        logger.debug("************ STORY PAGES ********************************");
        for (StoryPage page : story.getPages()) {
            page.logStoryPageDetails("GameRunner::playStory");

            if (page.getPageType() == PageType.PREGAME_PAGE) {
                currentPage = page;
            }
        }

        logger.debug("*********** STORY Game over screens *******************");
        for (StoryPage page : story.getPages()) {
            if (page.getPageType() == PageType.GAMEOVER_PAGE) {
                page.logStoryPageDetails("GameRunner::playStory");
            }
        }

        if (currentPage != null) {

            int destinationPageId;

            do {
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

                //get destination page id
                destinationPageId = PAGE_NOT_FOUND_ID;
                for (PageChoice choice : currentPage.getPageChoices()) {
                    logger.debug("Potential choice destinations= choice id = " + choice.getChoiceId() +
                            " dest info :: pageId=" + choice.getDestinationPageId() + " subName=" +
                            choice.getDestinationSubName());

                    if (choice.getChoiceId() == inputChoice) {
                        destinationPageId = choice.getDestinationPageId();
                    }
                }

                //search story for that destination page id
                for (StoryPage page : story.getPages()) {
                    if (page.getStoryPageId() == destinationPageId) {
                        currentPage = page;
                    }
                }
            } while (destinationPageId != PAGE_NOT_FOUND_ID && destinationPageId != TITLE_SCREEN_PAGE_ID);

            if (destinationPageId == TITLE_SCREEN_PAGE_ID) {
                logger.debug("Game over or won. Returning to title screen");
            } else if (destinationPageId == PAGE_NOT_FOUND_ID) {
                logger.debug("Destination page not found. Quitting back to title screen. Double check parsing results.");
            }
        }



        //return to menu if page not found or dest is title screen.
        run();

    }
}
