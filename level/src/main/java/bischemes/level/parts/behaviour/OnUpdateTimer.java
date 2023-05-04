package bischemes.level.parts.behaviour;

import bischemes.level.parts.RObject;

public class OnUpdateTimer implements OnUpdate {

    private final RObject timeable;
    private final int[] periods;

    private int time = 0;
    private int currentPeriod = 0;

    private boolean stateActivity = false;
    private boolean activeOnState;


    private OnUpdateTimer(RObject timeable, int[] periods) {
        this.timeable = timeable;
        this.periods = periods;
        timeable.addOnUpdate(this);
    }

    public static OnUpdateTimer assignOnUpdate(RObject timeable, int[] periods) {
        return new OnUpdateTimer(timeable, periods);
    }
    public static OnUpdateTimer assignOnUpdate(RObject timeable, int[] periods, int offset) {
        OnUpdateTimer u = assignOnUpdate(timeable, periods);
        u.time = offset;
        while (u.updateTime());
        return u;
    }
    public static OnUpdateTimer assignOnUpdate(RObject timeable, int period) {
        return assignOnUpdate(timeable, new int[]{period});
    }
    public static OnUpdateTimer assignOnUpdate(RObject timeable, int period, int offset) {
        return assignOnUpdate(timeable, new int[]{period}, offset);
    }

    public void setActiveOnState(boolean activeOnState) {
        this.activeOnState = activeOnState;
        this.stateActivity = true;
    }

    private boolean updateTime() {
        if (time >= periods[currentPeriod]) {
            time -= periods[currentPeriod];
            currentPeriod++;
            if (currentPeriod == periods.length) currentPeriod = 0;
            return true;
        }
        return false;
    }

    @Override
    public void run() {
        if (!stateActivity || activeOnState == timeable.getState()) {
            time++;
            if (updateTime()) timeable.switchState();
        }
    }

    @Override
    public void setColour(int colour) {}
}
