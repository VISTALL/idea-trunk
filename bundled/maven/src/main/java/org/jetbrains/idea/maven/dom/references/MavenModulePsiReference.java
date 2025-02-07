package org.jetbrains.idea.maven.dom.references;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.notification.Notifications;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PathUtil;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomBundle;
import org.jetbrains.idea.maven.dom.MavenDomUtil;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.project.MavenId;
import org.jetbrains.idea.maven.utils.MavenConstants;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MavenModulePsiReference extends MavenPsiReference implements LocalQuickFixProvider {
  public MavenModulePsiReference(PsiElement element, String text, TextRange range) {
    super(element, text, range);
  }

  public PsiElement resolve() {
    VirtualFile baseDir = myPsiFile.getVirtualFile().getParent();
    String relPath = FileUtil.toSystemIndependentName(myText + "/" + MavenConstants.POM_XML);
    VirtualFile file = baseDir.findFileByRelativePath(relPath);

    if (file == null) return null;

    return getPsiFile(file);
  }

  public Object[] getVariants() {
    List<DomFileElement<MavenDomProjectModel>> files = MavenDomUtil.collectProjectModels(getProject());

    List<Object> result = new ArrayList<Object>();

    for (DomFileElement<MavenDomProjectModel> eachDomFile : files) {
      VirtualFile eachVFile = eachDomFile.getOriginalFile().getVirtualFile();
      if (eachVFile == myVirtualFile) continue;

      PsiFile psiFile = eachDomFile.getFile();
      String modulePath = calcRelativeModulePath(myVirtualFile, eachVFile);

      result.add(LookupElementBuilder.create(psiFile, modulePath).setPresentableText(modulePath));
    }

    return result.toArray();
  }

  public static String calcRelativeModulePath(VirtualFile parentPom, VirtualFile modulePom) {
    String result = MavenDomUtil.calcRelativePath(parentPom.getParent(), modulePom);
    int to = result.length() - ("/" + MavenConstants.POM_XML).length();
    if (to < 0) {
      // todo IDEADEV-35440
      throw new RuntimeException("Filed to calculate relative path for:" +
                                 "\nparentPom: " + parentPom + "(valid: " + parentPom.isValid() + ")" +
                                 "\nmodulePom: " + modulePom + "(valid: " + modulePom.isValid() + ")" +
                                 "\nequals:" + parentPom.equals(modulePom));
    }
    return result.substring(0, to);
  }

  private PsiFile getPsiFile(VirtualFile file) {
    return PsiManager.getInstance(getProject()).findFile(file);
  }

  private Project getProject() {
    return myPsiFile.getProject();
  }

  public LocalQuickFix[] getQuickFixes() {
    if (myText.length() == 0 || resolve() != null) return LocalQuickFix.EMPTY_ARRAY;
    return new LocalQuickFix[]{new CreateModuleFix(true), new CreateModuleFix(false)};
  }

  private class CreateModuleFix implements LocalQuickFix {
    private final boolean myWithParent;

    private CreateModuleFix(boolean withParent) {
      myWithParent = withParent;
    }

    @NotNull
    public String getName() {
      return myWithParent ? MavenDomBundle.message("fix.create.module.with.parent") : MavenDomBundle.message("fix.create.module");
    }

    @NotNull
    public String getFamilyName() {
      return MavenDomBundle.message("inspection.group");
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor d) {
      try {
        VirtualFile modulePom = createModulePom();
        MavenId id = MavenDomUtil.describe(myPsiFile);

        String groupId = id.getGroupId() == null ? "groupId" : id.getGroupId();
        String artifactId = modulePom.getParent().getName();
        String version = id.getVersion() == null ? "version" : id.getVersion();
        MavenUtil.runMavenProjectWithParentFileTemplate(project,
                                                        modulePom,
                                                        new MavenId(groupId, artifactId, version),
                                                        myWithParent ? id : null);
      }
      catch (IOException e) {
        Notifications.Bus.notify(new Notification("Maven", "Cannot create a module", e.getMessage(), NotificationType.ERROR), project);
      }
    }

    private VirtualFile createModulePom() throws IOException {
      VirtualFile baseDir = myVirtualFile.getParent();
      String modulePath = PathUtil.getCanonicalPath(baseDir.getPath() + "/" + myText);
      VirtualFile moduleDir = VfsUtil.createDirectories(modulePath);
      return moduleDir.createChildData(this, MavenConstants.POM_XML);
    }
  }
}
