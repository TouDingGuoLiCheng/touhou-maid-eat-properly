package com.touhoumaid.eatproperly.satiety;

/**
 * Pure rules for when daily satiety drain applies.
 * Idle work-mode maids (TLM {@code touhou_little_maid:idle}) do not drain.
 */
public final class MaidSatietyDrainPolicy {
    /** TLM idle task id string form ({@code namespace:path}). */
    public static final String IDLE_TASK_UID = "touhou_little_maid:idle";

    private MaidSatietyDrainPolicy() {
    }

    /** Daily drain only when the maid is not on the idle work task. */
    public static boolean shouldApplyDailyDrain(boolean isIdleTask) {
        return !isIdleTask;
    }

    public static boolean isIdleTaskUid(String taskUid) {
        return IDLE_TASK_UID.equals(taskUid);
    }
}
