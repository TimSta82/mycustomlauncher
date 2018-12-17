package com.example.tstaats.mycustomlauncher;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.tstaats.mycustomlauncher.receiver.NetworkStateChangeReceiver;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "TimsMainActivity";

    public static final String APP_NAME = "myCustomLauncher";

    private AppWidgetManager mAppWidgetManager;
    private LauncherAppWidgetHost mAppWidgetHost;
    private RelativeLayout homeScreen;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String WIDGET_ID = "widgetID";
    public ArrayList<String> WIDGET_ID_LIST;
    public static final String NUMBER_OF_WIDGETS = "numberOfWidgets";

    private int REQUEST_CREATE_APPWIDGET = 900;
    private int REQUEST_PICK_APPWIDGET = 901;
    private int numWidgets;
    private int loadedWigetId;
    private int mSavedWidgetCounter = 0;
    private float fWidth, fHeight;
    private int width, height;

    private SharedPreferences appSettings;
    private WifiManager wifiManager;
    public static final String IS_WIFI_ENABLED = "isWifiEnabled";
    private int wifiImage;
    private ImageView btn3;
    private float imageAlphaValue = 1.0f;
    private boolean isWifiEnabled;

    /**
     * Saving a widget by saving its appWidgetId, then you can recreate the appWidgetInfo:
     * <p>
     * int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
     * AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()){
            wifiImage = R.drawable.ic_wifi_off;
            imageAlphaValue = 0.5f;
        } else {
            wifiImage = R.drawable.ic_wifi_on;
            imageAlphaValue = 1.0f;
        }



        appSettings = getSharedPreferences(APP_NAME, MODE_PRIVATE);
        if (!appSettings.getBoolean(IS_WIFI_ENABLED, false)){
            wifiImage = R.drawable.ic_wifi_off;
            imageAlphaValue = 0.5f;
        } else {
            wifiImage = R.drawable.ic_wifi_on;
            imageAlphaValue = 1.0f;
        }

        IntentFilter intentFilter = new IntentFilter(NetworkStateChangeReceiver.NETWORK_AVAILABLE_ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean isNetworkAvailable = intent.getBooleanExtra(NetworkStateChangeReceiver.IS_NETWORK_AVAILABLE, false);
                String networkStatus = isNetworkAvailable ? "connected" : "disconnected";
                Toast.makeText(MainActivity.this, "Network status: " + networkStatus, Toast.LENGTH_SHORT).show();
                if (networkStatus.equals("connected")){
                    btn3.setImageResource(R.drawable.ic_wifi_on);
                } else {
                    btn3.setImageResource(R.drawable.ic_wifi_off);
                }
            }
        }, intentFilter);


        mAppWidgetManager = AppWidgetManager.getInstance(this);
        mAppWidgetHost = new LauncherAppWidgetHost(this, R.id.APPWIDGET_HOST_ID);
        homeScreen = findViewById(R.id.home_screen);

        fWidth = homeScreen.getWidth() / 3;
        fHeight = homeScreen.getHeight() / 3;

        width = Math.round(fWidth);
        height = Math.round(fHeight);

        numWidgets = loadNumberOfWidgets();
        if (numWidgets > 0) {
            for (int i = 0; i < numWidgets; i++) {
                Log.d(TAG, "onCreate: widgetCount: " + i);
                int widget = loadWidget();
                Log.d(TAG, "onCreate: widgetId: " + widget);
                //selectLoadedWidget(widget);
                AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(widget);
                Log.d(TAG, "onCreate: appWidgetInfo: " + appWidgetInfo.toString());

            }
        }

        //configureLoadedWidget(1);
        //selectLoadedWidget();

        ImageView btn1 = findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.calculator2");
                startActivity(launchIntent);
            }
        });
        btn1.setImageDrawable(getActivityIcon(this, "com.android.calculator2", "com.android.calculator2.Calculator"));

        ImageView btn2 = findViewById(R.id.button2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.android.settings");
                startActivity(launchIntent);
            }
        });
        btn2.setImageDrawable(getActivityIcon(this, "com.android.settings", "com.android.settings.Settings"));



        btn3 = findViewById(R.id.button3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWifiEnabled = wifiManager.isWifiEnabled();
                if (isWifiEnabled){
                    // disable
                    wifiManager.setWifiEnabled(false);
                    SharedPreferences.Editor prefEditor = appSettings.edit();
                    prefEditor.putBoolean(IS_WIFI_ENABLED, false);
                    prefEditor.apply();
                    btn3.setImageResource(R.drawable.ic_wifi_off);
                    btn3.setAlpha(imageAlphaValue);
                    Toast.makeText(MainActivity.this, "Wifi off", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: isWifiEnabled:" + wifiManager.isWifiEnabled());

                } else {
                    // enable
                    wifiManager.setWifiEnabled(true);
                    SharedPreferences.Editor prefEditor = appSettings.edit();
                    prefEditor.putBoolean(IS_WIFI_ENABLED, true);
                    prefEditor.apply();
                    btn3.setImageResource(R.drawable.ic_wifi_on);
                    btn3.setAlpha(imageAlphaValue);
                    Toast.makeText(MainActivity.this, "Wifi on", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onClick: isWifiEnabled:" + wifiManager.isWifiEnabled());

                }

            }
        });
        btn3.setAlpha(imageAlphaValue);
        btn3.setImageResource(wifiImage);


        homeScreen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(MainActivity.this, "Hallo", Toast.LENGTH_SHORT).show();
                selectWidget();
                return false;
            }
        });



    }

    public void saveWidget(int widgetId, int numberOfWidgets) {
        Log.d(TAG, "saveWidget: is called");

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String WID = WIDGET_ID + "_" + mSavedWidgetCounter;
        editor.putInt(WID, widgetId);
        editor.putInt(NUMBER_OF_WIDGETS, numberOfWidgets);
        editor.apply();

        Log.d(TAG, "saveWidget: WID: " + WID);
        Toast.makeText(this, "Widget saved", Toast.LENGTH_SHORT).show();
        mSavedWidgetCounter++;

    }

    public int loadNumberOfWidgets() {
        Log.d(TAG, "loadNumberOfWidgets: is called");

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        numWidgets = preferences.getInt(NUMBER_OF_WIDGETS, 0);
        Log.d(TAG, "loadNumberOfWidgets: numWidgets: " + numWidgets);
        return numWidgets;
    }

    public int loadWidget() {
        Log.d(TAG, "loadWidget: is called");
        String WID = WIDGET_ID + "_" + mSavedWidgetCounter;

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        loadedWigetId = preferences.getInt(WID, 0);

        Log.d(TAG, "loadWidget: WID: " + WID);

        mSavedWidgetCounter++;
        return loadedWigetId;
    }


    private void configureLoadedWidget(int loadedWigetId) {
        //Log.d(TAG, "configureLoadedWidget: is called");
        loadedWigetId = 12;
        int appWidgetId = loadedWigetId;
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            //Log.d(TAG, "configureLoadedWidget: createWidget() is called");
            createLoadedWidget(loadedWigetId);
        }
    }


    public void createLoadedWidget(int loadedWigetId) {
        int appWidgetId = loadedWigetId;
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        LauncherAppWidgetHostView hostView = (LauncherAppWidgetHostView) mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        Log.d(TAG, "createAnalogClockWidget: appWidgetId: " + appWidgetId + " appWidgetInfo: " + appWidgetInfo.toString());
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(homeScreen.getWidth() / 3, homeScreen.getHeight() / 3);
        lp.leftMargin = numWidgets * homeScreen.getWidth() / 3;
        homeScreen.addView(hostView, lp);
        //homeScreen.addView(hostView);
        numWidgets++;
    }

    void selectWidget() {
        //Log.d(TAG, "selectWidget: is called");
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    void selectLoadedWidget(int id) {
        Log.d(TAG, "selectLoadedWidget: is called");
        int appWidgetId = id;
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult: is called");
        if (resultCode == RESULT_OK ) {
            if (requestCode == REQUEST_PICK_APPWIDGET) {
                configureWidget(data);
            }
            else if (requestCode == REQUEST_CREATE_APPWIDGET) {
                //Log.d(TAG, "onActivityResult: createWidget() is called: REQUEST_CREATE_APPWIDGET: " + REQUEST_CREATE_APPWIDGET);
                createWidget(data);
            }
        }
        else if (resultCode == RESULT_CANCELED && data != null) {
            int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    public void createWidget(Intent data) {
        //Log.d(TAG, "createWidget: is called");
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        final AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
        //Log.d(TAG, "createWidget: appWidgetId: " + appWidgetId + " appWidgetInfo: " + appWidgetInfo.toString());
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(homeScreen.getWidth() / 3, homeScreen.getHeight() / 3);
        lp.leftMargin = numWidgets * homeScreen.getWidth() / 3;

        mAppWidgetHost.startListening();
        hostView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Log.d(TAG, "onLongClick: widget clicked");
                removeWidget(hostView);
                numWidgets--;
                if(mSavedWidgetCounter > 0){
                    mSavedWidgetCounter--;
                }
                return true;
            }
        });

        homeScreen.addView(hostView, lp);
        //homeScreen.addView(hostView);
        numWidgets++;
        saveWidget(appWidgetId, numWidgets);
    }

    public void removeWidget(AppWidgetHostView hostView) {
        mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
        homeScreen.removeView(hostView);
    }

    private void configureWidget(Intent data) {
        //Log.d(TAG, "configureWidget: is called");
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            //Log.d(TAG, "configureWidget: appWidgetInfo.configure != null ");
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
        } else {
            //Log.d(TAG, "configureWidget: createWidget() is called");
            createWidget(data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAppWidgetHost.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAppWidgetHost.stopListening();
    }


    void addEmptyData(Intent pickIntent) {
        ArrayList customInfo = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList customExtras = new ArrayList();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }


    public static Drawable getActivityIcon(Context context, String packageName, String activityName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
        return resolveInfo.loadIcon(pm);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}
