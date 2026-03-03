package com.github.javaparser.ast.key;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 *
 * @author Alexander Weigl
 * @version 1 (3/3/26)
 */
public class JmlDoc extends Node {
    private Comment comment;

    @AllFieldsConstructor
    public JmlDoc(JavaToken comment) {
        this(new TokenRange(comment, comment), comment);
    }

    public JmlDoc(TokenRange tokenRange, JavaToken comment) {
        super(tokenRange);
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return null;
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {

    }
}
