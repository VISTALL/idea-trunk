/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GeronimoPortConfig extends JavaeePortConfig {

    private static final Factory<GeronimoLocalModel> LOCAL = factory("(:?tomcat|jetty)6?", "(?:Tomcat|Jetty)WebConnector");

    private static final Factory<GeronimoLocalModel> ADMIN = factory("rmi-naming", "RMIRegistry");

    private final File file;

    private final File props;

    private final String config;

    private final String gbean;

    private GeronimoPortConfig(String home, String config, String gbean) {
        file = new File(home, "var/config/config.xml");
        props = new File(home, "var/config/config-substitutions.properties");
        this.config = config;
        this.gbean = gbean;
    }

    @Override
    protected long getStamp(JavaeeServerModel data) {
        return getStamp(file) ^ getStamp(props);
    }

    @Override
    protected int getPort(JavaeeServerModel model) {
        if (file.exists()) {
            try {
                Element root = JDOMUtil.loadDocument(file).getRootElement();
                Element cfg = getChild(root, "configuration", "name", "geronimo/" + config + "/.*");
                if (cfg == null) {
                    cfg = getChild(root, "module", "name", "(:?geronimo|org\\.apache\\.geronimo\\.(:?configs|framework))/" + config + "/.*");
                }
                if (cfg != null) {
                    Element bean = getChild(cfg, "gbean", "name", gbean);
                    if (bean != null) {
                        Element attribute = getChild(bean, "attribute", "name", "port");
                        if (attribute != null) {
                            return parse(attribute.getTextTrim());
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return INVALID_PORT;
    }

    private int parse(String port) throws IOException {
        Matcher matcher = Pattern.compile("\\$\\{\\s*(\\w+)\\s*\\+\\s*(\\w+)\\s*\\}").matcher(port);
        if (matcher.matches() && props.exists()) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(props));
            String part1 = properties.getProperty(matcher.group(1), "0");
            String part2 = properties.getProperty(matcher.group(2), "0");
            return Integer.parseInt(part1) + Integer.parseInt(part2);
        }
        return Integer.parseInt(port);
    }

    static int getLocal(GeronimoLocalModel model) {
        return get(LOCAL, model, model.getDefaultPort());
    }

    static int getAdmin(GeronimoLocalModel model) {
        return get(ADMIN, model, INVALID_PORT);
    }

    private static Factory<GeronimoLocalModel> factory(@NonNls final String config, @NonNls final String gbean) {
        return new Factory<GeronimoLocalModel>() {
            @NotNull
            public Key createKey(GeronimoLocalModel data) {
                return new Key(data.getHome(), config, gbean);
            }

            @NotNull
            public JavaeePortConfig createConfig(GeronimoLocalModel data) {
                return new GeronimoPortConfig(data.getHome(), config, gbean);
            }
        };
    }
}
