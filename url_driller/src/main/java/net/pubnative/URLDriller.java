package net.pubnative;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

public class URLDriller {

    private static final String TAG = URLDriller.class.getSimpleName();

    private String mUserAgent = null;
    private int mDrillSize = 0;

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
    protected Handler mHandler;
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
     * This method will set user agent in request
     *
     * @param userAgent User-Agent string
     */
    public void setUserAgent(String userAgent) {

        mUserAgent = userAgent;
    }

    /**
     * Set the steps for URL drilling.
     *
     * @param drillSize how deep we must drill root URL
     */
    public void setDrillSize(int drillSize) {
        mDrillSize = drillSize;
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

    /**
     * Method do request for the URL and depends from the response status return last used URL
     * or made new request with new URL.
     *
     * @param url     URL for request
     */
    protected void doDrill(String url) {
        doDrill(url, 0);
    }

    /**
     * Method do request for the URL and depends from the response status return last used URL
     * or made new request with new URL.
     *
     * @param url     URL for request
     * @param counter number of request from start.
     */
    protected void doDrill(String url, int counter) {

        Log.v(TAG, "doDrill: " + url);
        try {
            URL urlObj = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();
            if (mUserAgent != null) {
                conn.setRequestProperty("User-Agent", mUserAgent);
            }
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
                    if (newUrl.startsWith("/")) {
                        String protocol = urlObj.getProtocol();
                        String host = urlObj.getHost();
                        newUrl = protocol + "://" + host + newUrl;
                    }
                    invokeRedirect(newUrl);
                    conn.disconnect();
                    if (mDrillSize == 0) {
                        doDrill(newUrl);
                    } else if (mDrillSize > 0 && counter < mDrillSize) {
                        doDrill(newUrl, counter + 1);
                    } else {
                        invokeFinish(url);
                    }
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
        } catch (Error error) {
            Log.e(TAG, "Drilling error: with URL = [" + url + "]", error);
            invokeFinish(null);
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
                    mListener.onURLDrillerFail(url == null ? "" : url, exception);
                }
                mListener = null;
            }
        });
    }
}
