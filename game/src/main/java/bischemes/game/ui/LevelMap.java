package bischemes.game.ui;


import bischemes.level.Level;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** A class to represent a directed graph of LevelNodes (vertically partitioned into MapSlice groups) */
public final class LevelMap {

    /** Array of all MapSlices in order (starting with root MapSlice) */
    private final MapSlice[] mapSlices;
    /** The currently selected LevelNode, storing it reduces need to recalculate selection if the cursor doesn't move */
    private LevelNode selection;

    /** width of the LevelMap, sum of all MapSlice widths */
    public final int width;
    /** height of LevelMap, maximum of all MapSlice heights */
    public final int height;

    /**
     * Constructs a new LevelMap from a HashMap of Levels
     * @param levels HashMap of Levels to build the LevelMap from (keys are Level ids)
     */
    public LevelMap(HashMap<Integer, Level> levels) {
        // Build a list of TemplateNodes to aid construction
        List<TemplateNode> template = TemplateNode.build(levels);
        // Create a list to hold MapSlices (will be used to form the final mapSlices array)
        List<MapSlice> slices = new ArrayList<>(1);
        // Creates a new MapSlice for every unit of depth 'template' possesses
        int depth = 0;
        MapSlice mapSlice = null;
        while(!template.isEmpty()) {
            // Get all TemplateNodes with the currently lowest depth
            ArrayList<TemplateNode> sliceNodes = new ArrayList<>(1);
            for (TemplateNode node : template)
                if (node.depth == depth)
                    sliceNodes.add(node);
            // Create a new MapSlice from the TemplateNodes of the lowest depth
            mapSlice = new MapSlice(mapSlice, sliceNodes);
            slices.add(mapSlice);

            depth++;
            template.removeAll(sliceNodes); // Remove TemplateNodes which have been used to create a MapSlice
        }

        // Create mapSlices array from slices List, calculating the values of width and height whilst iterating through
        mapSlices = new MapSlice[slices.size()];
        int w = 0;
        int h = 0;
        for (int i = 0; i < mapSlices.length; i++) {
            mapSlices[i] = slices.get(i);
            w += mapSlices[i].width; // width is sum of all MapSlice widths
            h = Math.max(mapSlices[i].height, h); // height is maximum MapSlice height
        }
        width = w;
        height = h;
    }

    /**
     * Determines whether the provided mouse position is hovering over one of the LevelNodes. If so, returns true and
     * sets 'selection' as the found LevelNode
     * @param cameraPosition position of the camera, used to offset position calculations
     * @param scale current scale of view
     * @param mouseX x coordinate of the cursor
     * @param mouseY y coordinate of the cursor
     * @return true if a LevelNode is currently selected, otherwise false
     */
    public boolean hasSelection(PVector cameraPosition, float scale, int mouseX, int mouseY) {
        // Calculates position of cursor within the map
        PVector mapPosition = (new PVector(mouseX, mouseY)).div(scale);
        // No need to calculate if cursor is outside LevelMap bounds
        if (mapPosition.x > width || mapPosition.x < 0) return false;
        if (mapPosition.y > height || mapPosition.y < 0) return false;
        // No need to calculate if the previous selected LevelNode is still selected
        if (selection != null && selection.trySelect(mapPosition, MapSlice.NODE_WIDTH / 2f))
            return true;
        // Find MapSlice that the cursor is within
        float xAcross = mapPosition.x - cameraPosition.x;
        for (MapSlice slice : mapSlices) {
            if (slice.width > xAcross) {
                // Check whether cursor is selecting a LevelNode in the MapSlice
                selection = slice.getSelection(mapPosition);
                return selection != null;
            }
            xAcross -= slice.width;
        }
        return false;
    }

    public LevelNode getSelection() {
        return selection;
    }

    /**
     * Draw the LevelMap, first calculating LevelNode realPositions, then drawing dependency edges, then drawing nodes
     * @param g PGraphics to draw the LevelMap with
     * @param cameraPosition camera position to offset component drawing with
     * @param scale scale at which to scale all drawing
     */
    public void draw(PGraphics g, PVector cameraPosition, float scale) {
        g.pushMatrix();
        g.scale(scale);
        g.strokeWeight(10);
        g.textAlign(PConstants.CENTER);
        for (MapSlice slice : mapSlices) slice.calcPositions(cameraPosition.copy());
        for (MapSlice slice : mapSlices) slice.drawEdges(g);
        for (MapSlice slice : mapSlices) slice.drawNodes(g);
        g.popMatrix();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LevelMap{");
        sb.append("width = ").append(width);
        sb.append(", height = ").append(height);
        sb.append(", slices = ").append(mapSlices.length);
        sb.append('}');
        for (MapSlice slice : mapSlices) {
            sb.append("\n\t").append(slice);
        }
        return sb.toString();
    }

}