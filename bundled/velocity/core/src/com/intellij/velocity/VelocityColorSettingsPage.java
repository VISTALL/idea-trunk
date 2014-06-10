package com.intellij.velocity;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.velocity.psi.files.VtlSyntaxHighlighter;
import static com.intellij.velocity.psi.files.VtlSyntaxHighlighter.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

/**
 * @author Alexey Chmutov
 */
public class VelocityColorSettingsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRS;

    static {
        ATTRS = new AttributesDescriptor[]{
                createAttributesDescriptor("velocity.color.dot", VELOCITY_DOT),
                createAttributesDescriptor("velocity.color.parenths", VELOCITY_PARENTHS),
                createAttributesDescriptor("velocity.color.brackets", VELOCITY_BRACKETS),
                createAttributesDescriptor("velocity.color.braces", VELOCITY_BRACES),
                createAttributesDescriptor("velocity.color.operation.sign", VELOCITY_OPERATION_SIGN),
                createAttributesDescriptor("velocity.color.string", VELOCITY_STRING),
                createAttributesDescriptor("velocity.color.escape", VELOCITY_ESCAPE),
                createAttributesDescriptor("velocity.color.number", VELOCITY_NUMBER),
                createAttributesDescriptor("velocity.color.keyword", VELOCITY_KEYWORD),
                createAttributesDescriptor("velocity.color.comma", VELOCITY_COMMA),
                createAttributesDescriptor("velocity.color.semicolon", VELOCITY_SEMICOLON),
                createAttributesDescriptor("velocity.color.directive", VELOCITY_DIRECTIVE),
                createAttributesDescriptor("velocity.color.reference", VELOCITY_REFERENCE),
                createAttributesDescriptor("velocity.color.comment", VELOCITY_COMMENT),
                createAttributesDescriptor("velocity.color.bad.character", VELOCITY_BAD_CHARACTER),
                createAttributesDescriptor("velocity.color.scripting.background", VELOCITY_SCRIPTING_BACKGROUND),
        };
    }

    private static AttributesDescriptor createAttributesDescriptor(String displayNameKey, TextAttributesKey textAttributesKey) {
        return new AttributesDescriptor(VelocityBundle.message(displayNameKey), textAttributesKey);
    }

    @NotNull
    public String getDisplayName() {
        return "Velocity";
    }

    public Icon getIcon() {
        return Icons.VTL_ICON;
    }

    @NotNull
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }

    @NotNull
    public ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new VtlSyntaxHighlighter();
    }

    @NotNull
    public String getDemoText() {
        return "#* comment  \n" +
                "  comment *#\n" +
                "$reference.method('string').property\n" +
                "## another comment\n" +
                "#foreach($loopvar in [-4..77])\n" +
                "  #set($var = {1:\"double-quoted ${loopvar.toString()}\", 2:false #fff})\n" +
                "#{end}\n\n" +
                "#{macro}(m1 $p1, $p2 $p3 $)\n" +
                "  #if($p1 * ${p2} eq $p3)\n" +
                "#templatetext\n" +
                "  #{else}template text\n" +
                "  #end\n" +
                "#end\n" +
                "#m1(4 5 7)\n" +
                "#{m1}(8, 7, 6)\n";
    }

    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }
}
