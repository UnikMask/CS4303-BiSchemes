package bischemes.level.parts.behaviour;

import bischemes.engine.GObject;
import bischemes.level.parts.RObject;

public class BHitKill extends BHit {

    private RObject killer;

    private BHitKill(RObject killer) {
        this.killer = killer;
        killer.addOnHit(this);
    }

    public static BHitKill assign(RObject killer) {
        return new BHitKill(killer);
    }

    @Override
    public void run(GObject hit) {
        //TODO
    }
}
