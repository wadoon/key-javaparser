package com.github.javaparser.ast.key;

import com.github.javaparser.JavaToken;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import java.util.Optional;
import java.util.function.Consumer;
import com.github.javaparser.ast.observer.ObservableProperty;
import static com.github.javaparser.utils.Utils.assertNotNull;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.visitor.CloneVisitor;
import com.github.javaparser.metamodel.KeyActiveCommentStatementMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.ast.Generated;

/**
 * @author Alexander Weigl
 * @version 1 (15.04.23)
 */
public class KeyActiveCommentStatement extends Statement {

    private String content;

    @AllFieldsConstructor
    public KeyActiveCommentStatement(String content) {
        this.content = content;
    }

    public KeyActiveCommentStatement(JavaToken content) {
        this(new TokenRange(content, content), content.getText());
    }

    /**
     * This constructor is used by the parser and is considered private.
     */
    @Generated("com.github.javaparser.generator.core.node.MainConstructorGenerator")
    public KeyActiveCommentStatement(TokenRange tokenRange, String content) {
        super(tokenRange);
        setContent(content);
        customInitialization();
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.AcceptGenerator")
    public <R, A> R accept(final GenericVisitor<R, A> v, final A arg) {
        return v.visit(this, arg);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.AcceptGenerator")
    public <A> void accept(final VoidVisitor<A> v, final A arg) {
        v.visit(this, arg);
    }

    @Override
    public boolean isKeyActiveCommentStatement() {
        return true;
    }

    @Override
    public KeyActiveCommentStatement asKeyActiveCommentStatement() {
        return this;
    }

    @Override
    public Optional<KeyActiveCommentStatement> toKeyActiveCommentStatement() {
        return Optional.of(this);
    }

    public void ifKeyActiveCommentStatement(Consumer<KeyActiveCommentStatement> action) {
        action.accept(this);
    }

    public String getContent() {
        return content;
    }

    public KeyActiveCommentStatement setContent(final String content) {
        assertNotNull(content);
        if (content == this.content) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.CONTENT, this.content, content);
        this.content = content;
        return this;
    }

    @Override
    public KeyActiveCommentStatement clone() {
        return (KeyActiveCommentStatement) accept(new CloneVisitor(), null);
    }

    @Override
    public KeyActiveCommentStatementMetaModel getMetaModel() {
        return JavaParserMetaModel.keyActiveCommentStatementMetaModel;
    }
}
