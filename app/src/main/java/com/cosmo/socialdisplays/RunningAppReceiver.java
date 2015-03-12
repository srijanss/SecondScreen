package com.cosmo.socialdisplays;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;

public class RunningAppReceiver extends BroadcastReceiver {
	 
    public final String TAG = "CheckRunningApplicationReceiver"; 
    Context mContext;
    MainActivity mActivity = null;
    Drawable icon = null;
    String applicationName;
    static String packageName;
    boolean keep;

    @SuppressWarnings("static-access")
	@Override

     public void onReceive(Context mContext, Intent anIntent) {
    	this.mContext = mContext;

    	MainActivity mActivity = MainActivity.instance;
    	packageName = mActivity.currentAppName;
    	
    	try {
    		PackageManager pm = mContext.getPackageManager();
    		
    		// Get a list of the running apps
    		ActivityManager am1 = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            @SuppressWarnings("deprecation")
			List<RunningTaskInfo> processes = am1.getRunningTasks(Integer.MAX_VALUE);

            if (processes != null) {
            
            	icon = null;
            	applicationName = null;
            	// Get the activity that is running in the foreground
            	packageName = processes.get(0).topActivity
                        .getPackageName();
            	
            	// If the current activity is not inkCaseCompanionApp or the launcher
            	// get the activity name and icon to send it to InkCase display
            	if ( (!packageName.equals(mActivity.currentAppName)) && 
            			(!packageName.equals("com.gajah.inkcase.companion")) &&
            			(!packageName.contains("launcher")) ){
            		
	                
	                try {
	                    ApplicationInfo a = pm.getApplicationInfo(packageName,
	                            PackageManager.GET_META_DATA);
	                    icon = mContext.getPackageManager().getApplicationIcon(
	                            processes.get(0).topActivity.getPackageName());
	                    String applicationName = (String) (a != null ? pm.getApplicationLabel(a) : "(unknown)");
	                    
	                    
	
	                    sendToInkCase(icon, applicationName);
	                } catch (NameNotFoundException e) {
	                    Log.e("ERROR", "Unable to find icon for package '"
	                            + packageName + "': " + e.getMessage());
	                }
	                mActivity.currentAppName = packageName;
            	}
            	// If the current activity is the launcher
            	// get the home.png icon from the assets to show on the InkCase
            	else if((!packageName.equals(mActivity.currentAppName)) && 
            			(packageName.contains("launcher")) ) {

            		sendToInkCaseFromAssets("home.png");
                    mActivity.currentAppName = packageName;
            	}
            }
 
        } catch (Throwable t) {
            Log.i(TAG, "Throwable caught: "
                        + t.getMessage(), t);
        }
    	
         
    }
    
    public void sendToInkCase(Drawable icon, String name) {
    	// Creates a bitmap file using the icon and the name of the app
    	// and sends it to be displayed on the InkCase
    	
    	String[] parts = splitIntoLines(name, 7);
    	
        Bitmap bitmap = null;
        bitmap = drawableToBitmap(icon);
        Paint paint = new Paint();
        paint.setColor(mContext.getResources().getColor(android.R.color.white));
        paint.setStyle(Paint.Style.FILL);
        bitmap = Bitmap.createScaledBitmap(bitmap, 150, 175, false);
        Bitmap newBitmap = Bitmap.createBitmap(300, 600, Config.RGB_565);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(newBitmap, 300, 600, paint);
        canvas.drawPaint(paint);
        paint.setColor(Color.BLACK);
        paint.setTextSize(60); 
        
        Typeface mono = Typeface.createFromAsset(mContext.getAssets(), "VeraMono.ttf");
        paint.setTypeface(mono);
        
        int counter = 0;
        for(String part : parts) {
    		canvas.drawText(part, 10, 250 + counter*65, paint);
    		counter++;
        }
        canvas.drawBitmap(bitmap, 75, 10, paint);
        
       
        
        if (newBitmap == null)
			throw new RuntimeException("No image to send");

        File fileToSend = new File(mContext.getExternalCacheDir(), "helloInkCase.jpg");
		try {
			FileOutputStream fOut = new FileOutputStream(fileToSend);

			newBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	try {
    	    Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
    	    sharingIntent.setType("image/jpeg");
    	    sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
    	    sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fileToSend));
    	    sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
    	    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    InkCaseUtils.startInkCaseActivity(mContext, sharingIntent);
    	 } catch (Exception e) {
    	    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
    	 }
	}
    
    public void sendToInkCaseFromAssets(String icon) {
    	// Sends a drawable file from the assets folder to InkCase as a bitmap
    	
    	AssetManager assetManager = mContext.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(icon);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            // handle exception
        }
        
        if (bitmap == null)
			throw new RuntimeException("No image to send");

        File fileToSend = new File(mContext.getExternalCacheDir(), icon);
		try {
			FileOutputStream fOut = new FileOutputStream(fileToSend);

			bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    	
    	try {
    	    Intent sharingIntent = new Intent(InkCase.ACTION_SEND_TO_INKCASE);
    	    sharingIntent.setType("image/jpeg");
    	    sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE,InkCase.CODE_SEND_WALLPAPER);
    	    sharingIntent.putExtra(Intent.EXTRA_STREAM,Uri.fromFile(fileToSend));
    	    sharingIntent.putExtra(InkCase.EXTRA_FILENAME,fileToSend.getName());
    	    sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    InkCaseUtils.startInkCaseActivity(mContext, sharingIntent);
    	 } catch (Exception e) {
    	    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
    	 }
    }
    
    public static Bitmap drawableToBitmap (Drawable drawable) {
    	// Converts a drawable object to bitmap
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    
    public String[] splitIntoLines(String input, int maxCharsInLine){
    	// Splits a String to multiple lines with maximum length equal to maxCharsInLine
    	
        StringTokenizer tok = new StringTokenizer(input, " ");
        StringBuilder output = new StringBuilder(input.length());
        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            while(word.length() > maxCharsInLine){
                output.append(word.substring(0, maxCharsInLine-lineLen) + "\n");
                word = word.substring(maxCharsInLine-lineLen);
                lineLen = 0;
            }

            if (lineLen + word.length() > maxCharsInLine) {
                output.append("\n");
                lineLen = 0;
            }
            output.append(word + " ");

            lineLen += word.length() + 1;
        }
        return output.toString().split("\n");
    }
}
