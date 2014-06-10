package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author vnikolaenko
 */
public class CfmlStringLiteralExpressionType extends CfmlLiteralExpressionType {
    public CfmlStringLiteralExpressionType() {
        super("StringLiteral", CommonClassNames.JAVA_LANG_STRING);
    }

    @Override
    public PsiElement createPsiElement(ASTNode node) {
        return new CfmlStringLiteralExpression(node);
    }

    class CfmlStringLiteralExpression extends CfmlCompositeElement implements CfmlExpression {
        public CfmlStringLiteralExpression(@NotNull final ASTNode node) {
            super(node);
        }

        public PsiType getPsiType() {
            return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, getResolveScope());
        }

      private class FileScope extends GlobalSearchScope {
        private final VirtualFile myVirtualFile;
        private final Module myModule;
        private final Project myProject;

        private FileScope(@NotNull VirtualFile file, Project project) {
          super();
          myProject = project;
          myVirtualFile = file;
          ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
          myModule = myVirtualFile != null ? fileIndex.getModuleForFile(myVirtualFile) : null;
        }

        public boolean contains(VirtualFile file) {
          /*
          if (myVirtualFile.isDirectory()) {
            for (VirtualFile childFile : myVirtualFile.getChildren()) {
              if (Comparing.equal(childFile, file)) {
                return true;
              }
            }
            return false;
          }
          *//*
          if (myVirtualFile.isDirectory()) {
            for (VirtualFile childFile : myVirtualFile.getChildren()) {
              if (Comparing.equal(childFile, file)) {
                return true;
              }
            }
            return false;
          }
          */
          return VfsUtil.isAncestor(myVirtualFile, file, true);
          // return Comparing.equal(myVirtualFile, file);
        }

        public int compare(VirtualFile file1, VirtualFile file2) {
          return 0;
        }

        public boolean isSearchInModuleContent(@NotNull Module aModule) {
          return aModule == myModule;
        }

        public boolean isSearchInLibraries() {
          return myModule == null;
        }
      }

        @NotNull
        @Override
        public PsiReference[] getReferences() {
            CfmlFunctionCallExpression functionCallEl =
              PsiTreeUtil.getParentOfType(this, CfmlFunctionCallExpression.class);
            if (functionCallEl != null && functionCallEl.getFunctionName().toLowerCase().equals("createobject")) {
                CfmlExpression[] expressions = functionCallEl.getArguments();
                if (expressions.length == 2 && expressions[1] == this) {
                    PsiElement stringTextElement = findChildByType(CfscriptTokenTypes.STRING_TEXT);
                    if (stringTextElement != null) {
                        String possibleJavaClassName = stringTextElement.getText();
                        final JavaClassReferenceProvider provider = new JavaClassReferenceProvider(getProject());
                        return provider.getReferencesByString(possibleJavaClassName, stringTextElement, 0);
                    }
                }
            } else if (functionCallEl.isCreateFromJavaLoader()) {
                CfmlExpression[] expressions = functionCallEl.getArguments();
                if (expressions.length > 0 && expressions[0] == this) {
                    PsiElement stringTextElement = findChildByType(CfscriptTokenTypes.STRING_TEXT);
                    if (stringTextElement != null) {
                      // getting javaloader type
                      PsiElement secondChild = functionCallEl.getFirstChild().getFirstChild();
                      if (!(secondChild instanceof CfmlReferenceExpression)) {
                        return super.getReferences();
                      }
                      PsiType type = ((CfmlReferenceExpression)secondChild).getPsiType();
                      if (!(type instanceof CfmlFunctionCallExpression.CfmlJavaLoaderClassType)) {
                        return super.getReferences();
                      }
                      GlobalSearchScope ss = ((CfmlFunctionCallExpression.CfmlJavaLoaderClassType)type).getSearchScope();

                      String possibleJavaClassName = stringTextElement.getText();
                        final VirtualFile fileByPath =
                          JarFileSystem.getInstance().findFileByPath("C:/Program Files/Java/jdk1.5.0_14/jre/lib/deploy.jar" +
                            JarFileSystem.JAR_SEPARATOR);
                        // final VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath();
                        // final PsiDirectory directory = fileByPath == null ? null : getManager().findDirectory(fileByPath);
                      /*
                        final JavaClassReferenceProvider provider = new
                          JavaClassReferenceProvider(new FileScope(fileByPath, getProject()), getProject());
                        return provider.getReferencesByString(possibleJavaClassName, stringTextElement, 0);
                        */
                      final JavaClassReferenceProvider provider = new
                        JavaClassReferenceProvider(ss, getProject());
                      return provider.getReferencesByString(possibleJavaClassName, stringTextElement, 0);
                    }
                }
            }

            return super.getReferences();    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
