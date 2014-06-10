package com.sixrr.xrp.deleteattribute;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;

import javax.swing.*;

@SuppressWarnings({"OverridableMethodCallInConstructor",
        "ThisEscapedInObjectConstruction"})
class DeleteAttributeDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final XmlAttribute attribute;
    private final XmlTag tag;

    DeleteAttributeDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        setTitle("Delete Attribute");
        this.attribute = attribute;
        tag = attribute.getParent();

        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        init();
        final String attributeName = this.attribute.getName();
        final String tagName = tag.getName();
        attributeNameLabel.setText("Delete attribute " + attributeName + " from tag " + tagName);
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.DeleteAttribute";
    }

    public JComponent getPreferredFocusedComponent(){
        return null;
    }

    protected JComponent createNorthPanel() {
        return attributeNameLabel;
    }

    protected boolean isValid() {
        return scopePanel.isScopeValid();
    }

    protected String calculateXSLT() {
        final String attributeName = attribute.getName();
        final String tagName = tag.getName();
        return " <xsl:template match=\"@" + attributeName + "[parent::" + tagName + "]\"/>";
    }

    protected void doHelpAction(){
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.DeleteAttribute);
    }
}