/*
 * Created on Jun 8, 2005
 */
package com.intellij.eclipse.export;

/**
 * @author Sergey.Grigorchuk
 */
public interface IdeaProjectFileConstants {

  public static final String IDEA_PROJECT_FILE_EXT = "ipr";

  public static final String IDEA_MODULE_FILE_EXT = "iml";

  public static final String IDEA_WORKSPACE_FILE_EXT = "iws";

  public static final String JAR_EXT = "jar";

  public static final String ZIP_EXT = "zip";

  public static final String WAR_EXT = "war";

  public static final String JAR_POSTFIX = "!/";

  public static final String JAR_URL_PREFIX = "jar";

  public static final String JAR_FULL_URL_PREFIX = JAR_URL_PREFIX + "://";

  public static final String FILE_URL_PREFIX = "file";

  public static final String FILE_FULL_URL_PREFIX = FILE_URL_PREFIX + "://";

  public static final String PROJECT_ROOT_TAG = "project";

  public static final String MODULE_ROOT_TAG = "module";

  public static final String MODULE_DIR_URL_VAR = "$MODULE_DIR$";

  public static final String PROJECT_VERSION_PROPERTY = "version";

  public static final String PROJECT_DIR_URL_VAR = "$PROJECT_DIR$";

  public static final String PROJECT_RELATIVE_PATH_PROPERTY = "relativePaths";

  public static final String MODULE_VERSION_PROPERTY = "version";

  public static final String MODULE_RELATIVE_PATH_PROPERTY = "relativePaths";

  public static final String MODULE_TYPE_PROPERTY = "type";

  public static final String COMPONENT_TAG = "component";

  public static final String COMPONENT_NAME_PROPERTY = "name";

  public static final String DEPLOYMENT_DESCRIPTOR_TAG = "deploymentDescriptor";

  public static final String DEPLOYMENT_DESCRIPTOR_NAME_PROPERTY = "name";

  public static final String DEPLOYMENT_DESCRIPTOR_URL_PROPERTY = "url";

  public static final String DEPLOYMENT_DESCRIPTOR_VERSION_PROPERTY = "version";

  public static final String DEFAULT_ANT_TAG = "defaultAnt";

  public static final String BUNDLED_ANT_PROPERTY = "bundledAnt";

  public static final String MODULES_TAG = "modules";

  public static final String MODULE_TAG = "module";

  public static final String MODULE_FILE_URL_PROPERTY = "fileurl";

  public static final String MODULE_FILE_PATH_PROPERTY = "filepath";

  public static final String LIBRARY_TAG = "library";

  public static final String LIBRARY_NAME_PROPERTY = "name";

  public static final String CLASSES_TAG = "CLASSES";

  public static final String CLASSES_ROOT_TAG = "root";

  public static final String CLASSES_ROOT_URL_PROPERTY = "url";

  public static final String OUTPUT_TAG = "output";

  public static final String CONTENT_TAG = "content";

  public static final String SOURCE_FOLDER_TAG = "sourceFolder";

  public static final String OUTPUT_FOLDER_TAG = "output";

  public static final String IS_TEST_SOURCE_PROPERTY = "isTestSource";

  public static final String MODULE_URL_PROPERTY = "url";

  public static final String ORDER_ENTRY_TAG = "orderEntry";

  public static final String ORDER_ENTRY_TYPE_PROPERTY = "type";

  public static final String ORDER_ENTRY_PROPERTIES_TAG = "orderEntryProperties";

  public static final String ORDER_ENTRY_NAME_PROPERTY = "name";

  public static final String ORDER_ENTRY_LEVEL_PROPERTY = "level";

  public static final String ORDER_ENTRY_MODULE_NAME_PROPERTY = "module-name";

  public static final String ORDER_ENTRY_JDKNAME_PROPERTY = "jdkName";

  public static final String ORDER_ENTRY_JDKTYPE_PROPERTY = "jdkType";

  public static final String CODE_STYLE_OPTION_TAG = "option";

  public static final String CODE_STYLE_VALUE_TAG = "value";

  public static final String CODE_STYLE_OPTION_NAME_PROPERTY = "name";

  public static final String CODE_STYLE_OPTION_VALUE_PROPERTY = "value";

  public static final String USE_PER_PROJECT_SETTINGS_TAG = "USE_PER_PROJECT_SETTINGS";

  public static final String CONTAINER_ELEMENT_TAG = "containerElement";

  public static final String CONTAINER_ELEMENT_TYPE_PROPERTY = "type";

  public static final String CONTAINER_ELEMENT_NAME_PROPERTY = "name";

  public static final String CONTAINER_ELEMENT_METHOD_VALUE = "method";

  public static final String CONTAINER_ELEMENT_URI_VALUE = "URI";

  public static final String CONTAINER_ELEMENT_CONTEXT_ROOT_VALUE = "contextRoot";

  public static final String ATTRIBUTE_TAG = "attribute";

  public static final String ATTRIBUTE_NAME_PROPERTY = "name";

  public static final String ATTRIBUTE_VALUE_PROPERTY = "value";
}