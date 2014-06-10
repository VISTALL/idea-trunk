package com.sixrr.xrp.deletetag;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;

import javax.swing.*;

@SuppressWarnings({"OverridableMethodCallInConstructor",
        "ThisEscapedInObjectConstruction"})
class DeleteTagDialog extends BaseRefactoringDialog {
    private final JLabel tagNameLabel = new JLabel();
    private final String tagName;

    DeleteTagDialog(XmlTag tag) {
        super(tag.getProject(), true);
        setModal(true);
        setTitle("Delete Tag");

        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        init();
        tagName = tag.getName();
        tagNameLabel.setText("Delete Tag " + tagName);
    }


    protected JComponent createNorthPanel() {
        return tagNameLabel;
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.DeleteTag";
    }

    public JComponent getPreferredFocusedComponent(){
        return null;
    }

    protected  boolean isValid() {
        return scopePanel.isScopeValid();
    }

    protected String calculateXSLT() {
        return "<xsl:template match = \"" + tagName + "/>";
    }


    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.DeleteTag);
    }



}
