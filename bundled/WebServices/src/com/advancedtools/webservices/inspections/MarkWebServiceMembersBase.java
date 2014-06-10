package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import static com.advancedtools.webservices.index.FileBasedWSIndex.getKey;
import com.advancedtools.webservices.index.WSIndexEntry;
import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.PsiUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * @author Maxim
 */
abstract class MarkWebServiceMembersBase extends BaseWebServicesInspection {
  protected static final Icon interfaceIcon = IconLoader.getIcon("WebServicesClass.png");

  private static @Nullable Editor findEditor(@NotNull PsiElement psiMember) {
    if (!psiMember.isValid()) return null;
    final PsiFile containingFile = psiMember.getContainingFile();
    final Project project = containingFile.getProject();
    if (project.isDisposed()) return null;

    FileEditor selectedEditor = FileEditorManager.getInstance(project).getSelectedEditor(containingFile.getVirtualFile());
    return selectedEditor instanceof TextEditor ? ((TextEditor)selectedEditor).getEditor():null;
  }

  protected void removeInvalidHighlighters(final PsiElement element) {
    if (ApplicationManager.getApplication().isHeadlessEnvironment()) return;
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        final Editor editor = findEditor(element);
        if (editor == null) return;
        doClearInvalidatedMarkers(editor, element);
      }
    });
  }
  
  protected void addOrUpdateHighlighter(final PsiMember member, final ExternallyBoundClassContext context) {
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        final Editor editor = findEditor(member);
        if (editor == null) return;
        RangeHighlighter ws_highlighter = findHighlighter(editor, member);

        if (ws_highlighter != null && !ws_highlighter.isValid()) {
          editor.getMarkupModel().removeHighlighter(ws_highlighter);
          ws_highlighter = null;
        }

        final Module module = PsiUtil.findModule(member);
        final WSIndexEntry[] entries = member instanceof PsiClass ? context.getEntries(module):context.index.getWsEntries(Arrays.asList(context.getEntries(module)), member);
        if (entries.length == 0 || (member instanceof PsiMethod && !DeployUtils.canBeWebMethod(member))) {
          if (ws_highlighter != null) removeHighlighter(ws_highlighter, editor, member);
          return;
        }
        final Map<String, PsiMember> exposed = getMap(member.getParent());

        if (ws_highlighter == null) {
          final PsiIdentifier nameIdentifier = findNameIdentifier(member);

          RangeHighlighter rangeHighlighter = createHighlighter(
            interfaceIcon,
            entries,
            nameIdentifier,
            editor,
            member,
            context //context.getModificationStamp()
          );

          if (rangeHighlighter != null) exposed.put(getKey(member),member);
        } else {
          ((MyWSGutterNavigator)ws_highlighter.getGutterIconRenderer()).setEntries( entries, context.getModificationStamp(), member );
        }
      }
    });
  }

  private static void doClearInvalidatedMarkers(Editor editor, PsiElement member) {
    for(Iterator<Map.Entry<String,PsiMember>> entryIter = getMap(member).entrySet().iterator();entryIter.hasNext();) {
      final Map.Entry<String,PsiMember> entry = entryIter.next();
      if (!entry.getValue().isValid() ||
          !getKey(entry.getValue()).equals(entry.getKey())
         ) {
        final RangeHighlighter rangeHighlighter = findHighlighterByKey(editor,entry.getKey());
        if (rangeHighlighter != null) editor.getMarkupModel().removeHighlighter(rangeHighlighter);
        entryIter.remove();
      }
    }
  }

  private static Map<String, PsiMember> getMap(PsiElement member) {
    Map<String, PsiMember> map = member.getUserData(MY_EXPOSED_KEY);

    if (map == null) {
      map = new THashMap<String, PsiMember>();
      member.putUserData(MY_EXPOSED_KEY, map);
    }
    return map;
  }

  protected static PsiIdentifier findNameIdentifier(PsiMember member) {
    final PsiIdentifier nameIdentifier = member instanceof PsiClass ?
      ((PsiClass) member).getNameIdentifier() :
      member instanceof PsiField ? ((PsiField) member).getNameIdentifier() :
        ((PsiMethod) member).getNameIdentifier();
    return nameIdentifier;
  }

  protected void removeHighlightersIfExist(final PsiMember member) {
    if (ApplicationManager.getApplication().isHeadlessEnvironment()) return;
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      public void run() {
        Editor editor = findEditor(member);
        if (editor == null) return;
        RangeHighlighter ws_highlighter = findHighlighter(editor, member);
        if (ws_highlighter != null) removeHighlighter(ws_highlighter, editor, member);
      }
    });
  }

  private static final Key<String> MY_WS_HIGHLIGHTER_KEY = Key.create("ws.highlighter.key");
  private static final Key<Map<String,PsiMember>> MY_EXPOSED_KEY = Key.create("ws.exposed.members");

  private static RangeHighlighter findHighlighter(Editor editor, PsiMember m) {
    return findHighlighterByKey(editor, getKey(m));
  }

  private static RangeHighlighter findHighlighterByKey(Editor editor, String key) {
    RangeHighlighter ws_highlighter = null;

    for(RangeHighlighter h:editor.getMarkupModel().getAllHighlighters()) {
      final String userData = h.getUserData(MY_WS_HIGHLIGHTER_KEY);

      if (userData != null && userData.equals(key)) {
        ws_highlighter = h;
        break;
      }
    }
    return ws_highlighter;
  }

  private void removeHighlighter(RangeHighlighter ws_highlighter, Editor editor, PsiElement c) {
    if (ws_highlighter != null) {
      editor.getMarkupModel().removeHighlighter(ws_highlighter);
      ws_highlighter.putUserData(MY_WS_HIGHLIGHTER_KEY, null);

      if (c instanceof PsiClass) {
        final PsiClass psiClass = ((PsiClass) c);
        for(PsiField f:psiClass.getFields()) {
          removeHighlighter(findHighlighter(editor, f),editor, f);
        }
        for(PsiMethod m:psiClass.getMethods()) {
          removeHighlighter(findHighlighter(editor, m), editor, m);
        }
      }
    }
  }

  private RangeHighlighter createHighlighter(Icon interfaceIcon, @NotNull WSIndexEntry[] entries,
                                 PsiIdentifier nameIdentifier, Editor editor, PsiMember c, ExternallyBoundClassContext context) {
    if (nameIdentifier == null) return null;
    RangeHighlighter ws_highlighter;
    MarkWebServiceMembersBase.MyWSGutterNavigator myWSGutterNavigator = new MyWSGutterNavigator(c,interfaceIcon, entries, context);
    TextRange textRange = nameIdentifier.getTextRange();
    ws_highlighter = editor.getMarkupModel().addRangeHighlighter(
      textRange.getStartOffset(),
      textRange.getEndOffset(),
      100,
      new TextAttributes(),
      HighlighterTargetArea.EXACT_RANGE
    );

    ws_highlighter.setGutterIconRenderer(myWSGutterNavigator);
    ws_highlighter.putUserData(MY_WS_HIGHLIGHTER_KEY, getKey(c));
    return ws_highlighter;
  }

  private static class MyWSGutterNavigator extends GutterIconRenderer {
    private final Icon interfaceIcon;
    private WSIndexEntry[] entries;
    private PsiMember member;
    private AnAction action;
    private String message;
    private long modificationStamp;
    private final ExternallyBoundClassContext context;

    public MyWSGutterNavigator(PsiMember _member, Icon _icon, WSIndexEntry[] _entries, ExternallyBoundClassContext context) {
      interfaceIcon = _icon;
      this.context = context;
      setEntries( _entries, context.getModificationStamp(), _member );
    }

    @NotNull
    public Icon getIcon() {
      return interfaceIcon;
    }

    void setEntries(WSIndexEntry[] _entries, long _modificationStamp, PsiMember _member) {
      entries = _entries;
      modificationStamp = _modificationStamp;
      member = _member;
      message = null;
      action = null;
    }

    public boolean isNavigateAction() {
      return true;
    }

    @Nullable
    public String getTooltipText() {
      if (message == null) {
        StringBuilder messageSource = new StringBuilder();
        for(WSIndexEntry entry:entries) {
          String wsStatus = entry.getWsStatus(member);
          if (wsStatus != null && messageSource.indexOf(wsStatus) == -1) {
            if(messageSource.length() > 0) messageSource.append(',');
            messageSource.append(wsStatus);
          }
        }
        message = messageSource.toString();
      }
      return message;
    }

    @Nullable
    public AnAction getClickAction() {
      if (action == null) {
        action = new AnAction() {
          public void actionPerformed(AnActionEvent e) {
            DataContext dataContext = e.getDataContext();
            Project project = (Project) dataContext.getData(DataConstants.PROJECT);
            final Module module = PsiUtil.findModule(member);
            entries = member instanceof PsiClass ? context.getEntries(module):context.index.getWsEntries(Arrays.asList(context.getEntries(module)), member);
            Arrays.sort(entries, MyWSGutterNavigator.IndexEntryComparator.INSTANCE);

            final java.util.List<VirtualFile> files = new ArrayList<VirtualFile>(entries.length);
            final java.util.List<TextRange> ranges = new ArrayList<TextRange>(entries.length);

            for(WSIndexEntry entry:entries) {
              if (!entry.isResolved(project)) entry.resolve(project);
              TextRange wsRange = entry.getWsRange(member);
              if (wsRange != null) {
                files.add(entry.getFile());
                ranges.add(wsRange);
              }
            }

            if (entries.length > 1) {
              final JList list = new JList(new AbstractListModel() {

                public int getSize() {
                  return files.size();
                }

                public Object getElementAt(int index) {
                  return files.get(index);
                }

              });
              list.setCellRenderer(new DefaultListCellRenderer() {
                public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                  return super.getListCellRendererComponent(list, ((VirtualFile)value).getPresentableName(),index, isSelected, cellHasFocus);
                }
              });

              showPopup("Select Target", list, files, ranges, project, dataContext);
            } else if (files.size() > 0 && ranges.size() > 0) {
              navigate(files.get(0),ranges.get(0));
            }
          }

          private void showPopup(String title, final JList list, final java.util.List<VirtualFile> files, final java.util.List<TextRange> ranges, Project project, DataContext dataContext) {
            EnvironmentFacade.getInstance().showPopup(
              title,
              list,
              new Runnable() {
                public void run() {
                  int selectedIndex = list.getSelectedIndex();
                  if (selectedIndex == -1) return;
                  navigate(files.get(selectedIndex),ranges.get(selectedIndex));
                }
              },
              project,
              dataContext
            );
          }

          void navigate(@NotNull VirtualFile file, @NotNull TextRange range) {
            final PsiManager psiManager = member.getManager();
            assert psiManager != null;
            if (!file.isValid()) return; // extra guard for inconsistency
            XmlFile psiFile = (XmlFile) psiManager.findFile(file);
            assert psiFile != null;
            int startOffset = range.getStartOffset();
            if (startOffset >= psiFile.getTextLength()) return;
            PsiElement elementAt = psiFile.findElementAt(startOffset);
            ((Navigatable)elementAt).navigate(true);
          }
        };
      }
      return action;
    }

    public long getModificationStamp() {
      return modificationStamp;
    }

    private static class IndexEntryComparator implements Comparator<WSIndexEntry> {
      static MarkWebServiceMembersBase.MyWSGutterNavigator.IndexEntryComparator INSTANCE = new MarkWebServiceMembersBase.MyWSGutterNavigator.IndexEntryComparator();
      public int compare(WSIndexEntry o1, WSIndexEntry o2) {
        return o1.getFile().getPresentableName().compareTo( o2.getFile().getPresentableName() );
      }
    }
  }

  abstract static class InsertAnnotationFix implements LocalQuickFix {
    private final String annotationToInsert;

    protected InsertAnnotationFix(String _annotationToInsert) {
      annotationToInsert = _annotationToInsert;
    }

    @NotNull
    public String getFamilyName() {
      return getName();
    }

    public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {

        public void run() {
          PsiElement psiElement = descriptor.getPsiElement();
          try {
            PsiModifierList modifierList = ((PsiModifierListOwner) psiElement.getParent()).getModifierList();
            if (modifierList == null) return;
            if (!EnvironmentFacade.getInstance().prepareFileForWrite(psiElement.getContainingFile())) return;

            PsiManager psiManager = psiElement.getManager();
            PsiAnnotation psiAnnotation = EnvironmentFacade.getInstance().getElementsFactory(psiManager.getProject()).createAnnotationFromText(annotationToInsert, psiElement);
            PsiElement element = modifierList.getFirstChild();

            if (element != null) {
              modifierList.addBefore(psiAnnotation,element);
            } else {
              modifierList.add(psiAnnotation);
            }

            EnvironmentFacade.getInstance().shortenClassReferences(modifierList, project);

          } catch (IncorrectOperationException e) {
            throw new RuntimeException(e);
          }
        }
      });
    }
  }
}
