package io.github.shamrice.wry.wryparser.sourceparser.validate;

import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import org.apache.log4j.Logger;

import java.util.List;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.GAME_OVER_SUB_NAME;

public class PageValidator {

    private static final Logger logger = Logger.getLogger(PageValidator.class);

    // TODO : This should probably be either a constant or a config value.
    public static boolean isPreGameScreen(String subName) {
        return (subName.contains("pregame") || subName.equals("ldemo"));
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
            if (data.contains("PRINT")) {
                hasStoryText = true;
            }

            if (data.contains("INPUT")) {
                hasInput = true;
            }

            if (data.contains("1)")) {
                hasChoicesText = true;
            }

            if (data.contains("GOTO")) {
                hasGotos = true;
            }

            if (data.contains("IF") && data.contains("THEN")) {
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
            if (data.contains("PRINT")) {
                hasStoryText = true;
            }

            if (data.contains("INPUT")) {
                hasInput = true;
            }

            if (data.contains("1)")) {
                hasChoicesText = true;
            }

            if (data.contains("SELECT CASE")) {
                hasChoices = true;
            }
        }

        //if has all these things, most likely is a story page.
        return hasStoryText && hasInput && hasChoicesText && hasChoices;

    }

    private static boolean isGameOverScreen(List<String> subLineData) {
        for (String line : subLineData) {
            if (line.contains("SUB Gameover")) {
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
            if (line.contains("SUB pregame") || line.contains("SUB ldemo")) {
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
                if (line.contains("SLEEP") || line.contains("opra")) {
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
                if (line.contains("GOTO")) {
                    logger.info("Found pass through page with matching line : " + line);
                    return true;
                }
            }
        }
        return false;
    }
}
