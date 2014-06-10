package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.UI.facet.CfmlFacet;
import com.intellij.facet.FacetManager;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Lera Nikolaenko
 * Date: 24.02.2009
 */
public class CfmlFunctionCallExpression extends CfmlCompositeElement implements CfmlExpression, ICfmlFunctionCall {
  public static class CfmlJavaLoaderClassType extends PsiType {
    private GlobalSearchScope mySearchScope;
    private Project myProject;

    private class JarFileScope extends GlobalSearchScope {
      private final VirtualFile myVirtualFile;
      private final Module myModule;

      private JarFileScope(@NotNull VirtualFile file) {
        super();
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
        */
        /*
        if (myVirtualFile.isDirectory()) {
          for (VirtualFile childFile : myVirtualFile.getChildren()) {
            if (Comparing.equal(childFile, file)) {
              return true;
            }
          }
          return false;
        }
        return Comparing.equal(myVirtualFile, file);
        */
        return VfsUtil.isAncestor(myVirtualFile, file, true);
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

    public CfmlJavaLoaderClassType(PsiComment comment, Project project) {
      super(PsiAnnotation.EMPTY_ARRAY);
      final String text = comment.getText();

      myProject = project;
      Pattern LOADER_DECL_PATTERN_TEMP = Pattern.compile(new StringBuilder().append("<!---[^@]*").append("@javaloader[ \\n\\r\\t\\f]*")
        .append("name=\"([^\"]+)\"[ \\n\\r\\t\\f]*(jarPath=\"([^\"]+)\"[ \\n\\r\\t\\f]*)*.*").toString());
      Matcher javaLoaderMatcher = LOADER_DECL_PATTERN_TEMP.matcher(text);
      List<String> myJarPaths = new LinkedList<String>();
      mySearchScope = GlobalSearchScope.EMPTY_SCOPE;

      if (javaLoaderMatcher.matches()) {
        Collection<String> collection = CfmlPsiUtil.findBetween(text, "jarPath=\"", "\"");
        for (String str : collection) {
          if (str.startsWith("/")) {
            VirtualFile file = comment.getContainingFile().getViewProvider().getVirtualFile();
            if (file == null) {
              continue;
            }
            Module module = ModuleUtil.findModuleForPsiElement(comment);
            if (module == null) {
              continue;
            }
            Collection<CfmlFacet> moduleFacet = FacetManager.getInstance(module).getFacetsByType(CfmlFacet.ID);
            if (moduleFacet.size() < 1) {
              continue;
            }
            CfmlFacet facet = moduleFacet.iterator().next();
            String serverDirectory = facet.getConfiguration().getMyServerRootPath();
            str = serverDirectory + str;
          }
          VirtualFile file = JarFileSystem.getInstance().findFileByPath(str + JarFileSystem.JAR_SEPARATOR);
          if (file != null) {
            mySearchScope = mySearchScope.uniteWith(new JarFileScope(file));
          }
        }
      }
    }

    public GlobalSearchScope getSearchScope() {
      return mySearchScope;
    }

    public String getPresentableText() {
      return "JavaLoader";
    }

    public String getCanonicalText() {
      return "JavaLoader";
    }

    public String getInternalCanonicalText() {
      return "JavaLoader";
    }

    public boolean isValid() {
      return true;
    }

    public boolean equalsToText(@NonNls String text) {
      return false;
    }

    public <A> A accept(PsiTypeVisitor<A> visitor) {
      return visitor.visitType(this);
    }

    public GlobalSearchScope getResolveScope() {
      return null;
    }

    @NotNull
    public PsiType[] getSuperTypes() {
      return new PsiType[0];
    }
  }

  public CfmlFunctionCallExpression(final ASTNode node) {
    super(node);
  }

  public boolean isCreateFromJavaLoader() {
    PsiElement firstChild = findChildByType(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);
    String create = firstChild.getLastChild().getText();
    PsiElement secondChild = firstChild.getFirstChild();

    if (!(create.toLowerCase().equals("create") && secondChild instanceof CfmlReferenceExpression)) {
      return false;
    }
    PsiType type = ((CfmlReferenceExpression)secondChild).getPsiType();
    if (type == null) {
      return false;
    }
    return type.getCanonicalText().toLowerCase().equals("javaloader");
  }

  @Nullable
  public PsiType getPsiType() {
    // getting function name
    String functionName = getFunctionName();

    // createObject specific code
    if ("createobject".equals(functionName.toLowerCase())) {
      CfmlExpression[] argumentsList = findArgumentList().getArguments();
      if (argumentsList.length < 2) {
        return null;
      }
      if ("\"java\"".equals(argumentsList[0].getText().toLowerCase())) {
        String className = argumentsList[1].getText();
        className = className.substring(1, className.length() - 1);
        GlobalSearchScope ss = getResolveScope();
        return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(className, ss);
      }
    }
    else {
      /*
PsiElement firstChild = findChildByType(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);
String create = firstChild.getLastChild().getText();
PsiElement secondChild = firstChild.getFirstChild();

if (create.toLowerCase().equals("create") && secondChild instanceof CfmlReferenceExpression &&
      ((CfmlReferenceExpression)secondChild).getPsiType().getCanonicalText().equals("javaloader"))*/
      if (isCreateFromJavaLoader()) {
        CfmlExpression[] argumentsList = findArgumentList().getArguments();
        if (argumentsList.length == 0) {
          return null;
        }
        String className = argumentsList[0].getText();
        className = className.substring(1, className.length() - 1);
        GlobalSearchScope ss = getResolveScope();
        return JavaPsiFacade.getInstance(getProject()).getElementFactory().createTypeByFQClassName(className, ss);
      }
    }
    return null;
  }

  @NotNull
  public CfmlExpression[] getArguments() {
    CfmlArgumentList argumentListEl = findChildByClass(CfmlArgumentList.class);
    if (argumentListEl == null) {
      return new CfmlExpression[0];
    }
    return argumentListEl.getArguments();
  }

  public String getFunctionName() {
    String functionName = "";
    if (getReferenceExpression().getText() != null) {
      functionName = getReferenceExpression().getText();
    }
    return functionName;
  }

  @NotNull
  public CfmlReferenceExpression getReferenceExpression() {
    return findNotNullChildByClass(CfmlReferenceExpression.class);
  }

  public CfmlArgumentList findArgumentList() {
    return findChildByClass(CfmlArgumentList.class);
  }

  public PsiType[] getArgumentTypes() {
    CfmlArgumentList argumentsList = findArgumentList();
    if (argumentsList == null) {
      return PsiType.EMPTY_ARRAY;
    }
    CfmlExpression[] args = argumentsList.getArguments();
    return ContainerUtil.map2Array(args, PsiType.class, new NullableFunction<CfmlExpression, PsiType>() {
      public PsiType fun(final CfmlExpression expression) {
        return expression.getPsiType();
      }
    });
  }
}
