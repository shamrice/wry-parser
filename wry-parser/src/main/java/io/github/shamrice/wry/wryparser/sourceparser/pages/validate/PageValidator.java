package io.github.shamrice.wry.wryparser.sourceparser.pages.validate;

import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.QBasicCommandConstants.*;

public class PageValidator {

    private static final Logger logger = LogManager.getLogger(PageValidator.class);

    // TODO : This should probably be either a constant or a config value.
    public static boolean isPreGameScreen(String subName) {
        return (subName.contains(PREGAME_SUB_NAME_PREFIX) || subName.equals(PREGAME_EPISODE_4_SUB_NAME));
    }

    public static PageType getPageType(List<String> subLineData) {

        if (subLineData == null) {
            return PageType.ERROR;
        }

        if (isMultiPage(subLineData)) {
            return PageType.MULTI_PAGE;
        }

        if (isGameOverScreen(subLineData)) {
            return PageType.GAMEOVER_PAGE;
        }

        if (isPreGameScreen(subLineData)) {
            return PageType.PREGAME_PAGE;
        }

        if (isWinningScreen(subLineData)) {
            return PageType.WIN_PAGE;
        }

        if (isValidPage(subLineData)) {
            return PageType.REGULAR_PAGE;
        }

        if (isPassThroughPage(subLineData)) {
            return PageType.PASS_THROUGH_PAGE;
        }

        if (isPassThroughPageFromMultiPage(subLineData)) {
            return PageType.PASS_THROUGH_PAGE;
        }

        return PageType.ERROR;

    }

    /**
     * Method used to check if sub has multiple pages on it instead of just one
     * These pages will require extended special parsing.
     * @param subLineData raw sub data to check
     * @return true on multi page sub.
     */
    private static boolean isMultiPage(List<String> subLineData) {
        boolean hasStoryText = false;
        boolean hasInput = false;
        boolean hasChoicesText = false;
        boolean hasGotos = false;
        boolean hasIfs = false;

        for (String data : subLineData) {
            if (data.contains(PRINT_COMMAND)) {
                hasStoryText = true;
            }

            if (data.contains(INPUT_COMMAND)) {
                hasInput = true;
            }

            if (data.contains(FIRST_CHOICE_PATTERN)) {
                hasChoicesText = true;
            }

            if (data.contains(GOTO_COMMAND)) {
                hasGotos = true;
            }

            if (data.contains(IF_COMMAND) && data.contains(THEN_COMMAND)) {
                hasIfs = true;
            }
        }

        //if has all these things, most likely is a multi page story sub
        return hasStoryText && hasInput && hasChoicesText && hasGotos && hasIfs;
    }

    private static boolean isValidPage(List<String> subLineData) {

        boolean hasStoryText = false;
        boolean hasInput = false;
        boolean hasChoicesText = false;
        boolean hasChoices = false;

        for (String data : subLineData) {
            if (data.contains(PRINT_COMMAND)) {
                hasStoryText = true;
            }

            if (data.contains(INPUT_COMMAND)) {
                hasInput = true;
            }

            if (data.contains("1)")) {
                hasChoicesText = true;
            }

            if (data.contains(SELECT_COMMAND + " " + CASE_COMMAND)) {
                hasChoices = true;
            }
        }

        //if has all these things, most likely is a story page.
        return hasStoryText && hasInput && hasChoicesText && hasChoices;

    }

    private static boolean isGameOverScreen(List<String> subLineData) {
        for (String line : subLineData) {
            if (line.contains(SUB_COMMAND + " " + GAME_OVER_SUB_NAME)) {
                logger.info("isGameOverScreen :: is gameover screen");
                return true;
            }
        }

        return false;
    }

    private static boolean isWinningScreen(List<String> subLineData) {
        for (String line : subLineData) {
            if (line.contains("YOU WON EPISODE") || line.toLowerCase().contains("you have beaten")) {
                logger.info("isWinningScreen :: is winning screen");
                return true;
            }
        }

        return false;
    }

    // TODO : these should be a constant strings or config strings
    private static boolean isPreGameScreen(List<String> subLineData) {
        for (String line : subLineData) {
            if (line.contains(SUB_COMMAND + " " + PREGAME_SUB_NAME_PREFIX)
                    || line.contains(SUB_COMMAND + " " + PREGAME_EPISODE_4_SUB_NAME)) {

                logger.info("isPreGameScreen :: is pregame screen");
                return true;
            }
        }
        return false;
    }

    //TODO: not crazy about this.......
    private static boolean isPassThroughPage(List<String> subLineData) {

        if (!isValidPage(subLineData) && !isWinningScreen(subLineData) && !isGameOverScreen(subLineData)
            && !isPreGameScreen(subLineData)) {

            for (String line : subLineData) {
                //TODO : not sure how I feel about opra sub being labeled explicitly even though it's an edge condition.
                if (line.contains(SLEEP_COMMAND) || line.contains(OPRA_SUB_NAME)) {
                    logger.info("Found pass through page with matching line : " + line);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isPassThroughPageFromMultiPage(List<String> subLineData) {
        if (!isValidPage(subLineData) && !isWinningScreen(subLineData) && !isGameOverScreen(subLineData)
                && !isPreGameScreen(subLineData)) {

            for (String line : subLineData) {
                if (line.contains(GOTO_COMMAND)) {
                    logger.info("Found pass through page with matching line : " + line);
                    return true;
                }
            }
        }
        return false;
    }
}
