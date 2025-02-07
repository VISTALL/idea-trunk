package com.sixrr.xrp.utils;

public class MyUsageViewUtil {

    public static String getUsageCountInfo(int usagesCount, int filesCount, String referenceWord) {
        String info;
        if (filesCount > 0) {
            String files = filesCount != 1 ? " files " : " file ";
            if (usagesCount > 1) {
                referenceWord += "s";
            }
            info = "( " + usagesCount + " " + referenceWord + " in " + filesCount + files + ")";
        } else {
            info = "( Not found )";
        }
        return info;
    }
}
