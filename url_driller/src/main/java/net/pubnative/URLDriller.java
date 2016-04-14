package net.pubnative;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

public class URLDriller {

    private static final String TAG = URLDriller.class.getSimpleName();

    //==============================================================================================
    // LISTENER
    //==============================================================================================
    public interface Listener {

        /**
         * Called when the drilling process start
         *
         * @param url url where drilling process started
         */
        void onURLDrillerStart(String url);

        /**
         * Called when the drilling process detects a redirection
         *
         * @param url url where redirection is pointing to
         */
        void onURLDrillerRedirect(String url);

        /**
         * Called whenever the drilling process finishes
         *
         * @param url url where the drilling process ends
         */
        void onURLDrillerFinish(String url);

        /**
         * Called when the drilling process fails, it will interrupt the drilling process.
         *
         * @param url       url where the drilling process stopped
         * @param exception exception with extended message of the error.
         */
        void onURLDrillerFail(String url, Exception exception);
    }

    protected Listener mListener;
    protected Handler  mHandler;
    //==============================================================================================
    // PUBLIC
    //==============================================================================================

    /**
     * This method will set up a listener in this drill
     *
     * @param listener valid Listener or null
     */
    public void setListener(Listener listener) {

        mListener = listener;
    }

    /**
     * This method will open the URL in background following redirections
     *
     * @param url valid url to drill
     */
    public void drill(final String url) {

        if (TextUtils.isEmpty(url)) {
            invokeFail(url, new IllegalArgumentException("URLDrill error: url is null or empty"));
        } else {
            mHandler = new Handler(Looper.getMainLooper());
            new Thread(new Runnable() {

                @Override
                public void run() {

                    invokeStart(url);
                    doDrill(url);
                }
            }).start();
        }
    }

    //==============================================================================================
    // PRIVATE
    //==============================================================================================
    protected void doDrill(String url) {

        Log.v(TAG, "doDrill: " + url);
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
                    invokeFinish(url);
                }
                break;
                case HttpURLConnection.HTTP_MOVED_TEMP:
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_SEE_OTHER: {
                    String newUrl = conn.getHeaderField("Location");
                    Log.v(TAG, " - Redirecting: " + newUrl);
                    if(newUrl.startsWith("/")) {
                        newUrl = URLDecoder.decode(newUrl.replaceFirst("\\/aff_r\\?(.*)&url=", ""));
                    }
                    invokeRedirect(newUrl);
                    doDrill(newUrl);
                }
                break;
                default: {
                    Exception statusException = new Exception("Drilling error: Invalid URL, Status: " + status);
                    Log.e(TAG, statusException.toString());
                    invokeFail(url, statusException);
                }
                break;
            }
        } catch (Exception exception) {
            Log.e(TAG, "Drilling error: " + exception);
            invokeFail(url, exception);
        }
    }
    //==============================================================================================
    // Listener helpers
    //==============================================================================================

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
                mListener = null;
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
                mListener = null;
            }
        });
    }
}
