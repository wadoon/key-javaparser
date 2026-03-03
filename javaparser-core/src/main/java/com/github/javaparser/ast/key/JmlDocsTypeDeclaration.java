package com.github.javaparser.ast.key;

import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;

/**
 *
 * @author Alexander Weigl
 * @version 1 (3/3/26)
 */
public class JmlDocsTypeDeclaration extends TypeDeclaration<JmlDocsTypeDeclaration> {
    private final NodeList<JmlDoc> jmlDocs;

    @AllFieldsConstructor
    public JmlDocsTypeDeclaration(NodeList<JmlDoc> seq) {
        this.jmlDocs = seq;
    }

    @Override
    public <R, A> R accept(GenericVisitor<R, A> v, A arg) {
        return null;
    }

    @Override
    public <A> void accept(VoidVisitor<A> v, A arg) {

    }

    @Override
    public ResolvedReferenceTypeDeclaration resolve() {
        return null;
    }
}
