package com.asus.supernote.indexservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;

public class IndexServiceClient {

	/** Messenger for communicating with service. */
	IndexService mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;

	static final int MSG_INDEX_SPECIFIC_PAGE = 1;
	
	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override  
	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  We are communicating with our
	        // service through an IDL interface, so get a client-side
	        // representation of that from the raw service object.
			mService = ((IndexService.LocalBinder)service).getService();
	    }
		
		@Override  
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        mService = null;
	    }
	};

	public void doBindService(Context context) {
	    // Establish a connection with the service.  We use an explicit
	    // class name because there is no reason to be able to let other
	    // applications replace our component.
		boolean ret = context.bindService(new Intent(context,IndexService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	public void doUnbindService(Context context) {
	    if (mIsBound) {

	        // Detach our existing connection.
	        context.unbindService(mConnection);
	    }
	}
	
	public void sendPageIDToIndexService(Long pageID) {
		try {
			if (mConnection != null && mIsBound) {
				Message msg = Message.obtain(null, MSG_INDEX_SPECIFIC_PAGE);
				msg.obj = pageID;
				mService.sendMsg(msg);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
}
