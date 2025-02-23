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
package com.github.javaparser.ast.type;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.joining;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.Generated;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.visitor.CloneVisitor;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.TypeParameterMetaModel;
import com.github.javaparser.resolution.Context;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * A type parameter. Examples:
 * <br>{@code <}<b>{@code U}</b>{@code > U getU() { ... }}
 * <br>{@code class D <}<b>{@code @Brain T extends B & A & @Tripe C}</b>{@code > { ... }}
 * <p>U and T are type parameter names.
 * <br>B, A, and C are type parameter bounds.
 * <br>Tripe is an annotation on type parameter bound C.
 * <br>Brain is an annotation on type parameter T.
 *
 * @author Julio Vilmar Gesser
 * @see com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters
 */
public class TypeParameter extends ReferenceType implements NodeWithSimpleName<TypeParameter>, NodeWithAnnotations<TypeParameter> {

    private SimpleName name;

    private NodeList<ClassOrInterfaceType> typeBound;

    public TypeParameter() {
        this(null, new SimpleName(), new NodeList<>(), new NodeList<>());
    }

    public TypeParameter(final String name) {
        this(null, new SimpleName(name), new NodeList<>(), new NodeList<>());
    }

    public TypeParameter(final String name, final NodeList<ClassOrInterfaceType> typeBound) {
        this(null, new SimpleName(name), typeBound, new NodeList<>());
    }

    @AllFieldsConstructor
    public TypeParameter(SimpleName name, NodeList<ClassOrInterfaceType> typeBound, NodeList<AnnotationExpr> annotations) {
        this(null, name, typeBound, annotations);
    }

    /**
     * This constructor is used by the parser and is considered private.
     */
    @Generated("com.github.javaparser.generator.core.node.MainConstructorGenerator")
    public TypeParameter(TokenRange tokenRange, SimpleName name, NodeList<ClassOrInterfaceType> typeBound, NodeList<AnnotationExpr> annotations) {
        super(tokenRange, annotations);
        setName(name);
        setTypeBound(typeBound);
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

    /**
     * Return the name of the paramenter.
     *
     * @return the name of the paramenter
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public SimpleName getName() {
        return name;
    }

    /**
     * Return the list of {@link ClassOrInterfaceType} that this parameter
     * extends. Return {@code null} null if there are no type.
     *
     * @return list of types that this paramente extends or {@code null}
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> getTypeBound() {
        return typeBound;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public TypeParameter setName(final SimpleName name) {
        assertNotNull(name);
        if (name == this.name) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.NAME, this.name, name);
        if (this.name != null)
            this.name.setParentNode(null);
        this.name = name;
        setAsParentNodeOf(name);
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public TypeParameter setTypeBound(final NodeList<ClassOrInterfaceType> typeBound) {
        assertNotNull(typeBound);
        if (typeBound == this.typeBound) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.TYPE_BOUND, this.typeBound, typeBound);
        if (this.typeBound != null)
            this.typeBound.setParentNode(null);
        this.typeBound = typeBound;
        setAsParentNodeOf(typeBound);
        return this;
    }

    @Override
    public TypeParameter setAnnotations(NodeList<AnnotationExpr> annotations) {
        super.setAnnotations(annotations);
        return this;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        for (int i = 0; i < typeBound.size(); i++) {
            if (typeBound.get(i) == node) {
                typeBound.remove(i);
                return true;
            }
        }
        return super.remove(node);
    }

    @Override
    public String asString() {
        StringBuilder str = new StringBuilder(getNameAsString());
        getTypeBound().ifNonEmpty(l -> str.append(l.stream().map(ClassOrInterfaceType::asString).collect(joining("&", " extends ", ""))));
        return str.toString();
    }

    @Override
    public String toDescriptor() {
        return String.format("L%s;", resolve().qualifiedName());
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.CloneGenerator")
    public TypeParameter clone() {
        return (TypeParameter) accept(new CloneVisitor(), null);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.GetMetaModelGenerator")
    public TypeParameterMetaModel getMetaModel() {
        return JavaParserMetaModel.typeParameterMetaModel;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.ReplaceMethodGenerator")
    public boolean replace(Node node, Node replacementNode) {
        if (node == null) {
            return false;
        }
        if (node == name) {
            setName((SimpleName) replacementNode);
            return true;
        }
        for (int i = 0; i < typeBound.size(); i++) {
            if (typeBound.get(i) == node) {
                typeBound.set(i, (ClassOrInterfaceType) replacementNode);
                return true;
            }
        }
        return super.replace(node, replacementNode);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public boolean isTypeParameter() {
        return true;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public TypeParameter asTypeParameter() {
        return this;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public void ifTypeParameter(Consumer<TypeParameter> action) {
        action.accept(this);
    }

    @Override
    public ResolvedTypeVariable resolve() {
        return getSymbolResolver().toResolvedType(this, ResolvedTypeVariable.class);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public Optional<TypeParameter> toTypeParameter() {
        return Optional.of(this);
    }

    @Override
    public ResolvedType convertToUsage(Context context) {
        throw new UnsupportedOperationException(getClass().getCanonicalName());
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public SimpleName name() {
        return Objects.requireNonNull(name);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> typeBound() {
        return Objects.requireNonNull(typeBound);
    }
}
