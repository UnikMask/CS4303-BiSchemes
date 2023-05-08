package bischemes.game.ui;


import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static java.lang.Math.max;

public final class MapSlice {

    private static final float SIDE_WIDTH = 0.2f;
    private static final float MIN_WIDTH = 0.2f;
    private static final float MIN_HEIGHT = 0.2f;
    private static final int DEFAULT_REAL_HEIGHT = 900;
    private static final int WIDTH_FACTOR = 100;
    public static final int NODE_WIDTH = 60;

    public final MapSlice previous;
    public final LevelNode[] nodes;

    public final int width;
    public final int height;

    public MapSlice(MapSlice previous, ArrayList<TemplateNode> sliceNodes) {
        this.previous = previous;
        nodes = new LevelNode[sliceNodes.size()];

        // Organise nodes into groups
        ArrayList<TemplateNode> sideNodes = new ArrayList<TemplateNode>();
        ArrayList<TemplateNode> coreNodes = new ArrayList<TemplateNode>();
        ArrayList<TemplateNode> edgeNodes = new ArrayList<TemplateNode>();
        for (TemplateNode node : sliceNodes) {
            if (node.isLeaf && node.isLonely) sideNodes.add(node);
            else if (!node.isRoot){
                if (node.hasLonelyLeaf) edgeNodes.add(node);
                else coreNodes.add(node);

                for (int id : node.dependencies) {
                    node.nodeHeight += getHeight(id);
                }
                node.nodeHeight /= node.dependencies.length;
            }
            else {
                coreNodes.add(node);
                node.nodeHeight = 0.5f;
            }
        }

        // TODO constraint on core+edge size (throw error if too many)

        // Sort central group (n^2 sort time as n<5)
        ArrayList<TemplateNode> sortNodes = new ArrayList<TemplateNode>(coreNodes.size() + edgeNodes.size());
        for (TemplateNode node : coreNodes) {
            boolean sorted = false;
            for (int i = 0; i < sortNodes.size(); i++) {
                if (sortNodes.get(i).nodeHeight > node.nodeHeight) continue;
                sortNodes.add(i, node);
                sorted = true;
                break;
            }
            if (!sorted) sortNodes.add(node);
        }

        switch(edgeNodes.size()) {
            case 0 : break;
            case 1 :
                if (edgeNodes.get(0).nodeHeight > 0.5) sortNodes.add(edgeNodes.get(0));
                else sortNodes.add(0, edgeNodes.get(0));
                break;
            case 2 :
                if (edgeNodes.get(0).nodeHeight > edgeNodes.get(1).nodeHeight) {
                    sortNodes.add(edgeNodes.get(0));
                    sortNodes.add(0, edgeNodes.get(1));
                }
                else {
                    sortNodes.add(0, edgeNodes.get(0));
                    sortNodes.add(edgeNodes.get(1));
                }
                break;
            default : break; // TODO throw error
        }

        // Position core nodes
        float increment = (float) (1f - (2f * SIDE_WIDTH)) / (sortNodes.size() + 1);
        for (int i = 0; i < sortNodes.size(); i++) {
            LevelNode[] dependencies = new LevelNode[sortNodes.get(i).dependencies.length];
            for (int j = 0; j < dependencies.length; j++) {
                dependencies[j] = getLevelNode(sortNodes.get(i).dependencies[j]);
            }
            nodes[i] = new LevelNode(
                    sortNodes.get(i).level,
                    dependencies,
                    new PVector(0.5f, SIDE_WIDTH + increment + (i * increment))
            );
        }


        // Position side nodes
        ArrayList<TemplateNode> bottomNodes = new ArrayList<TemplateNode>();
        ArrayList<TemplateNode> topNodes = new ArrayList<TemplateNode>();
        int index = sortNodes.size();

        for (TemplateNode node : sideNodes) {
            for (int id : node.dependencies) {
                if (id == nodes[0].getId()) bottomNodes.add(node);
                else topNodes.add(node);
            }
        }

        if (bottomNodes.size() > 0) {
            increment = (float) 1f / (bottomNodes.size() + 1);
            for (int i = 0; i < bottomNodes.size(); i++) {
                nodes[index++] = new LevelNode(
                        bottomNodes.get(i).level,
                        new LevelNode[]{nodes[0]},
                        new PVector(increment + (i * increment), SIDE_WIDTH / 2f)
                );
            }
        }

        if (topNodes.size() > 0) {
            increment = (float) 1f / (topNodes.size() + 1);
            for (int i = 0; i < topNodes.size(); i++) {
                nodes[index++] = new LevelNode(
                        topNodes.get(i).level,
                        new LevelNode[]{nodes[sortNodes.size() - 1]},
                        new PVector(increment + (i * increment), 1 - (SIDE_WIDTH / 2f))
                );
            }
        }

        this.width = WIDTH_FACTOR * (1 + max(1, max(topNodes.size(), bottomNodes.size())));
        this.height = DEFAULT_REAL_HEIGHT;
    }

    private float getHeight(int id) {
        LevelNode node = getLevelNode(id);
        return (node != null) ? node.getSlicePosition().y : 0.5f;
    }

    public LevelNode getLevelNode(int id) {
        for (LevelNode node : nodes) {
            if (node == null) continue;
            if (node.getId() == id) return node;
        }
        if (previous == null) return null;
        return previous.getLevelNode(id);
    }

    public LevelNode getSelection(PVector mapPosition) {
        for (LevelNode node : nodes) {
            if (node.trySelect(mapPosition, NODE_WIDTH / 2f)) return node;
        }
        return null;
    }

    void calcPositions(PVector offset) {
        for (LevelNode node : nodes) node.calcPosition(offset, width, height, NODE_WIDTH);
        offset.x += width;
    }

    void drawEdges(PGraphics g) {
        for (LevelNode node : nodes) node.drawEdges(g);
    }

    void drawNodes(PGraphics g) {
        for (LevelNode node : nodes) node.drawNode(g);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MapSlice{");
        sb.append("width = ").append(width);
        sb.append(", height = ").append(height);
        sb.append(", nodes = ").append(nodes.length);
        sb.append('}');
        for (LevelNode node : nodes) {
            sb.append("\n\t\t").append(node);
        }
        return sb.toString();
    }
}