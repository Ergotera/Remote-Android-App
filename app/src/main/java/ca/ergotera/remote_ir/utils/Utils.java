package ca.ergotera.remote_ir.utils;

/**
 * Class containing utility methods used throughout the project.
 *
 * @author Dominic Fournier (dominicfournier@outlook.com)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public class Utils {

    // Private constructor: class cannot be instantiated
    private Utils() {
    }

    /**
     * Takes in an array of integer and converts it into a single string of comma separated
     * numbers (ie.: '14,2,62,12,63,12').
     *
     * @param arrayToConvert integer array to convert into single string.
     * @return string of comma separated numbers.
     */
    public static String convertIntArrayToString(int[] arrayToConvert) {
        String arrayToStr = "";
        for (int i = 0; i < arrayToConvert.length; i++) {
            arrayToStr += Integer.toString(arrayToConvert[i]) + ",";
        }
        return arrayToStr;
    }

    /**
     * Takes in a single string of comma separated numbers and converts it into an
     * integer array.
     *
     * @param stringArray string of comma separated numbers.
     * @return array of the integers contained in the string and separated by commas.
     */
    public static int[] convertStrArrayToIntArray(String stringArray) {
        String[] strSplit = stringArray.split(",");
        int[] intSplit = new int[strSplit.length];
        for (int i = 0; i < strSplit.length; i++) {
            intSplit[i] = Integer.parseInt(strSplit[i]);
        }
        return intSplit;
    }
}
