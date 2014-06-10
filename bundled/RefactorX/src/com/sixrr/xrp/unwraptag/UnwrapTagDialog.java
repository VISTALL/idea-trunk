package com.sixrr.xrp.unwraptag;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;

import javax.swing.*;

@SuppressWarnings({"OverridableMethodCallInConstructor", "ThisEscapedInObjectConstruction"})
class UnwrapTagDialog extends BaseRefactoringDialog {
    private final JLabel tagNameLabel = new JLabel();
    private final String tagName;

    UnwrapTagDialog(XmlTag tag) {
        super(tag.getProject(), true);
        setModal(true);
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Unwrap Tag");
        tagName = tag.getName();
        tagNameLabel.setText("Unwrap Tag " + tagName);
        init();

    }

    protected JComponent createNorthPanel() {
        return tagNameLabel;
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.UnwrapTag";
    }

    public JComponent getPreferredFocusedComponent(){
        return null;
    }

    protected boolean isValid() {
        return scopePanel.isScopeValid();
    }


    protected String calculateXSLT() {
       return  "<xsl:template match=\""+ tagName +"\">\n" +
                "\t<xsl:apply-templates select=\"@*|node()\" />\n" +
                "</xsl:template>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.UnwrapTag);
    }

}
