package com.github.javaparser.ast.key;

import com.github.javaparser.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.ast.Node;
import org.junit.jupiter.api.Test;


/**
 *
 * @author Alexander Weigl
 * @version 1 (04.07.26)
 */
public class FPLiteralTest {
    @Test
    void testLiteral() {
        var expr = StaticJavaParser.parseExpression("2./a");
        assertEquals("2. / a", expr.toString());
        assertEquals("[BinaryExpr [DoubleLiteralExpr] [NameExpr [SimpleName]]]", toTree(expr));
        System.out.println(expr);

        var expr2 = StaticJavaParser.parseExpression("5..6");
        assertEquals("5..6", expr2.toString());
        assertEquals("[KeyRangeExpression [IntegerLiteralExpr] [IntegerLiteralExpr]]", toTree(expr2));

        // This takes a totally different lexer and parser tree
        var expr2b = StaticJavaParser.parseExpression("5 .. 6");
        assertEquals("5..6", expr2b.toString());
        assertEquals("[KeyRangeExpression [IntegerLiteralExpr] [IntegerLiteralExpr]]", toTree(expr2b));

        var expr3 = StaticJavaParser.parseExpression("a..b");
        assertEquals("a..b", expr3.toString());
        assertEquals("[KeyRangeExpression [NameExpr [SimpleName]] [NameExpr [SimpleName]]]", toTree(expr3));
    }

    private String toTree(Node expr) {
        return "[" + expr.getClass().getSimpleName() +
                expr.stream(Node.TreeTraversal.DIRECT_CHILDREN).map(this::toTree).reduce("", (a, b) -> a + " " + b) + "]";
    }
}
