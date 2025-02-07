// Generated on Mon Mar 17 18:02:09 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package org.jetbrains.idea.maven.dom.model;

import com.intellij.psi.PsiFile;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomElement;
import org.jetbrains.idea.maven.dom.converters.MavenParentRelativePathConverter;

/**
 * http://maven.apache.org/POM/4.0.0:Parent interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Parent documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface MavenDomParent extends MavenDomElement, MavenDomArtifactCoordinates {
  /**
   * Returns the value of the relativePath child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:relativePath documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the relativePath child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  @Convert(MavenParentRelativePathConverter.class)
  GenericDomValue<PsiFile> getRelativePath();
}
