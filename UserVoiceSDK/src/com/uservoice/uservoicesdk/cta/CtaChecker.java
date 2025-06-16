package com.uservoice.uservoicesdk.cta;

import java.lang.reflect.Field;

import android.content.Context;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * Proxy of CTA Service
 * */
public class CtaChecker {
    public static final String TAG = "CtaChecker";

    /**
     * CTA action code: Do a phone call.
     */
    public static final int CTA_PHONE_CALL = 0;

    /**
     * CTA action code: Send MMS.
     */
    public static final int CTA_SEND_MMS = 1;

    /**
     * CTA action code: Send SMS.
     */
    public static final int CTA_SEND_SMS = 2;

    /**
     * CTA action code: Read contacts.
     */
    public static final int CTA_READ_CONTACTS = 3;

    /**
     * CTA action code: Read call logs.
     */
    public static final int CTA_READ_CALL_LOG = 4;

    /**
     * CTA action code: Read MMS.
     */
    public static final int CTA_READ_MMS = 5;

    /**
     * CTA action code: Read SMS.
     */
    public static final int CTA_READ_SMS = 6;

    /**
     * CTA action code: Read MMS and SMS.
     */
    @Deprecated
    public static final int CTA_READ_MMS_SMS = CTA_READ_MMS;

    /**
     * CTA action code: Enable mobild network.
     */
    public static final int CTA_MOBILE_NETWORK = 8;

    /**
     * CTA action code: Enable WLAN.
     */
    public static final int CTA_WLAN = 9;

    /**
     * CTA action code: Get location information.
     */
    public static final int CTA_LOCATION = 10;

    /**
     * CTA action code: Voice call recording.
     */
    @Deprecated
    public static final int CTA_CALL_RECORDER = 11;

    /**
     * CTA action code: Sound recording.
     */
    public static final int CTA_SOUND_RECORDER = 12;

    /**
     * CTA action code: Use camera.
     */
    public static final int CTA_CAMERA = 13;

    /**
     * CTA action code: Enable bluetooth.
     */
    public static final int CTA_BLUETOOTH = 14;

    /**
     * CTA action code: Perform action that will have cost.
     */
    public static final int CTA_COST = 15;

    /**
     * CTA action code: Use network.
     */
    public static final int CTA_USE_NETWORK = 16;

    /**
     * CTA action code: Write contacts.
     */
    public static final int CTA_WRITE_CONTACTS = 17;

    /**
     * CTA action code: Write call logs.
     */
    public static final int CTA_WRITE_CALL_LOG = 18;

    /**
     * CTA action code: Write MMS.
     */
    public static final int CTA_WRITE_MMS = 19;

    /**
     * CTA action code: Write SMS.
     */
    public static final int CTA_WRITE_SMS = 20;

    /**
     * CTA action code: Write MMS and SMS.
     */
    @Deprecated
    public static final int CTA_WRITE_MMS_SMS = CTA_WRITE_MMS;

    /**
     * CTA action code: Use NFC.
     */
    public static final int CTA_NFC = 21;

    private Context mContext;

    /**
     * Constructor.
     * 
     * @param context
     */
    public CtaChecker(Context context) {
        mContext = context;
    }

    /**
     * Check CTA permission. It will block calling thread.
     * 
     * @param action
     *            CTA action code
     * @param caller
     *            Caller's package name
     */
    public boolean checkPermission(int action, String caller) {
        boolean accept = true;
        if (mContext != null && hasAction(action)) {
            IBinder remote = (IBinder) mContext.getSystemService("cta");
            if (remote != null) {
                try {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    data.writeInterfaceToken("com.asus.cta.ICtaService");
                    data.writeStrongBinder(new Binder());
                    data.writeInt(action);
                    data.writeString(caller);
                    remote.transact(1000, data, reply, 0);
                    reply.readException();
                    int result = reply.readInt();
                    accept = (result != 0);
                    data.recycle();
                    reply.recycle();
                }
                catch (RemoteException e) {
                    Log.w(TAG, "Check permission " + action + " failed !!!");
                }
            }
        }
        return accept;
    }

    private boolean hasAction(int action) {
        boolean exist = false;

        try {
            Class<?> checkerClz = Class.forName("com.asus.cta.CtaChecker");
            Field[] fields = checkerClz.getFields();
            for (Field field : fields) {
                if (field.getGenericType().equals(int.class) && field.getInt(null) == action) {
                    exist = true;
                }
            }
        }
        catch (ClassNotFoundException e) {
            Log.d(TAG, "Class CtaChecker not found, err: " + e.getMessage());
        }
        catch (IllegalAccessException e) {
            Log.d(TAG, "Cant not access CtaChecker field, err: " + e.getMessage());
        }
        catch (IllegalArgumentException e) {
            Log.d(TAG, "Cant not get field value, err: " + e.getMessage());
        }
        catch (NullPointerException e) {
            Log.d(TAG, "Cant not get field value, err: " + e.getMessage());
        }

        return exist;
    }
}
