package org.intellij.j2ee.web.resin.resin.version;

import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Helper class to find startup resin class
 */
class StartupClassFinder {
    //Constants
    private static final String MAPPING_PROP_FILE = "org/intellij/j2ee/web/resin/resin/version/versionmapping.properties";

    //Variables
    private static Properties versionMapping = null;

    static String getStartupClassForVersion(String version){
        //Check startup class
        if (versionMapping == null)
            loadVersionMapping();

        do{
            String startup = versionMapping.getProperty(version);
            if(startup != null)
                return startup;

            //Fallback into properties
            //First look for mapping of the version (ej: 3.1.13) if no found, substring to the last dot (ej: 3.1), ...
            int posDot = version.lastIndexOf('.');
            if(posDot == -1)
                version = null;
            else version = version.substring(0, posDot);
        }
        while(version != null);

        //No mapping found
        return null;
    }

    private static void loadVersionMapping() {
        versionMapping = new Properties();
        InputStream in = StartupClassFinder.class.getClassLoader().getResourceAsStream(MAPPING_PROP_FILE);
        if (in != null) {
            try {
                versionMapping.load(in);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
