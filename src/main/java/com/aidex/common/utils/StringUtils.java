package com.aidex.common.utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils extends org.apache.commons.lang3.StringUtils
{
    private static final char SEPARATOR = '_';

    public static String nvl(String value, String defaultValue)
    {
        return isBlank(value) ? defaultValue : value;
    }

    public static String toCamelCase(String value)
    {
        if (isBlank(value))
        {
            return value;
        }
        String lower = value.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (char c : lower.toCharArray())
        {
            if (c == SEPARATOR || c == '-' || c == ' ')
            {
                upperNext = true;
                continue;
            }
            sb.append(upperNext ? Character.toUpperCase(c) : c);
            upperNext = false;
        }
        return sb.toString();
    }

    public static String toCapitalizeCamelCase(String value)
    {
        String camel = toCamelCase(value);
        if (isBlank(camel))
        {
            return camel;
        }
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    public static String removePrefix(String value, List<String> prefixes)
    {
        if (isBlank(value) || prefixes == null)
        {
            return value;
        }
        for (String prefix : prefixes)
        {
            if (isNotBlank(prefix) && value.startsWith(prefix))
            {
                return value.substring(prefix.length());
            }
        }
        return value;
    }

    public static List<String> splitToList(String value, String separator)
    {
        List<String> list = new ArrayList<String>();
        if (isBlank(value))
        {
            return list;
        }
        for (String item : value.split(separator))
        {
            if (isNotBlank(item))
            {
                list.add(item.trim());
            }
        }
        return list;
    }

    public static String upperFirst(String value)
    {
        if (isBlank(value))
        {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
