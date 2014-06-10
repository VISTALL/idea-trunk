package org.intellij.j2ee.web.resin.resin;

import org.intellij.j2ee.web.resin.resin.version.ResinLibCollector;
import org.intellij.j2ee.web.resin.resin.version.ResinVersionDetector;
import org.intellij.j2ee.web.resin.resin.version.ResinVersion;

import java.io.File;

public class ResinInstallation {
    //Variables
    private final File resinHome;

    /**
     * Public constructor
     * @param resinHome the resin home
     */
    public ResinInstallation(File resinHome) {
        this.resinHome = resinHome;
    }

    public ResinVersion getVersion() {
        //New feature: auto detect resin version
        return ResinVersionDetector.getResinVersion(resinHome);
    }

    public boolean isResinHome() {
        ResinVersion ver = getVersion();
        return ver != null && !ver.equals(ResinVersion.UNKNOWN_VERSION);
    }

    public String getDisplayName() {
        return getVersion().toString();
    }

    public File[] getLibFiles(boolean all) {
        return ResinLibCollector.getLibFiles(resinHome, all);
    }

    public File getResinHome(){
        return resinHome;
    }
}
