package com.xcart16.xpresscart;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends AppCompatActivity implements CallBack {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    public static final int BARCODE_TRACKER_ID = 2;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private CameraSource cameraSource;
    private boolean barcodescanned;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        if (code != ConnectionResult.SUCCESS) {
            Toast.makeText(this, "can't get google api", Toast.LENGTH_LONG).show();
            finish();
        }

        setContentView(R.layout.activity_main);

        getPermission();
        mContentView = findViewById(R.id.fullscreen_content);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        findViewById(R.id.test_skip_welcome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMain(null);
            }
        });

        barcodescanned = false;
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 2);
        } else {
            cameraInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int  requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Access of your camera is required to use this application.", Toast.LENGTH_LONG).show();
            return;
        }
        cameraInit();
    }

    private void cameraInit() {
        BarcodeDetector detector = (new BarcodeDetector.Builder(this)).build();
        BarcodeTrackerFactory barcodeTrackerFactory = new BarcodeTrackerFactory(this);
        detector.setProcessor(new MultiProcessor.Builder<>(barcodeTrackerFactory).build());
        cameraSource = new CameraSource.Builder(this, detector).setAutoFocusEnabled(true).build();
        CameraPreview preview = new CameraPreview(this, cameraSource);
        FrameLayout cameraFrame = (FrameLayout) findViewById(R.id.welcome_camera);
        cameraFrame.addView(preview);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private void startMain(User user) {
        //remember to add the user into the intent
        Intent intent = new Intent(this, ShoppingActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    @Override
    public void callBack(int idfrom, Result result) {
        if (idfrom == BARCODE_TRACKER_ID && !barcodescanned) {
            barcodescanned = true;
            //need to look up the barcode or something, find the user or something
            Barcode barcode = ((BarcodeResult) result).barcode;
            Log.d("Barcode", barcode.displayValue);
            //do something here and then start main
            startMain(null);
            finish();
        }
    }
}
