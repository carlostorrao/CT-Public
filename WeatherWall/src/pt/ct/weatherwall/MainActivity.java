package pt.ct.weatherwall;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import pt.ct.weatherwallpaper.R;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

public class MainActivity extends Activity {
	
	private static String LOG_TAG = "WeatherWall.Main";
	private static String URL_STR = "http://rss.accuweather.com/rss/liveweather_rss.asp?metric=1&locCode=EUR%7CPT%7CPO012%7CLisbon";
	private static String IMG_DIR_STR = "/WWall/";
	private static String IMG_SUNNY_STR = "sunny.png";
	private static String IMG_CLOUD_STR = "cloud.png";
	private static String IMG_RAIN_STR = "rain.png";
	private static String IMG_SNOW_STR = "snow.png";
	// TODO fog, wind, etc..
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String sdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		// Android 4.0 Workaround
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		
		// Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP get
        HttpGet httpGet = new HttpGet(URL_STR);
  
        // Making HTTP Request
        try {
        	Log.d(LOG_TAG, "Preparing to invoke HTTP GET in '" + URL_STR + "'");
        	
        	// Invoking HTTP
            HttpResponse response = httpClient.execute(httpGet);
            String responseStr = null;
            if (response == null)
            {
            	Log.w(LOG_TAG, "HTTP Response was null!");
            	SetWallpaper(null);
            	return;
            }
            else
            {
            	HttpEntity entityResp = response.getEntity();
            	if (entityResp == null)
                {
                	Log.w(LOG_TAG, "HTTP Entity Response was null!");
                	SetWallpaper(null);
                	return;
                }
            	else
            		responseStr = EntityUtils.toString(entityResp);
            }
 
            Log.d(LOG_TAG, "Http Response: " + response.toString());
            
            // Parsing the response
            int firstI = responseStr.indexOf("<title>Currently:");
            int lastI = responseStr.indexOf("C</title>");
            if (firstI < 0 || lastI < 0)
            {
            	Log.w(LOG_TAG, "HTTP Response was invalid! Could not find the Current Conditions part!");
            	SetWallpaper(null);
            	return;
            }
            
            // Current Conditions
            String conditions = responseStr.substring(firstI, lastI).replace("<title>Currently:", "");
            Log.d(LOG_TAG, "Current Condtions: " + conditions);
            try
            {
            	SimpleDateFormat sdf = new SimpleDateFormat("ddMMM HH:mm");
            	FileWriter fw = new FileWriter(sdCardRootPath + IMG_DIR_STR + "conditions.txt", true);
            	fw.append(sdf.format(new Date()).toString() + ":" + conditions + "ºC\n");
            	fw.close();
            } catch (IOException e) {
            	Log.w(LOG_TAG, "Exception in writing Current Conditions to File", e);
            }
            
            
            if (conditions.contains("Sunny")
            		|| conditions.contains("sunny")
            		|| conditions.contains("Clear")
            		|| conditions.contains("clear"))
            {
            	SetWallpaper(IMG_SUNNY_STR);
            }
            else if (conditions.contains("Rain")
            		|| conditions.contains("rain")
            		|| conditions.contains("Shower")
            		|| conditions.contains("shower"))
            {
            	SetWallpaper(IMG_RAIN_STR);
            }
            else if (conditions.contains("Cloud")
            		|| conditions.contains("cloud"))
            {
            	SetWallpaper(IMG_CLOUD_STR);
            }
            else if (conditions.contains("Snow")
            		|| conditions.contains("snow"))
            {
            	SetWallpaper(IMG_SNOW_STR);
            }
            else
            	SetWallpaper(null);
            
        } catch (Exception e) {
        	Log.w(LOG_TAG, "Something went wrong in main process!", e);
            try {
            	SetWallpaper(null);
            	PrintWriter pw = new PrintWriter(sdCardRootPath + IMG_DIR_STR + "log.txt");
            	e.printStackTrace(pw);
            	pw.close();
            } catch (IOException ioe) {
            	Log.w(LOG_TAG, "Something went wrong when clearing the Wallpaper!", ioe);
            }
        }
        
        finish();
	}
	
	private void SetWallpaper(String filename) throws IOException
	{		
		String sdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Bitmap bMap = null;
		if (filename != null)
			bMap = BitmapFactory.decodeFile(sdCardRootPath + IMG_DIR_STR + filename);
		if (bMap == null)
		{
			Log.w(LOG_TAG, "Couldn't find a valid image in path '" + sdCardRootPath + IMG_DIR_STR + filename + "'");
			bMap = BitmapFactory.decodeResource(getResources(), R.drawable.walldefault);
		}
		WallpaperManager.getInstance(getApplicationContext()).setBitmap(bMap);
	}
}
