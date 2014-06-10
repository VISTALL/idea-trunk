/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.server;

import com.intellij.javaee.appServerIntegrations.DefaultPersistentData;
import org.jetbrains.annotations.NonNls;

public class JavaeePersistentData extends DefaultPersistentData {

    @NonNls
    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String HOME = "";

    @NonNls
    @SuppressWarnings({"PublicField", "InstanceVariableNamingConvention", "NonConstantFieldWithUpperCaseName"})
    public String VERSION = "";
}
