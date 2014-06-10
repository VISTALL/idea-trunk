package com.sixrr.xrp.moveattributeout;

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
class MoveAttributeOutDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final XmlAttribute attribute;

    MoveAttributeOutDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        setTitle("Move Attribute Out");
        this.attribute = attribute;
        final XmlTag tag = attribute.getParent();

        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        init();
        final String attributeName = attribute.getName();
        final String tagName = tag.getName();
        attributeNameLabel.setText("Move attribute " + attributeName + " out from tag " + tagName);
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.MoveAttributeOut";
    }
    protected JComponent createNorthPanel() {
        return attributeNameLabel;
    }

    public JComponent getPreferredFocusedComponent(){
        return null;
    }

    protected boolean isValid() {
        return scopePanel.isScopeValid();
    }

    protected String calculateXSLT() {
        final String tagName = attribute.getParent().getName();
        final String attributeName = attribute.getName();
        return "<xsl:template match=\"*[child::" + tagName + "/@" + attributeName + "]\">\n" +
                "\t<xsl:copy> \n" +
                "\t\t<xsl:attribute name = \"" + attributeName + "\">\n" +
                "\t\t\t<xsl:value-of select=\"" + tagName + "/@" + attributeName + "\"/>\n" +
                "\t\t</xsl:attribute>\n" +
                "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                "\t</xsl:copy>\n" +
                "</xsl:template>  \n" +
                "<xsl:template match = \"@" + attributeName + "[parent::" + tagName + "]\"/>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.MoveAttributeOut);
    }
}
