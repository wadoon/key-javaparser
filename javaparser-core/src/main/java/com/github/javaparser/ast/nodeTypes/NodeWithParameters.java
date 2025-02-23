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
package com.github.javaparser.ast.nodeTypes;

import static com.github.javaparser.StaticJavaParser.parseType;
import static java.util.stream.Collectors.toList;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public interface NodeWithParameters<N extends Node> {

    NodeList<Parameter> getParameters();

    default Parameter getParameter(int i) {
        return getParameters().get(i);
    }

    void tryAddImportToParentCompilationUnit(Class<?> clazz);

    @SuppressWarnings("unchecked")
    default N setParameter(int i, Parameter parameter) {
        getParameters().set(i, parameter);
        return (N) this;
    }

    N setParameters(NodeList<Parameter> parameters);

    default N addParameter(Type type, String name) {
        return addParameter(new Parameter(type, name));
    }

    default N addParameter(Class<?> paramClass, String name) {
        tryAddImportToParentCompilationUnit(paramClass);
        return addParameter(parseType(paramClass.getSimpleName()), name);
    }

    /**
     * Remember to import the class in the compilation unit yourself
     *
     * @param className the name of the class, ex : org.test.Foo or Foo if you added manually the import
     * @param name      the name of the parameter
     */
    default N addParameter(String className, String name) {
        return addParameter(parseType(className), name);
    }

    @SuppressWarnings("unchecked")
    default N addParameter(Parameter parameter) {
        getParameters().add(parameter);
        return (N) this;
    }

    default Parameter addAndGetParameter(Type type, String name) {
        return addAndGetParameter(new Parameter(type, name));
    }

    default Parameter addAndGetParameter(Class<?> paramClass, String name) {
        tryAddImportToParentCompilationUnit(paramClass);
        return addAndGetParameter(parseType(paramClass.getSimpleName()), name);
    }

    /**
     * Remember to import the class in the compilation unit yourself
     *
     * @param className the name of the class, ex : org.test.Foo or Foo if you added manually the import
     * @param name      the name of the parameter
     * @return the {@link Parameter} created
     */
    default Parameter addAndGetParameter(String className, String name) {
        return addAndGetParameter(parseType(className), name);
    }

    default Parameter addAndGetParameter(Parameter parameter) {
        getParameters().add(parameter);
        return parameter;
    }

    /**
     * Try to find a {@link Parameter} by its name
     *
     * @param name the name of the param
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByName(String name) {
        return getParameters().stream().filter(p -> p.getNameAsString().equals(name)).findFirst();
    }

    /**
     * Try to find a {@link Parameter} by its type
     *
     * @param type the type of the param
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByType(String type) {
        return getParameters().stream().filter(p -> p.getType().toString().equals(type)).findFirst();
    }

    /**
     * Try to find a {@link Parameter} by its type
     *
     * @param type the type of the param <b>take care about generics, it wont work</b>
     * @return null if not found, the param found otherwise
     */
    default Optional<Parameter> getParameterByType(Class<?> type) {
        return getParameters().stream().filter(p -> p.getType().toString().equals(type.getSimpleName())).findFirst();
    }

    /**
     * Check if the parameters have certain types. The given parameter types must <i>literally</i> match the declared
     * types of this node's parameters, so passing the string {@code "List"} to this method will be considered a match
     * if this node has exactly one parameter whose type is declared as {@code List}, but not if the parameter type is
     * declared as {@code java.util.List} or {@code java.awt.List}. Conversely, passing the string
     * {@code "java.util.List"} to this method will be considered a match if this node has exactly one parameter whose
     * type is declared as {@code java.util.List}, but not if the parameter type is declared as {@code List}. Similarly,
     * note that generics are matched as well: If this node has one parameter declared as {@code List&lt;String&gt;},
     * then it will be considered as a match only if the given string is {@code "List&lt;String&gt;"}, but not if the
     * given string is only {@code "List"}.
     *
     * @param paramTypes the types of parameters like {@code "Map&lt;Integer,String&gt;", "int"} to match
     *                   {@code void foo(Map&lt;Integer,String&gt; myMap, int number)}.
     * @return {@code true} if all parameters match one by one, in the given order.
     */
    default boolean hasParametersOfType(String... paramTypes) {
        return getParameters().stream().map(p -> p.getType().asString()).collect(toList()).equals(Arrays.asList(paramTypes));
    }

    /**
     * Check if the parameters have certain types. Note that this is a match in SimpleName, so {@code java.awt.List} and
     * {@code java.util.List} are identical to this algorithm. In addition, note that it is the erasure of each type
     * which is considered, so passing {@code List.class} to this method will be considered a match if this node has
     * exactly one parameter whose type is named {@code List}, regardless of whether the parameter type is declared
     * without generics as {@code List}, or with generics as {@code List&lt;String&gt;}, or {@code List&lt;Integer&gt;},
     * etc.
     *
     * @param paramTypes the types of parameters like {@code Map.class, int.class} to match
     *                   {@code void foo(Map&lt;Integer,String&gt; myMap, int number)}.
     * @return {@code true} if all parameters match one by one, in the given order.
     */
    default boolean hasParametersOfType(Class<?>... paramTypes) {
        return // if p.getType() is a class or interface type, we want to consider its erasure, i.e., if the
        // parameter
        // is "List<String>", we want to consider it as "List", so we need to call getName()
        getParameters().stream().map(p -> p.getType().toClassOrInterfaceType().map(NodeWithSimpleName::getNameAsString).orElseGet(() -> p.getType().asString())).collect(toList()).equals(Stream.of(paramTypes).map(Class::getSimpleName).collect(toList()));
    }
}
