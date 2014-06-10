/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.javaee.server.JavaeePortConfig;
import com.fuhrer.idea.javaee.server.JavaeeServerModel;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

class GlassfishPortConfig extends JavaeePortConfig {

    private static final Factory<GlassfishLocalModel> LOCAL = factory("http-listener-1");

    private static final Factory<GlassfishLocalModel> ADMIN = factory("admin-listener");

    private final File file;

    private final String id;

    private GlassfishPortConfig(String home, String version, String domain, String id) {
        if (GlassfishUtil.isGlassfish3(version)) {
            file = new File(new File(new File(home, "glassfish/domains"), domain), "config/domain.xml");
        } else {
            file = new File(new File(new File(home, "domains"), domain), "config/domain.xml");
        }
        this.id = id;
    }

    @Override
    protected long getStamp(JavaeeServerModel data) {
        return getStamp(file);
    }

    @Override
    protected int getPort(JavaeeServerModel model) {
        if (file.exists()) {
            try {
                Element root = JDOMUtil.loadDocument(file).getRootElement();
                @NonNls Element cfg = getChild(root.getChild("configs"), "config", "name", "server-config");
                if (cfg != null) {
                    @NonNls Element listener = getChild(cfg.getChild("http-service"), "http-listener", "id", id);
                    if (listener == null) {
                        @NonNls Element parent = cfg.getChild("network-config").getChild("network-listeners");
                        listener = getChild(parent, "network-listener", "protocol", id);
                    }
                    if (listener != null) {
                        return Integer.parseInt(listener.getAttributeValue("port"));
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return INVALID_PORT;
    }

    static int getLocal(GlassfishLocalModel model) {
        return get(LOCAL, model, model.getDefaultPort());
    }

    static int getAdmin(GlassfishLocalModel model) {
        return get(ADMIN, model, INVALID_PORT);
    }

    private static Factory<GlassfishLocalModel> factory(@NonNls final String id) {
        return new Factory<GlassfishLocalModel>() {
            @NotNull
            public Key createKey(GlassfishLocalModel data) {
                return new Key(data.getHome(), data.DOMAIN, id);
            }

            @NotNull
            public JavaeePortConfig createConfig(GlassfishLocalModel data) {
                return new GlassfishPortConfig(data.getHome(), data.getVersion(), data.DOMAIN, id);
            }
        };
    }
}
