package com.asus.supernote.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.view.View;

/**
 * @hide
 */
public class CursorIconLibrary {
    // ------------------------------------------------------------------------
    // TYPES
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // STATIC FIELDS
    // ------------------------------------------------------------------------
    // Need match the definition in PointerIcon
    public static final int STYLUS_ICON_FIRST = 10000;
    public static final int STYLUS_ICON_HOVER = STYLUS_ICON_FIRST + 10;
    public static final int STYLUS_ICON_FOCUS = STYLUS_ICON_FIRST + 1;
    public static final int STYLUS_ICON_ARROW_UP = STYLUS_ICON_FIRST + 2;
    public static final int STYLUS_ICON_ARROW_DOWN = STYLUS_ICON_FIRST + 3;
    public static final int STYLUS_ICON_LAST = STYLUS_ICON_FIRST + 4;

    // ------------------------------------------------------------------------
    // STATIC INITIALIZERS
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // STATIC METHODS
    // ------------------------------------------------------------------------
    public static final boolean setStylusIcon(View v, int style) {
        Method m = null;
        try {
        	String stylus = "STYLUS_ICON_HOVER";
        	switch(style){
        		case STYLUS_ICON_FIRST:
        			stylus = "STYLUS_ICON_FIRST";
        			break;
        		case STYLUS_ICON_HOVER:
        			stylus = "STYLUS_ICON_HOVER";
        			break;
        		case STYLUS_ICON_FOCUS:
        			stylus = "STYLUS_ICON_FOCUS";
        			break;
        		case STYLUS_ICON_ARROW_UP:
        			stylus = "STYLUS_ICON_ARROW_UP";
        			break;
        		case STYLUS_ICON_ARROW_DOWN:
        			stylus = "STYLUS_ICON_ARROW_DOWN";
        			break;
        		case STYLUS_ICON_LAST:
        			stylus = "STYLUS_ICON_LAST";
        			break;
        	}

        	int iconHover = 0;
            Field f = View.class.getField(stylus);
            f.setAccessible(true);
            iconHover = ((Integer)f.get(null)).intValue();

            m = View.class.getMethod("setPreferredStylusIcon", int.class);
            if (m != null && iconHover != 0) {
                m.setAccessible(true);
                m.invoke(v, iconHover);
                return true;
            }
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        }
        return false;
    }
    
    public static final void tryStylusIcon(InputManager manager, View view, Drawable drawable) {
    	try {
    	    // 另一個版本是 getMethod("tryStylusIconChecked", int.class, int.class)
    	    Method m = manager.getClass().getMethod("tryStylusIconChecked", int.class, Drawable.class);
    	    m.setAccessible(true);
    	    m.invoke(manager, view.hashCode(), drawable);
        } catch (NoSuchMethodException e) {
        } catch (Exception e) {
        }
    }

    static Drawable lastDrawable = null;
	public static final boolean setStylusIcon(View v, Drawable icon) {
		Method m = null;
		try {
			m = View.class.getMethod("setPreferredStylusIcon", Drawable.class);
			if (m != null) {
				m.setAccessible(true);
				m.invoke(v, icon);
				lastDrawable = icon;
				return true;
			}
		} catch (NoSuchMethodException e) {
		} catch (Exception e) {
		}
		return false;
	}
	
	public static final boolean setLastStylusIcon(View v){
		return setStylusIcon(v, lastDrawable);
	}

    // ------------------------------------------------------------------------
    // FIELDS
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // INITIALIZERS
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // CONSTRUCTORS
    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    // METHODS
    // ------------------------------------------------------------------------
}
