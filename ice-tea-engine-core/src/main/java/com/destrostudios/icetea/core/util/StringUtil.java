package com.destrostudios.icetea.core.util;

public class StringUtil {

    public static String addLineNumbers(String text) {
        String textWithLineNumbers = "";
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                textWithLineNumbers += "\n";
            }
            textWithLineNumbers += (i + 1) + ": " + lines[i];
        }
        return textWithLineNumbers;
    }
}
