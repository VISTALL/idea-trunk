/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyLexer;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RFileImpl;


public class RubyParserDefinition implements ParserDefinition, RubyElementTypes {

    @NotNull
    public Lexer createLexer(Project project) {
        return new RubyLexer();
    }

    @NotNull
    public PsiParser createParser(Project project) {
        return new RubyParser();
//        return new RubyMockParser();
    }

    @NotNull
    public IFileElementType getFileNodeType() {
        return RubyElementTypes.FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return BNF.tWHITESPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return BNF.tCOMMENTS;
    }

    @NotNull
    public PsiElement createElement(@NotNull ASTNode node) {
        return RubyPsiCreator.create(node);
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new RFileImpl(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

}
