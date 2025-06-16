package com.asus.supernote.picker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.asus.supernote.EditorActivity;
import com.asus.supernote.R;
import com.asus.supernote.data.BookCase;
import com.asus.supernote.data.MetaData;
import com.asus.supernote.data.NoteBook;
import com.asus.supernote.data.NoteItemArray;
import com.asus.supernote.data.NotePage;
import com.asus.supernote.data.PageDataLoader;
import com.asus.supernote.editable.DrawableSpan;
import com.asus.supernote.editable.PageEditor;
import com.asus.supernote.editable.noteitem.NoteHandWriteBaselineItem;
import com.asus.supernote.editable.noteitem.NoteHandWriteItem;
import com.asus.supernote.editable.noteitem.NoteItem;
import com.asus.supernote.editable.noteitem.NoteTimestampItem;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.asus.supernote.BitmapLender;

public class TimeStampViewAdapter extends BaseAdapter {
	private static final int TYPE_TITLE = 0;
	private static final int TYPE_ITEM = 1;
	private Context mContext;
	private ContentResolver mContentResolver;
	private int timestampCount;
	private final BookCase mBookcase;
	public static final String OBJ = String.valueOf((char) 65532);
	private String[] weekofday;
	private String[] monthofyear;

	private TimeStampGroup[] mgroup = new TimeStampGroup[18+18];//darwin
	
	private int mSum6monthDays = 0;
	private int mLastMonth = -1;
	private int mThisTwoMonthdays = 0;
	private int mCurDayofWeek = -1;
	private int mSumLastYearDays = 0;
	
	private int mSumNext6monthDays = 0;
	private int mNextMonth = -1;
	private int mNextTwoMonthdays = 0;
	private int mSumNextYearDays = 0;
	
	//darwin
    static private boolean isNotebookTimeStampPageProcessing = false;
    static private Object mLockObj = new Object();
    public boolean getIsNotebookTimeStampPageProcessing()
    {
    	synchronized (mLockObj)
		{
    		if(isNotebookTimeStampPageProcessing == true)
    		{
    			return true;
    		}
    		else
    		{
    			isNotebookTimeStampPageProcessing = true;
    			return false;
    		}
		}
    }

    static public void resetNotebookTimeStampPageProcessing()
    {
    	synchronized (mLockObj)
		{
    		isNotebookTimeStampPageProcessing = false;
		}
    }
    //darwin

	// timestampgroup //{today,yesterday,week-2,week-3,week-4,week-5,week-6,last
	// week, two weeks ago
	// three weeks ago, last month, month-1,month-2,
	// month-3,month-4,month-5,half an year ago, last year and more

	private class TimeStampGroup {
		private List<SimpleTimeStampInfo> timestamplist;
		private String mgroupname = null;
		private int mstartid = 0;

		public TimeStampGroup() {
			timestamplist = new ArrayList<SimpleTimeStampInfo>();
		}

		public int listsize() {
			return timestamplist.size();
		}

		public List<SimpleTimeStampInfo> getTimestampList() {
			return timestamplist;
		}

		public void setGroupName(String name) {
			mgroupname = name;
		}

		public String getGroupName() {
			return mgroupname;
		}

		public void setStartid(int startid) {
			mstartid = startid;
		}

		public int getStartid() {
			return mstartid;
		}

		public void clearGroupData() {
			if (timestamplist != null)
			{
				for( SimpleTimeStampInfo item: timestamplist ){
					if(item.mtimestampContent != null){
					recycleBitmaps(item.mtimestampContent);
					}
				}
				timestamplist.clear();
			}
			mstartid = 0;
			mgroupname = null;

		}
	}

	public TimeStampViewAdapter(Context context) {
		mContext = context;
		mContentResolver = mContext.getContentResolver();

		mBookcase = BookCase.getInstance(mContext);
		
		Resources res = context.getResources();
		weekofday = res.getStringArray(R.array.week_of_day);
		monthofyear = res.getStringArray(R.array.month_of_year);
		for (int i = 0; i < mgroup.length; i++) {//darwin
			mgroup[i] = new TimeStampGroup();
		}
		changeDataTimestamp();
		
	}

	private int getItemcount()
	{
		int count = 0;
		for (TimeStampGroup item : mgroup) {
			count = count + item.listsize();
		}
		return count;
	}
	public void changeDataTimestamp() {
		for (int i = 0; i < mgroup.length; i++) {//darwin
			mgroup[i].clearGroupData();
		}

		BitmapLender lender = BitmapLender.getInstance();
        lender.recycle();

		Cursor cursor = mContentResolver.query(MetaData.TimestampTable.uri,
				null, 
				"((userAccount = 0) OR (userAccount = ?))", //darwin
				new String[] { Long.toString(MetaData.CurUserAccount) }, //darwin
				"create_date DESC");

		long curDate = getDateTime(System.currentTimeMillis());
		getSumDaysWithin6Month(curDate);
		
		if (cursor != null) {
			if(cursor.getCount() > 0) {
				cursor.moveToFirst();
				int i = 0;
				while (!cursor.isAfterLast()) {
					SimpleTimeStampInfo tpinfo = new SimpleTimeStampInfo();
					tpinfo.mtimestampId = cursor
							.getLong(MetaData.TimestampTable.INDEX_CREATE_DATE);
					tpinfo.mtimestampPos = cursor
							.getInt(MetaData.TimestampTable.INDEX_POSITION);
					tpinfo.mpageId = cursor
							.getLong(MetaData.TimestampTable.INDEX_OWNER);
					if(getBookNameAndContent(tpinfo))
					{
						AddToSubGroup(curDate, tpinfo, i++);
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
		}
		timestampCount = getItemcount();
		notifyDataSetChanged();
	}
	private void getSumDaysWithin6Month(long CurDate_day)
	{
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTimeInMillis(CurDate_day);
		} catch (Exception e) {

		}
		int curDate_monthofyear = cal.get(Calendar.MONTH);
		int curDate_dayofmonth = cal.get(Calendar.DAY_OF_MONTH);
		mCurDayofWeek = cal.get(Calendar.DAY_OF_WEEK);
		
		//wendy begin 0801
		int curDate_nextdayofmonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH) - curDate_dayofmonth;		
		//wendy end 0801
		int lastmonth = (curDate_monthofyear == 0) ? 11
				: (curDate_monthofyear - 1);
		
		
		cal.set(Calendar.MONTH, lastmonth);
		int lastmonthDays = curDate_dayofmonth
				+ (cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		int summonthsDays = lastmonthDays;
		int month_last1 = lastmonth;
		for (int i = 0; i < 10; i++) {
			int month_x = (month_last1 == 0) ? 11 : (month_last1 - 1);
			month_last1 = month_x;
			cal.set(Calendar.MONTH, month_x);
			summonthsDays = summonthsDays
					+ (cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			
			if(i == 4 )
			{
				mSum6monthDays = summonthsDays;
			}
		}
		mSumLastYearDays = summonthsDays + curDate_nextdayofmonth;
		mLastMonth = lastmonth;
		mThisTwoMonthdays = lastmonthDays; 	
		
		//wendy begin 0801
		int nextmonth = (curDate_monthofyear == 11) ? 0
				: (curDate_monthofyear + 1);
		if(curDate_monthofyear == 11)
		{
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);//next year
		}
		cal.set(Calendar.MONTH, nextmonth);
		int nextmonthDays = curDate_nextdayofmonth
				+ (cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		int sumNextmonthsDays = nextmonthDays;
		int month_next1 = nextmonth;
		for (int i = 0; i < 10; i++) {
			int month_x = (month_next1 == 11) ? 0 : (month_next1 + 1);
			if(month_next1 == 11)
			{
				cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 1);//next year
			}
			month_next1 = month_x;
			cal.set(Calendar.MONTH, month_x);
			sumNextmonthsDays = sumNextmonthsDays
					+ (cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			if(i == 4) {
				mSumNext6monthDays = sumNextmonthsDays;
			}
		}
		mSumNextYearDays = sumNextmonthsDays + curDate_dayofmonth;
		mNextMonth = nextmonth;
		mNextTwoMonthdays = nextmonthDays;	
		//wendy end 0801
	}
	
	

	private void AddToSubGroup(long curDate, SimpleTimeStampInfo TPinfo,
			int index) {
		long TimestampDate = TPinfo.mtimestampId;		
		long TimestampDate_day = getDateTime(TimestampDate);
		int subDay = (int) ((curDate - TimestampDate_day) / (1000 * 60 * 60 * 24));

		Calendar cal = Calendar.getInstance();		
		try {
			cal.setTimeInMillis(TimestampDate_day);
		} catch (Exception e) {
		}
		int timestampDate_monthofyear = cal.get(Calendar.MONTH);
		int timestampDate_weekofday = cal.get(Calendar.DAY_OF_WEEK);

		//wendy begin 0801
		if(subDay < 0)
		{
			subDay = (int) ((TimestampDate_day - curDate  ) / (1000 * 60 * 60 * 24));
			int nextDaysofThisweek = 7 - mCurDayofWeek;
			if (subDay <= nextDaysofThisweek && subDay > 0) {
				TimeStampGroup group = mgroup[17-subDay];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (subDay == 0 && list.size() == 0) {
					group.setGroupName(weekofday[7]);
					group.setStartid(index);
				}
				if (subDay == 1 && list.size() == 0) {
					group.setGroupName(weekofday[12]);
					group.setStartid(index);
				}
				if (subDay > 1 && subDay < 7) {

					if (list.size() == 0) {
						group.setGroupName(weekofday[timestampDate_weekofday - 1]);
						group.setStartid(index);
					}
				}
				list.add(TPinfo);

				//Log.v("wendy", "group < 7 " + group.getGroupName());

			}
			if (subDay > nextDaysofThisweek && subDay <= nextDaysofThisweek + 7) {
				// last week
				TimeStampGroup group = mgroup[17-7];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(weekofday[13]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
			}
			if (subDay > nextDaysofThisweek + 7 && subDay <= nextDaysofThisweek + 7 * 2) {
				// two weeks ago
				TimeStampGroup group = mgroup[17-8];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(weekofday[14]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group > 14 && < 21  " + group.getGroupName());
			}
			if (subDay > nextDaysofThisweek + 7 * 2
					&& subDay <= nextDaysofThisweek + 7 * 4) {
				// three weeks ago
				TimeStampGroup group = mgroup[17-9];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(weekofday[15]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group > 21 && < 28 " + group.getGroupName());
			}
			if (subDay > nextDaysofThisweek + 7 * 4) {
				if (subDay <= mNextTwoMonthdays) {
					// last month
					TimeStampGroup group = mgroup[17-10];
					List<SimpleTimeStampInfo> list = group.getTimestampList();
					if (list.size() == 0) {
						group.setGroupName(monthofyear[15]);
						group.setStartid(index);
					}
					list.add(TPinfo);
					//Log.v("wendy", "group  " + group.getGroupName());
				} else if (subDay > mNextTwoMonthdays && subDay <= mSumNext6monthDays) {
					int month_next = mNextMonth;
					for (int i = 0; i < 5; i++) {
						int month_x = (month_next == 11) ? 0 : (month_next + 1);
						if (month_x == timestampDate_monthofyear) {
							TimeStampGroup group = mgroup[17 -11 - i];
							List<SimpleTimeStampInfo> list = group
									.getTimestampList();
							if (list.size() == 0) {
								group.setGroupName(monthofyear[month_x]);
								group.setStartid(index);
							}
							list.add(TPinfo);
							break;
							//Log.v("wendy","group < 14 && > 7 " + group.getGroupName());
						}
						month_next = month_x;
					}
				}

				if (subDay > mSumNext6monthDays && subDay <= mSumNextYearDays) {
					// half an year ago
					TimeStampGroup group = mgroup[17-16];
					List<SimpleTimeStampInfo> list = group.getTimestampList();
					if (list.size() == 0) {
						group.setGroupName(monthofyear[16]);
						group.setStartid(index);
					}
					list.add(TPinfo);
					//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
				}
				if (subDay > mSumNextYearDays) {
					// last year and more
					TimeStampGroup group = mgroup[17-17];
					List<SimpleTimeStampInfo> list = group.getTimestampList();
					if (list.size() == 0) {
						group.setGroupName(monthofyear[17]);
						group.setStartid(index);
					}
					list.add(TPinfo);
					//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
				}
			}	
			return;
		}
		//wendy end 0801
		if (subDay < mCurDayofWeek && subDay >= 0) {
			TimeStampGroup group = mgroup[subDay + 18];
			List<SimpleTimeStampInfo> list = group.getTimestampList();
			if (subDay == 0 && list.size() == 0) {
				group.setGroupName(weekofday[7]);
				group.setStartid(index);
			}

			if (subDay == 1 && list.size() == 0) {
				group.setGroupName(weekofday[8]);
				group.setStartid(index);
			}
			if (subDay > 1 && subDay < 7) {

				if (list.size() == 0) {
					group.setGroupName(weekofday[timestampDate_weekofday - 1]);
					group.setStartid(index);
				}
			}
			list.add(TPinfo);

			//Log.v("wendy", "group < 7 " + group.getGroupName());

		}
		if (subDay >= mCurDayofWeek && subDay < mCurDayofWeek + 7) {
			// last week
			TimeStampGroup group = mgroup[7+18];
			List<SimpleTimeStampInfo> list = group.getTimestampList();
			if (list.size() == 0) {
				group.setGroupName(weekofday[9]);
				group.setStartid(index);
			}
			list.add(TPinfo);
			//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
		}
		if (subDay >= mCurDayofWeek + 7 && subDay < mCurDayofWeek + 7 * 2) {
			// two weeks ago
			TimeStampGroup group = mgroup[8+18];
			List<SimpleTimeStampInfo> list = group.getTimestampList();
			if (list.size() == 0) {
				group.setGroupName(weekofday[10]);
				group.setStartid(index);
			}
			list.add(TPinfo);
			//Log.v("wendy", "group > 14 && < 21  " + group.getGroupName());
		}
		if (subDay >= mCurDayofWeek + 7 * 2
				&& subDay < mCurDayofWeek + 7 * 4) {
			// three weeks ago
			TimeStampGroup group = mgroup[9+18];
			List<SimpleTimeStampInfo> list = group.getTimestampList();
			if (list.size() == 0) {
				group.setGroupName(weekofday[11]);
				group.setStartid(index);
			}
			list.add(TPinfo);
			//Log.v("wendy", "group > 21 && < 28 " + group.getGroupName());
		}
		if (subDay >= mCurDayofWeek + 7 * 4) {
			if (subDay < mThisTwoMonthdays) {
				// last month
				TimeStampGroup group = mgroup[10+18];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(monthofyear[12]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group  " + group.getGroupName());
			} else if (subDay >= mThisTwoMonthdays && subDay < mSum6monthDays) {
				int month_last = mLastMonth;
				for (int i = 0; i < 5; i++) {
					int month_x = (month_last == 0) ? 11 : (month_last - 1);
					if (month_x == timestampDate_monthofyear) {
						TimeStampGroup group = mgroup[11+18 + i];
						List<SimpleTimeStampInfo> list = group
								.getTimestampList();
						if (list.size() == 0) {
							group.setGroupName(monthofyear[month_x]);
							group.setStartid(index);
						}
						list.add(TPinfo);
						break;
						//Log.v("wendy","group < 14 && > 7 " + group.getGroupName());
					}
					month_last = month_x;
				}
			}

			if (subDay >= mSum6monthDays && subDay < mSumLastYearDays) {
				// half an year ago
				TimeStampGroup group = mgroup[16+18];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(monthofyear[13]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
			}
			if (subDay >= mSumLastYearDays) {
				// last year and more
				TimeStampGroup group = mgroup[17+18];
				List<SimpleTimeStampInfo> list = group.getTimestampList();
				if (list.size() == 0) {
					group.setGroupName(monthofyear[14]);
					group.setStartid(index);
				}
				list.add(TPinfo);
				//Log.v("wendy", "group < 14 && > 7 " + group.getGroupName());
			}
		}

	}

	private long getDateTime(long time) {
		java.text.DateFormat dataFormat = DateFormat.getDateFormat(mContext);
		String dateTime = dataFormat.format(new Date(time));
		Date date = null;
		try {
			date = dataFormat.parse(dateTime);
		} catch (Exception e) {

		}
		return date.getTime();
	}

	private boolean getBookNameAndContent(SimpleTimeStampInfo info) {
		long pageid = info.mpageId;
		Cursor cursor = mContentResolver.query(MetaData.PageTable.uri, null,
				"created_date = ?", new String[] { Long.toString(pageid) },
				null);
		if(cursor == null){
			return false;
		}
		if (cursor.getCount() == 0)
		{
			cursor.close();//RICHARD FIX MEMORY LEAK
			return false;
		}
		cursor.moveToFirst();

		long bookid = cursor.getLong(MetaData.PageTable.INDEX_OWNER);
		NoteBook notebook = mBookcase.getNoteBook(bookid);
		cursor.close();
		if (notebook == null) {
			return false;
		}
		info.mbookName = notebook.getTitle();
		info.mbookId = bookid;
		if(notebook.getIsLocked() && NoteBookPickerActivity.islocked())
			return false;
		else return true;
	}
		private Editable getTSContent(long pageid, long bookid, int timestamppos, long timestampid) {
		try{
		NoteBook book = mBookcase.getNoteBook(bookid);
		if (book == null) {
			return null;
		}
		NotePage page = book.getNotePage(pageid);
		PageDataLoader loader = new PageDataLoader(mContext);
		if (!loader.load(page, false)) {
			return null;
		}		
		ArrayList<NoteItemArray> itemarray = loader.getAllNoteItemsForSearch();
		
		boolean findTimestamp = false;		
		
		NoteItem[] items = null;
		
		for(NoteItemArray array:itemarray)
		{
			items = array.getNoteItemArray();
			
			for(int i = 1;i < items.length;i++)
			{
				NoteItem noteitem = items[i];
				if (noteitem instanceof NoteTimestampItem)
				{
					Long timeid = ((NoteTimestampItem)noteitem).getTimestamp();
					if(timeid == timestampid)
					{
						findTimestamp = true;
					}
				}
			}
			if(findTimestamp) break;
		}
		
		if(items == null) return null;

		String str = items[0].getText();
		int startIndex = timestamppos;
		int end = timestamppos + 20;
		if (end > str.length()) {
			end = str.length();
		}
		String subString = str.substring(timestamppos, end);
		Editable editable = new SpannableStringBuilder(subString);
		int secoundtimestamp = end;
		for (int i = 1; i < items.length; i++) {
			NoteItem noteitem = items[i];
			if ((noteitem instanceof NoteTimestampItem)
					&& noteitem.getStart() > startIndex
					&& noteitem.getStart() < secoundtimestamp) {
				secoundtimestamp = noteitem.getStart();
			}
			if (noteitem.getStart() >= startIndex
					&& noteitem.getEnd() <= secoundtimestamp) {
				editable.setSpan(noteitem, noteitem.getStart() - startIndex,
						noteitem.getEnd() - startIndex,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		int substringlength = secoundtimestamp - startIndex;

		if (substringlength > editable.length())
			substringlength = editable.length();

		Editable subedite = (Editable) editable.subSequence(0, substringlength);
		setFontSize(subedite);

		return subedite;
		}
		catch(Exception e)
		{
			return null;
		}
	}

	public void setFontSize(Editable editable) {
		NoteHandWriteItem[] handwriteitems = editable.getSpans(0,
				editable.length(), NoteHandWriteItem.class);
		for (NoteHandWriteItem item : handwriteitems) {
			if (item instanceof NoteHandWriteBaselineItem) {
				item.setFontHeight(getImageSpanHeight());
			} else {
				item.setFontHeight(getFullImageSpanHeight());
			}
		}
	}

	public int getImageSpanHeight() {
		FontMetricsInt fontMetricsInt;
		Paint paint = new Paint();
		paint.setTextSize(mfontsize);
		fontMetricsInt = paint.getFontMetricsInt();
		return (int) (fontMetricsInt.descent * PageEditor.FONT_DESCENT_RATIO - fontMetricsInt.ascent);
	}

	public int getFullImageSpanHeight() {
		FontMetricsInt fontMetricsInt;
		Paint paint = new Paint();
		paint.setTextSize(mfontsize);
		fontMetricsInt = paint.getFontMetricsInt();
		return (int) (mTextheight + fontMetricsInt.descent
				* PageEditor.FONT_DESCENT_RATIO);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return timestampCount;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	private int mfontsize = 30;
	private int mTextheight = 49;
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.timestamp_item_row,
					null);
			holder = new ViewHolder();
			holder.titleBar = (LinearLayout) convertView
					.findViewById(R.id.timestamp_title_layout);
			holder.itemBar = (LinearLayout) convertView
					.findViewById(R.id.timestamp_item_layout);
			holder.timeCategory = (TextView) convertView
					.findViewById(R.id.timestamp_group_name);
			holder.timestampCount = (TextView) convertView
					.findViewById(R.id.timestamp_group_count);
			//begin smilefish
			holder.timeStampDate = (TextView) convertView
					.findViewById(R.id.timestamp_date);
			holder.timeStampTime = (TextView) convertView
					.findViewById(R.id.timestamp_time);
			//end smilefish
			holder.timeStampContent = (TextView) convertView
					.findViewById(R.id.timestamp_content);
			mfontsize = (int)holder.timeStampContent.getTextSize();
			mTextheight = holder.timeStampContent.getLineHeight();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		TimeStampGroup tsGroup = null;
		for (TimeStampGroup item : mgroup) {
			if(item.getTimestampList().size() == 0)
			{
				continue;
			}
			if (item.getStartid() > position) {
				break;
			} else {
				tsGroup = item;
			}
		}
		if(tsGroup == null)
			return convertView;
		int startid = tsGroup.getStartid();
		int type = (position == startid) ? TYPE_TITLE : TYPE_ITEM;
		List<SimpleTimeStampInfo> showlist = tsGroup.getTimestampList();
		SimpleTimeStampInfo tpinfo = null;
		
		//Begin:clare
		//need to justice the type of device
		int device_Type=mContext.getResources().getInteger(R.integer.device_type);
		//End:clare
		
		int listsize = showlist.size();
		if ( listsize == 0) {
			holder.titleBar.setVisibility(View.GONE);
			holder.itemBar.setVisibility(View.GONE);
			return convertView;
		} else if (type == TYPE_TITLE) {
			holder.titleBar.setVisibility(View.VISIBLE);
			holder.itemBar.setVisibility(View.VISIBLE);
			holder.timeCategory.setText(tsGroup.getGroupName());
			holder.timestampCount.setText(String.valueOf(listsize));
			String timestampCountText = "";
			timestampCountText = String.format(mContext.getResources().getString(R.string.timestamp_group_counter), String.valueOf(listsize));
            holder.timestampCount.setText(timestampCountText);

			tpinfo = showlist.get(0);

		} else if (type == TYPE_ITEM) {
			holder.titleBar.setVisibility(View.GONE);
			tpinfo = showlist.get(position - startid);
		}
		
		if(tpinfo.mtimestampContent != null)
		{
			recycleBitmaps(tpinfo.mtimestampContent);
		}

		// final SimpleTimeStampInfo tpinfo = mTimeStampinfolist.get(position);
		holder.timeStampContent.setTag(R.string.book_id, tpinfo.mbookId);
		holder.timeStampDate.setTag(R.string.page_id, tpinfo.mpageId);
		holder.timeStampDate.setTag(R.string.timestamp_pos, tpinfo.mtimestampPos);
		holder.timeStampDate.setTag(R.string.timestampid, tpinfo.mtimestampId);
		holder.timeStampContent.setText(""); //fix bug 309618 by smilefish
		new GetTimestampInfo().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,
				holder);

		final long bookid = tpinfo.mbookId;
		final long pageid = tpinfo.mpageId;
		final int tmpos = tpinfo.mtimestampPos;

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				//darwin
	        	if(getIsNotebookTimeStampPageProcessing())
	        	{
	        		return;
	        	}
	            //darwin
				// BEGIN: Better
	        	long pageId = pageid;//(Long) v.getTag(R.string.page_id);
	        	if (!MetaData.SavingPageIdList.contains(pageId)) {
	        		NoteBook book = mBookcase.getNoteBook(bookid);
	        		if (book != null) {
		                NotePage page = book.getNotePage(pageid);
		                if (page != null) {
		                	
			            	try
			            	{
								Intent intent = new Intent(mContext, EditorActivity.class);
								intent.putExtra(MetaData.BOOK_ID, page.getOwnerBookId());
								intent.putExtra(MetaData.PAGE_ID, pageId);
								intent.putExtra(MetaData.TIMESTAMP_POS, tmpos);
								mContext.startActivity(intent);
			            	}catch(Exception e)
			            	{
			            		e.printStackTrace();
			            	}
		                }
	        		}
	            }
	        	// END: Better
			}
		});

		//End:clare
		return convertView;
	}
	
    // To avoid memory leak
    public void recycleBitmaps( Editable editable) {
    	if(editable == null ) return;
        DrawableSpan[] spans = editable.getSpans(0, editable.length(), DrawableSpan.class);
        if (spans != null) {
            for (DrawableSpan span : spans) {
                Drawable drawable = span.getDrawable();
                if (drawable instanceof BitmapDrawable) {
                 	((BitmapDrawable) drawable).setCallback(null);
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                }
                
            }
        }
    }

	class ViewHolder {
		LinearLayout titleBar;
		LinearLayout itemBar;
		TextView timeCategory;
		TextView timestampCount;
		TextView timeStampDate;//smilefish
		TextView timeStampTime;//smilefish
		TextView timeStampContent;

	}

	public class GetTimestampInfo extends AsyncTask<ViewHolder, Void, Editable> {
		private Long mOldTimestampId = 0L;
		private WeakReference<ViewHolder> v;
		java.text.DateFormat dataFormat1 = DateFormat.getTimeFormat(mContext);
		java.text.DateFormat dataFormat2 = DateFormat.getDateFormat(mContext);
		
		public GetTimestampInfo(){

		}

		@Override
		protected Editable doInBackground(ViewHolder... params) {
			v = new WeakReference<TimeStampViewAdapter.ViewHolder>(params[0]);
			ViewHolder holder = v.get();
			mOldTimestampId = (Long) holder.timeStampDate
					.getTag(R.string.timestampid);
			Long bookid = (Long) holder.timeStampContent.getTag(R.string.book_id);
			Long pageid = (Long) holder.timeStampDate.getTag(R.string.page_id);
			int TimestampPos = (Integer) holder.timeStampDate
					.getTag(R.string.timestamp_pos);

			return getTSContent(pageid, bookid, TimestampPos,mOldTimestampId);
		}

		@Override
		protected void onPostExecute(Editable result) {
			super.onPostExecute(result);			
			if(result == null) return ;
			int resulelength = result.length();
			if (v.get() != null && result != null && resulelength > 0) {
				ViewHolder holder = v.get();
				Long timestampId = (Long) holder.timeStampDate
						.getTag(R.string.timestampid);
				if (mOldTimestampId.equals(timestampId)) {
					//begin smilefish
					NoteTimestampItem timestampItems[] = result.getSpans(0, resulelength, NoteTimestampItem.class);
		            int timestampCount = timestampItems.length;
		            if(timestampCount > 0){
		            	long time = timestampItems[0].getTimestamp();
		        		String dateTime1 = dataFormat1.format(new Date(time));
		        		String dateTime2 = dataFormat2.format(new Date(time));
		        		holder.timeStampTime.setText(dateTime1);
		            	holder.timeStampDate.setText(dateTime2);
		            }
		            //end smilefish
					holder.timeStampContent.setText(result.subSequence(1,
							resulelength));
				}
			}
		}

	}

}
