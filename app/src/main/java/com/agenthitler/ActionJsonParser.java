package com.agenthitler;

import org.json.*;
import java.util.*;

/** Converts only the approved local-model action schema into executable actions. */
public final class ActionJsonParser {
    private ActionJsonParser() {}
    public static CommandPlan parse(String raw) {
        if (raw == null) return null;
        int start = raw.indexOf('{'), end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) return null;
        try {
            String body = raw.substring(start, end + 1).trim();
            JSONArray list = body.startsWith("[") ? new JSONArray(body) : new JSONObject(body).optJSONArray("actions");
            if (list == null || list.length() == 0 || list.length() > 20) return null;
            List<Action> actions = new ArrayList<>();
            for (int i = 0; i < list.length(); i++) {
                JSONObject o = list.getJSONObject(i);
                Action.Type type = Action.Type.valueOf(o.getString("type").toUpperCase(Locale.US));
                Map<String,Object> args = new HashMap<>();
                Iterator<String> keys = o.keys();
                while (keys.hasNext()) { String k = keys.next(); if (!"type".equals(k)) args.put(k, o.get(k)); }
                actions.add(new Action(type, args));
            }
            CommandPlan plan = new CommandPlan(actions, "Done, sir.");
            return ActionValidator.valid(plan) ? plan : null;
        } catch (Exception ignored) { return null; }
    }
}
