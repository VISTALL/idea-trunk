package org.intellij.j2ee.web.resin.resin.version;

import org.intellij.j2ee.web.resin.ResinBundle;

import java.io.File;

public abstract class ResinVersion {
    //Variables
    private final String name; // for debug only
    private final String versionNumber;

    protected ResinVersion(String name, String versionNumber) {
        this.name = name;
        this.versionNumber = versionNumber;
    }

    public String toString() {
        return name;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public boolean equals(Object obj) {
        return obj instanceof ResinVersion && this.toString().equals(obj.toString());
    }

    public abstract boolean isOfVersion(File resinHome);

    public abstract String getStartupClass();

    public abstract boolean allowXdebug();


    //Inner implementation classes
    public static final ResinVersion VERSION_2_X = new ResinVersion(ResinBundle.message("resin.version.fallback.v2"), "2.x") {
        public boolean isOfVersion(File resinHome) {
            return new File(new File(resinHome, "lib"), "jsdk23.jar").exists();
        }

        public String getStartupClass() {
            return "com.caucho.server.http.HttpServer";
        }

        public boolean allowXdebug() {
            return false;
        }
    };
    public static final ResinVersion VERSION_3_X = new ResinVersion(ResinBundle.message("resin.version.fallback.v3"), "3.x") {
        public boolean isOfVersion(File resinHome) {
            return new File(new File(resinHome, "lib"), "jsdk-24.jar").exists();
        }

        public String getStartupClass() {
            return "com.caucho.server.resin.Resin";
        }

        public boolean allowXdebug() {
            return true;
        }
    };
    public static final ResinVersion UNKNOWN_VERSION = new ResinVersion(ResinBundle.message("resin.version.fallback.vUnknown"), "unknown") {
        public boolean isOfVersion(File resinHome) {
            return !VERSION_2_X.isOfVersion(resinHome) && !VERSION_3_X.isOfVersion(resinHome);
        }

        public String getStartupClass() {
            return null;
        }

        public boolean allowXdebug() {
            return false;
        }
    };

}
