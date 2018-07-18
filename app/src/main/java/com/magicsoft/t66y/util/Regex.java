package com.magicsoft.t66y.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    public static Matcher match(String pattern, String haystack) {
        Pattern p = Pattern.compile(pattern);
        return p.matcher(haystack);
    }

    public static String match_one(String pattern, String haystack, int id) {
        Matcher m = match(pattern, haystack);
        while (m.find()) {
            return m.group(id);
        }
        return null;
    }

}
