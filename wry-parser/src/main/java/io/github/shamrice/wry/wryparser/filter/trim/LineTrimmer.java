package io.github.shamrice.wry.wryparser.filter.trim;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.QBasicCommandConstants.PRINT_COMMAND;

public class LineTrimmer {

    public static String trimPrintCommandsAndSpaces(String line) {

        line = line.replace(PRINT_COMMAND, "");
        line = line.replace('\"', ' ');
        line = line.trim();

        return line;
    }
}
