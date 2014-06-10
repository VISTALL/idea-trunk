package com.sixrr.xrp.addattribute;

import com.intellij.openapi.help.HelpManager;
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
class AddAttributeDialog extends BaseRefactoringDialog {
    private final JLabel attributeNameLabel = new JLabel();
    private final JLabel attributeValueLabel = new JLabel();
    private final JTextField attributeNameField;
    private final JTextField attributeValueField;
    private final JCheckBox addIfAbsentCheckbox;

    private final XmlTag tag;

    AddAttributeDialog(XmlTag tag) {
        super(tag.getProject(), true);
        this.tag = tag;
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Add Attribute");
        attributeNameLabel.setText("Name for new attribute");
        attributeNameField = new JTextField();
        attributeValueLabel.setText("Value for new attribute");
        attributeValueField = new JTextField();
        addIfAbsentCheckbox = new JCheckBox("Add attribute only if absent");
        addIfAbsentCheckbox.setSelected(true);
        final Document document = attributeNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.AddAttribute";
    }

    public JComponent getPreferredFocusedComponent(){
        return attributeNameField;
    }
    public String getAttributeName() {
        final String text = attributeNameField.getText();
        return text.trim();
    }

    public String getAttributeValue() {
        final String text = attributeValueField.getText();
        return text.trim();
    }

    public boolean getAddOnlyIfAbsent() {
        return addIfAbsentCheckbox.isSelected();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Add attribute to tag " + tagName);
        panel.add(attributeNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        attributeNameField.setEditable(true);
        box.add(attributeNameField);
        box.add(attributeValueLabel);
        box.add(attributeValueField);
        box.add(addIfAbsentCheckbox);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }

    protected boolean isValid() {
        final String tagName = getAttributeName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String attributeName = getAttributeName();
        final String attributeValue = getAttributeValue();
        if (getAddOnlyIfAbsent()) {
            return "<xsl:template match=\"" + tagName + "\">\n" +
                    "\t<xsl:copy>\n" +
                    "\t\t<xsl:attribute name=\"" + attributeName + "\">" + attributeValue + "</xsl:attribute>\n" +
                    "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                    "\t</xsl:copy>\n" +
                    "</xsl:template>\n"+
                    "<xsl:template match=\"" + tagName + "[@" +attributeName + "]\">\n" +
                    "\t<xsl:copy>\n" +
                    "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                    "\t</xsl:copy>\n" +
                    "</xsl:template>";
        } else
        {
            return "<xsl:template match=\"" + tagName+"\">\n" +
                    "\t<xsl:copy>\n" +
                    "\t\t<xsl:attribute name=\"" + attributeName+"\">"+ attributeValue+"</xsl:attribute>\n" +
                    "\t\t<xsl:apply-templates select=\"@*|node()\"/>\n" +
                    "\t</xsl:copy>\n" +
                    "</xsl:template>";
        }
    }


    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.AddAttribute);
    }

}