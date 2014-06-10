package org.intellij.j2ee.web.resin;

import java.io.File;
import com.intellij.javaee.appServerIntegrations.*;
import org.intellij.j2ee.web.resin.resin.ResinInstallation;
import org.intellij.j2ee.web.resin.ui.SelectResinLocationEditor;

public class ResinApplicationServerHelper implements ApplicationServerHelper {

    public ApplicationServerInfo getApplicationServerInfo(ApplicationServerPersistentData persistentData)
            throws CantFindApplicationServerJarsException {

        org.intellij.j2ee.web.resin.ResinPersistentData resinPersistentData = (org.intellij.j2ee.web.resin.ResinPersistentData) persistentData;
        File resinHome = new File(resinPersistentData.RESIN_HOME == null ? "" : resinPersistentData.RESIN_HOME).getAbsoluteFile();
        ResinInstallation resinInstallation = new ResinInstallation(resinHome);

        try {
            File[] resinLib = resinInstallation.getLibFiles(resinPersistentData.INCLUDE_ALL_JARS);
            String version = resinInstallation.getDisplayName();
            return new ApplicationServerInfo(resinLib, version);
        }
        catch (RuntimeException e) {
            throw new CantFindApplicationServerJarsException(org.intellij.j2ee.web.resin.ResinBundle.message("message.text.cant.find.directory", resinHome.getAbsolutePath()));
        }
    }

    public ApplicationServerPersistentData createPersistentDataEmptyInstance() {
        return new org.intellij.j2ee.web.resin.ResinPersistentData();
    }

    public ApplicationServerPersistentDataEditor createConfigurable() {
        return new SelectResinLocationEditor();
    }
}
