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
package com.github.javaparser.ast;

import static com.github.javaparser.ast.Node.Parsedness.PARSED;
import static com.github.javaparser.ast.Node.TreeTraversal.PREORDER;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.NONNULL;
import com.github.javaparser.HasParentNode;
import com.github.javaparser.Position;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.nodeTypes.NodeWithOptionalScope;
import com.github.javaparser.ast.nodeTypes.NodeWithRange;
import com.github.javaparser.ast.nodeTypes.NodeWithScope;
import com.github.javaparser.ast.nodeTypes.NodeWithTokenRange;
import com.github.javaparser.ast.observer.AstObserver;
import com.github.javaparser.ast.observer.ObservableProperty;
import com.github.javaparser.ast.observer.PropagatingAstObserver;
import com.github.javaparser.ast.visitor.CloneVisitor;
import com.github.javaparser.ast.visitor.EqualsVisitor;
import com.github.javaparser.ast.visitor.HashCodeVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.metamodel.InternalProperty;
import com.github.javaparser.metamodel.JavaParserMetaModel;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.OptionalProperty;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.printer.ConfigurablePrinter;
import com.github.javaparser.printer.DefaultPrettyPrinter;
import com.github.javaparser.printer.Printer;
import com.github.javaparser.printer.configuration.DefaultConfigurationOption;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration;
import com.github.javaparser.printer.configuration.DefaultPrinterConfiguration.ConfigOption;
import com.github.javaparser.printer.configuration.PrinterConfiguration;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.utils.LineSeparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterators;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Base class for all nodes of the abstract syntax tree.
 * <h2>Construction</h2>
 * <p>The tree is built by instantiating the required nodes, then adding them to other nodes.
 * If it is the parser who is building the tree, it will use the largest constructor,
 * the one with "range" as the first parameter.
 * If you want to manually instantiate nodes, we suggest to...
 * <ul>
 * <li>use a convenience method, like "addStatement(...)", or if none are available...</li>
 * <li>use a convenient constructor, like ClassOrInterfaceType(String name), or if none are available...</li>
 * <li>use the default constructor.</li>
 * <li>Alternatively, use one of the JavaParser.parse(snippet) methods.</li>
 * </ul>
 * ... and use the various methods on the node to initialize it further, if needed.
 * <h2>Parent/child</h2>
 * <p>The parent node field is managed automatically and can be seen as read only.
 * Note that there is only one parent,
 * and trying to use the same node in two places will lead to unexpected behaviour.
 * It is advised to clone() a node before moving it around.
 * <h2>Comments</h2>
 * <p>Each Node can have one associated comment which describes it and
 * a number of "orphan comments" which it contains but are not specifically
 * associated to any child.
 * <h2>Positions</h2>
 * <p>When the parser creates nodes, it sets their source code position in the "range" field.
 * When you manually instantiate nodes, their range is not set.
 * The top left character is position 1, 1.
 * Note that since this is an <i>abstract</i> syntax tree,
 * it leaves out a lot of text from the original source file,
 * like where braces or comma's are exactly.
 * Therefore there is no position information on everything in the original source file.
 * <h2>Observers</h2>
 * <p>It is possible to add observers to the tree.
 * Any change in the tree is sent as an event to any observers watching.
 * <h2>Visitors</h2>
 * <p>The most comfortable way of working with an abstract syntax tree is using visitors.
 * You can use one of the visitors in the visitor package, or extend one of them.
 * A visitor can be "run" by calling accept on a node:
 * <pre>node.accept(visitor, argument);</pre>
 * where argument is an object of your choice (often simply null.)
 *
 * @author Julio Vilmar Gesser
 */
public abstract class Node implements Cloneable, HasParentNode<Node>, Visitable, NodeWithRange<Node>, NodeWithTokenRange<Node> {

    /**
     * Different registration mode for observers on nodes.
     */
    public enum ObserverRegistrationMode {

        /**
         * Notify exclusively for changes happening on this node alone.
         */
        JUST_THIS_NODE,
        /**
         * Notify for changes happening on this node and all its descendants existing at the moment in
         * which the observer was registered. Nodes attached later will not be observed.
         */
        THIS_NODE_AND_EXISTING_DESCENDANTS,
        /**
         * Notify for changes happening on this node and all its descendants. The descendants existing at the moment in
         * which the observer was registered will be observed immediately. As new nodes are attached later they are
         * automatically registered to be observed.
         */
        SELF_PROPAGATING
    }

    public enum Parsedness {

        PARSED, UNPARSABLE
    }

    /**
     * This can be used to sort nodes on position.
     */
    public static Comparator<NodeWithRange<?>> NODE_BY_BEGIN_POSITION = (a, b) -> {
        if (a.hasRange() && b.hasRange()) {
            return a.getRange().get().begin.compareTo(b.getRange().get().begin);
        }
        if (a.hasRange() || b.hasRange()) {
            if (a.hasRange()) {
                return 1;
            }
            return -1;
        }
        return 0;
    };

    // usefull to find if the node is a phantom node
    private static final int LEVELS_TO_EXPLORE = 3;

    protected static final PrinterConfiguration prettyPrinterNoCommentsConfiguration = new DefaultPrinterConfiguration().removeOption(new DefaultConfigurationOption(ConfigOption.PRINT_COMMENTS));

    @InternalProperty
    private Range range;

    @InternalProperty
    private TokenRange tokenRange;

    @InternalProperty
    private Node parentNode;

    @InternalProperty
    private ArrayList<Node> childNodes = new ArrayList<>(0);

    @InternalProperty
    private ArrayList<Comment> orphanComments = new ArrayList<>(0);

    @InternalProperty
    private IdentityHashMap<DataKey<?>, Object> data = null;

    @OptionalProperty
    private Comment comment;

    /**
     * for KeY, holds JML comments
     */
    @OptionalProperty
    private NodeList<Comment> associatedSpecificationComments;

    @InternalProperty
    private ArrayList<AstObserver> observers = new ArrayList<>(0);

    @InternalProperty
    private Parsedness parsed = PARSED;

    protected Node(TokenRange tokenRange) {
        setTokenRange(tokenRange);
    }

    /**
     * Called in every constructor for node specific code.
     * It can't be written in the constructor itself because it will
     * be overwritten during code generation.
     */
    protected void customInitialization() {
    }

    /*
     * If there is a printer defined in CompilationUnit, returns it
     * else create a new DefaultPrettyPrinter with default parameters
     */
    protected Printer getPrinter() {
        return findCompilationUnit().map(c -> c.getPrinter()).orElseGet(() -> createDefaultPrinter());
    }

    /*
     * Return the printer initialized with the specified configuration
     */
    protected Printer getPrinter(PrinterConfiguration configuration) {
        return findCompilationUnit().map(c -> c.getPrinter(configuration)).orElseGet(() -> createDefaultPrinter(configuration));
    }

    protected Printer createDefaultPrinter() {
        return createDefaultPrinter(getDefaultPrinterConfiguration());
    }

    protected Printer createDefaultPrinter(PrinterConfiguration configuration) {
        return new DefaultPrettyPrinter(configuration);
    }

    /*
     * returns a default printer configuration
     */
    protected PrinterConfiguration getDefaultPrinterConfiguration() {
        return new DefaultPrinterConfiguration();
    }

    /**
     * This is a comment associated with this node.
     *
     * @return comment property
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Optional<Comment> getComment() {
        return Optional.ofNullable(comment);
    }

    /**
     * @return the range of characters in the source code that this node covers.
     */
    @Override
    public Optional<Range> getRange() {
        return Optional.ofNullable(range);
    }

    /**
     * @return the range of tokens that this node covers.
     */
    @Override
    public Optional<TokenRange> getTokenRange() {
        return Optional.ofNullable(tokenRange);
    }

    @Override
    public Node setTokenRange(TokenRange tokenRange) {
        this.tokenRange = tokenRange;
        if (tokenRange == null || !(tokenRange.getBegin().hasRange() && tokenRange.getEnd().hasRange())) {
            range = null;
        } else {
            range = new Range(tokenRange.getBegin().getRange().get().begin, tokenRange.getEnd().getRange().get().end);
        }
        return this;
    }

    /**
     * @param range the range of characters in the source code that this node covers. null can be used to indicate that
     *              no range information is known, or that it is not of interest.
     */
    @Override
    public Node setRange(Range range) {
        if (this.range == range) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.RANGE, this.range, range);
        this.range = range;
        return this;
    }

    /**
     * Use this to store additional information to this node.
     *
     * @param comment to be set
     */
    public Node setComment(final Comment comment) {
        if (this.comment == comment) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.COMMENT, this.comment, comment);
        if (this.comment != null) {
            this.comment.setCommentedNode(null);
        }
        this.comment = comment;
        if (comment != null) {
            this.comment.setCommentedNode(this);
        }
        return this;
    }

    /**
     * Use this to store additional information to this node.
     *
     * @param comment to be set
     */
    public final Node setLineComment(String comment) {
        return setComment(new LineComment(comment));
    }

    /**
     * Use this to store additional information to this node.
     *
     * @param comment to be set
     */
    public final Node setBlockComment(String comment) {
        return setComment(new BlockComment(comment));
    }

    /**
     * @return pretty printed source code for this node and its children.
     */
    @Override
    public final String toString() {
        Printer printer = getPrinter();
        if (containsData(LINE_SEPARATOR_KEY)) {
            LineSeparator lineSeparator = getLineEndingStyleOrDefault(LineSeparator.SYSTEM);
            if (printer instanceof ConfigurablePrinter) {
                ConfigurablePrinter configurablePrinter = (ConfigurablePrinter) printer;
                PrinterConfiguration config = configurablePrinter.getConfiguration();
                if (config != null) {
                    config.addOption(new DefaultConfigurationOption(
                            ConfigOption.END_OF_LINE_CHARACTER, lineSeparator.asRawString()));
                    configurablePrinter.setConfiguration(config);
                }
            }
        }
        return printer.print(this);
    }

    /**
     * @return pretty printed source code for this node and its children.
     * Formatting can be configured with parameter PrinterConfiguration.
     */
    public final String toString(PrinterConfiguration configuration) {
        Printer printer = getPrinter();
        if (!(printer instanceof ConfigurablePrinter)) {
            return printer.print(this);
        }
        ConfigurablePrinter configurablePrinter = (ConfigurablePrinter) printer;
        // save the current configuration
        PrinterConfiguration previousConfiguration = configurablePrinter.getConfiguration();
        // print with the new configuration
        String result = getPrinter(configuration).print(this);
        // restore the previous printer configuration (issue 4163)
        configurablePrinter.setConfiguration(previousConfiguration);
        return result;
    }

    @Override
    public final int hashCode() {
        return HashCodeVisitor.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }
        return EqualsVisitor.equals(this, (Node) obj);
    }

    @Override
    public Optional<Node> getParentNode() {
        return Optional.ofNullable(parentNode);
    }

    /**
     * Contains all nodes that have this node set as their parent.
     * You can add and remove nodes from this list by adding or removing nodes from the fields of this node.
     *
     * @return all nodes that have this node as their parent.
     */
    public List<Node> getChildNodes() {
        return unmodifiableList(childNodes);
    }

    public void addOrphanComment(Comment comment) {
        notifyPropertyChange(ObservableProperty.COMMENT, null, comment);
        orphanComments.add(comment);
        comment.setParentNode(this);
    }

    public boolean removeOrphanComment(Comment comment) {
        boolean removed = orphanComments.remove(comment);
        if (removed) {
            notifyPropertyChange(ObservableProperty.COMMENT, comment, null);
            comment.setParentNode(null);
            orphanComments.trimToSize();
        }
        return removed;
    }

    /**
     * This is a list of Comment which are inside the node and are not associated
     * with any meaningful AST Node.
     * <p>
     * For example, comments at the end of methods (immediately before the parenthesis)
     * or at the end of CompilationUnit are orphan comments.
     * <p>
     * When more than one comment preceeds a statement, the one immediately preceding it
     * it is associated with the statements, while the others are orphans.
     * <p>
     * Changes to this list are not persisted.
     *
     * @return all comments that cannot be attributed to a concept
     */
    public List<Comment> getOrphanComments() {
        return unmodifiableList(orphanComments);
    }

    /**
     * This is the list of Comment which are contained in the Node either because
     * they are properly associated to one of its children or because they are floating
     * around inside the Node
     *
     * @return all Comments within the node as a list
     */
    public List<Comment> getAllContainedComments() {
        List<Comment> comments = new LinkedList<>(orphanComments);
        for (Node child : getChildNodes()) {
            child.getComment().ifPresent(comments::add);
            comments.addAll(child.getAllContainedComments());
        }
        return comments;
    }

    /**
     * Assign a new parent to this node, removing it
     * from the list of children of the previous parent, if any.
     *
     * @param newParentNode node to be set as parent
     */
    @Override
    public Node setParentNode(Node newParentNode) {
        if (newParentNode == parentNode) {
            return this;
        }
        observers.forEach(o -> o.parentChange(this, parentNode, newParentNode));
        // remove from old parent, if any
        if (parentNode != null) {
            final ArrayList<Node> parentChildNodes = parentNode.childNodes;
            for (int i = 0; i < parentChildNodes.size(); i++) {
                if (parentChildNodes.get(i) == this) {
                    parentChildNodes.remove(i);
                }
            }
            parentChildNodes.trimToSize();
        }
        parentNode = newParentNode;
        // add to new parent, if any
        if (parentNode != null) {
            parentNode.childNodes.add(this);
        }
        return this;
    }

    protected void setAsParentNodeOf(Node childNode) {
        if (childNode != null) {
            childNode.setParentNode(getParentNodeForChildren());
        }
    }

    /**
     * @deprecated Use {@link Position#ABSOLUTE_BEGIN_LINE}
     */
    @Deprecated
    public static final int ABSOLUTE_BEGIN_LINE = Position.ABSOLUTE_BEGIN_LINE;

    /**
     * @deprecated Use {@link Position#ABSOLUTE_END_LINE}
     */
    @Deprecated
    public static final int ABSOLUTE_END_LINE = Position.ABSOLUTE_END_LINE;

    public void tryAddImportToParentCompilationUnit(Class<?> clazz) {
        findAncestor(CompilationUnit.class).ifPresent(p -> p.addImport(clazz));
    }

    /**
     * Recursively finds all nodes of a certain type.
     *
     * @param clazz the type of node to find.
     * @deprecated use {@link Node#findAll(Class)} but be aware that findAll also considers the initial node.
     */
    @Deprecated
    public <N extends Node> List<N> getChildNodesByType(Class<N> clazz) {
        List<N> nodes = new ArrayList<>();
        for (Node child : getChildNodes()) {
            if (clazz.isInstance(child)) {
                nodes.add(clazz.cast(child));
            }
            nodes.addAll(child.getChildNodesByType(clazz));
        }
        return nodes;
    }

    /**
     * @deprecated use {@link Node#findAll(Class)} but be aware that findAll also considers the initial node.
     */
    @Deprecated
    public <N extends Node> List<N> getNodesByType(Class<N> clazz) {
        return getChildNodesByType(clazz);
    }

    /**
     * Gets data for this node using the given key.
     *
     * @param <M> The type of the data.
     * @param key The key for the data
     * @return The data.
     * @throws IllegalStateException if the key was not set in this node.
     * @see Node#containsData(DataKey)
     * @see DataKey
     */
    @SuppressWarnings("unchecked")
    public <M> M getData(final DataKey<M> key) {
        if (data == null) {
            throw new IllegalStateException("No data of this type found. Use containsData to check for this first.");
        }
        M value = (M) data.get(key);
        if (value == null) {
            throw new IllegalStateException("No data of this type found. Use containsData to check for this first.");
        }
        return value;
    }

    /**
     * Gets data for this node using the given key or returns an {@code Optional.empty()}.
     *
     * @param <M> The type of the data.
     * @param key The key for the data
     * @return The data.
     * @see DataKey
     */
    @SuppressWarnings("unchecked")
    public <M> Optional<M> findData(final DataKey<M> key) {
        if (containsData(key)) {
            return Optional.of(getData(key));
        }
        return Optional.empty();
    }

    /**
     * This method was added to support the clone method.
     *
     * @return all known data keys.
     */
    public Set<DataKey<?>> getDataKeys() {
        if (data == null) {
            return emptySet();
        }
        return data.keySet();
    }

    /**
     * Sets data for this node using the given key.
     * For information on creating DataKey, see {@link DataKey}.
     *
     * @param <M>    The type of data
     * @param key    The singleton key for the data
     * @param object The data object
     * @see DataKey
     */
    public <M> void setData(DataKey<M> key, M object) {
        if (data == null) {
            data = new IdentityHashMap<>();
        }
        data.put(key, object);
    }

    /**
     * @return does this node have data for this key?
     * @see DataKey
     */
    public boolean containsData(DataKey<?> key) {
        if (data == null) {
            return false;
        }
        return data.containsKey(key);
    }

    /**
     * Remove data by key.
     *
     * @see DataKey
     */
    public void removeData(DataKey<?> key) {
        if (data != null) {
            data.remove(key);
        }
    }

    /**
     * Try to remove this node from the parent
     *
     * @return true if removed, false if it is a required property of the parent, or if the parent isn't set.
     * @throws RuntimeException if it fails in an unexpected way
     */
    public boolean remove() {
        if (parentNode == null) {
            return false;
        }
        return parentNode.remove(this);
    }

    /**
     * Try to replace this node in the parent with the supplied node.
     *
     * @return true if removed, or if the parent isn't set.
     * @throws RuntimeException if it fails in an unexpected way
     */
    public boolean replace(Node node) {
        if (parentNode == null) {
            return false;
        }
        return parentNode.replace(this, node);
    }

    /**
     * Forcibly removes this node from the AST.
     * If it cannot be removed from the parent with remove(),
     * it will try to remove its parent instead,
     * until it finds a node that can be removed,
     * or no parent can be found.
     * <p>
     * Since everything at CompilationUnit level is removable,
     * this method will only (silently) fail when the node is in a detached AST fragment.
     */
    public void removeForced() {
        if (!remove()) {
            getParentNode().ifPresent(Node::remove);
        }
    }

    @Override
    public Node getParentNodeForChildren() {
        return this;
    }

    protected void setAsParentNodeOf(NodeList<? extends Node> list) {
        if (list != null) {
            list.setParentNode(getParentNodeForChildren());
        }
    }

    public <P> void notifyPropertyChange(ObservableProperty property, P oldValue, P newValue) {
        this.observers.forEach(o -> o.propertyChange(this, property, oldValue, newValue));
    }

    @Override
    public void unregister(AstObserver observer) {
        this.observers.remove(observer);
        this.observers.trimToSize();
    }

    @Override
    public void register(AstObserver observer) {
        // Check if the observer is not registered yet.
        // In this case we use a List instead of Set to save on memory space.
        if (!this.observers.contains(observer)) {
            this.observers.add(observer);
        }
    }

    /**
     * Register a new observer for the given node. Depending on the mode specified also descendants, existing
     * and new, could be observed. For more details see <i>ObserverRegistrationMode</i>.
     */
    public void register(AstObserver observer, ObserverRegistrationMode mode) {
        if (mode == null) {
            throw new IllegalArgumentException("Mode should be not null");
        }
        switch(mode) {
            case JUST_THIS_NODE:
                register(observer);
                break;
            case THIS_NODE_AND_EXISTING_DESCENDANTS:
                registerForSubtree(observer);
                break;
            case SELF_PROPAGATING:
                registerForSubtree(PropagatingAstObserver.transformInPropagatingObserver(observer));
                break;
            default:
                throw new UnsupportedOperationException("This mode is not supported: " + mode);
        }
    }

    /**
     * Register the observer for the current node and all the contained node and nodelists, recursively.
     */
    public void registerForSubtree(AstObserver observer) {
        register(observer);
        this.getChildNodes().forEach(c -> c.registerForSubtree(observer));
        for (PropertyMetaModel property : getMetaModel().getAllPropertyMetaModels()) {
            if (property.isNodeList()) {
                NodeList<?> nodeList = (NodeList<?>) property.getValue(this);
                if (nodeList != null)
                    nodeList.register(observer);
            }
        }
    }

    @Override
    public boolean isRegistered(AstObserver observer) {
        return this.observers.contains(observer);
    }

    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public boolean remove(Node node) {
        if (node == null) {
            return false;
        }
        if (associatedSpecificationComments != null) {
            for (int i = 0; i < associatedSpecificationComments.size(); i++) {
                if (associatedSpecificationComments.get(i) == node) {
                    associatedSpecificationComments.remove(i);
                    return true;
                }
            }
        }
        if (comment != null) {
            if (node == comment) {
                removeComment();
                return true;
            }
        }
        return false;
    }

    @Generated("com.github.javaparser.generator.core.node.RemoveMethodGenerator")
    public Node removeComment() {
        return setComment((Comment) null);
    }

    @Override
    @Generated("com.github.javaparser.generator.core.node.CloneGenerator")
    public Node clone() {
        return (Node) accept(new CloneVisitor(), null);
    }

    /**
     * @return get JavaParser specific node introspection information.
     */
    @Generated("com.github.javaparser.generator.core.node.GetMetaModelGenerator")
    public NodeMetaModel getMetaModel() {
        return JavaParserMetaModel.nodeMetaModel;
    }

    /**
     * @return whether this node was successfully parsed or not.
     * If it was not, only the range and tokenRange fields will be valid.
     */
    public Parsedness getParsed() {
        return parsed;
    }

    /**
     * Used by the parser to flag unparsable nodes.
     */
    public Node setParsed(Parsedness parsed) {
        this.parsed = parsed;
        return this;
    }

    @Generated("com.github.javaparser.generator.core.node.ReplaceMethodGenerator")
    public boolean replace(Node node, Node replacementNode) {
        if (node == null) {
            return false;
        }
        if (associatedSpecificationComments != null) {
            for (int i = 0; i < associatedSpecificationComments.size(); i++) {
                if (associatedSpecificationComments.get(i) == node) {
                    associatedSpecificationComments.set(i, (Comment) replacementNode);
                    return true;
                }
            }
        }
        if (comment != null) {
            if (node == comment) {
                setComment((Comment) replacementNode);
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the root node of this AST by finding the topmost parent.
     */
    public Node findRootNode() {
        Node n = this;
        while (n.getParentNode().isPresent()) {
            n = n.getParentNode().get();
        }
        return n;
    }

    /**
     * @return the containing CompilationUnit, or empty if this node is not inside a compilation unit.
     */
    public Optional<CompilationUnit> findCompilationUnit() {
        Node rootNode = findRootNode();
        if (rootNode instanceof CompilationUnit) {
            return Optional.of((CompilationUnit) rootNode);
        }
        return Optional.empty();
    }

    public LineSeparator getLineEndingStyleOrDefault(LineSeparator defaultLineSeparator) {
        if (getLineEndingStyle().isStandardEol()) {
            return getLineEndingStyle();
        }
        return defaultLineSeparator;
    }

    public LineSeparator getLineEndingStyle() {
        Node current = this;
        // First check this node
        if (current.containsData(Node.LINE_SEPARATOR_KEY)) {
            LineSeparator lineSeparator = current.getData(Node.LINE_SEPARATOR_KEY);
            return lineSeparator;
        }
        // Then check parent/ancestor nodes
        while (current.getParentNode().isPresent()) {
            current = current.getParentNode().get();
            if (current.containsData(Node.LINE_SEPARATOR_KEY)) {
                return current.getData(Node.LINE_SEPARATOR_KEY);
            }
        }
        // Default to the system line separator if it's not already set within the parsed node/code.
        return LineSeparator.SYSTEM;
    }

    public SymbolResolver getSymbolResolver() {
        return findCompilationUnit().map(cu -> {
            if (cu.containsData(SYMBOL_RESOLVER_KEY)) {
                return cu.getData(SYMBOL_RESOLVER_KEY);
            }
            throw new IllegalStateException("Symbol resolution not configured: to configure consider setting a SymbolResolver in the ParserConfiguration");
        }).orElseThrow(() -> new IllegalStateException("The node is not inserted in a CompilationUnit"));
    }

    // We need to expose it because we will need to use it to inject the SymbolSolver
    public static final DataKey<SymbolResolver> SYMBOL_RESOLVER_KEY = new DataKey<SymbolResolver>() {
    };

    public static final DataKey<LineSeparator> LINE_SEPARATOR_KEY = new DataKey<LineSeparator>() {
    };

    // We need to expose it because we will need to use it to inject the printer
    public static final DataKey<Printer> PRINTER_KEY = new DataKey<Printer>() {};

    protected static final DataKey<Boolean> PHANTOM_KEY = new DataKey<Boolean>() {
    };

    public enum TreeTraversal {

        PREORDER, BREADTHFIRST, POSTORDER, PARENTS, DIRECT_CHILDREN
    }

    private Iterator<Node> treeIterator(TreeTraversal traversal) {
        switch(traversal) {
            case BREADTHFIRST:
                return new BreadthFirstIterator(this);
            case POSTORDER:
                return new PostOrderIterator(this);
            case PREORDER:
                return new PreOrderIterator(this);
            case DIRECT_CHILDREN:
                return new DirectChildrenIterator(this);
            case PARENTS:
                return new ParentsVisitor(this);
            default:
                throw new IllegalArgumentException("Unknown traversal choice.");
        }
    }

    private Iterable<Node> treeIterable(TreeTraversal traversal) {
        return () -> treeIterator(traversal);
    }

    /**
     * Make a stream of nodes using traversal algorithm "traversal".
     */
    public Stream<Node> stream(TreeTraversal traversal) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(treeIterator(traversal), NONNULL | DISTINCT), false);
    }

    /**
     * Make a stream of nodes using pre-order traversal.
     */
    public Stream<Node> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(treeIterator(PREORDER), NONNULL | DISTINCT), false);
    }

    /**
     * Walks the AST, calling the consumer for every node, with traversal algorithm "traversal".
     * <br>This is the most general walk method. All other walk and findAll methods are based on this.
     */
    public void walk(TreeTraversal traversal, Consumer<Node> consumer) {
        // Could be implemented as a call to the above walk method, but this is a little more efficient.
        for (Node node : treeIterable(traversal)) {
            consumer.accept(node);
        }
    }

    /**
     * Walks the AST, calling the consumer for every node with pre-order traversal.
     */
    public void walk(Consumer<Node> consumer) {
        walk(PREORDER, consumer);
    }

    /**
     * Walks the AST with pre-order traversal, calling the consumer for every node of type "nodeType".
     */
    public <T extends Node> void walk(Class<T> nodeType, Consumer<T> consumer) {
        walk(TreeTraversal.PREORDER, node -> {
            if (nodeType.isAssignableFrom(node.getClass())) {
                consumer.accept(nodeType.cast(node));
            }
        });
    }

    /**
     * Walks the AST with pre-order traversal, returning all nodes of type "nodeType".
     */
    public <T extends Node> List<T> findAll(Class<T> nodeType) {
        final List<T> found = new ArrayList<>();
        walk(nodeType, found::add);
        return found;
    }

    /**
     * Walks the AST with specified traversal order, returning all nodes of type "nodeType".
     */
    public <T extends Node> List<T> findAll(Class<T> nodeType, TreeTraversal traversal) {
        final List<T> found = new ArrayList<>();
        walk(traversal, node -> {
            if (nodeType.isAssignableFrom(node.getClass())) {
                found.add(nodeType.cast(node));
            }
        });
        return found;
    }

    /**
     * Walks the AST with pre-order traversal, returning all nodes of type "nodeType" that match the predicate.
     */
    public <T extends Node> List<T> findAll(Class<T> nodeType, Predicate<T> predicate) {
        final List<T> found = new ArrayList<>();
        walk(nodeType, n -> {
            if (predicate.test(n))
                found.add(n);
        });
        return found;
    }

    /**
     * Walks the AST, applying the function for every node, with traversal algorithm "traversal". If the function
     * returns something else than null, the traversal is stopped and the function result is returned. <br>This is the
     * most general findFirst method. All other findFirst methods are based on this.
     */
    public <T> Optional<T> findFirst(TreeTraversal traversal, Function<Node, Optional<T>> consumer) {
        for (Node node : treeIterable(traversal)) {
            final Optional<T> result = consumer.apply(node);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * Walks the AST with pre-order traversal, returning the first node of type "nodeType" or empty() if none is found.
     */
    public <N extends Node> Optional<N> findFirst(Class<N> nodeType) {
        return findFirst(TreeTraversal.PREORDER, node -> {
            if (nodeType.isAssignableFrom(node.getClass())) {
                return Optional.of(nodeType.cast(node));
            }
            return Optional.empty();
        });
    }

    /**
     * Walks the AST with pre-order traversal, returning the first node of type "nodeType" that matches "predicate" or empty() if none is
     * found.
     */
    public <N extends Node> Optional<N> findFirst(Class<N> nodeType, Predicate<N> predicate) {
        return findFirst(TreeTraversal.PREORDER, node -> {
            if (nodeType.isAssignableFrom(node.getClass())) {
                final N castNode = nodeType.cast(node);
                if (predicate.test(castNode)) {
                    return Optional.of(castNode);
                }
            }
            return Optional.empty();
        });
    }

    /*
     * Find a node by a range. The search is performed on the current node and its children.
     */
    public Optional<Node> findByRange(Range range) {
        if (isPhantom()) {
            return Optional.empty();
        }
        if (!hasRange()) {
            return Optional.empty();
        }
        if (!getRange().get().contains(range)) {
            return Optional.empty();
        }
        for (Node child : getChildNodes()) {
            Optional<Node> found = child.findByRange(range);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.of(this);
    }

    /**
     * Determines whether this node is an ancestor of the given node. A node is <i>not</i> an ancestor of itself.
     *
     * @param descendant the node for which to determine whether it has this node as an ancestor.
     * @return {@code true} if this node is an ancestor of the given node, and {@code false} otherwise.
     * @see HasParentNode#isDescendantOf(Node)
     */
    public boolean isAncestorOf(Node descendant) {
        return this != descendant && findFirst(Node.class, n -> n == descendant).isPresent();
    }

    /**
     * Performs a breadth-first node traversal starting with a given node.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Breadth-first_search">Breadth-first traversal</a>
     */
    public static class BreadthFirstIterator implements Iterator<Node> {

        private final Queue<Node> queue = new LinkedList<>();

        public BreadthFirstIterator(Node node) {
            queue.add(node);
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Node next() {
            Node next = queue.remove();
            queue.addAll(next.getChildNodes());
            return next;
        }
    }

    /**
     * Performs a simple traversal over all nodes that have the passed node as their parent.
     */
    public static class DirectChildrenIterator implements Iterator<Node> {

        private final Iterator<Node> childrenIterator;

        public DirectChildrenIterator(Node node) {
            childrenIterator = node.getChildNodes().iterator();
        }

        @Override
        public boolean hasNext() {
            return childrenIterator.hasNext();
        }

        @Override
        public Node next() {
            return childrenIterator.next();
        }
    }

    /**
     * Iterates over the parent of the node, then the parent's parent, then the parent's parent's parent, until running
     * out of parents.
     */
    public static class ParentsVisitor implements Iterator<Node> {

        private Node node;

        public ParentsVisitor(Node node) {
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node.getParentNode().isPresent();
        }

        @Override
        public Node next() {
            node = node.getParentNode().orElse(null);
            return node;
        }
    }

    /**
     * Performs a pre-order (or depth-first) node traversal starting with a given node.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Pre-order">Pre-order traversal</a>
     */
    public static class PreOrderIterator implements Iterator<Node> {

        private final Stack<Node> stack = new Stack<>();

        public PreOrderIterator(Node node) {
            stack.add(node);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public Node next() {
            Node next = stack.pop();
            List<Node> children = next.getChildNodes();
            for (int i = children.size() - 1; i >= 0; i--) {
                stack.add(children.get(i));
            }
            return next;
        }
    }

    /**
     * Performs a post-order (or leaves-first) node traversal starting with a given node.
     *
     * @see <a href="https://en.wikipedia.org/wiki/Post-order">Post-order traversal</a>
     */
    public static class PostOrderIterator implements Iterator<Node> {

        private final Stack<Level> stack = new Stack<>();

        public PostOrderIterator(Node root) {
            stack.push(new Level(Collections.singletonList(root)));
        }

        @Override
        public boolean hasNext() {
            return !stack.empty();
        }

        @Override
        public Node next() {
            while (true) {
                Level state = stack.peek();
                if (state.isCurrentExpanded()) {
                    return getNextAndCleanupStack(state);
                }
                expand(state);
            }
        }

        private Node getNextAndCleanupStack(Level state) {
            Node result = state.getCurrent();
            state.goToNext();
            cleanupStack(state);
            return result;
        }

        private void cleanupStack(Level state) {
            if (!state.done()) {
                stack.pop();
            }
        }

        private void expand(Level state) {
            List<Node> children = state.getCurrent().getChildNodes();
            if (!children.isEmpty()) {
                stack.push(new Level(children));
            }
            state.setCurrentExpanded();
        }

        /**
         * Represents a level in the traversal stack during the post-order iteration. A level consists of a list of
         * siblings to be traversed, an index indicating the current node, and a flag to indicate if the current node
         * has been expanded, i.e., if its children have been processed.
         */
        private static class Level {

            private final List<Node> nodes;

            private int index = 0;

            private boolean expanded = false;

            public Level(List<Node> nodes) {
                this.nodes = nodes;
            }

            /**
             * Returns {@code true} if the last node was reached.
             *
             * @return {@code true} if the last node was reached
             */
            public boolean done() {
                return index < nodes.size();
            }

            /**
             * Returns the current node.
             *
             * @return the current node
             */
            public Node getCurrent() {
                return nodes.get(index);
            }

            /**
             * Sets the next node as the current node.
             */
            public void goToNext() {
                index++;
                expanded = false;
            }

            /**
             * Marks the current node as expanded.
             */
            public void setCurrentExpanded() {
                expanded = true;
            }

            /**
             * Returns {@code true} if the current node was expanded.
             *
             * @return {@code true} if the current node was expanded
             */
            public boolean isCurrentExpanded() {
                return expanded;
            }
        }
    }

    /*
     * Returns true if the node has an (optional) scope expression eg. method calls (object.method())
     */
    public boolean hasScope() {
        return (NodeWithOptionalScope.class.isAssignableFrom(this.getClass()) && ((NodeWithOptionalScope) this).getScope().isPresent()) || (NodeWithScope.class.isAssignableFrom(this.getClass()) && ((NodeWithScope) this).getScope() != null);
    }

    /*
     * A "phantom" node, is a node that is not really an AST node (like the fake type of variable in FieldDeclaration or an UnknownType)
     */
    public boolean isPhantom() {
        return isPhantom(this);
    }

    private boolean isPhantom(Node node) {
        if (!node.containsData(PHANTOM_KEY)) {
            boolean res = (node.getParentNode().isPresent() && node.getParentNode().get().hasRange() && node.hasRange() && !node.getParentNode().get().getRange().get().contains(node.getRange().get()) || inPhantomNode(node, LEVELS_TO_EXPLORE));
            node.setData(PHANTOM_KEY, res);
        }
        return node.getData(PHANTOM_KEY);
    }

    /**
     * A node contained in a phantom node is also a phantom node. We limit how many levels up we check just for performance reasons.
     */
    private boolean inPhantomNode(Node node, int levels) {
        return node.getParentNode().isPresent() && (isPhantom(node.getParentNode().get()) || inPhantomNode(node.getParentNode().get(), levels - 1));
    }

    /**
     * This field is used by key to associated (JML) comments to this node.
     */
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Optional<NodeList<Comment>> getAssociatedSpecificationComments() {
        return Optional.ofNullable(associatedSpecificationComments);
    }

    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Node setAssociatedSpecificationComments(final NodeList<Comment> associatedSpecificationComments) {
        if (associatedSpecificationComments == this.associatedSpecificationComments) {
            return this;
        }
        notifyPropertyChange(ObservableProperty.ASSOCIATED_SPECIFICATION_COMMENTS, this.associatedSpecificationComments, associatedSpecificationComments);
        if (this.associatedSpecificationComments != null)
            this.associatedSpecificationComments.setParentNode(null);
        this.associatedSpecificationComments = associatedSpecificationComments;
        setAsParentNodeOf(associatedSpecificationComments);
        return this;
    }

    @Nullable()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public NodeList<Comment> associatedSpecificationComments() {
        return associatedSpecificationComments;
    }

    @Nullable()
    @Generated("com.github.javaparser.generator.core.node.PropertyGenerator")
    public Comment comment() {
        return comment;
    }
}
