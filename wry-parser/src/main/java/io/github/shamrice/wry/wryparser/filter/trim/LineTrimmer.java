package io.github.shamrice.wry.wryparser.filter.trim;

public class LineTrimmer {

    public static String trimPrintCommandsAndSpaces(String line) {

        line = line.replace("PRINT", "");
        line = line.replace('\"', ' ');
        line = line.trim();

        return line;
    }
}
