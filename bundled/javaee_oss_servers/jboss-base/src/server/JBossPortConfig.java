/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JBossPortConfig extends JavaeePortConfig {

    private static final Factory<JBossLocalModel> FACTORY = factory();

    private final String home;

    private final File config;

    private final File naming1;

    private final File naming2;

    private final File binding1;

    private final File binding2;

    private final File binding3;

    private File bindings;

    private boolean argsUsed;

    private JBossPortConfig(String home, String server) {
        this.home = home;
        File base = new File(new File(home, "server"), server);
        config = new File(base, "conf/jboss-service.xml");
        naming1 = new File(base, "deploy/naming-service.xml");
        naming2 = new File(base, "deploy/naming.sar/META-INF/jboss-service.xml");
        binding1 = new File(base, "deploy/binding-service.xml");
        binding2 = new File(base, "conf/bootstrap/bindings.xml");
        binding3 = new File(base, "conf/bindingservice.beans/META-INF/bindings-jboss-beans.xml");
    }

    @Override
    protected long getStamp(JavaeeServerModel data) {
        long stamp = getStamp(config);
        stamp ^= getStamp(naming1);
        stamp ^= getStamp(naming2);
        stamp ^= getStamp(binding1);
        stamp ^= getStamp(binding2);
        stamp ^= getStamp(binding3);
        stamp ^= getStamp(bindings);
        if (argsUsed) {
            stamp ^= getStamp(data.getVmArguments());
        }
        return stamp;
    }

    @Override
    protected int getPort(JavaeeServerModel model) {
        int port = getPort1(config, model);
        if (port == INVALID_PORT) {
            port = getPort2(config, model);
            if (port == INVALID_PORT) {
                port = getPort1(binding1, model);
                if (port == INVALID_PORT) {
                    port = getPort2(naming1, model);
                    if (port == INVALID_PORT) {
                        port = getPort2(naming2, model);
                    }
                }
            }
        }
        return port;
    }

    private int getPort1(File file, JavaeeServerModel model) {
        if (file.exists()) {
            try {
                Document doc = JDOMUtil.loadDocument(file);
                Element bean = getChild(doc.getRootElement(), "mbean", "name", "jboss.system:service=ServiceBindingManager");
                if (bean != null) {
                    Element attribute = getChild(bean, "attribute", "name", "StoreURL");
                    if (attribute != null) {
                        bindings = new File(attribute.getTextTrim().replace("${jboss.home.url}", home));
                        Element name = getChild(bean, "attribute", "name", "ServerName");
                        if (bindings.exists() && (name != null)) {
                            return getPort3(bindings, name.getTextTrim(), model);
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return INVALID_PORT;
    }

    private int getPort2(File file, JavaeeServerModel model) {
        if (file.exists()) {
            try {
                Document doc = JDOMUtil.loadDocument(file);
                Element bean = getChild(doc.getRootElement(), "mbean", "name", "jboss:service=Naming");
                if (bean != null) {
                    Element attribute = getChild(bean, "attribute", "name", "Port");
                    if (attribute != null) {
                        return parse(attribute.getTextTrim(), model);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return INVALID_PORT;
    }

    private int getPort3(File file, @NonNls String name, JavaeeServerModel model) {
        if (file.exists()) {
            try {
                Document doc = JDOMUtil.loadDocument(file);
                Element server = getChild(doc.getRootElement(), "server", "name", name);
                if (server != null) {
                    Element service = getChild(server, "service-config", "name", "jboss:service=Naming");
                    if (service != null) {
                        return parse(service.getChild("binding").getAttributeValue("port"), model);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return INVALID_PORT;
    }

    private int parse(String value, JavaeeServerModel model) {
        int port = INVALID_PORT;
        try {
            port = Integer.parseInt(value);
            argsUsed = false;
        } catch (NumberFormatException e) {
            if (value.startsWith("${") && value.endsWith("}")) {
                argsUsed = true;
                @NonNls String prefix = "-D" + value.substring(2, value.length() - 1) + '=';
                for (String arg : model.getVmArguments().split("\\s+")) {
                    if (arg.startsWith(prefix)) {
                        port = Integer.parseInt(arg.substring(prefix.length()));
                    }
                }
            } else if (binding2.exists()) {
                try {
                    Document doc = JDOMUtil.loadDocument(binding2);
                    port = getStandard(doc) + getOffset(doc, getBinding(doc, "ServiceBindingManager"));
                } catch (Exception ignore) {
                }
            } else if (binding3.exists()) {
                try {
                    Document doc = JDOMUtil.loadDocument(binding3);
                    port = getStandard(doc) + getOffset(doc, getBinding(doc, "ServiceBindingManagementObject"));
                } catch (Exception ignore) {
                }
            }
        }
        return port;
    }

    @Nullable
    private String getBinding(Document doc, @NonNls String name) {
        Element bean = getChild(doc.getRootElement(), "bean", "name", name);
        if (bean != null) {
            String text = getChild(getChild(bean, "constructor"), "parameter").getTextTrim();
            Matcher matcher = Pattern.compile("\\$\\{jboss\\.service\\.binding\\.set:(.+)\\}").matcher(text);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    private int getStandard(Document doc) {
        Element bean = getChild(doc.getRootElement(), "bean", "name", "StandardBindings");
        if (bean != null) {
            Element set = getChild(getChild(getChild(bean, "constructor"), "parameter"), "set");
            for (Element child : getChildren(set, "bean")) {
                Element service = getChild(child, "property", "name", "serviceName");
                if ((service != null) && "jboss:service=Naming".equals(service.getTextTrim())) {
                    Element binding = getChild(child, "property", "name", "bindingName");
                    if ((binding != null) && "port".equalsIgnoreCase(binding.getTextTrim())) {
                        Element port = getChild(child, "property", "name", "port");
                        if (port != null) {
                            return Integer.parseInt(port.getTextTrim());
                        }
                    }
                }
            }
        }
        return INVALID_PORT;
    }

    private int getOffset(Document doc, String name) {
        for (Element child : getChildren(doc.getRootElement(), "bean", "class", ".*\\.ServiceBindingSet")) {
            List<Element> parameters = getChildren(getChild(child, "constructor"), "parameter");
            if ((parameters.size() == 4) && parameters.get(0).getTextTrim().equals(name)) {
                return Integer.valueOf(parameters.get(2).getTextTrim());
            }
        }
        throw new IllegalArgumentException();
    }

    static int get(JBossLocalModel model) {
        return get(FACTORY, model, INVALID_PORT);
    }

    private static Factory<JBossLocalModel> factory() {
        return new Factory<JBossLocalModel>() {
            @NotNull
            public Key createKey(JBossLocalModel data) {
                return new Key(data.getHome(), data.SERVER);
            }

            @NotNull
            public JavaeePortConfig createConfig(JBossLocalModel data) {
                return new JBossPortConfig(data.getHome(), data.SERVER);
            }
        };
    }
}
