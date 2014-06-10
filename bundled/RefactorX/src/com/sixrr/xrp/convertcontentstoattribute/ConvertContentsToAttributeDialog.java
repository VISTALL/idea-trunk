package com.sixrr.xrp.convertcontentstoattribute;

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
class ConvertContentsToAttributeDialog extends BaseRefactoringDialog {
    private final JLabel wrappingTagNameLabel = new JLabel();
    private final JTextField wrappingTagNameField;
    private final JCheckBox trimTextBox;
    private final XmlTag tag;

    ConvertContentsToAttributeDialog(XmlTag tag) {
        super(tag.getProject(), true);
        this.tag = tag;

        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Convert Tag Contents to Attribute");
        wrappingTagNameLabel.setText("Name for attribute");
        wrappingTagNameField = new JTextField("");
        trimTextBox = new JCheckBox("Trim contents");
        trimTextBox.setSelected(true);
        final Document document = wrappingTagNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.ConvertContentsToAttribute";
    }

    public JComponent getPreferredFocusedComponent(){
        return wrappingTagNameField;
    }

    public String getAttributeName()
    {
        final String text = wrappingTagNameField.getText();
        return text.trim();
    }

    public boolean getTrimContents() {
        return trimTextBox.isSelected();
    }
    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Convert contents of tag " + tagName);
        panel.add(wrappingTagNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        wrappingTagNameField.setEditable(true);
        box.add(wrappingTagNameField);
        box.add(trimTextBox);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }

    protected boolean isValid() {
        final String attributeName = getAttributeName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(attributeName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {

        final String tagName = tag.getName();
        final String attributeName = getAttributeName();
        if (getTrimContents()) {
            return "<xsl:template match=\"" + tagName +"\">\n" +
                    "\t<xsl:copy> \n" +
                    "\t\t<xsl:attribute name = \"" + attributeName +"\">\n" +
                    "\t\t\t<xsl:value-of select=\"normalize-space(.)\"/>\n" +
                    "\t\t</xsl:attribute>\n" +
                    "\t</xsl:copy>\n" +
                    "</xsl:template>";
        } else {
            return "<xsl:template match=\"" + tagName + "\">\n" +
                    "\t<xsl:copy> \n" +
                    "\t\t<xsl:attribute name = \"" + attributeName + "\">\n" +
                    "\t\t\t<xsl:value-of select=\".\"/>\n" +
                    "\t\t</xsl:attribute>\n" +
                    "\t</xsl:copy>\n" +
                    "</xsl:template>";
        }
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.ConvertContentsToAttribute);
    }
}
