package net.pubnative;

import android.os.Handler;

import net.pubnative.url_driller.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        sdk = 21)
public class URLDrillerTest {

    @Test
    public void creates() {

        URLDriller opener = new URLDriller();
        assertThat(opener).isNotNull();
    }

    @Test
    public void invokeRedirectCallbackWithListener() {

        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.invokeRedirect("");

        verify(drillerListener, times(1)).onURLDrillerRedirect(anyString());
    }

    @Test
    public void invokeFinishCallbackWithListener() {

        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.invokeFinish("");

        verify(drillerListener, times(1)).onURLDrillerFinish(anyString());
    }

    @Test
    public void invokeFailCallbackWithListener() {

        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.invokeFail("", mock(Exception.class));

        verify(drillerListener, times(1)).onURLDrillerFail(anyString(), any(Exception.class));
    }

    @Test
    public void invokeCallbacksWithoutListener() {

        URLDriller driller = spy(URLDriller.class);

        driller.mHandler = new Handler();

        driller.invokeFail("", mock(Exception.class));
        driller.invokeFinish("");
        driller.invokeRedirect("");
        driller.invokeStart("");
    }

    @Test
    public void invokeStartCallbackWithListener(){
        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.invokeStart("");

        verify(drillerListener, times(1)).onURLDrillerStart(anyString());
    }

    @Test
    public void failOnEmptyURL(){

        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.drill("");

        verify(drillerListener, times(1)).onURLDrillerFail(anyString(), any(IllegalArgumentException.class));
    }

    @Test
    public void failOnNullURL(){

        URLDriller driller = spy(URLDriller.class);
        URLDriller.Listener drillerListener = spy(URLDriller.Listener.class);

        driller.mHandler = new Handler();
        driller.setListener(drillerListener);
        driller.drill(null);

        verify(drillerListener, times(1)).onURLDrillerFail(anyString(), any(IllegalArgumentException.class));
    }
}