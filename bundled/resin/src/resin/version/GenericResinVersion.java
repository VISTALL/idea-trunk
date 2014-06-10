package org.intellij.j2ee.web.resin.resin.version;

import java.io.File;

/**
 * Generic class for autodetected resin version
 */
public class GenericResinVersion extends ResinVersion {
    private final String startupClass;
    private final boolean allowXdebug;

    public GenericResinVersion(String name, String versionNumber, String startupClass, boolean allowXdebug) {
        super(name, versionNumber);
        if (startupClass == null)
            throw new IllegalArgumentException("Startup class cannot be null");
        this.startupClass = startupClass;
        this.allowXdebug = allowXdebug;
    }

    public boolean isOfVersion(File resinHome) {
        ResinVersion ver = ResinVersionDetector.getResinVersion(resinHome);
        return this.equals(ver);
    }

    public String getStartupClass() {
        return startupClass;
    }

    public boolean allowXdebug() {
        return allowXdebug;
    }
}
