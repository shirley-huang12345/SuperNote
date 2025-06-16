package com.asus.supernote.template.widget;

public class ToDoWidgetItem {
	/* NOTEBOOK: ListView show one notebook ToDo items */
	public static final short ADAPTER_TYPE_NOTEBOOK = 0;
	/* ALL_TODOS: ListView show all ToDo items including all notebooks */
	public static final short ADAPTER_TYPE_ALL_TODOS = 1;	
	
	public int widgetId = -1;
	public short adapterType = ADAPTER_TYPE_NOTEBOOK;
	public long bookId = -1;
	public short sortBy = ToDoComparator.SORT_BY_TIME;//default sort by time
	public int width = 0;
	public int height = 0;
	public ToDoWidgetItem(){
		
	}
	
	public ToDoWidgetItem(int widgetId,short adapterType){
		this.widgetId = widgetId;
		this.adapterType = adapterType;
	}
}
