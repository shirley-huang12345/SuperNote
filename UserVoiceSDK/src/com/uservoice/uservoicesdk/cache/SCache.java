package com.uservoice.uservoicesdk.cache;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.uservoice.uservoicesdk.model.BaseModel;
import com.uservoice.uservoicesdk.rest.RestResult;
import com.uservoice.uservoicesdk.rest.RestTaskCallback;

import android.content.Context;
import android.os.AsyncTask;

public class SCache {
    public interface OnPutCacheEventListener {
        void OnPutSuccess();

        void OnPutFail(Exception e);
    }

    public interface OnGetCacheEventListener {
        void OnGetSuccess(JSONObject jsonObject);

        void OnGetFail(Exception e);
    }

    public static void PutCache(Context context, String key, JSONObject jsonObject) {
        if (context != null) {
            new PutCacheTask(context, key, jsonObject).execute();
        }
    }

    public static AsyncTask GetCache(Context context, String key, final RestTaskCallback callback) {

        if (context != null) {
            GetCacheTask getCacheTask = new GetCacheTask(context, key);
            getCacheTask.setListener(new OnGetCacheEventListener() {

                @Override
                public void OnGetSuccess(JSONObject jsonObject) {
                    try {
                        callback.onComplete(jsonObject);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                @Override
                public void OnGetFail(Exception e) {
                    // TODO Auto-generated method stub
                    callback.onError(new RestResult(e));
                }
            });
            getCacheTask.executeOnExecutor(BaseModel.mFixedThreadPool);
            return getCacheTask;
        }

        return null;
    }

    private static class PutCacheTask extends AsyncTask<Void, Void, Void> {

        private Context context;
        private String key;
        private JSONObject jsonObject;
        private OnPutCacheEventListener mOnPutCacheEventListener;

        public PutCacheTask(Context context, String key, JSONObject jsonObject) {
            this.context = context.getApplicationContext();
            this.key = key;
            this.jsonObject = jsonObject;
        }

        @Override
        protected Void doInBackground(Void... params) {
            String date = jsonObject.toString();
            try {
                IOUtils.writeStringAsFile(context, date, key);
            } catch (IOException e) {
                e.printStackTrace();
                if (mOnPutCacheEventListener != null) {
                    mOnPutCacheEventListener.OnPutFail(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mOnPutCacheEventListener != null) {
                mOnPutCacheEventListener.OnPutSuccess();
            }
        }

        public void setListener(OnPutCacheEventListener listener) {
            this.mOnPutCacheEventListener = listener;
        }

    }

    private static class GetCacheTask extends AsyncTask<Void, Void, JSONObject> {

        private Context context;
        private String key;
        private OnGetCacheEventListener mOnGetCacheEventListener;

        public GetCacheTask(Context context, String key) {
            this.context = context.getApplicationContext();
            this.key = key;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {

            try {
                return new JSONObject(IOUtils.readStringFromFile(context, key));
            } catch (IOException e) {

                e.printStackTrace();
                if (mOnGetCacheEventListener != null) {
                    mOnGetCacheEventListener.OnGetFail(e);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (mOnGetCacheEventListener != null) {
                    mOnGetCacheEventListener.OnGetFail(e);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (!isCancelled() && result!=null && mOnGetCacheEventListener != null) {
                mOnGetCacheEventListener.OnGetSuccess(result);
            }
        }

        public void setListener(OnGetCacheEventListener listener) {
            this.mOnGetCacheEventListener = listener;
        }

    }

}
