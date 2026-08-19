package com.touhoumaid.eatproperly.food;

/** Pure rule: whether public chat should enter the deferred queue. */
public final class MaidSpeechDeferPolicy {
    private MaidSpeechDeferPolicy() {
    }

    /**
     * @param forceImmediate item-triggered / one-shot speech that must never queue
     * @param inChatWindow   maid is currently in her non-sleep public-chat window
     */
    public static boolean shouldDeferPublicChat(boolean forceImmediate, boolean inChatWindow) {
        return !forceImmediate && !inChatWindow;
    }
}
