package com.agenthitler;

/** Rejects plans that are not made only from the approved action schema. */
public final class ActionValidator {
    private ActionValidator() {}

    public static boolean valid(CommandPlan plan) {
        if (plan == null || plan.actions == null || plan.actions.isEmpty() || plan.actions.size() > 20) return false;
        for (Action action : plan.actions) {
            if (action == null || action.type == null || action.args == null) return false;
            switch (action.type) {
                case OPEN_APP: if (!hasText(action, "package")) return false; break;
                case OPEN_URL: if (!hasText(action, "url")) return false; break;
                case SEARCH_WEB: if (!hasText(action, "query")) return false; break;
                case SCROLL: if (!hasText(action, "direction")) return false; break;
                default: break;
            }
        }
        return true;
    }

    private static boolean hasText(Action action, String key) {
        Object value = action.args.get(key);
        return value != null && !String.valueOf(value).trim().isEmpty();
    }
}
