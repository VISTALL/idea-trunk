package com.sixrr.xrp.renameattribute;

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
class RenameAttributeDialog extends BaseRefactoringDialog{
    private final JLabel attributeNameLabel = new JLabel();
    private final JTextField newAttributeField;

    private final XmlAttribute attribute;
    private final XmlTag tag;

    RenameAttributeDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        this.attribute = attribute;
        final String attributeName = attribute.getName();
        tag = attribute.getParent();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Rename Attribute");
        attributeNameLabel.setText("New name for attribute " + attributeName);
        newAttributeField = new JTextField("");
        final Document document = newAttributeField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.RenameAttribute";
    }

    public JComponent getPreferredFocusedComponent(){
        return newAttributeField;
    }
    public String getNewAttributeName() {
        final String text = newAttributeField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Change name of attribute " + attributeName +" in tag " + tagName);
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        newAttributeField.setEditable(true);
        box.add(newAttributeField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected boolean isValid() {
        final String tagName = getNewAttributeName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();
        final String newAttributeName = getNewAttributeName();

        return " <xsl:template match=\"@" + attributeName+"[parent::"+tagName + "]\">\n" +
                "\t<xsl:attribute name=\"" + newAttributeName+ "\">\n" +
                "\t\t<xsl:value-of select=\".\" />\n" +
                "\t</xsl:attribute>\n" +
                "</xsl:template>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.RenameAttribute);
    }
}
