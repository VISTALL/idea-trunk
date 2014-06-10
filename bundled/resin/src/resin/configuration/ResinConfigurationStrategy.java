package org.intellij.j2ee.web.resin.resin.configuration;

import com.intellij.execution.ExecutionException;
import org.intellij.j2ee.web.resin.resin.ResinInstallation;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.jdom.Document;

import java.io.InputStream;

public abstract class ResinConfigurationStrategy {
    public abstract boolean setPort(int port, Document document);
    public abstract boolean deploy(WebApp webApp, Document document) throws ExecutionException;
    public abstract boolean undeploy(WebApp webApp, Document document) throws ExecutionException;
    public abstract InputStream getDefaultResinConfContent();

    public static ResinConfigurationStrategy getForInstallation(final ResinInstallation resinInstallation) {
        ResinConfigurationStrategy strategy = null;

        String verNumber = resinInstallation.getVersion().getVersionNumber();
        //Extract only first version number (to accept 2.x, 3.x, 3.1.13, ...)
        int resinVersion = Integer.parseInt(verNumber.substring(0, verNumber.indexOf('.')));
        int buildVersion = Integer.parseInt(verNumber.substring(2, 3));
        switch(resinVersion){
            case 2:
                strategy = new Resin2XConfigurationStrategy();
                break;
            case 3:
                //From resin 3.2.0 resin.conf is not longer valid... instead resin.xml
                strategy = buildVersion >= 2 ? new ResinXmlConfigurationStrategy(resinInstallation) : new Resin3XConfigurationStrategy();
                break;
            default:
                strategy = new Resin3XConfigurationStrategy();
        }

        return strategy;
    }
}

