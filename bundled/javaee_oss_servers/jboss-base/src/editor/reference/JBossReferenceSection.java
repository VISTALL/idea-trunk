/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeSection;
import com.fuhrer.idea.javaee.editor.JavaeeSectionInfo;
import com.intellij.util.xml.DomElement;

abstract class JBossReferenceSection<T extends DomElement> implements JavaeeSection<T> {

    @SuppressWarnings({"unchecked"})
    public JavaeeSectionInfo<T>[] createColumnInfos() {
        return new JavaeeSectionInfo[]{createFirstColumn(), createSecondColumn()};
    }

    abstract JavaeeSectionInfo<T> createFirstColumn();

    abstract JavaeeSectionInfo<T> createSecondColumn();
}
