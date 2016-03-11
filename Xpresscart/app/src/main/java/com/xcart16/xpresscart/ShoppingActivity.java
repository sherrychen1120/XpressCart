package com.xcart16.xpresscart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.xcart16.xpresscart.itemclass.BarcodeResult;
import com.xcart16.xpresscart.itemclass.Item;
import com.xcart16.xpresscart.itemclass.Result;

import java.util.LinkedList;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShoppingActivity extends AppCompatActivity implements CallBack {
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private CameraSource cameraSource;
    private ListView cart;
    private ShoppingItemAdapter cartAdapter;
    private boolean updatePause;
    private final Activity activity = this;
    private static final int SCAN_REQUEST_CODE = 5;
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

        setContentView(R.layout.activity_shopping);

        mContentView = findViewById(R.id.fullscreen_content);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cart = (ListView) findViewById(R.id.shopping_cart_list);
        cartAdapter = new ShoppingItemAdapter(this, new LinkedList<Item>());
        cart.setAdapter(cartAdapter);

        updatePause = false;

        findViewById(R.id.pay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent scanIntent = new Intent(activity, CardIOActivity.class);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, true);
                startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
            }
        });

        cameraInit();
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

    private void cameraInit() {
        BarcodeDetector detector = (new BarcodeDetector.Builder(this)).build();
        BarcodeTrackerFactory barcodeTrackerFactory = new BarcodeTrackerFactory(this);
        detector.setProcessor(new MultiProcessor.Builder<>(barcodeTrackerFactory).build());
        cameraSource = new CameraSource.Builder(this, detector).setAutoFocusEnabled(true).build();
        CameraPreview preview = new CameraPreview(this, cameraSource);
        FrameLayout cameraFrame = (FrameLayout) findViewById(R.id.shopping_frame);
        cameraFrame.addView(preview);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void callBack(int idfrom, Result result) {
        if (idfrom == MainActivity.BARCODE_TRACKER_ID && !updatePause) {
            updatePause = true;
            Barcode barcode = ((BarcodeResult) result).barcode;
            //do some checking here of the barcode to get the correct item
            cartAdapter.getList().add(findBarcode(barcode));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cartAdapter.notifyDataSetChanged();
                    cart.setSelection(cartAdapter.getCount() - 1);
                }
            });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                updatePause = false;
            }
        }
    }

    private Item findBarcode(Barcode barcode) {
        return new Item(barcode.displayValue, barcode, 2.0, 2.99);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            } else {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }
}
