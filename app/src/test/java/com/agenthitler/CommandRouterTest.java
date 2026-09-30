package com.agenthitler;
import org.junit.Test; import static org.junit.Assert.*;
public class CommandRouterTest {
 @Test public void parsesHome(){CommandPlan p=CommandRouter.parse("go home");assertTrue(ActionValidator.valid(p));assertEquals(Action.Type.HOME,p.actions.get(0).type);}
 @Test public void parsesSearch(){CommandPlan p=CommandRouter.parse("search youtube for rockets");assertTrue(ActionValidator.valid(p));assertEquals(Action.Type.SEARCH_WEB,p.actions.get(0).type);assertEquals("rockets",p.actions.get(0).args.get("query"));}
 @Test public void rejectsUnknown(){assertNull(CommandRouter.parse("do something dangerous"));}
 @Test public void rejectsEmpty(){assertFalse(ActionValidator.valid(CommandRouter.parse("")));}
}
