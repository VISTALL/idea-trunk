package org.jetbrains.android.compiler;

import com.intellij.CommonBundle;
import com.intellij.compiler.impl.CompileContextImpl;
import com.intellij.compiler.impl.ModuleCompileScope;
import com.intellij.compiler.progress.CompilerTask;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.compiler.GeneratingCompiler;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yole
 */
public class AndroidCompileUtil {
  private static final Pattern ourMessagePattern = Pattern.compile("(.+):(\\d+):.+");

  private AndroidCompileUtil() {
  }

  static void addMessages(CompileContext context, Map<CompilerMessageCategory, List<String>> messages) {
    for (CompilerMessageCategory category : messages.keySet()) {
      List<String> messageList = messages.get(category);
      for (String message : messageList) {
        String url = null;
        int line = -1;
        Matcher matcher = ourMessagePattern.matcher(message);
        if (matcher.matches()) {
          String fileName = matcher.group(1);
          if (new File(fileName).exists()) {
            url = "file://" + fileName;
            line = Integer.parseInt(matcher.group(2));
          }
        }
        context.addMessage(category, message, url, line, -1);
      }
    }
  }

  public static void createSourceRootIfNotExist(@NotNull final String path, @NotNull final Module module) {
    final ModuleRootManager manager = ModuleRootManager.getInstance(module);
    final File rootFile = new File(path);
    final boolean created;
    if (!rootFile.exists()) {
      if (!rootFile.mkdir()) return;
      created = true;
    }
    else {
      created = false;
    }
    final Project project = module.getProject();
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        if (project.isDisposed()) return;
        final VirtualFile root;
        if (created) {
          root = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(rootFile);
        }
        else {
          root = LocalFileSystem.getInstance().findFileByIoFile(rootFile);
        }
        if (root != null) {
          for (VirtualFile existingRoot : manager.getSourceRoots()) {
            if (existingRoot == root) return;
          }
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              addSourceRoot(manager, root);
            }
          });
        }
      }
    }, project.getDisposed());
  }

  public static void addSourceRoot(final ModuleRootManager manager, final VirtualFile root) {
    final ModifiableRootModel model = manager.getModifiableModel();
    ContentEntry contentEntry = model.addContentEntry(root);
    contentEntry.addSourceFolder(root, false);
    model.commit();
  }

  public static void generate(Module module, GeneratingCompiler compiler) {
    Project project = module.getProject();
    CompilerTask task = new CompilerTask(project, true, "", true);
    CompileScope scope = new ModuleCompileScope(module, true);
    final CompileContext context = new CompileContextImpl(project, task, scope, null, false, false);
    GeneratingCompiler.GenerationItem[] items = compiler.getGenerationItems(context);
    compiler.generate(context, items, null);
  }

  public static void removeDuplicatingClass(final Module module, @NotNull final String packageName, @NotNull final File classFile) {
    final Project project = module.getProject();
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    String className = FileUtil.getNameWithoutExtension(classFile);
    final String interfaceQualifiedName = packageName + '.' + className;
    PsiClass c = ApplicationManager.getApplication().runReadAction(new Computable<PsiClass>() {
      @Nullable
      public PsiClass compute() {
        return facade.findClass(interfaceQualifiedName, GlobalSearchScope.moduleWithDependenciesScope(module));
      }
    });
    if (c == null) return;
    PsiFile psiFile = c.getContainingFile();
    if (className.equals(FileUtil.getNameWithoutExtension(psiFile.getName()))) {
      VirtualFile virtualFile = psiFile.getVirtualFile();
      if (virtualFile != null) {
        final String path = virtualFile.getPath();
        File f = new File(path);
        if (!f.equals(classFile) && f.exists()) {
          if (f.delete()) {
            virtualFile.refresh(true, false);
          }
          else {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                Messages.showErrorDialog(project, "Can't delete file " + path, CommonBundle.getErrorTitle());
              }
            }, project.getDisposed());
          }
        }
      }
    }
  }
}
