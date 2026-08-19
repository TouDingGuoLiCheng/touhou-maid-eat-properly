package com.touhoumaid.eatproperly.snackraid;

public enum SnackRaidPhase {
    IDLE(0),
    APPROACH(1),
    WAIT(2),
    FLEE(3);

    private final int id;

    SnackRaidPhase(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static SnackRaidPhase fromId(int id) {
        for (SnackRaidPhase phase : values()) {
            if (phase.id == id) {
                return phase;
            }
        }
        return IDLE;
    }
}
