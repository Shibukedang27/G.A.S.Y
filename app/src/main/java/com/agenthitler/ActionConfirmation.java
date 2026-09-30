package com.agenthitler;

import java.util.EnumSet;

/** Central policy boundary for actions that must ask before execution. */
public final class ActionConfirmation {
    private ActionConfirmation() {}
    private static final EnumSet<Action.Type> SENSITIVE = EnumSet.of(Action.Type.CALL_PHONE, Action.Type.SEND_SMS, Action.Type.DELETE_CONTENT);
    public static boolean required(Action action) {
        if (action == null) return false;
        // Keep the policy explicit. Sensitive action types are added here as capabilities land.
        return SENSITIVE.contains(action.type);
    }
    public static void markSensitive(Action.Type type) { if (type != null) SENSITIVE.add(type); }
}
