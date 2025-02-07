/***** BEGIN LICENSE BLOCK *****
 * Version: CPL 1.0/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Common Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2007 Ola Bini <ola@ologix.com>
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the CPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the CPL, the GPL or the LGPL.
 ***** END LICENSE BLOCK *****/
package org.jetbrains.jvaymlb;

import org.jetbrains.jvaymlb.tokens.*;
import org.jruby.util.ByteList;
import org.jvyamlb.Scanner;
import org.jvyamlb.exceptions.ScannerException;
import org.jvyamlb.exceptions.YAMLException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * <p>A Java implementation of the RbYAML scanner.</p>
 *
 * @author <a href="mailto:ola.bini@ki.se">Ola Bini</a>
 */
@SuppressWarnings({"EmptyCatchBlock", "ForLoopReplaceableByForEach", "SimplifiableIfStatement", "MismatchedReadAndWriteOfArray", "RedundantCast"})
public class ScannerImpl implements Scanner {
    private final static byte[] EMPTY = new byte[0];
    private final static byte[] NN = new byte[]{'\n'};
    private final static ByteList BANG = new ByteList(new byte[]{'!'},false);
    private final static ByteList SPACE = new ByteList(new byte[]{' '},false);

    private final static boolean[] ALL_FALSE = new boolean[256];
    private final static boolean[] ALL_TRUE = new boolean[256];
    private final static boolean[] LINEBR = new boolean[256];
    private final static boolean[] NULL_BL_LINEBR = new boolean[256];
    private final static boolean[] NULL_BL_T_LINEBR = new boolean[256];
    private final static boolean[] NULL_OR_LINEBR = new boolean[256];
    private final static boolean[] FULL_LINEBR = new boolean[256];
    private final static boolean[] BLANK_OR_LINEBR = new boolean[256];
    private final static boolean[] S4 = new boolean[256];
    private final static boolean[] ALPHA = new boolean[256];
    private final static boolean[] DIGIT = new boolean[256];
    private final static boolean[] HEXA = new boolean[256];
    private final static boolean[] STRANGE_CHAR = new boolean[256];
    private final static int[] RN = new int[]{'\r','\n'};
    private final static boolean[] BLANK_T = new boolean[256];
    private final static boolean[] SPACES_AND_STUFF = new boolean[256];
    private final static boolean[] DOUBLE_ESC = new boolean[256];
    private final static boolean[] NON_ALPHA_OR_NUM = new boolean[256];
    private final static boolean[] NON_PRINTABLE = new boolean[256];
    private final static boolean[] STUPID_CHAR = new boolean[256];
    private final static boolean[] R_FLOWZERO = NULL_BL_T_LINEBR;
    private final static boolean[] R_FLOWZERO1 = new boolean[256];
    private final static boolean[] R_FLOWNONZERO = new boolean[256];

    private final static byte[] ESCAPE_REPLACEMENTS = new byte[256];
    private final static boolean[] IS_ESCAPE_REPLACEMENT = new boolean[256];
    private final static Map<Character, Integer> ESCAPE_CODES = new HashMap<Character, Integer>();
    private final static boolean[] CHOMPING = new boolean[256];

    static {
        CHOMPING['+'] = true;
        CHOMPING['-'] = true;
        CHOMPING['\n'] = true;
        CHOMPING['\r'] = true;
        CHOMPING['0'] = true;
        CHOMPING['1'] = true;
        CHOMPING['2'] = true;
        CHOMPING['3'] = true;
        CHOMPING['4'] = true;
        CHOMPING['5'] = true;
        CHOMPING['6'] = true;
        CHOMPING['7'] = true;
        CHOMPING['8'] = true;
        CHOMPING['9'] = true;
        CHOMPING['#'] = true;
        CHOMPING[' '] = true;
        Arrays.fill(ALL_TRUE,true);
        LINEBR['\n'] = true;
        NULL_BL_LINEBR['\0'] = true;
        NULL_BL_LINEBR[' '] = true;
        NULL_BL_LINEBR['\r'] = true;
        NULL_BL_LINEBR['\n'] = true;
        NULL_BL_T_LINEBR['\0'] = true;
        NULL_BL_T_LINEBR[' '] = true;
        NULL_BL_T_LINEBR['\t'] = true;
        NULL_BL_T_LINEBR['\r'] = true;
        NULL_BL_T_LINEBR['\n'] = true;
        NULL_OR_LINEBR['\0'] = true;
        NULL_OR_LINEBR['\r'] = true;
        NULL_OR_LINEBR['\n'] = true;
        FULL_LINEBR['\r'] = true;
        FULL_LINEBR['\n'] = true;
        BLANK_OR_LINEBR[' '] = true;
        BLANK_OR_LINEBR['\r'] = true;
        BLANK_OR_LINEBR['\n'] = true;
        S4['\0'] = true;
        S4[' '] = true;
        S4['\t'] = true;
        S4['\r'] = true;
        S4['\n'] = true;
        S4['['] = true;
        S4[']'] = true;
        S4['{'] = true;
        S4['}'] = true;
        for(char c = 'a'; c<='z'; c++) {
            ALPHA[c] = true;
            STRANGE_CHAR[c] = true;
        }
        for(char c = 'A'; c<='Z'; c++) {
            ALPHA[c] = true;
            STRANGE_CHAR[c] = true;
        }
        for(char c = '0'; c<='9'; c++) {
            ALPHA[c] = true;
            STRANGE_CHAR[c] = true;
            HEXA[c] = true;
            DIGIT[c] = true;
        }
        for(char c = 'a'; c<='f'; c++) {
            HEXA[c] = true;
        }
        for(char c = 'A'; c<='F'; c++) {
            HEXA[c] = true;
        }
        ALPHA['-'] = true;
        ALPHA['_'] = true;
        STRANGE_CHAR['-'] = true;
        STRANGE_CHAR['_'] = true;
        STRANGE_CHAR['['] = true;
        STRANGE_CHAR[']'] = true;
        STRANGE_CHAR['('] = true;
        STRANGE_CHAR[')'] = true;
        STRANGE_CHAR['\''] = true;
        STRANGE_CHAR[';'] = true;
        STRANGE_CHAR['/'] = true;
        STRANGE_CHAR['?'] = true;
        STRANGE_CHAR[':'] = true;
        STRANGE_CHAR['@'] = true;
        STRANGE_CHAR['&'] = true;
        STRANGE_CHAR['='] = true;
        STRANGE_CHAR['+'] = true;
        STRANGE_CHAR['$'] = true;
        STRANGE_CHAR[','] = true;
        STRANGE_CHAR['.'] = true;
        STRANGE_CHAR['!'] = true;
        STRANGE_CHAR['~'] = true;
        STRANGE_CHAR['*'] = true;
        STRANGE_CHAR['%'] = true;
        STRANGE_CHAR['^'] = true;
        STRANGE_CHAR['#'] = true;
        BLANK_T[' '] = true;
        BLANK_T['\t'] = true;
        SPACES_AND_STUFF['\0'] = true;
        SPACES_AND_STUFF[' '] = true;
        SPACES_AND_STUFF['\t'] = true;
        SPACES_AND_STUFF['\r'] = true;
        SPACES_AND_STUFF['\n'] = true;
        SPACES_AND_STUFF['\\'] = true;
        SPACES_AND_STUFF['\''] = true;
        SPACES_AND_STUFF['"'] = true;
        DOUBLE_ESC['\\'] = true;
        DOUBLE_ESC['"'] = true;
        NON_ALPHA_OR_NUM['\0'] = true;
        NON_ALPHA_OR_NUM[' '] = true;
        NON_ALPHA_OR_NUM['\t'] = true;
        NON_ALPHA_OR_NUM['\r'] = true;
        NON_ALPHA_OR_NUM['\n'] = true;
        NON_ALPHA_OR_NUM['?'] = true;
        NON_ALPHA_OR_NUM[':'] = true;
        NON_ALPHA_OR_NUM[','] = true;
        NON_ALPHA_OR_NUM[']'] = true;
        NON_ALPHA_OR_NUM['}'] = true;
        NON_ALPHA_OR_NUM['%'] = true;
        NON_ALPHA_OR_NUM['@'] = true;
        NON_ALPHA_OR_NUM['`'] = true;

        Arrays.fill(ESCAPE_REPLACEMENTS,(byte)0);
        ESCAPE_REPLACEMENTS['0'] = 0;
        ESCAPE_REPLACEMENTS['a'] = 7;
        ESCAPE_REPLACEMENTS['b'] = 8;
        ESCAPE_REPLACEMENTS['t'] = 9;
        ESCAPE_REPLACEMENTS['\t'] = 9;
        ESCAPE_REPLACEMENTS['n'] = 10;
        ESCAPE_REPLACEMENTS['v'] = 11;
        ESCAPE_REPLACEMENTS['f'] = 12;
        ESCAPE_REPLACEMENTS['r'] = 13;
        ESCAPE_REPLACEMENTS['e'] = 27;
        //        ESCAPE_REPLACEMENTS[' '] = 32;
        ESCAPE_REPLACEMENTS['"'] = (byte)'"';
        ESCAPE_REPLACEMENTS['\\'] = (byte)'\\';
        ESCAPE_REPLACEMENTS['N'] = (byte)133;
        ESCAPE_REPLACEMENTS['_'] = (byte)160;
        IS_ESCAPE_REPLACEMENT['0'] = true;
        IS_ESCAPE_REPLACEMENT['a'] = true;
        IS_ESCAPE_REPLACEMENT['b'] = true;
        IS_ESCAPE_REPLACEMENT['t'] = true;
        IS_ESCAPE_REPLACEMENT['\t'] = true;
        IS_ESCAPE_REPLACEMENT['n'] = true;
        IS_ESCAPE_REPLACEMENT['v'] = true;
        IS_ESCAPE_REPLACEMENT['f'] = true;
        IS_ESCAPE_REPLACEMENT['r'] = true;
        IS_ESCAPE_REPLACEMENT['e'] = true;
        //        IS_ESCAPE_REPLACEMENT[' '] = true;
        IS_ESCAPE_REPLACEMENT['"'] = true;
        IS_ESCAPE_REPLACEMENT['\\'] = true;
        IS_ESCAPE_REPLACEMENT['N'] = true;
        IS_ESCAPE_REPLACEMENT['_'] = true;

        ESCAPE_CODES.put('x', 2);
        ESCAPE_CODES.put('u', 4);
        ESCAPE_CODES.put('U', 8);

        Arrays.fill(STUPID_CHAR,true);
        STUPID_CHAR['\0'] = false;
        STUPID_CHAR[' '] = false;
        STUPID_CHAR['\t'] = false;
        STUPID_CHAR['\r'] = false;
        STUPID_CHAR['\n'] = false;
        STUPID_CHAR['-'] = false;
        STUPID_CHAR['?'] = false;
        STUPID_CHAR[':'] = false;
        STUPID_CHAR['['] = false;
        STUPID_CHAR[']'] = false;
        STUPID_CHAR['{'] = false;
        STUPID_CHAR['#'] = false;
        STUPID_CHAR['&'] = false;
        STUPID_CHAR['*'] = false;
        STUPID_CHAR['!'] = false;
        STUPID_CHAR['|'] = false;
        STUPID_CHAR['\''] = false;
        STUPID_CHAR['"'] = false;
        STUPID_CHAR['@'] = false;
        R_FLOWZERO1[':'] = true;
        R_FLOWNONZERO['\0'] = true;
        R_FLOWNONZERO[' '] = true;
        R_FLOWNONZERO['\t'] = true;
        R_FLOWNONZERO['\r'] = true;
        R_FLOWNONZERO['\n'] = true;
        R_FLOWNONZERO['['] = true;
        R_FLOWNONZERO[']'] = true;
        R_FLOWNONZERO['{'] = true;
        R_FLOWNONZERO['}'] = true;
        R_FLOWNONZERO[','] = true;
        R_FLOWNONZERO[':'] = true;
        R_FLOWNONZERO['?'] = true;
    }

    private boolean done = false;
    private int flowLevel = 0;
    private int tokensTaken = 0;
    private int indent = -1;
    private boolean allowSimpleKey = true;
    private boolean eof = true;
    private int column = 0;
    private int pointer = 0;
    private ByteList buffer;
    private InputStream stream;

    private List<Token> tokens;
    private List<Integer> indents;

    private Map<Integer, SimpleKey> possibleSimpleKeys;

    private boolean docStart = false;

    private int offset = 0;
    private int last_offset = 0;

    public int getOffset() {
        return offset;
    }

    public ScannerImpl(final InputStream stream) {
        this.stream = stream;
        this.eof = false;
        this.buffer = new ByteList(100);
        this.tokens = new ArrayList<Token>();
        this.indents = new ArrayList<Integer>();
        this.possibleSimpleKeys = new HashMap<Integer, SimpleKey>();
        fetchStreamStart();
    }

    public ScannerImpl(final ByteList stream) {
        this.buffer = new ByteList(stream.bytes,stream.begin,stream.realSize);
        this.stream = null;
        this.tokens = new ArrayList<Token>();
        this.indents = new ArrayList<Integer>();
        this.possibleSimpleKeys = new HashMap<Integer, SimpleKey>();
        fetchStreamStart();
    }

    public ScannerImpl(final String stream) {
        try {
            this.buffer = new ByteList(ByteList.plain(stream),false);
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        this.stream = null;
        this.tokens = new ArrayList<Token>();
        this.indents = new ArrayList<Integer>();
        this.possibleSimpleKeys = new HashMap<Integer, SimpleKey>();
        fetchStreamStart();
    }

    private void update(final int length, final boolean reset) {
        if(!eof && reset) {
            this.buffer.delete(0,this.pointer);
            this.pointer = 0;
        }
        while(this.buffer.realSize < (this.pointer+length)) {
            byte[] rawData = ByteList.NULL_ARRAY;
            int converted = -2;
            if(!this.eof) {
                byte[] data = new byte[1024];
                try {
                    converted = this.stream.read(data);
                } catch(final IOException ioe) {
                    throw new YAMLException(ioe);
                }
                if(converted == -1) {
                    this.eof = true;
                } else {
                    rawData = data;
                }
            }
            if(this.eof) {
                this.buffer.append('\0');
                break;
            } else {
                checkPrintable(rawData,converted);
                this.buffer.append(rawData,0,converted);
            }
        }
    }

    private void checkPrintable(final byte[] b, final int len) {
        for(int i=0;i<len;i++) {
            if(NON_PRINTABLE[((int)b[i] & 0xFF)]) {
                final int position = this.buffer.length() - this.pointer + i;
                throw new YAMLException("At " + position + " we found: " + (char)((int)b[i] & 0xFF) + ". Special characters are not allowed");
            }
        }
    }

    private boolean ensure(final int len, final boolean reset) {
        if(this.pointer + len >= this.buffer.realSize) {
            update(len, reset);
        }
        return true;
    }

    private char peek() {
        ensure(1,false);
        return (char)((char)(buffer.bytes[this.pointer]) & 0xFF);
    }

    private char peek(final int index) {
        ensure(index+1,false);
        return (char)((char)this.buffer.bytes[this.pointer + index] & 0xFF);
    }

    public void forward() {
        ensure(2,true);
        final char ch1 = (char)((int)this.buffer.bytes[this.pointer++] & 0xFF);
        if(ch1 == '\n' || (ch1 == '\r' && (((int)this.buffer.bytes[this.pointer] & 0xFF) != '\n'))) {
            this.possibleSimpleKeys.clear();
            this.column = 0;
        } else {
            this.column++;
        }

        // IDEA replaces each \r or \r\n with \n
        offset++;
    }

    private void forward(final int length) {
        ensure(length+1,true);
        int ch = 0;
        for(int i=0;i<length;i++) {
            ch = this.buffer.bytes[this.pointer] & 0xFF;
            this.pointer++;
            if(LINEBR[ch] || (ch == '\r' && (this.buffer.bytes[this.pointer] & 0xFF) != '\n')) {
                this.possibleSimpleKeys.clear();
                this.column = 0;
            } else {
                this.column++;
            }
            // IDEA replaces each \r or \r\n with \n
            offset++;
        }
    }

    public boolean checkToken(final Class[] choices) {
        while(needMoreTokens()) {
            fetchMoreTokens();
        }
        if(!this.tokens.isEmpty()) {
            if(choices.length == 0) {
                return true;
            }
            final Object first = this.tokens.get(0);
            for(int i=0,j=choices.length;i<j;i++) {
                if(choices[i].isInstance(first)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Token peekToken(int index) {
        while(needMoreTokens(index+1)) {
            fetchMoreTokens();
        }
        return (Token)(this.tokens.size() < (index+1) ? null : this.tokens.get(index));
    }

    public Token peekToken() {
        return peekToken(0);
    }

    public Token getToken() {
        while(needMoreTokens()) {
            fetchMoreTokens();
        }
        if(!this.tokens.isEmpty()) {
            this.tokensTaken++;
            return this.tokens.remove(0);
        }
        return null;
    }
    
    private class TokenIterator implements Iterator<Token> {
        public boolean hasNext() {
            return null != peekToken();
        }

        public Token next() {
            return getToken();
        }

        public void remove() {
        }
    }

    public Iterator<Token> eachToken() {
        return new TokenIterator();
    }

    public Iterator<Token> iterator() {
        return eachToken();
    }

    private boolean needMoreTokens() {
        return needMoreTokens(1);
    }

    private boolean needMoreTokens(int size) {
        if(this.done) {
            return false;
        }
        return this.tokens.size() < size || nextPossibleSimpleKey() == this.tokensTaken;
    }

    private boolean isEnding() {
        ensure(4,false);
        return (this.buffer.bytes[this.pointer] & 0xFF) == '-' &&
            (this.buffer.bytes[this.pointer+1] & 0xFF) == '-' &&
            (this.buffer.bytes[this.pointer+2] & 0xFF) == '-' &&
            (this.buffer.bytes[this.pointer+3] != 0) &&
            !(this.buffer.realSize<=(this.pointer+4) || 
              ((this.buffer.bytes[this.pointer+3] == '\n') && 
               (this.buffer.bytes[this.pointer+4] == 0))) &&
            (NULL_BL_T_LINEBR[this.buffer.bytes[this.pointer+3]]);
    }

    private boolean isStart() {
        ensure(4,false);
        return (this.buffer.bytes[this.pointer] & 0xFF) == '.' &&
            (this.buffer.bytes[this.pointer+1] & 0xFF) == '.' &&
            (this.buffer.bytes[this.pointer+2] & 0xFF) == '.' &&
            (NULL_BL_T_LINEBR[this.buffer.bytes[this.pointer+3]]);
    }

    private boolean isEndOrStart() {
        ensure(4,false);
        return (((this.buffer.bytes[this.pointer] & 0xFF) == '-' &&
                 (this.buffer.bytes[this.pointer+1] & 0xFF) == '-' &&
                 (this.buffer.bytes[this.pointer+2] & 0xFF) == '-') ||
                ((this.buffer.bytes[this.pointer] & 0xFF) == '.' &&
                 (this.buffer.bytes[this.pointer+1] & 0xFF) == '.' &&
                 (this.buffer.bytes[this.pointer+2] & 0xFF) == '.')) &&
                 (NULL_BL_T_LINEBR[this.buffer.bytes[this.pointer+3]]);
    }

    private Token fetchMoreTokens() {
        final List<Token> list = scanToNextToken();
        if (!list.isEmpty()){
            tokens.addAll(list);
            return list.get(0);
        }
        unwindIndent(this.column);
        final char ch =  peek();
        final boolean colz = this.column == 0;
        switch(ch) {
        case '\0': return fetchStreamEnd();
        case '\'': return fetchSingle();
        case '"': return fetchDouble();
        case '?': if(this.flowLevel != 0 || NULL_BL_T_LINEBR[peek(1)]) { return fetchKey(); } break;
        case ':': if(this.flowLevel != 0 || NULL_BL_T_LINEBR[peek(1)]) { return fetchValue(); } break;
        case '%': if(colz) {return fetchDirective(); } break;
        case '-': 
            if((colz || docStart) && isEnding()) {
                return fetchDocumentStart(); 
            } else if(NULL_BL_T_LINEBR[peek(1)]) {
                return fetchBlockEntry(); 
            }
            break;
        case '.': 
            if(colz && isStart()) {
                return fetchDocumentEnd(); 
            }
            break;
        case '[': return fetchFlowSequenceStart();
        case '{': return fetchFlowMappingStart();
        case ']': return fetchFlowSequenceEnd();
        case '}': return fetchFlowMappingEnd();
        case ',': if(this.flowLevel != 0) {
                return fetchFlowEntry();
            }
            break;
        case '*': return fetchAlias();
        case '&': return fetchAnchor();
        case '!': return fetchTag();
        case '|': if(this.flowLevel == 0 && CHOMPING[peek(1)]) { return fetchLiteral(); } break;
        case '>': if(this.flowLevel == 0 && CHOMPING[peek(1)]) { return fetchFolded(); } break;
        }

        //TODO: this is probably incorrect...
        if(STUPID_CHAR[this.buffer.bytes[this.pointer]&0xFF] || 
           (ensure(1,false) && (this.buffer.bytes[this.pointer] == '-' || 
                                this.buffer.bytes[this.pointer] == '?' || 
                                this.buffer.bytes[this.pointer] == ':') && 
            !NULL_BL_T_LINEBR[this.buffer.bytes[this.pointer+1]&0xFF])) {
            return fetchPlain();
        }

        throw new ScannerException("while scanning for the next token","found character " + ch + "(" + (int)ch + ") that cannot start any token",null);
    }

    private Token fetchStreamStart() {
        this.docStart = true;
        final StreamStartToken stream_start = new StreamStartToken(offset, offset);
        this.tokens.add(stream_start);
        return stream_start;
    }

    private Token fetchStreamEnd() {
        unwindIndent(-1);
        this.allowSimpleKey = false;
        this.possibleSimpleKeys = new HashMap<Integer, SimpleKey>();
        final StreamEndToken stream_end = new StreamEndToken(offset, offset);
        this.tokens.add(stream_end);
        this.done = true;
        this.docStart = false;
        return stream_end;
    }

    private List<Token> scanToNextToken() {
        final ArrayList<Token> list = new ArrayList<Token>();
        for(;;) {
            char ch;
            if ((ch=peek()) == ' ' || ch=='\t'){
                last_offset = offset;
                while((ch=peek()) == ' ' || ch=='\t') {
                    forward();
                }
                list.add(new WhiteSpaceToken(last_offset, offset));
            }
            if(ch == '#') {
                last_offset = offset;
                forward();
                while(!NULL_OR_LINEBR[peek()]) {
                    forward();
                }
                list.add(new CommentToken(last_offset, offset));
            }
            last_offset = offset;
            if(scanLineBreak().length != 0 ) {
                if(this.flowLevel == 0) {
                    this.allowSimpleKey = true;
                }
                list.add(new WhiteSpaceToken(last_offset, offset));
            } else {
                break;
            }
        }
        return list;
    }

    
    private byte[] scanLineBreak() {
        // Transforms:
        //   '\r\n'      :   '\n'
        //   '\r'        :   '\n'
        //   '\n'        :   '\n'
        //   '\x85'      :   '\n'
        //   default     :   ''
        final int val = peek();
        if(FULL_LINEBR[val]) {
            ensure(2,false);
            if(RN[0] == buffer.bytes[this.pointer] && RN[1] == buffer.bytes[this.pointer+1]) {
                forward(2);
            } else {
                forward();
            }
            return NN;
        } else {
            return EMPTY;
        }
    }

    private void unwindIndent(final int col) {
        if(this.flowLevel != 0) {
            return;
        }

        while(this.indent > col) {
            this.indent = this.indents.remove(0);
            this.tokens.add(new BlockEndToken(offset, offset));
        }
    }
    
    private Token fetchDocumentStart() {
        this.docStart = false;
        return fetchDocumentIndicator(new DocumentStartToken(offset, offset));
    }

    private Token fetchDocumentIndicator(final Token tok) {
        unwindIndent(-1);
        removePossibleSimpleKey();
        this.allowSimpleKey = false;
        forward(3);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchBlockEntry() {
        this.docStart = false;
        if(this.flowLevel == 0) {
            if(!this.allowSimpleKey) {
                throw new ScannerException(null,"sequence entries are not allowed here",null);
            }
            if(addIndent(this.column)) {
                this.tokens.add(new BlockSequenceStartToken(offset, offset));
            }
        }
        this.allowSimpleKey = true;
        removePossibleSimpleKey();
        forward();
        final BlockEntryToken block_entry = new BlockEntryToken(offset, offset);
        this.tokens.add(block_entry);
        return block_entry;
    }        

    private boolean addIndent(final int col) {
        if(this.indent < col) {
            this.indents.add(0, this.indent);
            this.indent = col;
            return true;
        }
        return false;
    }

    private Token fetchTag() {
        this.docStart = false;
        savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanTag();
        this.tokens.add(tok);
        return tok;
    }
    
    private void removePossibleSimpleKey() {
        SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
        if(key != null) {
            if(key.isRequired()) {
                throw new ScannerException("while scanning a simple key","could not find expected ':'",null);
            }
        }
    }

    private void savePossibleSimpleKey() {
        if(this.allowSimpleKey) {
            this.removePossibleSimpleKey();
            this.possibleSimpleKeys.put(this.flowLevel,new SimpleKey(this.tokensTaken+this.tokens.size(),(this.flowLevel == 0) && this.indent == this.column,-1,-1,this.column));
        }
    }
    
    private Token scanTag() {
        last_offset = offset;
        char ch = peek(1);
        ByteList handle = null;
        ByteList suffix = null;
        if(ch == '<') {
            forward(2);
            suffix = scanTagUri("tag");
            if(peek() != '>') {
                throw new ScannerException("while scanning a tag","expected '>', but found "+ peek() + "(" + (int)peek() + ")",null);
            }
            forward();
        } else if(NULL_BL_T_LINEBR[ch]) {
            suffix = BANG;
            forward();
        } else {
            int length = 1;
            boolean useHandle = false;
            while(!NULL_BL_T_LINEBR[ch]) {
                if(ch == '!') {
                    useHandle = true;
                    break;
                }
                length++;
                ch = peek(length);
            }
            handle = BANG;
            if(useHandle) {
                handle = scanTagHandle("tag");
            } else {
                handle = BANG;
                forward();
            }
            suffix = scanTagUri("tag");
        }
        if(!NULL_BL_LINEBR[peek()]) {
            throw new ScannerException("while scanning a tag","expected ' ', but found " + peek() + "(" + (int)peek() + ")",null);
        }
        return new TagToken(new ByteList[] {handle,suffix}, last_offset, offset);
    }

    private ByteList scanTagUri(final String name) {
        final ByteList chunks = new ByteList(10);
        int length = 0;
        char ch = peek(length);
        while(STRANGE_CHAR[ch]) {
            if('%' == ch) {
                ensure(length,false);
                chunks.append(this.buffer.bytes,this.pointer,length);
                length = 0;
                chunks.append(scanUriEscapes(name));
            } else {
                length++;
            }
            ch = peek(length);
        }
        if(length != 0) {
            ensure(length,false);
            chunks.append(this.buffer.bytes,this.pointer,length);
            forward(length);
        }
        if(chunks.length() == 0) {
            throw new ScannerException("while scanning a " + name,"expected URI, but found " + ch + "(" + (int)ch + ")",null);
        }
        return chunks;
    }

    private ByteList scanTagHandle(final String name) {
        char ch =  peek();
        if(ch != '!') {
            throw new ScannerException("while scanning a " + name,"expected '!', but found " + ch + "(" + (int)ch + ")",null);
        }
        int length = 1;
        ch = peek(length);
        if(ch != ' ') {
            while(ALPHA[ch]) {
                length++;
                ch = peek(length);
            }
            if('!' != ch) {
                forward(length);
                throw new ScannerException("while scanning a " + name,"expected '!', but found " + ch + "(" + ((int)ch) + ")",null);
            }
            length++;
        }
        ensure(length,false);
        final ByteList value = new ByteList(this.buffer.bytes,this.pointer,length,false);
        forward(length);
        return value;
    }

    private ByteList scanUriEscapes(final String name) {
        final ByteList bytes = new ByteList();
        while(peek() == '%') {
            forward();
            try {
                ensure(2,false);
                bytes.append(Integer.parseInt(new String(ByteList.plain(this.buffer.bytes,this.pointer,2)),16));
            } catch(final NumberFormatException nfe) {
                throw new ScannerException("while scanning a " + name,"expected URI escape sequence of 2 hexadecimal numbers, but found " + peek(1) + "(" + ((int)peek(1)) + ") and "+ peek(2) + "(" + ((int)peek(2)) + ")",null);
            }
            forward(2);
        }
        return bytes;
    }

    private Token fetchPlain() {
        this.docStart = false;
        savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanPlain();
        this.tokens.add(tok);
        return tok;
    }
   
    private Token scanPlain() {
        last_offset = offset;
        final ByteList chunks = new ByteList(7);
        final int ind = this.indent+1;
        ByteList spaces = new ByteList(0);
        boolean f_nzero = true;
        boolean[] r_check = R_FLOWNONZERO;
        boolean[] r_check2 = ALL_FALSE;
        boolean[] r_check3 = ALL_FALSE;
        if(this.flowLevel == 0) {
            f_nzero = false;
            r_check = R_FLOWZERO;
            r_check2 = R_FLOWZERO1;
            r_check3 = R_FLOWZERO;
        }
        while(peek() != '#') {
            int length = 0;
            int i = 0;
            for(;;i++) {
                ensure(i+2,false);
                if(r_check[this.buffer.bytes[this.pointer+i]&0xFF] || (r_check2[this.buffer.bytes[this.pointer+i]&0xFF] && r_check3[this.buffer.bytes[this.pointer+i+1]&0xFF])) {
                    length = i;
                    final char ch = peek(length);
                    if(!(f_nzero && ch == ':' && !S4[peek(length+1)])) {
                        break;
                    }
                }
            }

            if(length == 0) {
                break;
            }
            this.allowSimpleKey = false;
            chunks.append(spaces);
            ensure(length,false);
            chunks.append(this.buffer.bytes,this.pointer,length);
            forward(length);
            spaces = scanPlainSpaces(ind);
            if(spaces == null || (this.flowLevel == 0 && this.column < ind)) {
                break;
            }
        }
        return new ScalarToken(chunks,true, last_offset, offset);
    }

    private int nextPossibleSimpleKey() {
        for(final Iterator<SimpleKey> iter = this.possibleSimpleKeys.values().iterator();iter.hasNext();) {
            final SimpleKey key = iter.next();
            if(key.getTokenNumber() > 0) {
                return key.getTokenNumber();
            }
        }
        return -1;
    }

    private ByteList scanPlainSpaces(final int indent) {
        final ByteList chunks = new ByteList();
        int length = 0;
        while(peek(length) == ' ') {
            length++;
        }
        final byte[] whitespaces = new byte[length];
        Arrays.fill(whitespaces,(byte)' ');
        forward(length);
        char ch  = peek();
        if(FULL_LINEBR[ch]) {
            final byte[] lineBreak = scanLineBreak();
            this.allowSimpleKey = true;
            if(isEndOrStart()) {
                return new ByteList(0);
            }
            final ByteList breaks = new ByteList();
            while(BLANK_OR_LINEBR[peek()]) {
                if(' ' == peek()) {
                    forward();
                } else {
                    breaks.append(scanLineBreak());
                    if(isEndOrStart()) {
                        return new ByteList(0);
                    }
                }
            }            
            if(!(lineBreak.length == 1 && lineBreak[0] == '\n')) {
                chunks.append(lineBreak);
            } else if(breaks == null || breaks.realSize == 0) {
                chunks.append(SPACE);
            }
            chunks.append(breaks);
        } else {
            chunks.append(whitespaces);
        }
        return chunks;
    }

    private Token fetchSingle() {
        return fetchFlowScalar('\'');
    }
    
    private Token fetchDouble() {
        return fetchFlowScalar('"');
    }
    
    private Token fetchFlowScalar(final char style) {
        this.docStart = false;
        savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanFlowScalar(style);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token scanFlowScalar(final char style) {
        last_offset = offset;
        final boolean dbl = style == '"';
        final ByteList chunks = new ByteList();
        final char quote = peek();
        forward();
        chunks.append(scanFlowScalarNonSpaces(dbl));
        while(peek() != quote) {
            chunks.append(scanFlowScalarSpaces());
            chunks.append(scanFlowScalarNonSpaces(dbl));
        }
        forward();
        return new FlowScalarToken(chunks,false,style, last_offset, offset);
    }

    private final static byte[] HEXA_VALUES = new byte[256];
    static {
        Arrays.fill(HEXA_VALUES,(byte)-1);
        HEXA_VALUES['0'] = 0;
        HEXA_VALUES['1'] = 1;
        HEXA_VALUES['2'] = 2;
        HEXA_VALUES['3'] = 3;
        HEXA_VALUES['4'] = 4;
        HEXA_VALUES['5'] = 5;
        HEXA_VALUES['6'] = 6;
        HEXA_VALUES['7'] = 7;
        HEXA_VALUES['8'] = 8;
        HEXA_VALUES['9'] = 9;
        HEXA_VALUES['A'] = 10; 
        HEXA_VALUES['B'] = 11;
        HEXA_VALUES['C'] = 12;
        HEXA_VALUES['D'] = 13;
        HEXA_VALUES['E'] = 14;
        HEXA_VALUES['F'] = 15;
        HEXA_VALUES['a'] = 10; 
        HEXA_VALUES['b'] = 11;
        HEXA_VALUES['c'] = 12;
        HEXA_VALUES['d'] = 13;
        HEXA_VALUES['e'] = 14;
        HEXA_VALUES['f'] = 15;
   }

    private ByteList parseHexa(int length) {
        ensure(length,false);
        ByteList chunks = new ByteList(length/2);
        for(int i=0;i<length;i+=2) {
            byte val = HEXA_VALUES[this.buffer.bytes[this.pointer+i] & 0xFF];
            if(val == -1) {
                throw new ScannerException("while scanning a double-quoted scalar","expected escape sequence of " + length + " hexadecimal numbers, but found something else: " + (char)(this.buffer.bytes[this.pointer+i] & 0xFF),null);
            }
            if(i+1 < length) {
                val = (byte)(val << 4);
                byte v2 = HEXA_VALUES[this.buffer.bytes[this.pointer+i+1] & 0xFF];
                if(v2 == -1) {
                    throw new ScannerException("while scanning a double-quoted scalar","expected escape sequence of " + length + " hexadecimal numbers, but found something else: " + (char)(this.buffer.bytes[this.pointer+i+1] & 0xFF),null);
                }
                val+=v2;
            }
            chunks.append(val);
        }
        forward(length);
        return chunks;
    }

    private ByteList scanFlowScalarNonSpaces(final boolean dbl) {
        final ByteList chunks = new ByteList();
        for(;;) {
            int length = 0;
            while(!SPACES_AND_STUFF[peek(length)]) {
                length++;
            }
            if(length != 0) {
                ensure(length,false);
                chunks.append(this.buffer.bytes,this.pointer,length);
                forward(length);
            }
            char ch = peek();
            if(!dbl && ch == '\'' && peek(1) == '\'') {
                chunks.append('\'');
                forward(2);
            } else if((dbl && ch == '\'') || (!dbl && DOUBLE_ESC[ch])) {
                chunks.append(ch);
                forward();
            } else if(dbl && ch == '\\') {
                forward();
                ch = peek();
                if(IS_ESCAPE_REPLACEMENT[ch]) {
                    chunks.append(ESCAPE_REPLACEMENTS[ch]);
                    forward();
                } else if(ESCAPE_CODES.containsKey(ch)) {
                    length = ESCAPE_CODES.get(ch);
                    forward();
                    chunks.append(parseHexa(length));
                } else if(FULL_LINEBR[ch]) {
                    scanLineBreak();
                    ByteList ss = scanFlowScalarBreaks();
                    chunks.append(ss);
                } else {
                    chunks.append('\\');
                }
            } else {
                return chunks;
            }
        }
    }

    private ByteList scanFlowScalarSpaces() {
        final ByteList chunks = new ByteList();
        int length = 0;
        while(BLANK_T[peek(length)]) {
            length++;
        }
        ensure(length,false);
        ByteList whitespaces = new ByteList(this.buffer,this.pointer,length);
        forward(length);
        char ch = peek();
        if(ch == '\0') {
            throw new ScannerException("while scanning a quoted scalar","found unexpected end of stream",null);
        } else if(FULL_LINEBR[ch]) {
            final byte[] lineBreak = scanLineBreak();
            final ByteList breaks = scanFlowScalarBreaks();
            if(!(lineBreak.length == 1 && lineBreak[0] == '\n')) {
                chunks.append(lineBreak);
            } else if(breaks.length() == 0) {
                chunks.append(SPACE);
            }
            chunks.append(breaks);
        } else {
            chunks.append(whitespaces);
        }
        return chunks;
    }

    private ByteList scanFlowScalarBreaks() {
        final ByteList chunks = new ByteList();
        boolean colz=true;
        for(;;) {
            if(colz && isEndOrStart()) {
                throw new ScannerException("while scanning a quoted scalar","found unexpected document separator",null);
            }
            while(BLANK_T[peek()]) {
                forward();
            }
            if(FULL_LINEBR[peek()]) {
                chunks.append(scanLineBreak());
                colz = true;
            } else if('\\' == peek() && BLANK_T[peek(1)]) {
                forward();
                chunks.append(scanFlowScalarSpaces());
                colz = false;
            } else {
                return chunks;
            }            
        }
    }

    private Token fetchValue() {
        last_offset = offset;
        this.docStart = false;
        final SimpleKey key = this.possibleSimpleKeys.get(this.flowLevel);
        if(null == key) {
            if(this.flowLevel == 0 && !this.allowSimpleKey) {
                throw new ScannerException(null,"mapping values are not allowed here",null);
            }
            this.allowSimpleKey = this.flowLevel == 0;
            removePossibleSimpleKey();
        } else {
            this.possibleSimpleKeys.remove(this.flowLevel);
            this.tokens.add(key.getTokenNumber()-this.tokensTaken,new KeyToken(offset, offset));
            if(this.flowLevel == 0 && addIndent(key.getColumn())) {
                this.tokens.add(key.getTokenNumber()-this.tokensTaken,new BlockMappingStartToken(offset, offset));
            }
            this.allowSimpleKey = false;
        }
        forward();
        final ValueToken value_token = new ValueToken(last_offset, offset);
        this.tokens.add(value_token);
        return value_token;
    }

    private Token fetchFlowSequenceStart() {
        return fetchFlowCollectionStart(new FlowSequenceStartToken(offset, offset));
    }

    private Token fetchFlowMappingStart() {
        return fetchFlowCollectionStart(new FlowMappingStartToken(offset, offset));
    }

    private Token fetchFlowCollectionStart(final Token tok) {
        this.docStart = false;
        savePossibleSimpleKey();
        this.flowLevel++;
        this.allowSimpleKey = true;
        forward(1);
        this.tokens.add(tok);
        return tok;
    }

    private Token fetchDocumentEnd() {
        return fetchDocumentIndicator(new DocumentEndToken(offset, offset));
    }

    private Token fetchFlowSequenceEnd() {
        return fetchFlowCollectionEnd(new FlowSequenceEndToken(offset, offset));
    }
    
    private Token fetchFlowMappingEnd() {
        return fetchFlowCollectionEnd(new FlowMappingEndToken(offset, offset));
    }
    
    private Token fetchFlowCollectionEnd(final Token tok) {
        removePossibleSimpleKey();
        this.flowLevel--;
        this.allowSimpleKey = false;
        forward(1);
        this.tokens.add(tok);
        return tok;
    }
    
    private Token fetchFlowEntry() {
        this.allowSimpleKey = true;
        removePossibleSimpleKey();
        forward(1);
        final FlowEntryToken flow_entry_token = new FlowEntryToken(offset, offset);
        this.tokens.add(flow_entry_token);
        return flow_entry_token;
    }

    private Token fetchLiteral() {
        return fetchBlockScalar('|');
    }
    
    private Token fetchFolded() {
        return fetchBlockScalar('>');
    }
    
    private Token fetchBlockScalar(final char style) {
        this.docStart = false;
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        final Token tok = scanBlockScalar(style);
        this.tokens.add(tok);
        return tok;
    }

    private Token scanBlockScalar(final char style) {
        last_offset = offset;
        final boolean folded = style == '>';
        final ByteList chunks = new ByteList();
        forward();
        final Object[] chompi = scanBlockScalarIndicators();
        final Boolean chomping = (Boolean)chompi[0];
        final int increment = (Integer) chompi[1];

        boolean sameLine = scanBlockScalarIgnoredLine();

        int minIndent = this.indent+1;
        if(minIndent < 0) {
            minIndent = 0;
        }
        ByteList breaks = null;
        int maxIndent = 0;
        int ind = 0;
        if(sameLine) {
            final boolean leadingNonSpace = !BLANK_T[peek()];
            int length = 0;
            while(!NULL_OR_LINEBR[peek(length)]) {
                length++;
            }
            ensure(length,false);
            chunks.append(this.buffer.bytes,this.pointer,length);
            forward(length);
        }
        if(increment == -1) {
            final Object[] brme = scanBlockScalarIndentation();
            breaks = (ByteList)brme[0];
            maxIndent = (Integer) brme[1];
            if(minIndent > maxIndent) {
                ind = minIndent;
            } else {
                ind = maxIndent;
            }
        } else {
            ind = minIndent + increment - 1;
            breaks = scanBlockScalarBreaks(ind);
        }

        byte[] lineBreak = ByteList.NULL_ARRAY;
        while(this.column == ind && peek() != '\0') {
            chunks.append(breaks);
            final boolean leadingNonSpace = !BLANK_T[peek()];
            int length = 0;
            while(!NULL_OR_LINEBR[peek(length)]) {
                length++;
            }
            ensure(length,false);
            chunks.append(this.buffer.bytes,this.pointer,length);
            forward(length);
            lineBreak = scanLineBreak();
            breaks = scanBlockScalarBreaks(ind);
            if(this.column == ind && peek() != '\0') {
                if(folded && lineBreak.length == 1 && lineBreak[0] == '\n' && leadingNonSpace && !BLANK_T[peek()]) {
                    if(breaks.length() == 0) {
                        chunks.append(SPACE);
                    }
                } else {
                    chunks.append(lineBreak);
                }
            } else {
                break;
            }
        }

        if(chomping != Boolean.FALSE) {
            chunks.append(lineBreak);
        }
        if(chomping == Boolean.TRUE) {
            chunks.append(breaks);
        }

        return new BlockScalarToken(chunks,false,style, last_offset, offset);
    }

    private ByteList scanBlockScalarBreaks(final int indent) {
        final ByteList chunks = new ByteList();
        while(this.column < indent && peek() == ' ') {
            forward();
        }
        while(FULL_LINEBR[peek()]) {
            chunks.append(scanLineBreak());
            while(this.column < indent && peek() == ' ') {
                forward();
            }
        }
        return chunks;
    }
    

    private Object[] scanBlockScalarIndentation() {
        final ByteList chunks = new ByteList();
        int maxIndent = 0;
        while(BLANK_OR_LINEBR[peek()]) {
            if(peek() != ' ') {
                chunks.append(scanLineBreak());
            } else {
                forward();
                if(this.column > maxIndent) {
                    maxIndent = column;
                }
            }
        }
        return new Object[] {chunks, maxIndent};
    }


    private Object[] scanBlockScalarIndicators() {
        Boolean chomping = null;
        int increment = -1;
        char ch = peek();
        if(ch == '-' || ch == '+') {
            chomping = ch == '+' ? Boolean.TRUE : Boolean.FALSE;
            forward();
            ch = peek();
            if(DIGIT[ch]) {
                increment = ch-'0';
                if(increment == 0) {
                    throw new ScannerException("while scanning a block scalar","expected indentation indicator in the range 1-9, but found 0",null);
                }
                forward();
            }
        } else if(DIGIT[ch]) {
            increment = ch-'0';
            if(increment == 0) {
                throw new ScannerException("while scanning a block scalar","expected indentation indicator in the range 1-9, but found 0",null);
            }
            forward();
            ch = peek();
            if(ch == '-' || ch == '+') {
                chomping = ch == '+' ? Boolean.TRUE : Boolean.FALSE;
                forward();
            }
        }
        if(!NULL_BL_LINEBR[peek()]) {
            throw new ScannerException("while scanning a block scalar","expected chomping or indentation indicators, but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        return new Object[] {chomping, increment};
    }

    private boolean scanBlockScalarIgnoredLine() {
        boolean same = true;
        while(peek() == ' ') {
            forward();
        }
        if(peek() == '#') {
            while(!NULL_OR_LINEBR[peek()]) {
                forward();
            }
            same = false;
        }
        if(NULL_OR_LINEBR[peek()]) {
            scanLineBreak();
            return false;
        }
        return same;
    }

    private Token fetchDirective() {
        unwindIndent(-1);
        removePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanDirective();
        this.tokens.add(tok);
        return tok;
    }

    private Token fetchKey() {
        if(this.flowLevel == 0) {
            if(!this.allowSimpleKey) {
                throw new ScannerException(null,"mapping keys are not allowed here",null);
            }
            if(addIndent(this.column)) {
                this.tokens.add(new BlockMappingStartToken(offset, offset));
            }
        }
        this.allowSimpleKey = this.flowLevel == 0;
        removePossibleSimpleKey();
        forward();
        final KeyToken key_token = new KeyToken(offset, offset);
        this.tokens.add(key_token);
        return key_token;
    }

    private Token fetchAlias() {
        savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanAnchor(new AliasToken(offset, offset));
        this.tokens.add(tok);
        return tok;
    }

    private Token fetchAnchor() {
        savePossibleSimpleKey();
        this.allowSimpleKey = false;
        final Token tok = scanAnchor(new AnchorToken(offset, offset));
        this.tokens.add(tok);
        return tok;
    }

    private Token scanDirective() {
        last_offset = offset;
        forward();
        final String name = scanDirectiveName();
        String[] value = null;
        if(name.equals("YAML")) {
            value = scanYamlDirectiveValue();
        } else if(name.equals("TAG")) {
            value = scanTagDirectiveValue();
        } else {
            while(!NULL_OR_LINEBR[peek()]) {
                forward();
            }
        }
        scanDirectiveIgnoredLine();
        return new DirectiveToken(name,value, last_offset, offset);
    }

    private String scanDirectiveName() {
        int length = 0;
        char ch = peek(length);
        boolean zlen = true;
        while(ALPHA[ch]) {
            zlen = false;
            length++;
            ch = peek(length);
        }
        if(zlen) {
            throw new ScannerException("while scanning a directive","expected alphabetic or numeric character, but found " + ch + "(" + ((int)ch) + ")",null);
        }
        String value = null;
        try {
            ensure(length,false);
            value = new String(this.buffer.bytes,this.pointer,length,"ISO8859-1");
        } catch(Exception e) {
        }
        forward(length);
        if(!NULL_BL_LINEBR[peek()]) {
            throw new ScannerException("while scanning a directive","expected alphabetic or numeric character, but found " + ch + "(" + ((int)ch) + ")",null);
        }
        return value;
    }

    private byte[] scanDirectiveIgnoredLine() {
        while(peek() == ' ') {
            forward();
        }
        if(peek() == '"') {
            while(!NULL_OR_LINEBR[peek()]) {
                forward();
            }
        }
        final char ch = peek();
        if(!NULL_OR_LINEBR[ch]) {
            throw new ScannerException("while scanning a directive","expected a comment or a line break, but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        return scanLineBreak();
    }

    private Token scanAnchor(final Token tok) {
        final char indicator = peek();
        final String name = indicator == '*' ? "alias" : "anchor";
        forward();
        int length = 0;
        while(ALPHA[peek(length)]) {
            length++;
        }
        if(length == 0) {
            throw new ScannerException("while scanning an " + name,"expected alphabetic or numeric character, but found something else...",null);
        }
        String value = null;
        try {
            ensure(length,false);
            value = new String(this.buffer.bytes,this.pointer,length,"ISO8859-1");
        } catch(Exception e) {
        }
        forward(length);
        if(!NON_ALPHA_OR_NUM[peek()]) {
            throw new ScannerException("while scanning an " + name,"expected alphabetic or numeric character, but found "+ peek() + "(" + ((int)peek()) + ")",null);

        }
        tok.setValue(value);
        return tok;
    }

    private String[] scanYamlDirectiveValue() {
        while(peek() == ' ') {
            forward();
        }
        final String major = scanYamlDirectiveNumber();
        if(peek() != '.') {
            throw new ScannerException("while scanning a directive","expected a digit or '.', but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        forward();
        final String minor = scanYamlDirectiveNumber();
        if(!NULL_BL_LINEBR[peek()]) {
            throw new ScannerException("while scanning a directive","expected a digit or ' ', but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        return new String[] {major,minor};
    }

    private String scanYamlDirectiveNumber() {
        final char ch = peek();
        if(!Character.isDigit(ch)) {
            throw new ScannerException("while scanning a directive","expected a digit, but found " + ch + "(" + ((int)ch) + ")",null);
        }
        int length = 0;
        StringBuffer sb = new StringBuffer();
        while(Character.isDigit(peek(length))) {
            sb.append(peek(length));
            length++;
        }
        forward(length);
        return sb.toString();
    }

    public static String into(ByteList b) {
        try {
            return new String(b.bytes,0,b.realSize,"ISO8859-1");
        } catch(Exception e) {
            return null; // Shouldn't happen
        }
    }

    private String[] scanTagDirectiveValue()  {
        while(peek() == ' ') {
            forward();
        }
        final String handle = into(scanTagDirectiveHandle());
        while(peek() == ' ') {
            forward();
        }
        final String prefix = into(scanTagDirectivePrefix());
        return new String[] {handle,prefix};
    }

    private ByteList scanTagDirectiveHandle() {
        final ByteList value = scanTagHandle("directive");
        if(peek() != ' ') {
            throw new ScannerException("while scanning a directive","expected ' ', but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        return value;
    }
    
    private ByteList scanTagDirectivePrefix() {
        final ByteList value = scanTagUri("directive");
        if(!NULL_BL_LINEBR[peek()]) {
            throw new ScannerException("while scanning a directive","expected ' ', but found " + peek() + "(" + ((int)peek()) + ")",null);
        }
        return value;
    }
}// Scanner