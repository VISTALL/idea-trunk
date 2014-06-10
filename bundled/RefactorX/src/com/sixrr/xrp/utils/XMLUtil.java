package com.sixrr.xrp.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.templateLanguages.OuterLanguageElement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLUtil {
    public static final String CDATA_PREFIX = "<![CDATA[";
    public static final String CDATA_SUFFIX = "]]>";
    public static final String EMPTY_STR = "";

    private static final Pattern tagPattern =
            Pattern.compile("([A-Za-z0-9_]*:)?[A-Za-z0-9_]+");
    private static final Pattern attributePattern =
            Pattern.compile("([A-Za-z0-9_]*:)?[A-Za-z0-9_]+");

    private XMLUtil() {
        super();
    }

    public static boolean tagNameIsValid(String tagName) {
        final Matcher matcher = tagPattern.matcher(tagName);
        return matcher.matches();
    }

    public static boolean attributeNameIsValid(String attributeName) {
        final Matcher matcher = attributePattern.matcher(attributeName);
        return matcher.matches();
    }

    /**
     * Escapes out xml characters in String s
     *
     * @param s
     * @return
     */
    public static String XMLEscape(String s) {
        if (s == null) {
            return s;
        }
        final int len = s.length();
        final StringBuffer sb = new StringBuffer(len);
        char ch;
        for (int i = 0; i < len; i++) {
            ch = s.charAt(i);
            switch (ch) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                case '\"':
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Takes the escape sequences and replace with
     * their xml character values
     *
     * @param s
     * @return
     */
    public static String XMLUnEscape(String s) {
        if (s == null) {
            return s;
        }

        final int len = s.length();
        StringBuffer sb = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            String subStr = null;
            final char ch = s.charAt(i);

            if (ch == '&') { // potential replacement

                int subEndIndex = i + 8;
                if (subEndIndex > len) {
                    subEndIndex = len;
                }

                subStr = s.substring(i, subEndIndex);
                if (subStr.startsWith("&amp;")) {
                    sb.append('&');
                    i += "amp;".length();
                    continue;
                }
                if (subStr.startsWith("&lt;")) {
                    sb.append('<');
                    i += "lt;".length();
                    continue;
                }
                if (subStr.startsWith("&gt;")) {
                    sb.append('>');
                    i += "gt;".length();
                    continue;
                }
                if (subStr.startsWith("&apos;")) {
                    sb.append('\'');
                    i += "apos;".length();
                    continue;
                }
                if (subStr.startsWith("&quot;")) {
                    sb.append('\"');
                    i += "quot;".length();
                    continue;
                }
            }
            // if fell through just append the char
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Take the str and wrap it in cdata tags
     *
     * @param str
     * @return
     */
    public static String formatCDataWrapper(String str) {
        if (str == null) {
            str = EMPTY_STR;
        }
        return CDATA_PREFIX + str + CDATA_SUFFIX;
    }

    /**
     * Removes the cdata wrapper
     *
     * @param str
     * @return
     */
    public static String stripCDataWrapper(String str) {
        if (str == null) {
            return null;
        }

        String trimmedStr = str.trim();
        if (!trimmedStr.startsWith(CDATA_PREFIX)) {
            return str;
        }
        // remove the header
        trimmedStr = trimmedStr.substring(CDATA_PREFIX.length());

        if (!trimmedStr.endsWith(CDATA_SUFFIX)) {
            return str;
        }
        if (trimmedStr.length() == CDATA_SUFFIX.length()) {
            return EMPTY_STR;
        }
        return trimmedStr.substring(0, trimmedStr.lastIndexOf(CDATA_SUFFIX));
    }

    public static boolean isWhitespace(String contents) {
        for(int i = 0;i<contents.length();i++)
        {
            final char ch = contents.charAt(i);
            if(!Character.isWhitespace(ch))
            {
                return false;
            }
        }
        return true;
    }


  public static boolean containsOuterElements(PsiElement elt) {
    final boolean[] result = {false};
    elt.accept(new PsiRecursiveElementWalkingVisitor() {
      @Override
      public void visitOuterLanguageElement(OuterLanguageElement element) {
        result[0] = true;
      }

      @Override
      public void visitElement(PsiElement element) {
        if (!result[0]) {
          super.visitElement(element);
        }
      }
    });

    return result[0];
  }
}
