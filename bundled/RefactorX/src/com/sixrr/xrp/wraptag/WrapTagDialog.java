package com.sixrr.xrp.wraptag;

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
class WrapTagDialog extends BaseRefactoringDialog {
    private final JLabel wrappingTagNameLabel = new JLabel();
    private final JTextField wrappingTagNameField;
    private final XmlTag tag;

    WrapTagDialog(XmlTag tag) {
        super(tag.getProject(), true);
        setModal(true);
        this.tag = tag;

        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Wrap Tag");
        wrappingTagNameLabel.setText("Name for wrapping tag");
        wrappingTagNameField = new JTextField("");
        final Document document = wrappingTagNameField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.WrapTag";
    }

    public JComponent getPreferredFocusedComponent(){
        return wrappingTagNameField;
    }
    
    public String getWrappingTagName()
    {
        final String text = wrappingTagNameField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();
        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Wrap tag " + tagName);
        panel.add(wrappingTagNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        wrappingTagNameField.setEditable(true);
        box.add(wrappingTagNameField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String wrappingTagName = getWrappingTagName();
        return "<xsl:template match = \"" + tagName + "\">\n" +
                "\t<" + wrappingTagName +">\n" +
                "\t\t<xsl:copy-of select = \".\"/>\n" +
                "\t</" + wrappingTagName + ">\n" +
                "</xsl:template>";
    }

    protected boolean isValid() {
        final String tagName = getWrappingTagName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.WrapTag);
    }

}