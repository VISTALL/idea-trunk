package com.sixrr.xrp.changeattributevalue;

import com.intellij.openapi.help.HelpManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.IdeBorderFactory;
import com.sixrr.xrp.RefactorXHelpID;
import com.sixrr.xrp.base.BaseRefactoringDialog;
import com.sixrr.xrp.ui.ScopePanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.Document;
import java.awt.*;

@SuppressWarnings({"OverridableMethodCallInConstructor", "ThisEscapedInObjectConstruction"})
class ChangeAttributeValueDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final JTextField newAttributeField;

    private final XmlAttribute attribute;
    private final XmlTag tag;

    ChangeAttributeValueDialog(XmlAttribute attribute) {
        super(attribute.getProject(), true);
        this.attribute = attribute;
        final String attributeName = attribute.getName();
        tag = attribute.getParent();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Change Attribute Value");
        attributeNameLabel.setText("New value for attribute " + attributeName);
        newAttributeField = new JTextField("");
        final Document document = newAttributeField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.ChangeAttributeValue";
    }

    public JComponent getPreferredFocusedComponent(){
        return newAttributeField;
    }
    public String getNewAttributeValue() {
        final String text = newAttributeField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Change value of attribute " + attributeName +"='" + attribute.getValue()+"' in tag " + tagName);
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        newAttributeField.setEditable(true);
        box.add(newAttributeField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected boolean isValid() {
        return scopePanel.isScopeValid();
    }

    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();
        final String attributeValue = attribute.getValue();
        final String newAttributeValue = getNewAttributeValue();
        return " <xsl:template match=\"@" + attributeName+"[.='" + attributeValue+"' and parent::"+tagName + "]\">\n" +
                "    <xsl:attribute name=\"" + attributeName+ "\">" + newAttributeValue+"</xsl:attribute>\n" +
                "  </xsl:template>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.ChangeAttributeValue);
    }
}
