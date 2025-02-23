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
package com.github.javaparser.ast.body;

import static com.github.javaparser.utils.Utils.assertNotNull;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithExtends;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithAbstractModifier;
import com.github.javaparser.ast.nodeTypes.modifiers.NodeWithFinalModifier;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.visitor.CloneVisitor;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.metamodel.ClassOrInterfaceDeclarationMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Objects;
import org.jspecify.annotations.NonNull;

/**
 * A definition of a class or interface.<br>{@code class X { ... }}<br>{@code interface X { ... }}
 *
 * @author Julio Vilmar Gesser
 */
public class ClassOrInterfaceDeclaration extends TypeDeclaration<ClassOrInterfaceDeclaration> implements NodeWithImplements<ClassOrInterfaceDeclaration>, NodeWithExtends<ClassOrInterfaceDeclaration>, NodeWithTypeParameters<ClassOrInterfaceDeclaration>, NodeWithAbstractModifier<ClassOrInterfaceDeclaration>, NodeWithFinalModifier<ClassOrInterfaceDeclaration>, Resolvable<ResolvedReferenceTypeDeclaration> {

    private boolean isInterface;

    private NodeList<TypeParameter> typeParameters;

    // Can contain more than one item if this is an interface
    private NodeList<ClassOrInterfaceType> extendedTypes;

    private NodeList<ClassOrInterfaceType> implementedTypes;

    private NodeList<ClassOrInterfaceType> permittedTypes;

    public ClassOrInterfaceDeclaration() {
        this(null, new NodeList<>(), new NodeList<>(), false, new SimpleName(), new NodeList<>(), new NodeList<>(), new NodeList<>(), new NodeList<>(), new NodeList<>());
    }

    public ClassOrInterfaceDeclaration(final NodeList<Modifier> modifiers, final boolean isInterface, final String name) {
        this(null, modifiers, new NodeList<>(), isInterface, new SimpleName(name), new NodeList<>(), new NodeList<>(), new NodeList<>(), new NodeList<>(), new NodeList<>());
    }

    @AllFieldsConstructor
    public ClassOrInterfaceDeclaration(final NodeList<Modifier> modifiers, final NodeList<AnnotationExpr> annotations, final boolean isInterface, final SimpleName name, final NodeList<TypeParameter> typeParameters, final NodeList<ClassOrInterfaceType> extendedTypes, final NodeList<ClassOrInterfaceType> implementedTypes, final NodeList<ClassOrInterfaceType> permittedTypes, final NodeList<BodyDeclaration<?>> members) {
        this(null, modifiers, annotations, isInterface, name, typeParameters, extendedTypes, implementedTypes, permittedTypes, members);
    }

    /**
     * This constructor is used by the parser and is considered private.
     */
    @Generated("com.github.javaparser.generator.core.node.MainConstructorGenerator")
    public ClassOrInterfaceDeclaration(TokenRange tokenRange, NodeList<Modifier> modifiers, NodeList<AnnotationExpr> annotations, boolean isInterface, SimpleName name, NodeList<TypeParameter> typeParameters, NodeList<ClassOrInterfaceType> extendedTypes, NodeList<ClassOrInterfaceType> implementedTypes, NodeList<ClassOrInterfaceType> permittedTypes, NodeList<BodyDeclaration<?>> members) {
        super(tokenRange, modifiers, annotations, name, members);
        setInterface(isInterface);
        setTypeParameters(typeParameters);
        setExtendedTypes(extendedTypes);
        setImplementedTypes(implementedTypes);
        setPermittedTypes(permittedTypes);
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

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> getExtendedTypes() {
        return extendedTypes;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> getImplementedTypes() {
        return implementedTypes;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> getPermittedTypes() {
        return permittedTypes;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<TypeParameter> getTypeParameters() {
        return typeParameters;
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public boolean isInterface() {
        return Objects.requireNonNull(isInterface);
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceDeclaration setExtendedTypes(final NodeList<ClassOrInterfaceType> extendedTypes) {
        assertNotNull(extendedTypes);
        if (extendedTypes == this.extendedTypes) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.EXTENDED_TYPES, this.extendedTypes, extendedTypes);
        if (this.extendedTypes != null)
            this.extendedTypes.setParentNode(null);
        this.extendedTypes = extendedTypes;
        setAsParentNodeOf(extendedTypes);
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceDeclaration setImplementedTypes(final NodeList<ClassOrInterfaceType> implementedTypes) {
        assertNotNull(implementedTypes);
        if (implementedTypes == this.implementedTypes) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.IMPLEMENTED_TYPES, this.implementedTypes, implementedTypes);
        if (this.implementedTypes != null)
            this.implementedTypes.setParentNode(null);
        this.implementedTypes = implementedTypes;
        setAsParentNodeOf(implementedTypes);
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceDeclaration setPermittedTypes(final NodeList<ClassOrInterfaceType> permittedTypes) {
        assertNotNull(permittedTypes);
        if (permittedTypes == this.permittedTypes) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.PERMITTED_TYPES, this.permittedTypes, permittedTypes);
        if (this.permittedTypes != null)
            this.permittedTypes.setParentNode(null);
        this.permittedTypes = permittedTypes;
        setAsParentNodeOf(permittedTypes);
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceDeclaration setInterface(final boolean isInterface) {
        if (isInterface == this.isInterface) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.INTERFACE, this.isInterface, isInterface);
        this.isInterface = isInterface;
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceDeclaration setTypeParameters(final NodeList<TypeParameter> typeParameters) {
        assertNotNull(typeParameters);
        if (typeParameters == this.typeParameters) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.TYPE_PARAMETERS, this.typeParameters, typeParameters);
        if (this.typeParameters != null)
            this.typeParameters.setParentNode(null);
        this.typeParameters = typeParameters;
        setAsParentNodeOf(typeParameters);
        return this;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        for (int i = 0; i < extendedTypes.size(); i++) {
            if (extendedTypes.get(i) == node) {
                extendedTypes.remove(i);
                return true;
            }
        }
        for (int i = 0; i < implementedTypes.size(); i++) {
            if (implementedTypes.get(i) == node) {
                implementedTypes.remove(i);
                return true;
            }
        }
        for (int i = 0; i < permittedTypes.size(); i++) {
            if (permittedTypes.get(i) == node) {
                permittedTypes.remove(i);
                return true;
            }
        }
        for (int i = 0; i < typeParameters.size(); i++) {
            if (typeParameters.get(i) == node) {
                typeParameters.remove(i);
                return true;
            }
        }
        return super.remove(node);
    }

    /**
     * @return is this class's parent a LocalClassDeclarationStmt ?
     */
    public boolean isLocalClassDeclaration() {
        return getParentNode().map(p -> p instanceof LocalClassDeclarationStmt).orElse(false);
    }

    @Override
    public Optional<String> getFullyQualifiedName() {
        if (isLocalClassDeclaration()) {
            return Optional.empty();
        }
        return super.getFullyQualifiedName();
    }

    /**
     * @return is this an inner class?
     * NOTE: many people are confused over terminology. Refer to https://docs.oracle.com/javase/tutorial/java/javaOO/nested.html .
     */
    public boolean isInnerClass() {
        return isNestedType() && !isInterface && !isStatic();
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.CloneGenerator")
    public ClassOrInterfaceDeclaration clone() {
        return (ClassOrInterfaceDeclaration) accept(new CloneVisitor(), null);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.GetMetaModelGenerator")
    public ClassOrInterfaceDeclarationMetaModel getMetaModel() {
        return JavaParserMetaModel.classOrInterfaceDeclarationMetaModel;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.ReplaceMethodGenerator")
    public boolean replace(Node node, Node replacementNode) {
        if (node == null) {
            return false;
        }
        for (int i = 0; i < extendedTypes.size(); i++) {
            if (extendedTypes.get(i) == node) {
                extendedTypes.set(i, (ClassOrInterfaceType) replacementNode);
                return true;
            }
        }
        for (int i = 0; i < implementedTypes.size(); i++) {
            if (implementedTypes.get(i) == node) {
                implementedTypes.set(i, (ClassOrInterfaceType) replacementNode);
                return true;
            }
        }
        for (int i = 0; i < permittedTypes.size(); i++) {
            if (permittedTypes.get(i) == node) {
                permittedTypes.set(i, (ClassOrInterfaceType) replacementNode);
                return true;
            }
        }
        for (int i = 0; i < typeParameters.size(); i++) {
            if (typeParameters.get(i) == node) {
                typeParameters.set(i, (TypeParameter) replacementNode);
                return true;
            }
        }
        return super.replace(node, replacementNode);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public boolean isClassOrInterfaceDeclaration() {
        return true;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public ClassOrInterfaceDeclaration asClassOrInterfaceDeclaration() {
        return this;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public void ifClassOrInterfaceDeclaration(Consumer<ClassOrInterfaceDeclaration> action) {
        action.accept(this);
    }

    @Override
    public ResolvedReferenceTypeDeclaration resolve() {
        return getSymbolResolver().resolveDeclaration(this, ResolvedReferenceTypeDeclaration.class);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public Optional<ClassOrInterfaceDeclaration> toClassOrInterfaceDeclaration() {
        return Optional.of(this);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> extendedTypes() {
        return Objects.requireNonNull(extendedTypes);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> implementedTypes() {
        return Objects.requireNonNull(implementedTypes);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<ClassOrInterfaceType> permittedTypes() {
        return Objects.requireNonNull(permittedTypes);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<TypeParameter> typeParameters() {
        return Objects.requireNonNull(typeParameters);
    }
}
