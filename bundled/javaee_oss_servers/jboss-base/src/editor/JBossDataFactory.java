/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.openapi.util.Factory;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import java.lang.reflect.Field;
import java.sql.Types;
import java.util.*;

public class JBossDataFactory {

    @NonNls
    private static final String[] MAPPINGS = createMappings(
            "FirstSQL/J",
            "Ingres",
            "McKoi",
            "Firebird",
            "InterBase",
            "DB2",
            "Derby",
            "Oracle9i",
            "Oracle8",
            "Oracle7",
            "Sybase",
            "PostgreSQL",
            "PostgreSQL 8.0",
            "PostgreSQL 7.2",
            "Hypersonic SQL",
            "PointBase",
            "SOLID",
            "mySQL",
            "MS SQLSERVER",
            "MS SQLSERVER2000",
            "DB2/400",
            "SapDB",
            "Cloudscape",
            "InformixDB",
            "Mimer SQL"
    );

    private static final String[] TYPES = createTypes();

    private JBossDataFactory() {
    }

    public static String[] getMappings() {
        return MAPPINGS;
    }

    public static String[] getTypes() {
        return TYPES;
    }

    public static String[] getLoadGroups(JBossCmpBean cmp) {
        List<String> list = new ArrayList<String>();
        list.add("*");
        for (JBossLoadGroup group : cmp.getLoadGroups().getLoadGroups()) {
            list.add(group.getLoadGroupName().getValue());
        }
        list.add(null);
        return list.toArray(new String[list.size()]);
    }

    public static Factory<List<Pair<String, Icon>>> getFactory(final String[] values) {
        return new Factory<List<Pair<String, Icon>>>() {
            public List<Pair<String, Icon>> create() {
                return ContainerUtil.map2List(values, new Function<String, Pair<String, Icon>>() {
                    public Pair<String, Icon> fun(String str) {
                        return Pair.create(str, null);
                    }
                });
            }
        };
    }

    private static String[] createMappings(String... mappings) {
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(mappings));
        Collections.sort(list, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        list.add(null);
        return list.toArray(new String[list.size()]);
    }

    private static String[] createTypes() {
        List<String> list = new ArrayList<String>();
        for (Field field : Types.class.getFields()) {
            list.add(field.getName());
        }
        list.add(null);
        return list.toArray(new String[list.size()]);
    }
}
