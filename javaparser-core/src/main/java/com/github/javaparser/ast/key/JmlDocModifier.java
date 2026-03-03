package com.github.javaparser.ast.key;

import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;

/**
 *
 * @author Alexander Weigl
 * @version 1 (3/3/26)
 */
public class JmlDocModifier implements Modifier.Keyword {
    private final NodeList<JmlDoc> jmlDocs;

    @AllFieldsConstructor
    public JmlDocModifier(NodeList<JmlDoc> jmlDocs) {
        this.jmlDocs = jmlDocs;
    }

    @Override
    public String name() {
        return "";
    }

    @Override
    public String asString() {
        return "";
    }
}
