package org.jetbrains.idea.tomcat;

import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.openapi.fileTypes.StdFileTypes;

/**
 * @author nik
 */
public class TomcatFileTemplateDescriptorFactory implements FileTemplateGroupDescriptorFactory {
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor root = new FileTemplateGroupDescriptor(TomcatBundle.message("templates.group.title"), TomcatManager.ICON_TOMCAT);
    root.addTemplate(new FileTemplateDescriptor(TomcatConstants.CONTEXT_XML_TEMPLATE_FILE_NAME, StdFileTypes.XML.getIcon()));
    return root;
  }
}
