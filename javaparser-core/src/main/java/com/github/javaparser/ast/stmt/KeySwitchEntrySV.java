package com.github.javaparser.ast.stmt;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.NodeList;

/**
 * @author Daniel Drodt
 * @version 1 (4/22/26)
 */
public class KeySwitchEntrySV extends SwitchEntry {

    private final String schemaVar;

    public KeySwitchEntrySV(TokenRange range, String text) {
        super(range, new NodeList<>(), Type.STATEMENT_GROUP, new NodeList<>());
        this.schemaVar = text;
    }

    public String getSchemaVar() {
        return schemaVar;
    }
}
