package org.intellij.j2ee.web.resin.resin.version;

import org.intellij.j2ee.web.resin.ResinBundle;

import java.io.File;
import java.lang.reflect.Field;

/**
 * This class is able to detect the version of the selected resin home
 */
class ClassCallDetector {
    //Constants
    private static final String RESIN_LIB_NAME = "lib";
    private static final String RESIN_JAR_NAME = "resin.jar";
    private static final String RESIN_VERSION_CLASS = "com.caucho.Version";
    private static final String RESIN_XDEBUG_CLASS = "com.caucho.log.LogManagerImpl";
    private static final String RESIN_VERSION_CLASS_ATT_NAME = "VERSION";

    /**
     * This method will try to retrieve the version of the selected Resin by calling the class com.caucho.Version
     * @param resinHome the resin home
     * @return ResinVersion representing the detected version. null if it was unable to detect it
     */
    public static ResinVersion getResinVersion(File resinHome){
        //java -cp "<resinHome>/lib/resin.jar" com.caucho.Version
        try {
            //Locate resin.jar lib
            String resinJarPath = getResinJarFile(resinHome);
            if (resinJarPath == null)
                return null;

            //Extract version from class
            JarClassLoader loader = new JarClassLoader(resinJarPath);
            Class versionClass = loader.loadClass(RESIN_VERSION_CLASS);
            Field f = versionClass.getDeclaredField(RESIN_VERSION_CLASS_ATT_NAME);
            String version = f.get(null).toString();
            String startupClass = StartupClassFinder.getStartupClassForVersion(version);

            //Support debug?
            boolean xDebug = false;
            try{
                xDebug = loader.loadClass(RESIN_XDEBUG_CLASS) != null;
            }
            catch(ClassNotFoundException ignore){}

            return startupClass == null ? null :
                    new GenericResinVersion(ResinBundle.message("resin.version.prefix", version), version, startupClass, xDebug);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String getResinJarFile(File resinHome){
        try {
            //Locate resin.jar lib
            String resinJarLocation = resinHome.getAbsolutePath() + File.separator + RESIN_LIB_NAME + File.separator + RESIN_JAR_NAME;
            File resinJarFile = new File(resinJarLocation);
            if (!resinJarFile.exists())
                return null; //Jar not found

            return resinJarLocation;
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

}