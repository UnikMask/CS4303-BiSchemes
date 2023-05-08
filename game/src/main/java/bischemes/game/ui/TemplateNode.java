package bischemes.game.ui;


import bischemes.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TemplateNode {

    public int depth = 0;

    public List<TemplateNode> children = new ArrayList<>(0);
    public List<TemplateNode> parents = new ArrayList<>(0);

    public final Level level;
    public final int id;
    public final int[] dependencies;

    public boolean isLeaf = true;
    public final boolean isRoot;
    public final boolean isLonely;
    public boolean hasLonelyLeaf = false;

    public float nodeHeight = 0f;

    public static List<TemplateNode> build(HashMap<Integer, Level> levels) {

        Map<Integer, TemplateNode> nodes = new HashMap<>(levels.size());
        for (Map.Entry<Integer, Level> level : levels.entrySet())
            nodes.put(level.getKey(), new TemplateNode(level.getValue()));

        for (TemplateNode node : nodes.values())
            for (int id : node.dependencies)
                nodes.get(id).addChildNode(node);

        List<TemplateNode> template = new ArrayList<>();

        for (TemplateNode node : nodes.values()) {
            if (node.isLeaf && node.isLonely)
                node.depth -= 1;
            for (TemplateNode child : node.children)
                if (child.isLonely && child.isLeaf) {
                    node.hasLonelyLeaf = true;
                    break;
                }
            template.add(node);
        }

        return template;
    }

    private TemplateNode(Level level) {
        this.level = level;
        id = level.getId();
        dependencies = level.getPrerequisites();

        isRoot = dependencies.length == 0;
        isLonely = dependencies.length == 1;
    }


    private void addChildNode(TemplateNode child) {
        children.add(child);
        child.parents.add(this);
        isLeaf = false;
        child.setDepth(depth + 1);
    }

    private void setDepth(int depth) {
        if (depth <= this.depth) return;
        this.depth = depth;
        for(TemplateNode child : children)
            child.setDepth(depth + 1);
    }

}