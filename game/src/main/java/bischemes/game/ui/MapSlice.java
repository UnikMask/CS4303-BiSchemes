package bischemes.game.ui;


import bischemes.level.util.LevelParseException;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;

import static java.lang.Math.max;

/** A class used to hold LevelNodes grouped into a vertical section based off TemplateNode's depth values. Several
 * MapSlices comprise a LevelMap*/
public final class MapSlice {

    private static final float SIDE_WIDTH = 0.2f;
    private static final int DEFAULT_REAL_HEIGHT = 900;
    private static final int WIDTH_FACTOR = 100;
    public static final int NODE_WIDTH = 60;

    /** The preceding MapSlice, used for recursive LevelNode searches*/
    public final MapSlice previous;
    /** The LevelNodes held in this MapSlice */
    public final LevelNode[] nodes;

    /** width of the MapSlice */
    public final int width;
    /** height of the MapSlice */
    public final int height;

    /**
     * Creates a new MapSlice to represent a vertical partition of an overall LevelMap
     * @param previous The preceding MapSlice (or null if this is the root MapSlice)
     * @param sliceNodes TemplateNodes which share a depth, each TemplateNode will correspond to a LevelNode in 'nodes'
     */
    public MapSlice(MapSlice previous, ArrayList<TemplateNode> sliceNodes) {
        this.previous = previous;
        nodes = new LevelNode[sliceNodes.size()];

        // Organise nodes into groups
        ArrayList<TemplateNode> sideNodes = new ArrayList<>(); // lonelyLeaf nodes
        ArrayList<TemplateNode> edgeNodes = new ArrayList<>(); // lonelyLeaf parents
        ArrayList<TemplateNode> coreNodes = new ArrayList<>(); // all other nodes
        for (TemplateNode node : sliceNodes) {
            if (node.isLeaf && node.isLonely) sideNodes.add(node);
            else if (!node.isRoot){
                if (node.hasLonelyLeaf) edgeNodes.add(node);
                else coreNodes.add(node);
                for (int id : node.dependencies) node.nodeHeight += getHeight(id);
                node.nodeHeight /= node.dependencies.length; // calculate nodeHeight as average heights of dependencies
            }
            else {
                coreNodes.add(node);
                node.nodeHeight = 0.5f; // root node is in the middle
            }
        }

        // sortNodes is a sorted combination of coreNodes and edgeNodes, ordered by their nodeHeight values. (n^2 time)
        ArrayList<TemplateNode> sortNodes = new ArrayList<>(coreNodes.size() + edgeNodes.size());
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
        // add edgeNodes to the beginning/end of sortNodes based off the nodeHeight of their dependencies
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
            default :
                throw new LevelParseException("Too many hasLonelyLeaf nodes at depth " + sliceNodes.get(0).depth +
                        " please re-arrange Level prerequisites");
        }

        // Calculate an actual height for every node in sortNode based off their index in the list and create a
        // corresponding LevelNode for every sortNode
        float increment = (1f - (2f * SIDE_WIDTH)) / (sortNodes.size() + 1f);
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


        // Sort side nodes (lonely leaf nodes) depending on whether their parent is at the top or bottom of sortNodes
        ArrayList<TemplateNode> bottomNodes = new ArrayList<>();
        ArrayList<TemplateNode> topNodes = new ArrayList<>();
        int index = sortNodes.size();
        for (TemplateNode node : sideNodes) {
            for (int id : node.dependencies) {
                if (id == nodes[0].getId()) bottomNodes.add(node);
                else topNodes.add(node);
            }
        }
        // Create LevelNodes for all the nodes in bottomNodes
        if (bottomNodes.size() > 0) {
            increment = 1f / (bottomNodes.size() + 1);
            for (int i = 0; i < bottomNodes.size(); i++) {
                nodes[index++] = new LevelNode(
                        bottomNodes.get(i).level,
                        new LevelNode[]{nodes[0]},
                        new PVector(increment + (i * increment), SIDE_WIDTH / 2f)
                );
            }
        }
        // Create LevelNodes for all the nodes in topNodes
        if (topNodes.size() > 0) {
            increment = 1f / (topNodes.size() + 1);
            for (int i = 0; i < topNodes.size(); i++) {
                nodes[index++] = new LevelNode(
                        topNodes.get(i).level,
                        new LevelNode[]{nodes[sortNodes.size() - 1]},
                        new PVector(increment + (i * increment), 1 - (SIDE_WIDTH / 2f))
                );
            }
        }
        // Calculate width (based on maximum length between topNodes and bottomNodes) and height (constant)
        this.width = WIDTH_FACTOR * (1 + max(1, max(topNodes.size(), bottomNodes.size())));
        this.height = DEFAULT_REAL_HEIGHT;
    }

    /**
     * Gets the slicePosition height of the specified (by id) LevelNode
     * @param id id identifying the LevelNode in this MapSlice or any preceding MapSlice
     * @return height of the specified LevelNode, or 0.5f if it doesn't exist
     */
    private float getHeight(int id) {
        LevelNode node = getLevelNode(id);
        return (node != null) ? node.getSlicePosition().y : 0.5f;
    }

    /**
     * Gets the LevelNode specified by the id from this MapSlice or any preceding MapSlice
     * @param id id identifying the LevelNode in this MapSlice or any preceding MapSlice
     * @return the LevelNode if it is found, otherwise null if the root is reached without finding the LevelNode
     */
    public LevelNode getLevelNode(int id) {
        for (LevelNode node : nodes) {
            if (node == null) continue;
            if (node.getId() == id) return node;
        }
        if (previous == null) return null;
        return previous.getLevelNode(id);
    }

    /**
     * Attempts trySelect() for every LevelNode until a match is found
     * @param mapPosition The position of the cursor which is checked to be hovering over each LevelNode
     * @return the selected LevelNode or null if no node is selected
     */
    public LevelNode getSelection(PVector mapPosition) {
        for (LevelNode node : nodes) {
            if (node.trySelect(mapPosition, NODE_WIDTH / 2f)) return node;
        }
        return null;
    }

    /**
     * Runs calcPosition() for every LevelNode, calculating their realPositions
     * @param offset sum of camera position and preceding MapSlice widths. This MapSlice's width gets added to offset.x
     */
    void calcPositions(PVector offset) {
        for (LevelNode node : nodes) node.calcPosition(offset, width, height, NODE_WIDTH);
        offset.x += width;
    }

    /**
     * Runs drawEdges() for every LevelNode, drawing their dependency edges
     * @param g PGraphics to draw the LevelNodes with
     */
    void drawEdges(PGraphics g) {
        for (LevelNode node : nodes) node.drawEdges(g);
    }

    /**
     * Runs drawNodes() for every LevelNode, drawing the rectangular nodes
     * @param g PGraphics to draw the LevelNodes with
     */
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