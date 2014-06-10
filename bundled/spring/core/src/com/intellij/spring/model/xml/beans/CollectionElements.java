// Generated on Thu Nov 09 17:15:14 MSK 2006
// DTD/Schema  :    http://www.springframework.org/schema/beans

package com.intellij.spring.model.xml.beans;

import com.intellij.spring.model.values.ListOrSetValueConverter;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.CustomChildren;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * http://www.springframework.org/schema/beans:collectionElements model group interface.
 */
public interface CollectionElements extends DomElement {

  @NotNull
  List<SpringBean> getBeans();

  SpringBean addBean();

  @CustomChildren
  List<CustomBeanWrapper> getCustomBeans();


  @NotNull
  List<SpringRef> getRefs();

  SpringRef addRef();


  @NotNull
  List<Idref> getIdrefs();

  Idref addIdref();


  /**
   * Returns the list of value children.
   * <pre>
   * <h3>Element http://www.springframework.org/schema/beans:value documentation</h3>
   * 	Contains a string representation of a property value.
   * 	The property may be a string, or may be converted to the required
   * 	type using the JavaBeans PropertyEditor machinery. This makes it
   * 	possible for application developers to write custom PropertyEditor
   * 	implementations that can convert strings to arbitrary target objects.
   * 	Note that this is recommended for simple objects only. Configure
   * 	more complex objects by populating JavaBean properties with
   * 	references to other beans.
   * <p/>
   * </pre>
   *
   * @return the list of value children.
   */
  @Convert(ListOrSetValueConverter.class)
  @NotNull
  List<SpringValue> getValues();

  SpringValue addValue();


  @NotNull
  SpringNull getNull();


  @NotNull
  List<ListOrSet> getLists();

  ListOrSet addList();


  @NotNull
  List<ListOrSet> getSets();

  ListOrSet addSet();


  @NotNull
  List<SpringMap> getMaps();

  SpringMap addMap();


  @NotNull
  @SubTagList("props")
  List<Props> getPropses();

  @SubTagList("props")
  Props addProps();


}
