package com.agenthitler;

import org.junit.Test;
import static org.junit.Assert.*;

public class ActionJsonParserTest {
    @Test public void acceptsSafePlan() {
        java.util.List<Action> actions = java.util.Collections.singletonList(
                new Action(Action.Type.HOME, java.util.Collections.emptyMap()));
        assertTrue(ActionValidator.valid(new CommandPlan(actions, "Done, sir.")));
    }
    @Test public void rejectsArrayWithoutRequiredArgument() {
        java.util.List<Action> actions = java.util.Collections.singletonList(
                new Action(Action.Type.OPEN_APP, java.util.Collections.emptyMap()));
        assertFalse(ActionValidator.valid(new CommandPlan(actions, "Done, sir.")));
    }
    @Test public void rejectsMissingRequiredArguments() {
        assertFalse(ActionValidator.valid(ActionJsonParser.parse("{\"actions\":[{\"type\":\"OPEN_APP\"}]}")));
        assertFalse(ActionValidator.valid(ActionJsonParser.parse("{\"actions\":[{\"type\":\"SEARCH_WEB\"}]}")));
    }
    @Test public void rejectsUnknownType() {
        assertNull(ActionJsonParser.parse("{\"actions\":[{\"type\":\"RUN_SHELL\"}] }"));
    }
}
