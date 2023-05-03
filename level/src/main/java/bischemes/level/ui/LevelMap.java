package bischemes.level.ui;


import bischemes.level.Level;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class LevelMap {

    private final MapSlice[] mapSlices;
    private LevelNode selection;

    public final int width;
    public final int height;

    public LevelMap(HashMap<Integer, Level> levels) {

        List<TemplateNode> template = TemplateNode.build(levels);

        List<MapSlice> slices = new ArrayList<>(1);

        int depth = 0;
        MapSlice mapSlice = null;
        while(!template.isEmpty()) {
            ArrayList<TemplateNode> sliceNodes = new ArrayList<>(1);

            for (TemplateNode node : template)
                if (node.depth == depth)
                    sliceNodes.add(node);

            mapSlice = new MapSlice(mapSlice, sliceNodes);
            slices.add(mapSlice);

            depth++;
            template.removeAll(sliceNodes);
        }


        mapSlices = new MapSlice[slices.size()];
        int w = 0;
        int h = 0;
        for (int i = 0; i < mapSlices.length; i++) {
            mapSlices[i] = slices.get(i);
            w += mapSlices[i].width;
            h = Math.max(mapSlices[i].height, h);
        }
        width = w;
        height = h;
    }

    public boolean hasSelection(PVector cameraPosition, float scale, int mouseX, int mouseY) {
        PVector mapPosition = (new PVector(mouseX, mouseY)).div(scale);

        if (mapPosition.x > width || mapPosition.x < 0) return false;
        if (mapPosition.y > height || mapPosition.y < 0) return false;

        if (selection != null && selection.trySelect(mapPosition, MapSlice.NODE_WIDTH / 2f))
            return true;

        float xAcross = mapPosition.x - cameraPosition.x;
        for (MapSlice slice : mapSlices) {
            if (slice.width > xAcross) {
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

    public void draw(PGraphics g, PVector cameraPosition, float scale) {
        PVector offset = cameraPosition.copy();
        g.pushMatrix();
        g.scale(scale);
        g.strokeWeight(10);
        g.textAlign(PConstants.CENTER);
        for (MapSlice slice : mapSlices) slice.calcPositions(offset);
        for (MapSlice slice : mapSlices) slice.drawEdges(g);
        for (MapSlice slice : mapSlices) slice.drawNodes(g);
        g.popMatrix();
    }

}