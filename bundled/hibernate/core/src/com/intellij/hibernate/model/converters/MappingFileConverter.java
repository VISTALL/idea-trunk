package com.intellij.hibernate.model.converters;

import com.intellij.persistence.model.PersistenceMappings;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomFileElement;

/**
 * @author Gregory.Shrago
 */
public class MappingFileConverter extends MappingElementConverterBase {

  protected boolean isFileAccepted(final PsiFile file) {
    if (file instanceof XmlFile) {
      final DomFileElement fileElement = DomManager.getDomManager(file.getProject()).getFileElement((XmlFile)file, DomElement.class);
      if (fileElement != null) {
        return fileElement.getRootElement() instanceof PersistenceMappings;
      }
    }
    return false;
  }

}
