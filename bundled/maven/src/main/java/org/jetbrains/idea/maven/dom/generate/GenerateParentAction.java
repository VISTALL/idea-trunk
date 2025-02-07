package org.jetbrains.idea.maven.dom.generate;

import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.xml.ui.actions.generate.GenerateDomElementAction;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomParent;
import org.jetbrains.idea.maven.navigator.SelectMavenProjectDialog;
import org.jetbrains.idea.maven.project.MavenProject;

public class GenerateParentAction extends GenerateDomElementAction {
  public GenerateParentAction() {
    super(new MavenGenerateProvider<MavenDomParent>("Generate Parent", MavenDomParent.class) {
      protected MavenDomParent doGenerate(final MavenDomProjectModel mavenModel, Editor editor) {
        SelectMavenProjectDialog d = new SelectMavenProjectDialog(editor.getProject(), null);
        d.show();
        if (!d.isOK()) return null;
        final MavenProject parentProject = d.getResult();
        if (parentProject == null) return null;

        return new WriteCommandAction<MavenDomParent>(editor.getProject(), getDescription()) {
          protected void run(Result result) throws Throwable {
            result.setResult(MavenDomUtil.updateMavenParent(mavenModel, parentProject));
          }
        }.execute().getResultObject();
      }

      @Override
      protected boolean isAvailableForModel(MavenDomProjectModel mavenModel) {
        return !DomUtil.hasXml(mavenModel.getMavenParent());
      }
    });
  }

  @Override
  protected boolean startInWriteAction() {
    return false;
  }
}
