package com.cosmo.socialdisplays;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.view.ViewPager;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.gajah.inkcaseLib.InkCase;
import com.gajah.inkcaseLib.InkCaseUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


public class MainActivity extends ActionBarActivity  {

    public final String TAG = "MainActivity";
    public List<Drawable> icons = null;
    public static String currentAppName = null;
    private BroadcastReceiver mScreenReceiver;

    public static MainActivity instance = null;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    //SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    //ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private static int[] mLayouts = { R.layout.fragment_main,
            R.layout.fragment_second, R.layout.fragment_third, };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        currentAppName = "";
        instance = this;
        this.initialisePaging();

        /*
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        */

    }
    private void initialisePaging() {

        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(Fragment.instantiate(this, PlaceholderFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, SFragment.class.getName()));
        fragments.add(Fragment.instantiate(this, TFragment.class.getName()));
        this.mPagerAdapter  = new PagerAdapter(super.getSupportFragmentManager(), fragments);
        //
        ViewPager pager = (ViewPager)super.findViewById(R.id.container);
        pager.setAdapter(this.mPagerAdapter);
    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments;
        /**
         * @param fm
         * @param fragments
         */
        public PagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }
        /* (non-Javadoc)
         * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
         */
        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        /* (non-Javadoc)
         * @see android.support.v4.view.PagerAdapter#getCount()
         */
        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

    /*public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    //Fragment for Android Tab
                    return PlaceholderFragment.newInstance(1);
                case 1:
                    //Fragment for Ios Tab
                    return SecondFragment.newInstance(2);
                case 2:
                    //Fragment for Windows Tab
                    return SecondFragment.newInstance(3);

            }
            return PlaceholderFragment.newInstance(1);
                }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return mLayouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    */

    private void initialize() {
        // Initialize the broadcast receivers
        getBaseContext().getApplicationContext().sendBroadcast(
                new Intent("StartupReceiver_Manual_Start"));

    }

    public void onToggleClicked(View view) { //  checking if toggled on
        boolean on = ((Switch) view).isChecked();

        // Dragging the slider does not change button isChecked state... -MH
        // boolean on = ((Switch) view).onTouchEvent();

        TextView debugTextView1 = (TextView) findViewById(R.id.debugtextView);

        // Still experimenting how to turn off the receivers if the slider is in off position -MH

        if (on) {
            IntentFilter screenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            mScreenReceiver = new ScreenReceiver();
            registerReceiver(mScreenReceiver, screenOffFilter);

            initialize();
            debugTextView1.setText("App is sending info to the Second Screen."); // Debug text - to be deleted
        } else {

            // Setting up the intent call
            Intent i7 = new Intent(this, RunningAppReceiver.class);
            PendingIntent ServiceManagementIntent = PendingIntent.getBroadcast(this,
                    1111111, i7, 0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // Cancelling a repeating alarm in StartupReceiver
            alarmManager.cancel(ServiceManagementIntent);

            // Separate disengaging measure to unregister the receiver registered at if(on)
            // Not really necessary? No change during testing if enabled or not. -MH
            // By definition, Receivers should "fade off" by themselves. -MH
            // this.unregisterReceiver(mScreenReceiver);

            debugTextView1.setText("App is inactive."); // Debug text - to be deleted
        }
    }

    public class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
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
            sharingIntent.putExtra(InkCase.EXTRA_FUNCTION_CODE, InkCase.CODE_SEND_WALLPAPER);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToSend));
            sharingIntent.putExtra(InkCase.EXTRA_FILENAME, fileToSend.getName());
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            InkCaseUtils.startInkCaseActivity(this, sharingIntent);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends android.support.v4.app.Fragment {

        /*public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);


            TextView mainTextView1 = (TextView) rootView.findViewById(R.id.main_text_view1);
            mainTextView1.setText("Use InkCaseCompanion app to connect to the InkCase display.\n");
            // mainTextView1.append("Use InkCaseCompanion app to connect to the InkCase display.\n");
            // Commented out to prevent showing the text in double? -MH
            return rootView;
        }
        */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

            }

    public static class SFragment extends android.support.v4.app.Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static SFragment newInstance(int sectionNumber) {
            SFragment fragment = new SFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public SFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_second, container, false);
            return rootView;
        }

    }
    public static class TFragment extends android.support.v4.app.Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

                public static TFragment newInstance(int sectionNumber) {
            TFragment fragment = new TFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        ArrayAdapter<String> mSocialAppAdapter;

        public TFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_third, container, false);

            String[] data = {
                    "Facebook",
                    "YouTube",
                    "Skype",
                    "LinkedIn",
                    "Tumblr",
                    "SoundCloud",
                    "WhatsApp"
            };
            List<String> socialApps = new ArrayList<String>(Arrays.asList(data));

            mSocialAppAdapter =
                    new ArrayAdapter<String>(
                            getActivity(), // The current context (this activity)
                            R.layout.list_item_socialapp, // The name of the layout ID.
                            R.id.list_item_forecast_textview, // The ID of the textview to populate.
                            socialApps);
            ListView listView = (ListView) rootView.findViewById(R.id.listView_socialapp);
            listView.setAdapter(mSocialAppAdapter);

            return rootView;
        }

    }
}
