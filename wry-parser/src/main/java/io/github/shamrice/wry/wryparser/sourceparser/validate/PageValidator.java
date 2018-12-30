package io.github.shamrice.wry.wryparser.sourceparser.validate;

import java.util.List;

public class PageValidator {

    private final static String GAME_OVER_SUB_NAME = "Gameover";

    public static boolean isValidPage(List<String> subLineData) {

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

    public static boolean isGameOverScreen(List<String> subLineData) {
        for (String line : subLineData) {
            if (line.contains(GAME_OVER_SUB_NAME)) {
                return true;
            }
        }

        return false;
    }
}
