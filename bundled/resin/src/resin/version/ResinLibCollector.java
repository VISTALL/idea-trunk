/*
 * Copyright (c) 2006 Openwave Systems Inc. All rights reserved.
 * 
 * The copyright to the computer software herein is the property of Openwave Systems Inc. The software may be used
 * and/or copied only with the written permission of Openwave Systems Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the software has been supplied.
 */

package org.intellij.j2ee.web.resin.resin.version;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Sergio Cuellar (sergio.cuellar@openwave.com)
 */
public class ResinLibCollector {
    //Variables
    private static final FilenameFilter JAR_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(".jar");
        }
    };

    public static File[] getLibFiles(File resinHome, boolean all) {
        File libDir = new File(resinHome, "lib");
        if (all) {
            return libDir.listFiles(JAR_FILTER);
        }
        else {
            //Resin 3.2.0 -> javaee-XX.jar
            //Other versions -> jsdk-XX.jar
            return libDir.listFiles(
                    new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return JAR_FILTER.accept(dir, name) && ((name.indexOf("jsdk") != -1) || (name.indexOf("javaee-") != -1));
                        }
                    }
            );
        }
    }
}
