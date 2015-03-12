package com.cosmo.socialdisplays;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class MainActivity extends Activity {

	public final String TAG = "MainActivity";
	public List<Drawable> icons = null;
	public static String currentAppName = null;
	private BroadcastReceiver mScreenReceiver;
	
	public static MainActivity instance = null;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
          
        currentAppName = "";
        instance = this;

        IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, screenOffFilter);
        
        TextView mainTextView1 = (TextView) findViewById(R.id.main_text_view1);
        mainTextView1.setText("Use InkCaseCompanion app to connect to the InkCase display.\n");
        // mainTextView1.append("Use InkCaseCompanion app to connect to the InkCase display.\n");
        // Commented out to prevent showing the text in double? -MH
    }
	
 
    private void initialize() {
    	// Initialize the broadcast receivers
    	getBaseContext().getApplicationContext().sendBroadcast(
                new Intent("StartupReceiver_Manual_Start"));
    	
    }


    /*Switch switch = (Switch) findViewById(R.id.on_off_switch);
    switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                initialize();
            } else {
                // The toggle is disabled
            }
        }
    }*/

    /*public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();

        if (on) {
            initialize();
        } else {
            // Disable vibrate
        }
    }*/


    public void onToggleClicked(View view) { //  checking if toggled on
        boolean on = ((Switch) view).isChecked();

        // Dragging the slider does not change button isChecked state... -MH
        // boolean on = ((Switch) view).onTouchEvent();
        TextView debugTextView1 = (TextView) findViewById(R.id.debugtextView);
        TextView debugTextView2 = (TextView) findViewById(R.id.debugtextView2);
        debugTextView2.setText("onToggleClicked activated.");

        // Still experimenting how to turn off the receivers if the slider is in off position -MH

        //Intent intent = new Intent(MainActivity.this, RunningAppReceiver.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (on) {
            initialize();
            //intent.putExtra("keep", true);
            //startActivity(intent);
            debugTextView1.setText("Toggle is on!!");
        } else {
            //intent.putExtra("keep", false);
            //startActivity(intent);
            debugTextView1.setText("Toggle is off...");
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
        	if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
        	{
        		sendToInkCase("sleep.png");
        		Log.v(TAG, "In Method:  ACTION_SCREEN_OFF");
        	}
        }
    }
    
    public void sendToInkCase(String icon) {
    	
    	AssetManager assetManager = getAssets();

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

        File fileToSend = new File(getExternalCacheDir(), icon);
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
    	    InkCaseUtils.startInkCaseActivity(this, sharingIntent);
    	 } catch (Exception e) {
    	    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    	 }
    }
    
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); 
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
