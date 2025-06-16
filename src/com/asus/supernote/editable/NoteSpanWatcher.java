// BEGIN: Better
package com.asus.supernote.editable;

import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;

public class NoteSpanWatcher implements SpanWatcher, NoteChangeWatcher {
	
	private SpanWatcher mOverriddenWatcher = null;
	
	public NoteSpanWatcher(SpanWatcher overiddenWatcher) {
		mOverriddenWatcher = overiddenWatcher;
	}
	
	@Override
	public void onSpanAdded(Spannable arg0, Object arg1, int arg2, int arg3) {
		if (mOverriddenWatcher != null) {
			mOverriddenWatcher.onSpanAdded(arg0, arg1, arg2, arg3);
		}
	}

	@Override
	public void onSpanChanged(Spannable arg0, Object arg1, int arg2, int arg3,
			int arg4, int arg5) {
		if (mOverriddenWatcher != null) {
			if((arg1 == Selection.SELECTION_END) || (arg1 == Selection.SELECTION_START)) {
				mOverriddenWatcher.onSpanChanged(arg0, arg1, arg2, arg3, arg4, arg5);
			}
		}
	}

	@Override
	public void onSpanRemoved(Spannable arg0, Object arg1, int arg2, int arg3) {
		if (mOverriddenWatcher != null) {
			mOverriddenWatcher.onSpanRemoved(arg0, arg1, arg2, arg3);
		}
	}

}
// END: Better
