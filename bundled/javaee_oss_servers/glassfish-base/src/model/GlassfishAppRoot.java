/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.model;

import java.util.List;

@SuppressWarnings({"InterfaceNeverImplemented"})
public interface GlassfishAppRoot extends GlassfishSecurityRoleHolder {

    List<GlassfishWebModule> getWebs();

    GlassfishWebModule addWeb();
}
