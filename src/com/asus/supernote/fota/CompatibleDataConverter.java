package com.asus.supernote.fota;

import java.util.ArrayList;

import com.asus.supernote.data.NotePage;

public class CompatibleDataConverter {

	public boolean preprocessBooks() {
		return true;
	}
	
	public boolean preprocessPages() {
		return true;
	}

	public boolean load(NotePage notePage) {
		return true;
	}

	public boolean convert() {
		return true;
	}

	protected ArrayList<Observer> mObservers = new ArrayList<Observer>();
	
	public interface Observer {
        public void notify(long bookId, long pageId);
    }
	
	public void registerObserver(Observer o) {
        mObservers.add(o);
    }

    public void removeObserver(Observer o) {
        mObservers.remove(o);
    }
	
}
