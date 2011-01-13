/*******************************************************************************
 * Copyright (c) 2004, 2010 BREDEX GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BREDEX GmbH - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 */
package org.eclipse.jubula.tools.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jubula.tools.constants.StringConstants;

/**
 * @author BREDEX GmbH
 *
 */
public class StringParsing {
    
    /** constant for default mask */
    public static final int DEFAULTMASK = 0;
    
    /** constant for mask, lists and combos */
    public static final int LISTCOMBOMASK = 1;
    
    /** constant for mask, menuitems and treenodes */
    public static final int MENUTREEMASK = 2;
    
    /** constant for singlequotes */
    public static final String SINGLEQUOTE = "'"; //$NON-NLS-1$
    
    /** constant for masked singlequote */
    public static final String MASKED_SINGLEQUOTE = "'" + "\\\\" + "''"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    /** constant for singlequotes */
    public static final String ALHANUMERIC = "[A-Za-z0-9]*"; //$NON-NLS-1$
    
    /**
     * invisible constructor
     */
    private StringParsing() {
        // this is an utility class
    }
    
    /**
     * Converts a Locale.toString() value into a Locale.
     * @param localeToString a Locale.toString() value. Must have the Format:
     * language_country!
     * @return a Locale
     */
    public static Locale toLocale(String localeToString) {
        if (localeToString == null) {
            return null;
        }
        String[] locales = localeToString.split("_"); //$NON-NLS-1$
        
        Validate.isTrue(locales.length == 2, 
                "Could not create locale for '" + localeToString  //$NON-NLS-1$
                + "' because it is not in the format '<language>_<country>'"); //$NON-NLS-1$
        final Locale locale = new Locale(locales[0], locales[1]);
        
        return locale;
    }
    
    /**
     * Method to split a text with a given delimeter and escapeChar into
     * a List of Strings
     * @param string
     *      String
     * @param delimeter
     *      char
     * @param escape
     *      char
     * @param includeEmptyToken should empty tokens (i.e. <del><del>) 
     * be returned
     * @return String[]
     *      Splitted text
     */    
    
    public static List splitToList(String string, char delimeter, char escape,
            boolean includeEmptyToken) {
        List list = new ArrayList();
        if (string == null) {
            return list;
        }
        int length = string.length();
        if (length == 0) {
            list.add(new String());
            return list;
        }
        // index for list
        int index = 0;
        int postIndex = 0;
        // flag to signalize to escape next char
        boolean escapeNextChar = false;
        boolean delimWasLastChar = false;
        StringBuffer word = new StringBuffer();
        
        while (index < length) {
            // Escape Recognition
            postIndex = (index < (string.length() - 1)) ? (index + 1) : index;
            if (string.charAt(index) == escape
                && string.charAt(postIndex) == delimeter
                && !escapeNextChar) {
                delimWasLastChar = false;
                escapeNextChar = true; 
            // Delimeter Recognition
            } else  if (string.charAt(index) == delimeter
                && !escapeNextChar) {
                if ((word.length() > 0) || includeEmptyToken) {
                    list.add(word.toString());
                    word.delete(0, word.length());
                }
                delimWasLastChar = true;
                escapeNextChar = false;
            // build subString
            } else {  
                word.append(string.charAt(index));
                delimWasLastChar = false;
                escapeNextChar = false;
            }
            index++;
        }
        if ((word.length() > 0) || (includeEmptyToken && delimWasLastChar)) {
            list.add(word.toString());
        }
        return list;
    }

    /**
     * Method to split a text with a given delimeter and escapeChar into
     * a String Array
     * @param string
     *      String
     * @param delimeter
     *      char
     * @param escape
     *      char
     * @return String[]
     *      Splitted text
     */
    public static String[] splitToArray(
            String string, 
            char delimeter, 
            char escape) {
        return splitToArray(string, delimeter, escape, false);
    }
    
    /**
     * Method to split a text with a given delimeter and escapeChar into
     * a String Array
     * @param string
     *      String
     * @param delimeter
     *      char
     * @param escape
     *      char
     * @param includeEmptyToken should empty tokens (i.e. <del><del>) 
     * be returned
     * @return String[]
     *      Splitted text
     */
    public static String[] splitToArray(
            String string, 
            char delimeter, 
            char escape,
            boolean includeEmptyToken) {
        List list = splitToList(string, delimeter, escape, includeEmptyToken);
        return (String[]) list.toArray(new String[list.size()]);
    }
    
    /**
     * Counts the number of words in the given string. A word is a string 
     * surrounded by whitespace.
     * 
     * @param text The text for which to count the words. If this is 
     *             <code>null</code>, the number of words is considered 0.
     * @return the number of words in the given text
     */
    public static int countWords(String text) {
        if (text != null) {
            int count = text.split("\\s+").length; //$NON-NLS-1$
            return count;
        }
        return 0;
    }
    
    /**
     * Method to mask special characters like backslash, dollar etc
     * @param string String
     * @return masked String
     */
    public static String singleQuoteText(String string) {        
        
        if (string == null || string.equals(StringConstants.EMPTY)) {
            return "''"; //$NON-NLS-1$
        }
        if (!(string.matches(ALHANUMERIC))) {
            String str = string.replaceAll(SINGLEQUOTE, MASKED_SINGLEQUOTE);
            return SINGLEQUOTE + str + SINGLEQUOTE;
        }
        return string;        
    }
    
    /**
     * Method to mask special characters for lists, menus and trees
     * @param string String
     * @param mask int
     * @return masked String
     */
    public static String maskAndSingleQuoteText(String string, int mask) {
        String maskedString = singleQuoteText(string);
        if (mask == LISTCOMBOMASK) {
            maskedString = maskedString.replaceAll(",", "\\\\,"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (mask == MENUTREEMASK) {
            maskedString = maskedString.replaceAll("/", "\\\\/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return maskedString; 
    }
    
    /**
     * converts boolean to String
     * @param b boolean
     * @return string to boolean
     */
    public static String boolToString(boolean b) {
        String str;
        if (b) {
            str = "true"; //$NON-NLS-1$
        } else {
            str = "false"; //$NON-NLS-1$
        }
        return str;
    }

    /**
     * Equivalent to calling 
     * {@link StringParsing#incrementSequence(String, String)} with 
     * <code>sequencePrefix="_"</code>.
     * 
     * @param str The string for which the sequence should be incremented. 
     *            Must not be empty (<code>null</code> or empty string). 
     * @return a string that represents the given string <code>str</code> with
     *         the sequence number incremented.
     */
    public static String incrementSequence(String str) {
        return incrementSequence(str, "_"); //$NON-NLS-1$
    }

    /**
     * Checks <code>str</code> for a sequence number, based on the 
     * <code>sequencePrefix</code>. If a sequence number is found, it is 
     * incremented, and the incremented string is returned. If no sequence is 
     * found, a new sequence is started, and the string with the newly started 
     * sequence is returned. Only positive integers are recognized as a 
     * valid sequence.
     * 
     * <pre>
     * StringParsing.incrementSequence("abc", "_")      = "abc_1"
     * StringParsing.incrementSequence("abc_5", "_")    = "abc_6"
     * StringParsing.incrementSequence("_2", "_")       = "_3"
     * StringParsing.incrementSequence("1", "_")        = "1_1"
     * StringParsing.incrementSequence("abc_-1", "_")   = "abc_-1_1"
     * StringParsing.incrementSequence("abc_0", "_")    = "abc_0_1"
     * StringParsing.incrementSequence("abc_1_1", "_")  = "abc_1_2"
     * </pre>
     * 
     * @param str The string for which the sequence should be incremented. 
     *            Must not be empty (<code>null</code> or empty string). 
     * @param sequencePrefix The string that precedes the sequence. Must not be 
     *                       empty (<code>null</code> or empty string).
     * @return a string that represents the given string <code>str</code> with
     *         the sequence number incremented.
     */
    public static String incrementSequence(String str, 
            String sequencePrefix) {

        Validate.notEmpty(str);
        Validate.notEmpty(sequencePrefix);
        
        StringBuffer builder = new StringBuffer(str);
        String suffix = StringUtils.substringAfterLast(str, "_"); //$NON-NLS-1$

        // parse suffix to integer and increment if possible.
        // if we can't parse it, then we just start a new sequence.
        int sequence = -1;
        try {
            sequence = Integer.parseInt(suffix);
            if (sequence > 0) {
                sequence++;
            }
        } catch (NumberFormatException nfe) {
            // Could not parse the suffix to an integer.
            // The sequence will remain at its initialized value.
        }
        
        if (sequence > 0) {
            builder.replace(builder.lastIndexOf(suffix), 
                    builder.length(), String.valueOf(sequence));
        } else {
            builder.append(sequencePrefix).append(1);
        }

        return builder.toString();
    }
}
