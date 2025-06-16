package com.asus.supernote.template.widget;

import java.util.Comparator;

import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.NoteBook;

public class ToDoComparator implements Comparator<ToDoWidgetListItem> {
	public static final short SORT_BY_PAGE = 0;
	public static final short SORT_BY_TIME = 1;

	private short mSortBy = -1;

	public ToDoComparator(short sortBy) {
		this.mSortBy = sortBy;
	}
	
	//Begin Emmanual to fix bug 556536
	BookCase mBookcase;
	private int getPageIndex(ToDoWidgetListItem item) {
		if (mBookcase == null) {
			mBookcase = BookCase.getInstance(item.getContext());
		}
		if (mBookcase == null) {
			return 0;
		}
		NoteBook book = mBookcase.getNoteBook(item.bookId);
		if (book == null) {
			return 0;
		}
		return book.getPageOrderList().indexOf(item.pageId);
	}
	//End Emmanual

	@Override
	public int compare(ToDoWidgetListItem litem, ToDoWidgetListItem ritem) {
		long result = 0;
		switch (mSortBy) {
		case SORT_BY_PAGE:
			//emmanual to fix bug 556536
			result = litem.bookId - ritem.bookId;
			if(result == 0){
				result = getPageIndex(litem) - getPageIndex(ritem); 
			}

			break;
		case SORT_BY_TIME:
			/* Sort by Time
			 *  1.By day, the last day will in the top of the window
			 *  2.By unfinished (unchecked), unfinished will list in top than done
			 *  3.By last modify time, user checked sequence (the last check will list in the top)
			 * */
			result = (litem.isChecked ? 1 : 0) - (ritem.isChecked ? 1 : 0);

			if(result == 0){
				result = ritem.lastModifyTime - litem.lastModifyTime;
			}
			break;

		default:
			result = 0;
		}
		//emmanual to fix bug 481675, 481360
		if (result < 1000 && result > -1000) {
			return (int) result;
		}
		return (int) (result/1000);
	}
}
