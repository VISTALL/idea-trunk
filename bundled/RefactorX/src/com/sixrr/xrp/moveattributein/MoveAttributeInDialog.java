package com.sixrr.xrp.moveattributein;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.IdeBorderFactory;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;
import com.sixrr.xrp.utils.XMLUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.awt.*;

@SuppressWarnings({"OverridableMethodCallInConstructor", "ThisEscapedInObjectConstruction"})
class MoveAttributeInDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final JTextField tagNameField;

    private final XmlAttribute attribute;
    private final XmlTag tag;

    MoveAttributeInDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        this.attribute = attribute;
        final String attributeName = attribute.getName();
        tag = attribute.getParent();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Move Attribute In");
        attributeNameLabel.setText("Move Attribute to Subtag:");
        tagNameField = new JTextField("");
        final Document document = tagNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }
    protected String getDimensionServiceKey() {
        return "RefactorX.MoveAttributeIn";
    }

    public JComponent getPreferredFocusedComponent(){
        return tagNameField;
    }
    public String getTagName() {
        final String text = tagNameField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Move attribute " + attributeName + " of tag " + tagName + " in to subtag");
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        tagNameField.setEditable(true);
        box.add(tagNameField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected boolean isValid() {
        final String tagName = getTagName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String attributeName = attribute.getName();
        final String parentTagName = attribute.getParent().getName();
        final String childTagName = getTagName();
        return "<xsl:template match=\"" + parentTagName + "[@" + attributeName + "]/" + childTagName + "\">\n" +
                "\t<xsl:copy> \n" +
                "\t\t<xsl:attribute name = \"" + attributeName + "\">\n" +
                "\t\t\t<xsl:value-of select=\"../@" + attributeName + "\"/>\n" +
                "\t\t</xsl:attribute>\n" +
                "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                "\t</xsl:copy>          \n" +
                "</xsl:template>  \n" +
                "<xsl:template match = \"@" + attributeName + "[parent::" + parentTagName + "]\"/>";
    }


    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.MoveAttributeIn);
    }
}
