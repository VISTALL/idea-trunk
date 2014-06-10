/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.fuhrer.idea.javaee.util.CachedConfig;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.configurations.RuntimeConfigurationWarning;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class GlassfishDebugConfig extends CachedConfig<GlassfishLocalModel> {

    private static final Pattern PORT = Pattern.compile("^(?:.*,)?address=(\\d+)(?:,.*)?$");

    private static final Pattern SERVER = Pattern.compile("^(?:.*,)?server=([yn])(?:,.*)?$");

    private static final Pattern SUSPEND = Pattern.compile("^(?:.*,)?suspend=([yn])(?:,.*)?$");

    private static final Map<Key, GlassfishDebugConfig> cache = new HashMap<Key, GlassfishDebugConfig>();

    private static final Factory<GlassfishLocalModel, GlassfishDebugConfig> FACTORY = factory();

    private final File file;

    @NonNls
    private String port;

    @NonNls
    private String server;

    @NonNls
    private String suspend;

    private GlassfishDebugConfig(String home, String domain) {
        File tmp = new File(new File(new File(home, "domains"), domain), "config/domain.xml");
        if (!tmp.exists()) {
            tmp = new File(new File(new File(new File(home, "glassfish"), "domains"), domain), "config/domain.xml");
        }
        file = tmp;
    }

    @Override
    protected long getStamp(GlassfishLocalModel data) {
        return getStamp(file);
    }

    @Override
    protected void update(GlassfishLocalModel data) {
        String options = getOptions();
        port = find(PORT, options);
        server = find(SERVER, options);
        suspend = find(SUSPEND, options);
    }

    @Nullable
    private String getOptions() {
        if (file.exists()) {
            try {
                Element root = JDOMUtil.loadDocument(file).getRootElement();
                @NonNls Element cfg = getChild(root.getChild("configs"), "config", "name", "server-config");
                if (cfg != null) {
                    Element config = cfg.getChild("java-config");
                    if (config != null) {
                        return config.getAttributeValue("debug-options");
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    @Nullable
    private String find(Pattern pattern, String options) {
        if (options != null) {
            Matcher matcher = pattern.matcher(options);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    @SuppressWarnings({"ParameterHidesMemberVariable"})
    private void fix(String port) throws JDOMException, IOException {
        Document doc = JDOMUtil.loadDocument(file);
        @NonNls Element cfg = getChild(doc.getRootElement().getChild("configs"), "config", "name", "server-config");
        if (cfg != null) {
            Element config = cfg.getChild("java-config");
            if (config != null) {
                @NonNls String options = config.getAttributeValue("debug-options");
                if (options != null) {
                    options = replace(PORT, options, port);
                    options = replace(SERVER, options, "n");
                    options = replace(SUSPEND, options, "y");
                } else {
                    options = "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=" + port;
                }
                config.setAttribute("debug-options", options);
                config.setAttribute("debug-enabled", "false");
                File tmp = File.createTempFile(file.getName(), null, file.getParentFile());
                JDOMUtil.writeDocument(doc, tmp, "\n");
                file.delete();
                tmp.renameTo(file);
            }
        }
    }

    private String replace(Pattern pattern, @NonNls String options, @NonNls String value) {
        Matcher matcher = pattern.matcher(options);
        if (matcher.matches()) {
            return options.substring(0, matcher.start(1)) + value + options.substring(matcher.end(1));
        } else {
            String str = pattern.pattern();
            String key = str.substring(str.indexOf(")?") + 2, str.indexOf('='));
            return options + ',' + key + '=' + value;
        }
    }

    @Nullable
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    static String get(GlassfishLocalModel model) {
        GlassfishDebugConfig config = get(cache, FACTORY, model);
        return (config != null) ? config.port : null;
    }

    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject"})
    static void check(GlassfishLocalModel model, final String port) throws RuntimeConfigurationException {
        final GlassfishDebugConfig config = get(cache, FACTORY, model);
        if (config != null) {
            if (!Comparing.equal(config.port, port) || !"n".equals(config.server) || !"y".equals(config.suspend)) {
                String message = GlassfishBundle.getText("GlassfishRemoteModel.error.debug", port);
                RuntimeConfigurationException warning = new RuntimeConfigurationWarning(message);
                if (config.file.canWrite()) {
                    warning.setQuickFix(new Runnable() {
                        public void run() {
                            try {
                                config.fix(port);
                            } catch (Exception ignore) {
                            }
                        }
                    });
                }
                throw warning;
            }
        }
    }

    private static Factory<GlassfishLocalModel, GlassfishDebugConfig> factory() {
        return new Factory<GlassfishLocalModel, GlassfishDebugConfig>() {
            @NotNull
            public Key createKey(GlassfishLocalModel data) {
                return new Key(data.getHome(), data.DOMAIN);
            }

            @NotNull
            public GlassfishDebugConfig createConfig(GlassfishLocalModel data) {
                return new GlassfishDebugConfig(data.getHome(), data.DOMAIN);
            }
        };
    }
}
