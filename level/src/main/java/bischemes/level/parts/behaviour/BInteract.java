package bischemes.level.parts.behaviour;

import bischemes.engine.VisualAttribute;
import bischemes.engine.VisualUtils;
import bischemes.level.PlayerAbstract;
import bischemes.level.parts.RObject;
import bischemes.level.util.SpriteLoader;
import processing.core.PImage;
import processing.core.PVector;

public abstract class BInteract extends BUpdate {

	private final boolean useCircleProx;
	private float xRange;
	private float yRange;
	private float radius;

	private static final float INDICATOR_SCALE_RATE = 0.05f;
	protected VisualAttribute indicator = null;
	private boolean showingIndicator = false;
	private float indicatorScale = 0f;

	private boolean activeOnState = false;
	private boolean stateActivity;

	private PVector indicatorDimension;
	private PImage indicatorTexture;
	private PVector indicatorOffset;

	protected BInteract(RObject interactable, float x, float y) {
		super(interactable);
		useCircleProx = false;
		xRange = x;
		yRange = y;
		interactable.addOnUpdate(this);
	}

	protected BInteract(RObject interactable, float r) {
		super(interactable);
		useCircleProx = true;
		radius = r;
		interactable.addOnUpdate(this);
	}

	public void addIndicator(PVector indicatorOffset) {
		this.indicatorTexture = SpriteLoader.getInteractSymbol();
		this.indicatorDimension = new PVector(1, 1);
		this.indicatorOffset = indicatorOffset;
		indicator = VisualUtils.makeTexturedPolygon(indicatorDimension, 4, 0, indicatorOffset,
				indicatorTexture);
	}

	public void setActiveOnState(boolean activeOnState) {
		this.activeOnState = activeOnState;
		this.stateActivity = true;
	}

	// Checks whether a position is close enough to the interactable to interact
	protected boolean canInteract(PVector position) {
		if (activeOnState && (stateActivity != baseObj.getState()))
			return false;
		if (useCircleProx) {
			float distance = position.dist(baseObj.getPosition());
			return distance < radius;
		} else {
			PVector distance = position.copy().sub(baseObj.getPosition());
			if (distance.x < 0)
				distance.x *= -1;
			if (distance.y < 0)
				distance.y *= -1;
			return distance.x < xRange && distance.y < yRange;
		}
	}

	// Checks whether the InputHandler currently holds the InputCommand INTERACT
	private boolean isInteraction() {
		// TODO check if this works
		if (room.isInteraction()) System.out.println("Interaction Detected for RObject with id=" + baseObj.getId());
		return room.isInteraction();
	}

	private void updateIndicator(boolean showIndicator) {
		if (showIndicator) {
			if (!showingIndicator) {
				baseObj.addVisualAttributes(indicator);
				indicatorScale = INDICATOR_SCALE_RATE;
				showingIndicator = true;
			} else if (indicatorScale < 1f) {
				indicatorScale += INDICATOR_SCALE_RATE;
				if (indicatorScale > 1f)
					indicatorScale = 1f;
			}
			indicator.setScaling(indicatorScale);
		} else if (showingIndicator) {
			indicatorScale -= INDICATOR_SCALE_RATE;
			showingIndicator = indicatorScale >= 0f;
			if (showingIndicator)
				indicator.setScaling(indicatorScale);
			else
				baseObj.removeVisualAttributes(indicator);
		}
	}

	protected abstract void onInteraction();

	@Override
	public void run() {
		if (room == null) {
			System.out.println("NULL room for interactable, id = " + baseObj.getId());
			return;
		}
		PlayerAbstract player = baseObj.getPlayer();
		if (player == null) return;
		PVector playerPos = player.getPosition();
		boolean canInteract = canInteract(playerPos);
		if (canInteract && isInteraction()) onInteraction();
		if (indicator != null) updateIndicator(canInteract);
	}

	@Override
	public void setColour(int colour) {
		if (indicator == null) return;
		if (showingIndicator) baseObj.removeVisualAttributes(indicator);
		indicator = VisualUtils.makeTexturedPolygon(indicatorDimension, 4, 0, indicatorOffset,
				indicatorTexture, colour);
		indicator.setScaling(indicatorScale);
		if (showingIndicator) baseObj.addVisualAttributes(indicator);

	}

}
