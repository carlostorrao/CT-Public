package pt.ct.weatherwall;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	//private static String LOG_TAG = "WeatherWall.Main";
	
	private static String URL_STR = "http://rss.accuweather.com/rss/liveweather_rss.asp?metric=1&locCode=EUR%7CPT%7CPO012%7CLisbon";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		AsyncTask<Object,Void,Object[]> updateTask = new UpdateWeatherWallTask();
		
		updateTask.execute(URL_STR, getApplicationContext());
		
		try {
			updateTask.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
		} catch (TimeoutException e) {
		}
		
        finish();
	}
}
