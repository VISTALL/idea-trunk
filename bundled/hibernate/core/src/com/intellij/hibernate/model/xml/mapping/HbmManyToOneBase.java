/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

// Generated on Fri Nov 17 19:09:30 MSK 2006
// DTD/Schema  :    hibernate-mapping-3.0.dtd

package com.intellij.hibernate.model.xml.mapping;

import com.intellij.hibernate.model.converters.LazyTypeConverter;
import com.intellij.hibernate.model.enums.LazyType;
import com.intellij.javaee.model.xml.CommonDomModelElement;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HbmManyToOneBase extends CommonDomModelElement, HbmRelationAttributeBase.AnyToOneBase,
                                         HbmColumnsHolderBase {

	@NotNull
        @Convert(LazyTypeConverter.class)
        GenericAttributeValue<LazyType> getLazy();

	@NotNull
	GenericAttributeValue<String> getForeignKey();

	@NotNull
	List<HbmMeta> getMetas();
	HbmMeta addMeta();

}