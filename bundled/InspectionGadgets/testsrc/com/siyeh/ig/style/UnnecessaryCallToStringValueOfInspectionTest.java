package com.siyeh.ig.style;

import com.IGInspectionTestCase;

public class UnnecessaryCallToStringValueOfInspectionTest extends IGInspectionTestCase {

    public void test() throws Exception {
        doTest("com/siyeh/igtest/style/unnecessary_valueof",
                new UnnecessaryCallToStringValueOfInspection());
    }
}