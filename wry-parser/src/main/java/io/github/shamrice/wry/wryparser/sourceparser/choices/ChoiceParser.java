package io.github.shamrice.wry.wryparser.sourceparser.choices;

import io.github.shamrice.wry.wryparser.configuration.Configuration;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilter;
import io.github.shamrice.wry.wryparser.filter.exclude.ExcludeFilterType;
import io.github.shamrice.wry.wryparser.filter.trim.LineTrimmer;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;
import static io.github.shamrice.wry.wryparser.sourceparser.constants.QBasicCommandConstants.*;

public class ChoiceParser {

    private Logger logger = Logger.getLogger(ChoiceParser.class);

    public List<PageChoice> getChoicesForSub(List<String> rawSubLineData) {
        Map<Integer, String> choiceDestinations = new HashMap<>();
        Map<Integer, String> choicesText = new HashMap<>();

        boolean isNextLineChoiceDestination = false;
        int choiceId = -1;

        for (String currentLine : rawSubLineData) {

            ExcludeFilter cmdExcludeFilter = Configuration.getInstance().getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);

                //if choices area of story text
                if (currentLine.matches(CHOICE_REGEX_PATTERN)) {
                    String[] idAndText = currentLine.split("\\) ");

                    logger.debug("getChoicesForSub :: idAndText[0] = " + idAndText[0]);
                    logger.debug("getChoicesForSub :: idAndText[1] = " + idAndText[1]);

                    int id = Integer.parseInt(idAndText[0]);
                    String text = idAndText[1];

                    choicesText.put(id, text);
                }

                //get destination sub for each switch choice
                if (currentLine.contains(CASE_COMMAND) && !currentLine.contains(SELECT_COMMAND)
                        && !currentLine.contains(ELSE_COMMAND)) {
                    isNextLineChoiceDestination = true;
                    choiceId = Integer.parseInt(currentLine.split("\\ ")[1]); //TODO : ewww...
                }

                if (isNextLineChoiceDestination && !currentLine.contains(CASE_COMMAND)) {

                    logger.debug("getChoiceDestinationsForSub :: Potential destination SUB = " + currentLine
                            + " choice = " + choiceId);

                    choiceDestinations.put(choiceId, currentLine);
                    isNextLineChoiceDestination = false;
                    choiceId = -1;
                }
            }
        }

        List<PageChoice> pageChoices = new LinkedList<>();

        for (int id : choiceDestinations.keySet()) {
            if (id < 10) {
                pageChoices.add(new PageChoice(id, choicesText.get(id), choiceDestinations.get(id)));
            }
        }

        return pageChoices;
    }


    public List<PageChoice> getChoicesForMultiPageSub(List<String> rawSubLineData) {
        Map<Integer, String> choiceDestinations = new HashMap<>();
        Map<Integer, String> choicesText = new HashMap<>();

        for (String currentLine : rawSubLineData) {

            ExcludeFilter cmdExcludeFilter = Configuration.getInstance().getExcludeFilter(ExcludeFilterType.BASIC_COMMANDS);

            if (cmdExcludeFilter != null && !cmdExcludeFilter.isExcludedWordInLine(currentLine)) {
                currentLine = LineTrimmer.trimPrintCommandsAndSpaces(currentLine);

                //get choices area of story text
                if (currentLine.matches(CHOICE_REGEX_PATTERN)) {
                    String[] idAndText = currentLine.split("\\)");

                    logger.debug("getChoicesForSub :: idAndText[0] = " + idAndText[0]);
                    logger.debug("getChoicesForSub :: idAndText[1] = " + idAndText[1]);

                    int id = Integer.parseInt(idAndText[0]);
                    String text = idAndText[1].trim();

                    choicesText.put(id, text);
                }

                //get choice destinations from if statements
                if (currentLine.contains(IF_COMMAND) && currentLine.contains(THEN_COMMAND)) {

                    logger.debug("Multi-sub choice line: " + currentLine);
                    String condensedLine = currentLine.replace(" ", "");
                    logger.debug("Multi-sub condensed line: " + condensedLine);

                    try {
                        int choiceId = Integer.parseInt(condensedLine.substring(
                                condensedLine.indexOf("=") + 1,
                                condensedLine.indexOf("=") + 2)); //TODO : major ew

                        String destinationLabel;

                        if (currentLine.contains(THEN_COMMAND + " " + GOTO_COMMAND)) {
                            destinationLabel = condensedLine.substring(
                                    condensedLine.lastIndexOf(GOTO_COMMAND) + 4);
                        } else {
                            destinationLabel = condensedLine.substring(
                                    condensedLine.lastIndexOf(THEN_COMMAND) + 4);
                        }

                        logger.debug("Multi-sub choice dest=" + choiceId + " :: dest label=" + destinationLabel);

                        choiceDestinations.put(choiceId, destinationLabel);
                    } catch (Exception ex) {
                        logger.error("Multi-sub failed: ", ex);
                        if (!Configuration.getInstance().isForceContinueOnErrors()) {
                            System.exit(-9);
                        }
                    }
                }
            }
        }

        List<PageChoice> pageChoices = new LinkedList<>();

        try {
            for (int id : choiceDestinations.keySet()) {
                if (id < 10) {
                    logger.info("Adding multi-sub choice " + id + "-" + choicesText.get(id) + " :: dest="
                            + choiceDestinations.get(id));
                    pageChoices.add(new PageChoice(id, choicesText.get(id), choiceDestinations.get(id)));
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to add multi-sub choices", ex);
            if (!Configuration.getInstance().isForceContinueOnErrors()) {
                System.exit(-1);
            }
        }

        return pageChoices;
    }

}
