package bischemes.game.ui;


import bischemes.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A class used in the construction of a LevelMap. Each TemplateNode represents a node of a directed acyclic graph.
 *  Each TemplateNode represents a Level. */
public final class TemplateNode {

    /** Depth represents how far removed a TemplateNode is from a root node */
    public int depth = 0;

    /** List of TemplateNodes which follow this node. Each child as the parent TemplateNode's level as a dependency*/
    public List<TemplateNode> children = new ArrayList<>(0);

    /** The Level which the TemplateNode corresponds to */
    public final Level level;
    /** The prerequisite array of the Level is copied into TemplateNode to prevent repeated getPrerequisite() calls */
    public final int[] dependencies;

    /** True if the TemplateNode has no children */
    public boolean isLeaf = true;
    /** True if the TemplateNode has no parents (dependencies/prerequisites) */
    public final boolean isRoot;
    /** True if the TemplateNode has only one parent */
    public final boolean isLonely;
    /** True if one of the TemplateNode's children has 'isLonely' as true */
    public boolean hasLonelyLeaf = false;
    /** Calculated and provided by MapSlice. 'nodeHeight' describes the height in a MapSlice which the node appears */
    public float nodeHeight = 0f;

    /**
     * Creates a List of TemplateNodes from a HashMap if Levels (keys are level ids). Creates a TemplateNode for every
     * Level and calculates their depths, children, and boolean properties.
     * @param levels HashMap of levels with the Level ids as keys.
     * @return List of TemplateNodes with one TemplateNode for every Level in levels
     */
    public static List<TemplateNode> build(HashMap<Integer, Level> levels) {
        // Create a Map of Integers and TemplateNodes for easier lookup by ID
        Map<Integer, TemplateNode> nodes = new HashMap<>(levels.size());
        for (Map.Entry<Integer, Level> level : levels.entrySet())
            nodes.put(level.getKey(), new TemplateNode(level.getValue()));
        // Fill out the children Lists for every TemplateNode
        for (TemplateNode node : nodes.values())
            for (int id : node.dependencies)
                nodes.get(id).addChildNode(node);
        // Calculate the boolean properties of every TemplateNode and add them to a List to be returned
        List<TemplateNode> template = new ArrayList<>();
        for (TemplateNode node : nodes.values()) {
            if (node.isLeaf && node.isLonely) {
                node.depth -= 1; // LonelyLeaf TemplateNodes are moved to the same depth as their parent
                nodes.get(node.dependencies[0]).hasLonelyLeaf = true;
            }
            template.add(node);
        }
        return template;
    }

    /**
     * Constructor is private. TemplateNodes should be constructed through build().
     * @param level Level which the TemplateNode will represent.
     */
    private TemplateNode(Level level) {
        this.level = level;
        dependencies = level.getPrerequisites();
        isRoot = dependencies.length == 0;
        isLonely = dependencies.length == 1;
    }

    /**
     * Adds a TemplateNode into the children list
     * @param child The TemplateNode which has this as a dependency
     */
    private void addChildNode(TemplateNode child) {
        children.add(child);
        isLeaf = false; // A node is no longer a leaf if it has a child
        child.setDepth(depth + 1); // The depth of a child is at least one greater than the parent
    }

    /**
     * Sets the depth of this TemplateNode to be the greatest of its current depth or the provided depth.
     * If depth changes, run setDepth(depth+1) for all children in increase their depths.
     * @param depth The new depth of the TemplateNode, ignored if it isn't larger than the current depth
     */
    private void setDepth(int depth) {
        if (depth <= this.depth) return;
        this.depth = depth;
        for(TemplateNode child : children)
            child.setDepth(depth + 1);
    }

}