package net.pubnative;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLDriller {

    private static final String TAG = URLDriller.class.getSimpleName();

    public interface Listener {

        void onURLDrillerStart(String url);

        void onURLDrillerRedirect(String url);

        void onURLDrillerFinish(String url);

        void onURLDrillerFail(String url, Exception exception);
    }

    protected Context  mContext;
    protected Listener mListener;
    protected Handler  mHandler;

    public URLDriller() {

    }

    /**
     * This method will open the URL in background following redirections
     *
     * @param url      valid URL
     * @param listener valid URLDriller.Listener for callbacks
     */
    public void drill(Context context, final String url, Listener listener) {

        if (listener == null) {
            Log.w(TAG, "URLDrill: listener not specified, drilling without callbacks");
        }
        mListener = listener;
        if (TextUtils.isEmpty(url)) {
            invokeFail(url, new IllegalArgumentException("URLDrill error: url is null or empty"));
        } else if (context == null) {
            invokeFail(url, new IllegalArgumentException("URLDrill error: context cannot be null"));
        } else {
            mContext = context;
            new Thread(new Runnable() {

                @Override
                public void run() {

                    Looper.prepare();
                    mHandler = new Handler();
                    invokeStart(url);
                    drillInBackground(url);
                }
            }).start();
        }
    }

    protected void drillInBackground(String url) {

        Log.v(TAG, "drillInBackground: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            conn.setReadTimeout(5000);
            int status = conn.getResponseCode();
            Log.v(TAG, " - Status: " + status);
            switch (status) {
                case HttpURLConnection.HTTP_OK: {
                    Log.v(TAG, " - Done: " + url);
                    openIntent(url);
                }
                break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER: {
                    String newUrl = conn.getHeaderField("Location");
                    Log.v(TAG, " - Redirecting: " + newUrl);
                    invokeRedirect(newUrl);
                    drillInBackground(newUrl);
                }
                break;
                default: {
                    Exception statusException = new Exception("Drilling error: Invalid URL, Status: " + status);
                    Log.e(TAG, statusException.toString());
                    invokeFail(url, statusException);
                    openIntent(url);
                }
                break;
            }
        } catch (Exception exception) {
            Log.e(TAG, "Drilling error: " + exception);
            invokeFail(url, exception);
            openIntent(url);
        }
    }

    protected void openIntent(String url) {

        Log.v(TAG, "openIntent: " + url);
        if (mContext != null && !TextUtils.isEmpty(url)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(intent);
            invokeFinish(url);
        }
    }
    //==================================================
    // Listener helpers
    //==================================================

    protected void invokeStart(final String url) {

        Log.v(TAG, "invokeStart");
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onURLDrillerStart(url);
                }
            }
        });
    }

    protected void invokeRedirect(final String url) {

        Log.v(TAG, "invokeRedirect");
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onURLDrillerRedirect(url);
                }
            }
        });
    }

    protected void invokeFinish(final String url) {

        Log.v(TAG, "invokeFinish");
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onURLDrillerFinish(url);
                }
            }
        });
    }

    protected void invokeFail(final String url, final Exception exception) {

        Log.v(TAG, "invokeFail: " + exception);
        mHandler.post(new Runnable() {

            @Override
            public void run() {

                if (mListener != null) {
                    mListener.onURLDrillerFail(url, exception);
                }
            }
        });
    }
}
