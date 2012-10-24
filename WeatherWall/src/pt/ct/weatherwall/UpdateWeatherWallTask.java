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

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class UpdateWeatherWallTask extends AsyncTask<Object,Void,Object[]> {
	
	private static String LOG_TAG = "WeatherWall.UpdateTask";
	
	private static String IMG_DIR_STR = "/WWall/";
	private static String IMG_SUNNY_STR = "sunny.png";
	private static String IMG_CLOUD_STR = "cloud.png";
	private static String IMG_RAIN_STR = "rain.png";
	private static String IMG_FOG_STR = "fog.png";
	private static String IMG_SNOW_STR = "snow.png";
	// TODO fog, wind, etc..
	
    protected Object[] doInBackground(Object... objs) {
    	
    	String url = (String)objs[0];
    	Context ctx = (Context)objs[1];
    	
    	Object[] output = new Object[2];
    	output[0] = null;
    	output[1] = ctx;
    	
    	String sdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	
    	// Creating HTTP client
        HttpClient httpClient = new DefaultHttpClient();
        // Creating HTTP get
        HttpGet httpGet = new HttpGet(url);
  
        // Making HTTP Request
        try {
        	Log.d(LOG_TAG, "Preparing to invoke HTTP GET in '" + url + "'");
        	
        	// Invoking HTTP
            HttpResponse response = httpClient.execute(httpGet);
            String responseStr = null;
            if (response == null)
            {
            	Log.w(LOG_TAG, "HTTP Response was null!");
            	return output;
            }
            else
            {
            	HttpEntity entityResp = response.getEntity();
            	if (entityResp == null)
                {
                	Log.w(LOG_TAG, "HTTP Entity Response was null!");
                	return output;
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
            	return output;
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
            
            output[0] = conditions;
            
            return output;
        } catch (Exception e) {
        	Log.w(LOG_TAG, "Something went wrong in main process!", e);
            try {
            	//SetWallpaper(null);
            	PrintWriter pw = new PrintWriter(sdCardRootPath + IMG_DIR_STR + "log.txt");
            	e.printStackTrace(pw);
            	pw.close();
            } catch (IOException ioe) {
            	Log.w(LOG_TAG, "Something went wrong when clearing the Wallpaper!", ioe);
            }
        }
        
        return output;
    }

    protected void onPostExecute(Object[] objs) {
    	
    	String result = (String)objs[0];
    	Context ctx = (Context)objs[1];
    	
    	String sdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    	
    	try
		{
    		if (result == null)
    			SetWallpaper(null, ctx);
    		else if (result.contains("Sunny")
            		|| result.contains("sunny")
            		|| result.contains("Clear")
            		|| result.contains("clear"))
            {
            	SetWallpaper(IMG_SUNNY_STR, ctx);
            }
            else if (result.contains("Rain")
            		|| result.contains("rain")
            		|| result.contains("Shower")
            		|| result.contains("shower"))
            {
            	SetWallpaper(IMG_RAIN_STR, ctx);
            }
            else if (result.contains("Fog")
            		|| result.contains("fog"))
            {
            	SetWallpaper(IMG_FOG_STR, ctx);
            }
            else if (result.contains("Cloud")
            		|| result.contains("cloud"))
            {
            	SetWallpaper(IMG_CLOUD_STR, ctx);
            }
            else if (result.contains("Snow")
            		|| result.contains("snow"))
            {
            	SetWallpaper(IMG_SNOW_STR, ctx);
            }
            else
            	SetWallpaper(null, ctx);
            
        } catch (Exception e) {
        	Log.w(LOG_TAG, "Something went wrong in main process!", e);
            try {
            	SetWallpaper(null, ctx);
            	PrintWriter pw = new PrintWriter(sdCardRootPath + IMG_DIR_STR + "log.txt");
            	e.printStackTrace(pw);
            	pw.close();
            } catch (IOException ioe) {
            	Log.w(LOG_TAG, "Something went wrong when clearing the Wallpaper!", ioe);
            }
        }
    }
    
    private void SetWallpaper(String filename, Context ctx) throws IOException
	{		
		String sdCardRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Bitmap bMap = null;
		if (filename != null)
			bMap = BitmapFactory.decodeFile(sdCardRootPath + IMG_DIR_STR + filename);
		if (bMap == null)
		{
			Log.w(LOG_TAG, "Couldn't find a valid image in path '" + sdCardRootPath + IMG_DIR_STR + filename + "'");
			bMap = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.walldefault);
		}
		WallpaperManager.getInstance(ctx).setBitmap(bMap);
	}
}
