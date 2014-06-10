package com.sixrr.xrp.renametag;

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
class RenameTagDialog extends BaseRefactoringDialog{
    private final JLabel tagNameLabel = new JLabel();
    private final JTextField newTagField;

    private final XmlTag tag;

    RenameTagDialog(XmlTag tag) {
        super(tag.getProject(), true);
        this.tag = tag;
        final String tagName = tag.getName();
        final XmlFile containingFile = (XmlFile) tag.getContainingFile();
        scopePanel = new ScopePanel(containingFile, this);
        setTitle("Rename Tag");
        tagNameLabel.setText("New name for tag " + tagName);
        newTagField = new JTextField("");
        final Document document = newTagField.getDocument();
        document.addDocumentListener(docListener);
        init();
        validateButtons();
    }

    protected String getDimensionServiceKey() {
        return "RefactorX.RenameTag";
    }

    public JComponent getPreferredFocusedComponent(){
        return newTagField;
    }
    public String getNewTagName() {
        final String text = newTagField.getText();
        return text.trim();
    }

    protected JComponent createNorthPanel() {
        final String tagName = tag.getName();

        final JPanel panel = new JPanel(new BorderLayout());
        final TitledBorder border =
                IdeBorderFactory.createTitledBorder("Change name of tag " + tagName );
        panel.add(tagNameLabel, BorderLayout.NORTH);
        final Box box = Box.createVerticalBox();
        panel.setBorder(border);
        newTagField.setEditable(true);
        box.add(newTagField);
        panel.add(box, BorderLayout.CENTER);
        return panel;
    }


    protected boolean isValid() {
        final String tagName = getNewTagName();
        final boolean tagNameEntered = XMLUtil.tagNameIsValid(tagName);
        final boolean scopeIsValid = scopePanel.isScopeValid();
        return tagNameEntered && scopeIsValid;
    }

    protected String calculateXSLT() {
        final String tagName = tag.getName();
        final String newTagName = getNewTagName();
        return " <xsl:template match=\"" + tagName+"\">\n" +
                "/t<xsl:tag name=\"" + newTagName+ "\">\n" +
                "/t/t<xsl:value-of select=\".\" />\n" +
                "/t</xsl:tag>\n" +
                " </xsl:template>";
    }

    protected void doHelpAction() {
        final HelpManager helpManager = HelpManager.getInstance();
        helpManager.invokeHelp(RefactorXHelpID.RenameTag);
    }
}
