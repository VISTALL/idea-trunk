package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel;
import com.intellij.ide.util.treeView.smartTree.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by Lera Nikolaenko
 * Date: 19.02.2009
 */
public class CfmlStructureViewModel extends TextEditorBasedStructureViewModel {
    private PsiFile myCfmlPsiFile;
    private static final Class[] myClasses = new Class[]{CfmlTag.class, CfmlFile.class};
    private StructureViewTreeElement myRoot;

    protected CfmlStructureViewModel(@NotNull PsiFile psiFile) {
        super(psiFile);

        myCfmlPsiFile = psiFile.getViewProvider().getPsi(CfmlLanguage.INSTANCE);
        final String rootLabel = psiFile.getName();
        myRoot = new CfmlStructureViewElement(myCfmlPsiFile);
    }

    protected PsiFile getPsiFile() {
        return myCfmlPsiFile;
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return myRoot;
    }


    @NotNull
    public Grouper[] getGroupers() {
        return new Grouper[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public Sorter[] getSorters() {
        return new Sorter[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public Filter[] getFilters() {
        return new Filter[]{
                new Filter() {
                    public boolean isVisible(TreeElement treeNode) {
                        if (!(treeNode instanceof CfmlStructureViewElement)) {
                            return false;
                        }
                        CfmlStructureViewElement element = (CfmlStructureViewElement) treeNode;
                        if (element.isMethodDefinition() || element.getElement() instanceof CfmlFile) {
                            return true;
                        }
                        PsiElement psiElement = element.getElement();
                        if (psiElement instanceof CfmlTag) {
                            String tagName = ((CfmlTag) psiElement).getTagName();
                            if ("cfcomponent".equals(tagName) ||
                                    "cfscript".equals(tagName)) {
                                return true;
                            }

                        }
                        return false;
                    }

                    public boolean isReverted() {
                        return false;
                    }

                    @NotNull
                    public ActionPresentation getPresentation() {
                        return new ActionPresentation() {
                            public String getText() {
                                return "text";  //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public String getDescription() {
                                return "descr";  //To change body of implemented methods use File | Settings | File Templates.
                            }

                            public Icon getIcon() {
                                return CfmlIcons.FUNCTIONTAG_ICON;  //To change body of implemented methods use File | Settings | File Templates.
                            }
                        };
                    }

                    @NotNull
                    public String getName() {
                        return "Functions";
                    }
                }
        };
        // return CfmlFilter.EMPTY_ARRAY;
    }
}
/*implements StructureViewModel {
}
    private PsiFile myCfmlPsiFile;
    private static final Class[] myClasses = new Class[]{CfmlTag.class, CfmlFile.class};
    private StructureViewTreeElement myRoot;

    protected CfmlStructureViewModel(@NotNull PsiFile psiFile) {
        this(getEditorForFile(psiFile));

        myCfmlPsiFile = psiFile.getViewProvider().getPsi(CfmlLanguage.INSTANCE);
        final String rootLabel = psiFile.getName();
        myRoot = new CfmlStructureViewElement(myCfmlPsiFile);
    }

    @NotNull
    public StructureViewTreeElement getRoot() {
        return myRoot;
    }

    public boolean shouldEnterElement(Object element) {

        return (element instanceof CfmlTag && ((CfmlTag) element).getSameTypeChildren().length > 0) ||
                element instanceof CfmlFile;
    }

    @NotNull
    protected Class[] getSuitableClasses() {
        return myClasses;
    }

    private final Editor myEditor;
    private final CaretListener myCaretListener;
    private final List<FileEditorPositionListener> myListeners // = ContainerUtil.createEmptyCOWList();
            = new CopyOnWriteArrayList<FileEditorPositionListener>();

    protected CfmlStructureViewModel(final Editor editor) {
        myEditor = editor;
        myCaretListener = new CaretListener() {
            public void caretPositionChanged(CaretEvent e) {
                if (Comparing.equal(e.getEditor(), myEditor)) {
                    fireCaretPositionChanged();
                }
            }

            private void fireCaretPositionChanged() {
                for (FileEditorPositionListener listener : myListeners) {
                    listener.onCurrentElementChanged();
                }
            }
        };

        EditorFactory.getInstance().getEventMulticaster().addCaretListener(myCaretListener);
    }

    private static FileEditor[] getFileEditors(@NotNull final PsiFile psiFile) {
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            PsiFile originalFile = psiFile.getOriginalFile();
            if (originalFile == null) return null;
            virtualFile = originalFile.getVirtualFile();
            if (virtualFile == null) return null;
        }
        return FileEditorManager.getInstance(psiFile.getProject()).getEditors(virtualFile);
    }

    @Nullable
    private static Editor getEditorForFile(@NotNull final PsiFile psiFile) {
        final FileEditor[] editors = getFileEditors(psiFile);
        for (FileEditor editor : editors) {
            if (editor instanceof TextEditor) {
                return ((TextEditor) editor).getEditor();
            }
        }
        return null;
    }

    public final void addEditorPositionListener(FileEditorPositionListener listener) {
        myListeners.add(listener);
    }

    public final void removeEditorPositionListener(FileEditorPositionListener listener) {
        myListeners.remove(listener);
    }

    public void dispose() {
        EditorFactory.getInstance().getEventMulticaster().removeCaretListener(myCaretListener);
    }

    public Object getCurrentEditorElement() {
        if (myEditor == null) return null;
        final int offset = myEditor.getCaretModel().getOffset();
        final PsiFile file = myCfmlPsiFile;

        return findAcceptableElement(file.getViewProvider().findElementAt(offset, file.getLanguage()));
    }

    protected Object findAcceptableElement(PsiElement element) {
        while (element != null && !(element instanceof PsiFile)) {
            if (isSuitable(element)) return element;
            element = element.getParent();
        }
        return null;
    }

    protected boolean isSuitable(final PsiElement element) {
        if (element == null) return false;
        final Class[] suitableClasses = getSuitableClasses();
        for (Class suitableClass : suitableClasses) {
            if (ReflectionCache.isAssignable(suitableClass, element.getClass())) return true;
        }
        return false;
    }

    public void addModelListener(ModelListener modelListener) {

    }

    public void removeModelListener(ModelListener modelListener) {

    }

    protected Editor getEditor() {
        return myEditor;
    }
}
*/