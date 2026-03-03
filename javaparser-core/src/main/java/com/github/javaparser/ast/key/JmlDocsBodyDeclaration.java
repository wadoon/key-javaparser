package com.github.javaparser.ast.key;

import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;

/**
 *
 * @author Alexander Weigl
 * @version 1 (3/3/26)
 */
public class JmlDocsBodyDeclaration extends BodyDeclaration<JmlDocsTypeDeclaration> {

    private final NodeList<JmlDoc> jmlDocs;

    @AllFieldsConstructor
    public JmlDocsBodyDeclaration(NodeList<JmlDoc> jmlDocs) {
        this.jmlDocs = jmlDocs;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return null;
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {

    }
}
