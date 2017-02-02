package com.insightfullogic.honest_profiler.core.aggregation.result.diff;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import com.insightfullogic.honest_profiler.core.aggregation.result.straight.Node;

/**
 * Subclass of {@link DiffEntry} which allows to arrange the items into a tree.
 */
public class DiffNode extends DiffEntry
{
    // Instance Properties

    private final Map<String, DiffNode> children;

    // Instance Constructors

    public DiffNode(Node baseNode, Node newNode)
    {
        super(baseNode, newNode);

        this.children = new HashMap<>();
        addBaseChildren(baseNode);
        addNewChildren(newNode);
    }

    /**
     * Specialized internal constructor for {@link #copyWithFilter(Predicate)}.
     *
     * @param node the {@link DiffNode} being copied
     * @param children the new, filtered childrem
     */
    private DiffNode(DiffNode node, List<DiffNode> children)
    {
        super(node.getBaseEntry(), node.getNewEntry());

        this.children = new HashMap<>();
        children.forEach(child -> this.children.put(child.getKey(), child));
    }

    // Instance Accessors

    /**
     * Sets the Base {@link Node}.
     *
     * The return value is provided as a convenience for
     * {@link TreeDiff#setBase(com.insightfullogic.honest_profiler.core.aggregation.result.straight.Tree)}.
     *
     * @param entry the Base {@link Node}
     * @return this {@link DiffNode}
     */
    public DiffNode setBase(Node node)
    {
        super.setBase(node);

        addBaseChildren(node);
        return this;
    }

    /**
     * Sets the New {@link Node}.
     *
     * The return value is provided as a convenience for
     * {@link TreeDiff#setNew(com.insightfullogic.honest_profiler.core.aggregation.result.straight.Tree)}.
     *
     * @param entry the New {@link Node}
     * @return this {@link DiffNode}
     */
    public DiffNode setNew(Node node)
    {
        super.setNew(node);

        addNewChildren(node);
        return this;
    }

    /**
     * Returns the children of this node.
     *
     * @return a {@link Collection} containing the children of this node.
     */
    public Collection<DiffNode> getChildren()
    {
        return children.values();
    }

    /**
     * Filter the descendants of this DiffNode recursively, creating copies of the "survivors". If this node has
     * survivor descendants or is accepted by the filter, the copy is returned, otherwise the method returns <null>.
     *
     * @param filter the filter to be applied to this node and its descendants.
     * @return
     */
    public DiffNode copyWithFilter(Predicate<DiffNode> filter)
    {
        List<DiffNode> newChildren = children.values().stream()
            .map(child -> child.copyWithFilter(filter)).filter(child -> child != null)
            .collect(toList());
        return newChildren.size() > 0 || filter.test(this) ? new DiffNode(this, newChildren) : null;
    }

    // Helper Methods

    /**
     * Create child DiffNodes or set the Base {@link Node} for existing ones, based on the children from the provided
     * {@link Node}.
     *
     * @param node the Base {@link Node} whose children need to be incorporated into the children of this node
     */
    private void addBaseChildren(Node node)
    {
        if (node == null)
        {
            return;
        }
        node.getChildren().forEach(this::addBaseChild);
    }

    /**
     * Create child DiffNodes or set the New {@link Node} for existing ones, based on the children from the provided
     * {@link Node}.
     *
     * @param node the Base {@link Node} whose children need to be incorporated into the children of this node
     */
    private void addNewChildren(Node node)
    {
        if (node == null)
        {
            return;
        }
        node.getChildren().forEach(this::addNewChild);
    }

    /**
     * Sets the Base {@link Node} of the correct child DiffNode to the provided {@link Node}, or create a new child
     * DiffNode if it doesn't exist yet, and set the Base {@link Node} in it to the provided {@link Node}.
     *
     * @param child the {@link Node} to be added as Base {@link Node} of a child DiffNode
     */
    private void addBaseChild(Node child)
    {
        children.compute(
            child.getKey(),
            (k, v) -> v == null ? new DiffNode(child, null) : v.setBase(child));
    }

    /**
     * Sets the New {@link Node} of the correct child DiffNode to the provided {@link Node}, or create a new child
     * DiffNode if it doesn't exist yet, and set the New {@link Node} in it to the provided {@link Node}.
     *
     * @param child the {@link Node} to be added as New {@link Node} of a child DiffNode
     */
    private void addNewChild(Node child)
    {
        children.compute(
            child.getKey(),
            (k, v) -> v == null ? new DiffNode(null, child) : v.setNew(child));
    }
}
