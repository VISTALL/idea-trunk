package com.intellij.hibernate.model;

import org.jetbrains.annotations.NonNls;
import com.intellij.hibernate.HibernateMessages;

public interface HibernateConstants {
  @NonNls String HIBERNATE_INSPECTIONS_GROUP = HibernateMessages.message("inspection.group.name.hibernate.issues");

  @NonNls String CFG_XML_ROOT_TAG = "hibernate-configuration";
  @NonNls String HBM_XML_ROOT_TAG = "hibernate-mapping";

  @NonNls String TEMPLATE_HIBERNATE_CONFIGURATION_3_0 = "hibernate-configuration-3.0.xml";
  @NonNls String TEMPLATE_HIBERNATE_MAPPING_3_0 = "hibernate-mapping-3.0.xml";

  @NonNls String HIBERNATE_CONFIGURATION_3_0 = "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd";
  @NonNls String HIBERNATE_MAPPING_3_0 = "http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd";

  String SESSION_CLASS = "org.hibernate.Session";
  String QUERY_CLASS = "org.hibernate.Query";

  String USER_TYPE_CLASS = "org.hibernate.usertype.UserType";
  String COMPOSITE_USER_TYPE_CLASS = "org.hibernate.usertype.CompositeUserType";

  String GENERATOR_BASE_CLASS = "org.hibernate.id.IdentifierGenerator";
  String GEN_ASSIGNED = "org.hibernate.id.Assigned";
  String GEN_INCREMENT = "org.hibernate.id.IncrementGenerator";
  String GEN_IDENTITY = "org.hibernate.id.IdentityGenerator";
  String GEN_SEQUENCE = "org.hibernate.id.SequenceGenerator";
  String GEN_HILO = "org.hibernate.id.TableHiLoGenerator";
  String GEN_SEQHILO = "org.hibernate.id.SequenceHiLoGenerator";
  String GEN_UUID = "org.hibernate.id.UUIDHexGenerator";
  String GEN_GUID = "org.hibernate.id.GUIDGenerator";
  String GEN_SELECT = "org.hibernate.id.SelectGenerator";
  String GEN_FOREIGN = "org.hibernate.id.ForeignGenerator";
  String GEN_SEQUENCE_IDENTITY = "org.hibernate.id.SequenceIdentityGenerator";

  String COLLECTION_OF_ELEMENTS_ANNO = "org.hibernate.annotations.CollectionOfElements";

  String TYPE_ANNO = "org.hibernate.annotations.Type";
  String TYPE_PARAM = "type";
}
