/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2024 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */
package com.github.javaparser;

import static com.github.javaparser.utils.CodeGenerationUtils.f;
import static com.github.javaparser.utils.Utils.assertNotNull;

import com.github.javaparser.ast.Generated;
import com.github.javaparser.utils.LineSeparator;
import java.util.List;
import java.util.Optional;

/**
 * A token from a parsed source file.
 * (Awkwardly named "Java"Token since JavaCC already generates an internal class Token.)
 * It is a node in a double linked list called token list.
 */
public class JavaToken {

    public static final JavaToken INVALID = new JavaToken();

    private Range range;

    private int kind;

    private String text;

    private JavaToken previousToken = null;

    private JavaToken nextToken = null;

    private JavaToken() {
        this(null, 0, "INVALID", null, null);
    }

    public JavaToken(int kind, String text) {
        this(null, kind, text, null, null);
    }

    JavaToken(Token token, List<JavaToken> tokens) {
        // You could be puzzled by the following lines
        //
        // The reason why these lines are necessary is the fact that Java is ambiguous. There are cases where the
        // sequence of characters ">>>" and ">>" should be recognized as the single tokens ">>>" and ">>". In other
        // cases however we want to split those characters in single GT tokens (">").
        //
        // For example, in expressions ">>" and ">>>" are valid, while when defining types we could have this:
        //
        // List<List<Set<String>>>>
        //
        // You can see that the sequence ">>>>" should be interpreted as four consecutive ">" tokens closing a type
        // parameter list.
        //
        // The JavaCC handle this case by first recognizing always the longest token, and then depending on the context
        // putting back the unused chars in the stream. However in those cases the token provided is invalid: it has an
        // image corresponding to the text originally recognized, without considering that after some characters could
        // have been put back into the stream.
        //
        // So in the case of:
        //
        // List<List<Set<String>>>>
        // ___   -> recognized as ">>>", then ">>" put back in the stream but Token(type=GT, image=">>>") passed to this
        // class
        // ___  -> recognized as ">>>", then ">>" put back in the stream but Token(type=GT, image=">>>") passed to this
        // class
        // __  -> recognized as ">>", then ">" put back in the stream but Token(type=GT, image=">>") passed to this
        // class
        // _  -> Token(type=GT, image=">") good!
        //
        // So given the image could be wrong but the type is correct, we look at the type of the token and we fix
        // the image. Everybody is happy and we can keep this horrible thing as our little secret.
        Range range = Range.range(token.beginLine, token.beginColumn, token.endLine, token.endColumn);
        String text = token.image;
        if (token.kind == GeneratedJavaParserConstants.GT) {
            range = Range.range(token.beginLine, token.beginColumn, token.endLine, token.beginColumn);
            text = ">";
        } else if (token.kind == GeneratedJavaParserConstants.RSIGNEDSHIFT) {
            range = Range.range(token.beginLine, token.beginColumn, token.endLine, token.beginColumn + 1);
            text = ">>";
        }
        this.range = range;
        this.kind = token.kind;
        this.text = text;
        if (!tokens.isEmpty()) {
            final JavaToken previousToken = tokens.get(tokens.size() - 1);
            this.previousToken = previousToken;
            previousToken.nextToken = this;
        } else {
            previousToken = null;
        }
    }

    /**
     * Create a token of a certain kind.
     */
    public JavaToken(int kind) {
        String content = GeneratedJavaParserConstants.tokenImage[kind];
        if (content.startsWith("\"")) {
            content = content.substring(1, content.length() - 1);
        }
        if (TokenTypes.isEndOfLineToken(kind)) {
            content = LineSeparator.SYSTEM.asRawString();
        } else if (TokenTypes.isWhitespace(kind)) {
            content = " ";
        }
        this.kind = kind;
        this.text = content;
    }

    public JavaToken(Range range, int kind, String text, JavaToken previousToken, JavaToken nextToken) {
        assertNotNull(text);
        this.range = range;
        this.kind = kind;
        this.text = text;
        this.previousToken = previousToken;
        this.nextToken = nextToken;
    }

    public Optional<Range> getRange() {
        return Optional.ofNullable(range);
    }

    /*
     * Returns true if the token has a range
     */
    public boolean hasRange() {
        return getRange().isPresent();
    }

    public int getKind() {
        return kind;
    }

    void setKind(int kind) {
        this.kind = kind;
    }

    public String getText() {
        return text;
    }

    public Optional<JavaToken> getNextToken() {
        return Optional.ofNullable(nextToken);
    }

    public Optional<JavaToken> getPreviousToken() {
        return Optional.ofNullable(previousToken);
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String asString() {
        return text;
    }

    /**
     * @return the token range that goes from the beginning to the end of the token list this token is a part of.
     */
    public TokenRange toTokenRange() {
        return new TokenRange(findFirstToken(), findLastToken());
    }

    @Override
    public String toString() {
        String text = getText()
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\r\n", "\\r\\n")
                .replace("\t", "\\t");
        return f(
                "\"%s\"   <%s>   %s",
                text, getKind(), getRange().map(Range::toString).orElse("(?)-(?)"));
    }

    /**
     * Used by the parser while constructing nodes. No tokens should be invalid when the parser is done.
     */
    public boolean valid() {
        return !invalid();
    }

    /**
     * Used by the parser while constructing nodes. No tokens should be invalid when the parser is done.
     */
    public boolean invalid() {
        return this == INVALID;
    }

    public enum Category {
        WHITESPACE_NO_EOL,
        EOL,
        COMMENT,
        IDENTIFIER,
        KEYWORD,
        LITERAL,
        SEPARATOR,
        OPERATOR;

        public boolean isWhitespaceOrComment() {
            return isWhitespace() || this == COMMENT;
        }

        public boolean isWhitespace() {
            return this == WHITESPACE_NO_EOL || this == EOL;
        }

        public boolean isEndOfLine() {
            return this == EOL;
        }

        public boolean isComment() {
            return this == COMMENT;
        }

        public boolean isWhitespaceButNotEndOfLine() {
            return this == WHITESPACE_NO_EOL;
        }

        public boolean isIdentifier() {
            return this == IDENTIFIER;
        }

        public boolean isKeyword() {
            return this == KEYWORD;
        }

        public boolean isLiteral() {
            return this == LITERAL;
        }

        public boolean isSeparator() {
            return this == SEPARATOR;
        }

        public boolean isOperator() {
            return this == OPERATOR;
        }
    }

    @Generated("com.github.javaparser.generator.core.other.TokenKindGenerator")
    public enum Kind {
        EOF(0),
        SPACE(1),
        WINDOWS_EOL(2),
        UNIX_EOL(3),
        OLD_MAC_EOL(4),
        JML_LINE_COMMENT(5),
        SINGLE_LINE_COMMENT(6),
        JML_ENTER_MULTILINE_COMMENT(7),
        ENTER_JAVADOC_COMMENT(8),
        ENTER_JML_BLOCK_COMMENT(9),
        ENTER_MULTILINE_COMMENT(10),
        JML_BLOCK_COMMENT(11),
        JAVADOC_COMMENT(12),
        MULTI_LINE_COMMENT(13),
        COMMENT_CONTENT(14),
        ABSTRACT(15),
        ASSERT(16),
        BOOLEAN(17),
        BREAK(18),
        BYTE(19),
        CASE(20),
        CATCH(21),
        CHAR(22),
        CLASS(23),
        CONST(24),
        CONTINUE(25),
        _DEFAULT(26),
        DO(27),
        DOUBLE(28),
        ELSE(29),
        ENUM(30),
        EXTENDS(31),
        FALSE(32),
        FINAL(33),
        FINALLY(34),
        FLOAT(35),
        FOR(36),
        GOTO(37),
        IF(38),
        IMPLEMENTS(39),
        IMPORT(40),
        INSTANCEOF(41),
        INT(42),
        INTERFACE(43),
        LONG(44),
        NATIVE(45),
        NEW(46),
        NON_SEALED(47),
        NULL(48),
        PACKAGE(49),
        PERMITS(50),
        PRIVATE(51),
        PROTECTED(52),
        PUBLIC(53),
        RECORD(54),
        RETURN(55),
        SEALED(56),
        SHORT(57),
        STATIC(58),
        STRICTFP(59),
        SUPER(60),
        SWITCH(61),
        SYNCHRONIZED(62),
        THIS(63),
        THROW(64),
        THROWS(65),
        TRANSIENT(66),
        TRUE(67),
        TRY(68),
        VOID(69),
        VOLATILE(70),
        WHILE(71),
        YIELD(72),
        REQUIRES(73),
        TO(74),
        WITH(75),
        OPEN(76),
        OPENS(77),
        USES(78),
        MODULE(79),
        EXPORTS(80),
        PROVIDES(81),
        TRANSITIVE(82),
        WHEN(83),
        SOURCE(84),
        TRANSACTIONBEGIN(85),
        TRANSACTIONCOMMIT(86),
        TRANSACTIONFINISH(87),
        TRANSACTIONABORT(88),
        RETURNTYPE(89),
        LOOPSCOPE(90),
        MERGE_POINT(91),
        METHODFRAME(92),
        EXEC(93),
        CONTINUETYPE(94),
        CCATCH(95),
        CCAT(96),
        BREAKTYPE(97),
        CONTEXTSTART(98),
        TYPEOF(99),
        SWITCHTOIF(100),
        UNPACK(101),
        REATTACHLOOPINVARIANT(102),
        FORINITUNFOLDTRANSFORMER(103),
        LOOPSCOPEINVARIANTTRANSFORMER(104),
        SETSV(105),
        ISSTATIC(106),
        EVALARGS(107),
        REPLACEARGS(108),
        UNWINDLOOP(109),
        CATCHALL(110),
        BEGIN(111),
        COMMIT(112),
        FINISH(113),
        ABORT(114),
        UNWIND_LOOP_BOUNDED(115),
        FORTOWHILE(116),
        DOBREAK(117),
        METHODCALL(118),
        EXPANDMETHODBODY(119),
        CONSTRUCTORCALL(120),
        SPECIALCONSTRUCTORECALL(121),
        POSTWORK(122),
        STATICINITIALIZATION(123),
        RESOLVE_MULTIPLE_VAR_DECL(124),
        ARRAY_POST_DECL(125),
        ARRAY_INIT_CREATION(126),
        ARRAY_INIT_CREATION_TRANSIENT(127),
        ARRAY_INIT_CREATION_ASSIGNMENTS(128),
        ENHANCEDFOR_ELIM(129),
        STATIC_EVALUATE(130),
        CREATE_OBJECT(131),
        LENGTHREF(132),
        GHOST(133),
        MODEL(134),
        TWO_STATE(135),
        NO_STATE(136),
        RESULTARROW(137),
        LONG_LITERAL(138),
        INTEGER_LITERAL(139),
        DECIMAL_LITERAL(140),
        HEX_LITERAL(141),
        OCTAL_LITERAL(142),
        BINARY_LITERAL(143),
        FLOATING_POINT_LITERAL(144),
        DECIMAL_FLOATING_POINT_LITERAL(145),
        DECIMAL_EXPONENT(146),
        HEXADECIMAL_FLOATING_POINT_LITERAL(147),
        HEXADECIMAL_EXPONENT(148),
        HEX_DIGITS(149),
        UNICODE_ESCAPE(150),
        CHARACTER_LITERAL(151),
        STRING_LITERAL(152),
        ENTER_TEXT_BLOCK(153),
        TEXT_BLOCK_LITERAL(154),
        TEXT_BLOCK_CONTENT(155),
        IDENTIFIER(156),
        JMLIDENTIFIER(157),
        SVIDENTIFIER(158),
        KEYIDENTIFIER(159),
        NON_UNDERSCORE_LETTER(160),
        PART_LETTER(161),
        LPAREN(162),
        RPAREN(163),
        LBRACE(164),
        RBRACE(165),
        LBRACKET(166),
        RBRACKET(167),
        SEMICOLON(168),
        COMMA(169),
        DOT(170),
        ELLIPSIS(171),
        AT(172),
        DOUBLECOLON(173),
        ASSIGN(174),
        LT(175),
        BANG(176),
        TILDE(177),
        HOOK(178),
        COLON(179),
        ARROW(180),
        EQ(181),
        GE(182),
        LE(183),
        NE(184),
        SC_AND(185),
        SC_OR(186),
        INCR(187),
        DECR(188),
        PLUS(189),
        MINUS(190),
        STAR(191),
        SLASH(192),
        BIT_AND(193),
        BIT_OR(194),
        XOR(195),
        REM(196),
        LSHIFT(197),
        SHARP(198),
        PLUSASSIGN(199),
        MINUSASSIGN(200),
        STARASSIGN(201),
        SLASHASSIGN(202),
        ANDASSIGN(203),
        ORASSIGN(204),
        XORASSIGN(205),
        REMASSIGN(206),
        LSHIFTASSIGN(207),
        RSIGNEDSHIFTASSIGN(208),
        RUNSIGNEDSHIFTASSIGN(209),
        RUNSIGNEDSHIFT(210),
        RSIGNEDSHIFT(211),
        GT(212),
        CTRL_Z(213),
        UNNAMED_PLACEHOLDER(214);

        private final int kind;

        Kind(int kind) {
            this.kind = kind;
        }

        public static Kind valueOf(int kind) {
            switch (kind) {
                case 214:
                    return UNNAMED_PLACEHOLDER;
                case 213:
                    return CTRL_Z;
                case 212:
                    return GT;
                case 211:
                    return RSIGNEDSHIFT;
                case 210:
                    return RUNSIGNEDSHIFT;
                case 209:
                    return RUNSIGNEDSHIFTASSIGN;
                case 208:
                    return RSIGNEDSHIFTASSIGN;
                case 207:
                    return LSHIFTASSIGN;
                case 206:
                    return REMASSIGN;
                case 205:
                    return XORASSIGN;
                case 204:
                    return ORASSIGN;
                case 203:
                    return ANDASSIGN;
                case 202:
                    return SLASHASSIGN;
                case 201:
                    return STARASSIGN;
                case 200:
                    return MINUSASSIGN;
                case 199:
                    return PLUSASSIGN;
                case 198:
                    return SHARP;
                case 197:
                    return LSHIFT;
                case 196:
                    return REM;
                case 195:
                    return XOR;
                case 194:
                    return BIT_OR;
                case 193:
                    return BIT_AND;
                case 192:
                    return SLASH;
                case 191:
                    return STAR;
                case 190:
                    return MINUS;
                case 189:
                    return PLUS;
                case 188:
                    return DECR;
                case 187:
                    return INCR;
                case 186:
                    return SC_OR;
                case 185:
                    return SC_AND;
                case 184:
                    return NE;
                case 183:
                    return LE;
                case 182:
                    return GE;
                case 181:
                    return EQ;
                case 180:
                    return ARROW;
                case 179:
                    return COLON;
                case 178:
                    return HOOK;
                case 177:
                    return TILDE;
                case 176:
                    return BANG;
                case 175:
                    return LT;
                case 174:
                    return ASSIGN;
                case 173:
                    return DOUBLECOLON;
                case 172:
                    return AT;
                case 171:
                    return ELLIPSIS;
                case 170:
                    return DOT;
                case 169:
                    return COMMA;
                case 168:
                    return SEMICOLON;
                case 167:
                    return RBRACKET;
                case 166:
                    return LBRACKET;
                case 165:
                    return RBRACE;
                case 164:
                    return LBRACE;
                case 163:
                    return RPAREN;
                case 162:
                    return LPAREN;
                case 161:
                    return PART_LETTER;
                case 160:
                    return NON_UNDERSCORE_LETTER;
                case 159:
                    return KEYIDENTIFIER;
                case 158:
                    return SVIDENTIFIER;
                case 157:
                    return JMLIDENTIFIER;
                case 156:
                    return IDENTIFIER;
                case 155:
                    return TEXT_BLOCK_CONTENT;
                case 154:
                    return TEXT_BLOCK_LITERAL;
                case 153:
                    return ENTER_TEXT_BLOCK;
                case 152:
                    return STRING_LITERAL;
                case 151:
                    return CHARACTER_LITERAL;
                case 150:
                    return UNICODE_ESCAPE;
                case 149:
                    return HEX_DIGITS;
                case 148:
                    return HEXADECIMAL_EXPONENT;
                case 147:
                    return HEXADECIMAL_FLOATING_POINT_LITERAL;
                case 146:
                    return DECIMAL_EXPONENT;
                case 145:
                    return DECIMAL_FLOATING_POINT_LITERAL;
                case 144:
                    return FLOATING_POINT_LITERAL;
                case 143:
                    return BINARY_LITERAL;
                case 142:
                    return OCTAL_LITERAL;
                case 141:
                    return HEX_LITERAL;
                case 140:
                    return DECIMAL_LITERAL;
                case 139:
                    return INTEGER_LITERAL;
                case 138:
                    return LONG_LITERAL;
                case 137:
                    return RESULTARROW;
                case 136:
                    return NO_STATE;
                case 135:
                    return TWO_STATE;
                case 134:
                    return MODEL;
                case 133:
                    return GHOST;
                case 132:
                    return LENGTHREF;
                case 131:
                    return CREATE_OBJECT;
                case 130:
                    return STATIC_EVALUATE;
                case 129:
                    return ENHANCEDFOR_ELIM;
                case 128:
                    return ARRAY_INIT_CREATION_ASSIGNMENTS;
                case 127:
                    return ARRAY_INIT_CREATION_TRANSIENT;
                case 126:
                    return ARRAY_INIT_CREATION;
                case 125:
                    return ARRAY_POST_DECL;
                case 124:
                    return RESOLVE_MULTIPLE_VAR_DECL;
                case 123:
                    return STATICINITIALIZATION;
                case 122:
                    return POSTWORK;
                case 121:
                    return SPECIALCONSTRUCTORECALL;
                case 120:
                    return CONSTRUCTORCALL;
                case 119:
                    return EXPANDMETHODBODY;
                case 118:
                    return METHODCALL;
                case 117:
                    return DOBREAK;
                case 116:
                    return FORTOWHILE;
                case 115:
                    return UNWIND_LOOP_BOUNDED;
                case 114:
                    return ABORT;
                case 113:
                    return FINISH;
                case 112:
                    return COMMIT;
                case 111:
                    return BEGIN;
                case 110:
                    return CATCHALL;
                case 109:
                    return UNWINDLOOP;
                case 108:
                    return REPLACEARGS;
                case 107:
                    return EVALARGS;
                case 106:
                    return ISSTATIC;
                case 105:
                    return SETSV;
                case 104:
                    return LOOPSCOPEINVARIANTTRANSFORMER;
                case 103:
                    return FORINITUNFOLDTRANSFORMER;
                case 102:
                    return REATTACHLOOPINVARIANT;
                case 101:
                    return UNPACK;
                case 100:
                    return SWITCHTOIF;
                case 99:
                    return TYPEOF;
                case 98:
                    return CONTEXTSTART;
                case 97:
                    return BREAKTYPE;
                case 96:
                    return CCAT;
                case 95:
                    return CCATCH;
                case 94:
                    return CONTINUETYPE;
                case 93:
                    return EXEC;
                case 92:
                    return METHODFRAME;
                case 91:
                    return MERGE_POINT;
                case 90:
                    return LOOPSCOPE;
                case 89:
                    return RETURNTYPE;
                case 88:
                    return TRANSACTIONABORT;
                case 87:
                    return TRANSACTIONFINISH;
                case 86:
                    return TRANSACTIONCOMMIT;
                case 85:
                    return TRANSACTIONBEGIN;
                case 84:
                    return SOURCE;
                case 83:
                    return WHEN;
                case 82:
                    return TRANSITIVE;
                case 81:
                    return PROVIDES;
                case 80:
                    return EXPORTS;
                case 79:
                    return MODULE;
                case 78:
                    return USES;
                case 77:
                    return OPENS;
                case 76:
                    return OPEN;
                case 75:
                    return WITH;
                case 74:
                    return TO;
                case 73:
                    return REQUIRES;
                case 72:
                    return YIELD;
                case 71:
                    return WHILE;
                case 70:
                    return VOLATILE;
                case 69:
                    return VOID;
                case 68:
                    return TRY;
                case 67:
                    return TRUE;
                case 66:
                    return TRANSIENT;
                case 65:
                    return THROWS;
                case 64:
                    return THROW;
                case 63:
                    return THIS;
                case 62:
                    return SYNCHRONIZED;
                case 61:
                    return SWITCH;
                case 60:
                    return SUPER;
                case 59:
                    return STRICTFP;
                case 58:
                    return STATIC;
                case 57:
                    return SHORT;
                case 56:
                    return SEALED;
                case 55:
                    return RETURN;
                case 54:
                    return RECORD;
                case 53:
                    return PUBLIC;
                case 52:
                    return PROTECTED;
                case 51:
                    return PRIVATE;
                case 50:
                    return PERMITS;
                case 49:
                    return PACKAGE;
                case 48:
                    return NULL;
                case 47:
                    return NON_SEALED;
                case 46:
                    return NEW;
                case 45:
                    return NATIVE;
                case 44:
                    return LONG;
                case 43:
                    return INTERFACE;
                case 42:
                    return INT;
                case 41:
                    return INSTANCEOF;
                case 40:
                    return IMPORT;
                case 39:
                    return IMPLEMENTS;
                case 38:
                    return IF;
                case 37:
                    return GOTO;
                case 36:
                    return FOR;
                case 35:
                    return FLOAT;
                case 34:
                    return FINALLY;
                case 33:
                    return FINAL;
                case 32:
                    return FALSE;
                case 31:
                    return EXTENDS;
                case 30:
                    return ENUM;
                case 29:
                    return ELSE;
                case 28:
                    return DOUBLE;
                case 27:
                    return DO;
                case 26:
                    return _DEFAULT;
                case 25:
                    return CONTINUE;
                case 24:
                    return CONST;
                case 23:
                    return CLASS;
                case 22:
                    return CHAR;
                case 21:
                    return CATCH;
                case 20:
                    return CASE;
                case 19:
                    return BYTE;
                case 18:
                    return BREAK;
                case 17:
                    return BOOLEAN;
                case 16:
                    return ASSERT;
                case 15:
                    return ABSTRACT;
                case 14:
                    return COMMENT_CONTENT;
                case 13:
                    return MULTI_LINE_COMMENT;
                case 12:
                    return JAVADOC_COMMENT;
                case 11:
                    return JML_BLOCK_COMMENT;
                case 10:
                    return ENTER_MULTILINE_COMMENT;
                case 9:
                    return ENTER_JML_BLOCK_COMMENT;
                case 8:
                    return ENTER_JAVADOC_COMMENT;
                case 7:
                    return JML_ENTER_MULTILINE_COMMENT;
                case 6:
                    return SINGLE_LINE_COMMENT;
                case 5:
                    return JML_LINE_COMMENT;
                case 4:
                    return OLD_MAC_EOL;
                case 3:
                    return UNIX_EOL;
                case 2:
                    return WINDOWS_EOL;
                case 1:
                    return SPACE;
                case 0:
                    return EOF;
                default:
                    throw new IllegalArgumentException(f("Token kind %i is unknown.", kind));
            }
        }

        public boolean isPrimitive() {
            return this == BYTE
                    || this == CHAR
                    || this == SHORT
                    || this == INT
                    || this == LONG
                    || this == FLOAT
                    || this == DOUBLE;
        }

        public int getKind() {
            return kind;
        }
    }

    public JavaToken.Category getCategory() {
        return TokenTypes.getCategory(kind);
    }

    /**
     * Inserts newToken into the token list just before this token.
     */
    public void insert(JavaToken newToken) {
        assertNotNull(newToken);
        getPreviousToken().ifPresent(p -> {
            p.nextToken = newToken;
            newToken.previousToken = p;
        });
        previousToken = newToken;
        newToken.nextToken = this;
    }

    /**
     * Inserts newToken into the token list just after this token.
     */
    public void insertAfter(JavaToken newToken) {
        assertNotNull(newToken);
        getNextToken().ifPresent(n -> {
            n.previousToken = newToken;
            newToken.nextToken = n;
        });
        nextToken = newToken;
        newToken.previousToken = this;
    }

    /**
     * Links the tokens around the current token together, making the current token disappear from the list.
     */
    public void deleteToken() {
        final Optional<JavaToken> nextToken = getNextToken();
        final Optional<JavaToken> previousToken = getPreviousToken();
        previousToken.ifPresent(p -> p.nextToken = nextToken.orElse(null));
        nextToken.ifPresent(n -> n.previousToken = previousToken.orElse(null));
    }

    /**
     * Replaces the current token with newToken.
     */
    public void replaceToken(JavaToken newToken) {
        assertNotNull(newToken);
        getPreviousToken().ifPresent(p -> {
            p.nextToken = newToken;
            newToken.previousToken = p;
        });
        getNextToken().ifPresent(n -> {
            n.previousToken = newToken;
            newToken.nextToken = n;
        });
    }

    /**
     * @return the last token in the token list.
     */
    public JavaToken findLastToken() {
        JavaToken current = this;
        while (current.getNextToken().isPresent()) {
            current = current.getNextToken().get();
        }
        return current;
    }

    /**
     * @return the first token in the token list.
     */
    public JavaToken findFirstToken() {
        JavaToken current = this;
        while (current.getPreviousToken().isPresent()) {
            current = current.getPreviousToken().get();
        }
        return current;
    }

    @Override
    public int hashCode() {
        int result = kind;
        result = 31 * result + text.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaToken javaToken = (JavaToken) o;
        if (kind != javaToken.kind) return false;
        if (!text.equals(javaToken.text)) return false;
        return true;
    }
}
