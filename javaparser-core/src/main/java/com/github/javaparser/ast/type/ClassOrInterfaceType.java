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
import com.github.javaparser.ast.nodeTypes.NodeWithTypeArguments;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.visitor.CloneVisitor;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.metamodel.ClassOrInterfaceTypeMetaModel;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.OptionalProperty;
import com.github.javaparser.resolution.Context;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedTypeDeclaration;
import com.github.javaparser.resolution.model.SymbolReference;
import com.github.javaparser.resolution.model.typesystem.ReferenceTypeImpl;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.Objects;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A class or an interface type.
 * <br>{@code Object}
 * <br>{@code HashMap<String, String>}
 * <br>{@code java.util.Punchcard}
 * <p>
 * <p>Note that the syntax is ambiguous here, and JavaParser does not know what is to the left of the class. It assumes
 * cases like {@code Map.Entry} where Map is the scope of Entry. In {@code java.util.Punchcard}, it will not
 * recognize that java and util are parts of the package name. Instead, it will set util as the scope of Punchcard, as a
 * ClassOrInterfaceType (which it is not). In turn, util will have java as its scope, also as a
 * ClassOrInterfaceType</p>
 *
 * @author Julio Vilmar Gesser
 */
public class ClassOrInterfaceType extends ReferenceType implements NodeWithSimpleName<ClassOrInterfaceType>, NodeWithAnnotations<ClassOrInterfaceType>, NodeWithTypeArguments<ClassOrInterfaceType> {

    @OptionalProperty
    private ClassOrInterfaceType scope;

    private SimpleName name;

    @OptionalProperty
    private NodeList<Type> typeArguments;

    public ClassOrInterfaceType() {
        this(null, null, new SimpleName(), null, new NodeList<>());
    }

    /**
     * @deprecated use JavaParser.parseClassOrInterfaceType instead. This constructor does not understand generics.
     */
    public ClassOrInterfaceType(final String name) {
        this(null, null, new SimpleName(name), null, new NodeList<>());
    }

    public ClassOrInterfaceType(final ClassOrInterfaceType scope, final String name) {
        this(null, scope, new SimpleName(name), null, new NodeList<>());
    }

    public ClassOrInterfaceType(final ClassOrInterfaceType scope, final SimpleName name, final NodeList<Type> typeArguments) {
        this(null, scope, name, typeArguments, new NodeList<>());
    }

    @AllFieldsConstructor
    public ClassOrInterfaceType(final ClassOrInterfaceType scope, final SimpleName name, final NodeList<Type> typeArguments, final NodeList<AnnotationExpr> annotations) {
        this(null, scope, name, typeArguments, annotations);
    }

    /**
     * This constructor is used by the parser and is considered private.
     */
    @Generated("com.github.javaparser.generator.core.node.MainConstructorGenerator")
    public ClassOrInterfaceType(TokenRange tokenRange, ClassOrInterfaceType scope, SimpleName name, NodeList<Type> typeArguments, NodeList<AnnotationExpr> annotations) {
        super(tokenRange, annotations);
        setScope(scope);
        setName(name);
        setTypeArguments(typeArguments);
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
    public SimpleName getName() {
        return name;
    }

    public String getNameWithScope() {
        StringBuilder str = new StringBuilder();
        getScope().ifPresent(s -> str.append(s.getNameWithScope()).append("."));
        str.append(name.asString());
        return str.toString();
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Optional<ClassOrInterfaceType> getScope() {
        return Optional.ofNullable(scope);
    }

    public boolean isBoxedType() {
        return PrimitiveType.unboxMap.containsKey(name.getIdentifier());
    }

    public PrimitiveType toUnboxedType() throws UnsupportedOperationException {
        if (!isBoxedType()) {
            throw new UnsupportedOperationException(name + " isn't a boxed type.");
        }
        return new PrimitiveType(PrimitiveType.unboxMap.get(name.getIdentifier()));
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceType setName(final SimpleName name) {
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

    /**
     * Sets the scope
     *
     * @param scope the scope, can be null
     * @return this, the ClassOrInterfaceType
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceType setScope(final ClassOrInterfaceType scope) {
        if (scope == this.scope) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.SCOPE, this.scope, scope);
        if (this.scope != null)
            this.scope.setParentNode(null);
        this.scope = scope;
        setAsParentNodeOf(scope);
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Optional<NodeList<Type>> getTypeArguments() {
        return Optional.ofNullable(typeArguments);
    }

    /**
     * Sets the typeArguments
     *
     * @param typeArguments the typeArguments, can be null
     * @return this, the ClassOrInterfaceType
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceType setTypeArguments(final NodeList<Type> typeArguments) {
        if (typeArguments == this.typeArguments) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.TYPE_ARGUMENTS, this.typeArguments, typeArguments);
        if (this.typeArguments != null)
            this.typeArguments.setParentNode(null);
        this.typeArguments = typeArguments;
        setAsParentNodeOf(typeArguments);
        return this;
    }

    @Override
    public ClassOrInterfaceType setAnnotations(NodeList<AnnotationExpr> annotations) {
        return (ClassOrInterfaceType) super.setAnnotations(annotations);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        if (scope != null) {
            if (node == scope) {
                removeScope();
                return true;
            }
        }
        if (typeArguments != null) {
            for (int i = 0; i < typeArguments.size(); i++) {
                if (typeArguments.get(i) == node) {
                    typeArguments.remove(i);
                    return true;
                }
            }
        }
        return super.remove(node);
    }

    @Override
    public String asString() {
        StringBuilder str = new StringBuilder();
        getScope().ifPresent(s -> str.append(s.asString()).append("."));
        str.append(name.asString());
        getTypeArguments().ifPresent(ta -> str.append(ta.stream().map(Type::asString).collect(joining(",", "<", ">"))));
        return str.toString();
    }

    /*
     * Note that the internal forms of the binary names of object are used.
     * for example java/lang/Object
     */
    @Override
    public String toDescriptor() {
        return String.format("L%s;", resolve().erasure().asReferenceType().getQualifiedName().replace(".", "/"));
    }

    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public ClassOrInterfaceType removeScope() {
        return setScope((ClassOrInterfaceType) null);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.CloneGenerator")
    public ClassOrInterfaceType clone() {
        return (ClassOrInterfaceType) accept(new CloneVisitor(), null);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.GetMetaModelGenerator")
    public ClassOrInterfaceTypeMetaModel getMetaModel() {
        return JavaParserMetaModel.classOrInterfaceTypeMetaModel;
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
        if (scope != null) {
            if (node == scope) {
                setScope((ClassOrInterfaceType) replacementNode);
                return true;
            }
        }
        if (typeArguments != null) {
            for (int i = 0; i < typeArguments.size(); i++) {
                if (typeArguments.get(i) == node) {
                    typeArguments.set(i, (Type) replacementNode);
                    return true;
                }
            }
        }
        return super.replace(node, replacementNode);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public boolean isClassOrInterfaceType() {
        return true;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public ClassOrInterfaceType asClassOrInterfaceType() {
        return this;
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public void ifClassOrInterfaceType(Consumer<ClassOrInterfaceType> action) {
        action.accept(this);
    }

    @Override
    public ResolvedType resolve() {
        return getSymbolResolver().toResolvedType(this, ResolvedType.class);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.TypeCastingGenerator")
    public Optional<ClassOrInterfaceType> toClassOrInterfaceType() {
        return Optional.of(this);
    }

    /**
     * Convert a {@link ClassOrInterfaceType} into a {@link ResolvedType}.
     *
     * @param classOrInterfaceType  The class of interface type to be converted.
     * @param context               The current context.
     *
     * @return The type resolved.
     */
    @Override
    public ResolvedType convertToUsage(Context context) {
        String name = getNameWithScope();
        SymbolReference<ResolvedTypeDeclaration> ref = context.solveType(name);
        if (!ref.isSolved()) {
            throw new UnsolvedSymbolException(name);
        }
        ResolvedTypeDeclaration typeDeclaration = ref.getCorrespondingDeclaration();
        List<ResolvedType> typeParameters = Collections.emptyList();
        if (getTypeArguments().isPresent()) {
            typeParameters = getTypeArguments().get().stream().map((pt) -> pt.convertToUsage(context)).collect(Collectors.toList());
        }
        if (typeDeclaration.isTypeParameter()) {
            return new ResolvedTypeVariable(typeDeclaration.asTypeParameter());
        }
        return new ReferenceTypeImpl((ResolvedReferenceTypeDeclaration) typeDeclaration, typeParameters);
    }

    @NonNull()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public SimpleName name() {
        return Objects.requireNonNull(name);
    }

    @Nullable()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public ClassOrInterfaceType scope() {
        return scope;
    }

    @Nullable()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<Type> typeArguments() {
        return typeArguments;
    }
}
