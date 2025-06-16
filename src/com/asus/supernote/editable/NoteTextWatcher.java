// BEGIN: Better
package com.asus.supernote.editable;

import android.text.Editable;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.TextWatcher;

public class NoteTextWatcher implements TextWatcher, SpanWatcher,
		NoteChangeWatcher {
	
	private Object mOverriddenObject = null;
	
	public NoteTextWatcher(Object overriddenObject) {
		mOverriddenObject = overriddenObject;
	}

	@Override
	public void onSpanAdded(Spannable arg0, Object arg1, int arg2, int arg3) {
		if (mOverriddenObject != null) {
			SpanWatcher watcher = null;
			try {
				watcher = (SpanWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				watcher.onSpanAdded(arg0, arg1, arg2, arg3);
			}
		}
	}

	@Override
	public void onSpanChanged(Spannable arg0, Object arg1, int arg2, int arg3,
			int arg4, int arg5) {
		if (mOverriddenObject != null) {
			SpanWatcher watcher = null;
			try {
				watcher = (SpanWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				if((arg1 == Selection.SELECTION_END) || (arg1 == Selection.SELECTION_START)) {
					watcher.onSpanChanged(arg0, arg1, arg2, arg3, arg4, arg5);
				}
			}
		}
	}

	@Override
	public void onSpanRemoved(Spannable arg0, Object arg1, int arg2, int arg3) {
		if (mOverriddenObject != null) {
			SpanWatcher watcher = null;
			try {
				watcher = (SpanWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				watcher.onSpanRemoved(arg0, arg1, arg2, arg3);
			}
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		if (mOverriddenObject != null) {
			TextWatcher watcher = null;
			try {
				watcher = (TextWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				watcher.afterTextChanged(arg0);
			}
		}
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		if (mOverriddenObject != null) {
			TextWatcher watcher = null;
			try {
				watcher = (TextWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				watcher.beforeTextChanged(arg0, arg1, arg2, arg3);
			}
		}
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		if (mOverriddenObject != null) {
			TextWatcher watcher = null;
			try {
				watcher = (TextWatcher) mOverriddenObject;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			if (watcher != null) {
				watcher.onTextChanged(arg0, arg1, arg2, arg3);
			}
		}
	}

}
// END: Better
