package com.agenthitler;
import java.util.Collections;
import java.util.Map;
public final class Action {
    public enum Type { OPEN_APP,CLOSE_CURRENT_APP,HOME,BACK,RECENTS,INPUT_TEXT,CLICK_TEXT,CLICK_DESCRIPTION,CLICK_COORDINATE,SWIPE,SCROLL,PRESS_ENTER,TAKE_SCREENSHOT,VOLUME_UP,VOLUME_DOWN,VOLUME_MUTE,PLAY_MEDIA,PAUSE_MEDIA,NEXT_MEDIA,PREVIOUS_MEDIA,OPEN_URL,SEARCH_WEB,OPEN_SETTINGS,CALL_PHONE,SEND_SMS,DELETE_CONTENT,WAIT,READ_VISIBLE_UI,SHOW_MESSAGE,SPEAK }
    public final Type type; public final Map<String,Object> args;
    public Action(Type type, Map<String,Object> args){this.type=type;this.args=args==null?Collections.emptyMap():Collections.unmodifiableMap(args);}
}
