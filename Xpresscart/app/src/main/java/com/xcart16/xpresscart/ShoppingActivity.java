package com.xcart16.xpresscart;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.xcart16.xpresscart.itemclass.BarcodeResult;
import com.xcart16.xpresscart.itemclass.Item;
import com.xcart16.xpresscart.itemclass.Result;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.LinkedList;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShoppingActivity extends AppCompatActivity implements CallBack {

  //  public final String ACTION_USB_PERMISSION = "com.xcart16.xpresscart.USB_PERMISSION";
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private CameraSource cameraSource;
    private ListView cart;
    private ShoppingCartAdapter cartAdapter;
    private boolean updatePause;
    private final ShoppingActivity activity = this;
    private static final int SCAN_REQUEST_CODE = 5;
    private static int idcount;
    private Item[] hardCodeArray;
    private double amount;
    private TextView totalAmount;
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

    private RelativeLayout shoppingwrapper, bannerwrapper, keywrapper;
    private View curtain;
    private RelativeLayout dialog;
    private LinearLayout recommend;
    private int code;
    private TextView code_input;
    private boolean error;
    private ListView shoppinglist;
    private boolean show;

//    private UsbManager usbManager;
//    private UsbDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shopping);

        mContentView = findViewById(R.id.fullscreen_content);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cart = (ListView) findViewById(R.id.shopping_cart_list);
        cartAdapter = new ShoppingCartAdapter(this, new LinkedList<Item>());
        cart.setAdapter(cartAdapter);

        updatePause = false;
        show = false;

        shoppingwrapper = (RelativeLayout) findViewById(R.id.shopping_wrapper);
        bannerwrapper = (RelativeLayout) findViewById(R.id.banner_wrapper);
        keywrapper = (RelativeLayout) findViewById(R.id.key_wrapper);
        curtain = findViewById(R.id.curtain);
        dialog = (RelativeLayout) findViewById(R.id.dialog);
        code_input = (TextView) findViewById(R.id.code_input);
        shoppinglist = (ListView) findViewById(R.id.shopping_list_lv);

        findViewById(R.id.pay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                try {
                    cameraSource.release();
                } catch (NullPointerException e){
                    //ignore
                }
                Intent scanIntent = new Intent(activity, CardIOActivity.class);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false);
                scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, false);
                startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
            }
        });

        findViewById(R.id.key_code).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoppingwrapper.setVisibility(View.GONE);
                keywrapper.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.code_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shoppingwrapper.setVisibility(View.VISIBLE);
                keywrapper.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.code_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemToCart(findItemWithCode(code));
                idcount++;
                shoppingwrapper.setVisibility(View.VISIBLE);
                keywrapper.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.help_shopping).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                help();
            }
        });

        findViewById(R.id.confirm_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cameraSource.release();
                } catch (NullPointerException e) {
                    //ignored, it's just a check
                }
                startActivity(new Intent(activity, FinishActivity.class));
            }
        });

        findViewById(R.id.empty_tv_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.shopping_list_rl).setVisibility(show ? View.VISIBLE : View.GONE);
                show = !show;
            }
        });

        codeInit();

        cameraInit();
        idcount = 0;
        hardCodeArrayInit();
//        usbManager = (UsbManager) getSystemService(USB_SERVICE);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(broadcastReceiver, filter);

        recommend = (LinearLayout) findViewById(R.id.recommend_ll);
        amount = 0;
        totalAmount = (TextView) findViewById(R.id.total_amount);
        code = 0;
        error = false;
    }

    private void hardCodeArrayInit() {
        hardCodeArray = new Item[3];
        hardCodeArray[0] = new Item("Cheerios Oat Cereal (12OZ)", "0016000275263", 0.91, 3.94);
        hardCodeArray[0].setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cherrio_oat));
        hardCodeArray[1] = new Item("APPLE: McIntosh, large", "4019", 2, 2.98);
        hardCodeArray[1].setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.apple));
        hardCodeArray[1].setCustomQuantity("2.00lb  @$1.49/lb");
        hardCodeArray[2] = new Item("SG 135 Elbow Macaroni", "0033400601355", 1.69, 4.49);
        hardCodeArray[2].setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.tissue));

        //add buffer
        Item buffer = new Item("Ocean Spray Cranberry juice(64FO)", "0031200200723", 4.28, 2.49);
        buffer.setDiscount("2 for 4.00");
        buffer.setLocation("Lane 6 Shelf 2-2");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cranberry_juice));
        hardCodeArray[0].addSuggestion(buffer);
        hardCodeArray[1].addSuggestion(buffer);

        buffer = new Item("Kellogg's Special K Cereal (12OZ)", "0038000016110", 0.88, 3.29);
        buffer.setLocation("Lane 3 Shelf 2-4");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.kellogg_cereal));
        hardCodeArray[0].addSuggestion(buffer);

        buffer = new Item("Kellogg's Raisin Bran (13.7OZ)", "0038000596650", 1.33, 3.29);
        buffer.setLocation("Lane 3 Shelf 2-3");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.kellogg_bran));
        hardCodeArray[0].addSuggestion(buffer);

        buffer = new Item("HS Itchy Scalp Care Shampoo (13.5FO)", "0037000062059", 4.99, 0.79);
        buffer.setLocation("Lane 1 Shelf 2-2");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hs_shampoo));
        hardCodeArray[1].addSuggestion(buffer);

        buffer = new Item("HS Itchy Scalp Care Conditioner(13.5FO)", "0037000473640", 4.99, 0.82);
        buffer.setLocation("Lane 1 Shelf 2-2");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hs_conditioner));
        hardCodeArray[1].addSuggestion(buffer);

        buffer = new Item("Hunts Tomato Sauce", "0027000390146", 1.39, 0.82);
        buffer.setLocation("Lane 2 Shelf 5");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.hunts_tomato_sauce));
        hardCodeArray[2].addSuggestion(buffer);

        buffer = new Item("Bertolli Alfredo Sauce", "0036200219294", 2.49, 0.82);
        buffer.setLocation("Lane 2 Shelf 1");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.bertolli_alfredo_sauce));
        hardCodeArray[2].addSuggestion(buffer);

        buffer = new Item("SG 109 Thin Spaghetti", "0033400601096", 1.69, 0.82);
        buffer.setLocation("Lane 2 Shelf 3");
        buffer.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.thin_spaghetti));
        hardCodeArray[2].addSuggestion(buffer);

        LinkedList<Item> list = new LinkedList<>();
        buffer = new Item("Cereal", "21", 3, 2);
        buffer.setLocation("Lane 3 Shelf 2-4");
        list.add(buffer);
        buffer = new Item("Apple", "0000", 1, 1);
        buffer.setLocation("Lane 4 Shelf 1-2");
        list.add(buffer);
        buffer = new Item("Pasta", "333", 2,1);
        buffer.setLocation("Lane 2 Shelf 3");
        list.add(buffer);

        ShoppingListAdapter adapter = new ShoppingListAdapter(this, list);
        shoppinglist.setAdapter(adapter);
    }

    private void codeInit() {
        findViewById(R.id.code_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 1;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 2;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 3;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 4;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 5;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 6;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 7;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 8;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = 10 * code + 9;
                code_input.setText(code + "");
            }
        });

        findViewById(R.id.code_backspace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                code = code / 10;
                code_input.setText(code + "");
            }
        });
    }

    protected void help() {
        if (idcount >= 6) {
            cameraSource.release();
            Intent intent = new Intent(this, ShoppingActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        if (idcount == 1) {
            idcount ++;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraSource.release();
                    shoppingwrapper.setVisibility(View.GONE);
                    bannerwrapper.setVisibility(View.VISIBLE);
                    FrameLayout frame = (FrameLayout) findViewById(R.id.shopping_frame);
                    frame.getChildAt(0).setVisibility(View.GONE);
                    frame.setVisibility(View.GONE);
                    TextView title = (TextView) findViewById(R.id.top_banner_title);
                    title.setText(R.string.removed_item);
                    TextView main = (TextView) findViewById(R.id.top_banner_tv);
                    main.setText(R.string.removed_scan);
                    BarcodeDetector detector = (new BarcodeDetector.Builder(activity)).build();
                    BarcodeTrackerFactory barcodeTrackerFactory = new BarcodeTrackerFactory(activity);
                    detector.setProcessor(new MultiProcessor.Builder<>(barcodeTrackerFactory).build());
                    cameraSource = new CameraSource.Builder(activity, detector).setAutoFocusEnabled(true).build();
                    CameraPreview preview = new CameraPreview(activity, cameraSource);
                    FrameLayout cameraFrame = (FrameLayout) findViewById(R.id.remove_frame);
                    cameraFrame.addView(preview);
                    findViewById(R.id.remove_help).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cameraSource.release();
                                    findViewById(R.id.shopping_frame).setVisibility(View.VISIBLE);
                                    shoppingwrapper.setVisibility(View.VISIBLE);
                                    FrameLayout frame = (FrameLayout) findViewById(R.id.remove_frame);
                                    frame.getChildAt(0).setVisibility(View.GONE);
                                    bannerwrapper.setVisibility(View.GONE);
                                    cartAdapter.getList().remove(hardCodeArray[0]);
                                    amount -= hardCodeArray[0].getPrice();
                                    cartAdapter.notifyDataSetChanged();
                                    NumberFormat formatter = new DecimalFormat("#0.00");
                                    StringBuilder sb = new StringBuilder().append("Total: $").append(formatter.format(amount));
                                    totalAmount.setText(sb.toString());
                                    cameraInit();
                                }
                            });
                        }
                    });
                }
            });
            return;
        }
        else if (idcount == 4 && !error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shoppingwrapper.setVisibility(View.GONE);
                    curtain.setVisibility(View.VISIBLE);
                    curtain.setBackgroundResource(R.color.black_overlay);
                    dialog.setVisibility(View.VISIBLE);
                    findViewById(R.id.recommend_wrapper).setVisibility(View.GONE);
                    findViewById(R.id.error_wrapper).setVisibility(View.VISIBLE);
                    mContentView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            findViewById(R.id.error_wrapper).setVisibility(View.GONE);
                            findViewById(R.id.recommend_wrapper).setVisibility(View.VISIBLE);
                            dialog.setVisibility(View.GONE);
                            curtain.setVisibility(View.GONE);
                            shoppingwrapper.setVisibility(View.VISIBLE);
                            mContentView.setOnClickListener(null);
                            error = true;
                        }
                    });
                }
            });
            idcount++;
            return;
        }
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        int id = idcount;
        if (idcount >= 1) {
            id = idcount - 1;
        }
        idcount++;
        addItemToCart(hardCodeArray[id]);
    }



    private void addItemToCart(final Item i) {
        cartAdapter.getList().add(i);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                curtain.setBackgroundResource(R.color.welcome_text_color);
                curtain.setVisibility(View.VISIBLE);
                TextView tv = (TextView) findViewById(R.id.empty_tv_1);
                tv.setText(R.string.cart_title);
                amount += i.getPrice();
                NumberFormat formatter = new DecimalFormat("#0.00");
                StringBuilder sb = new StringBuilder().append("Total: $").append(formatter.format(amount));
                totalAmount.setText(sb.toString());
                findViewById(R.id.empty_tv_2).setVisibility(View.GONE);
                cartAdapter.notifyDataSetChanged();
                cart.setSelection(cartAdapter.getCount() - 1);
                recommend.removeAllViews();
                for (Item item : i.getSuggestion()) {
                    addRecommended(item);
                }
            }
        });
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                curtain.setVisibility(View.GONE);
            }
        });
    }

    private void addRecommended(Item item) {
        final Item fitem = item;
        LayoutInflater inflater = LayoutInflater.from(this);
        LinearLayout recommended_item = (LinearLayout) inflater.inflate(R.layout.recommended_item, null, false);
        ImageView iv = (ImageView) recommended_item.findViewById(R.id.recommend_picture);
        iv.setImageBitmap(item.getBitmap());
        TextView tv = (TextView) recommended_item.findViewById(R.id.recommend_item_name);
        tv.setText(item.getName());
        recommended_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        curtain.setBackgroundResource(R.color.black_overlay);
                        curtain.setVisibility(View.VISIBLE);
                        dialog.setVisibility(View.VISIBLE);
                        ImageView iv = (ImageView) dialog.findViewById(R.id.recommend_iv);
                        iv.setImageBitmap(fitem.getBitmap());
                        String display = fitem.getName() + "\n" + fitem.getDisplayPrice() + "\n" + fitem.getLocation();
                        TextView tv = (TextView) dialog.findViewById(R.id.recommend_popup_tv);
                        tv.setText(display);
                        dialog.findViewById(R.id.recommend_ok).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        curtain.setVisibility(View.GONE);
                                        dialog.setVisibility(View.GONE);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
        recommend.addView(recommended_item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
//        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
//        if (!usbDevices.isEmpty()) {
//            boolean keep = true;
//            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
//                device = entry.getValue();
//                int deviceVID = device.getVendorId();
//                if (deviceVID == 0x2341)//Arduino Vendor ID
//                {
//                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//                    usbManager.requestPermission(device, pi);
//                    keep = false;
//                } else {
//                    device = null;
//                }
//                if (!keep)
//                    break;
//            }
//        }
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
        if (idfrom == MainActivity.BARCODE_TRACKER_ID && findViewById(R.id.banner_wrapper).getVisibility() == View.VISIBLE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraSource.release();
                    findViewById(R.id.shopping_wrapper).setVisibility(View.GONE);
                    findViewById(R.id.banner_wrapper).setVisibility(View.VISIBLE);
                    cartAdapter.getList().remove(hardCodeArray[4]);
                    cameraInit();
                }
            });
        }
        if (idfrom == MainActivity.BARCODE_TRACKER_ID && !updatePause) {
            updatePause = true;
            Barcode barcode = ((BarcodeResult) result).barcode;
            //do some checking here of the barcode to get the correct item
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            cartAdapter.getList().add(findBarcode(barcode));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.empty_tv_1).setVisibility(View.GONE);
                    findViewById(R.id.empty_tv_2).setVisibility(View.GONE);
                    cartAdapter.notifyDataSetChanged();
                    cart.setSelection(cartAdapter.getCount() - 1);
                }
            });
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                updatePause = false;
            }
        }
    }

    private Item findItemWithCode(int code) {
        String c = code + "";
        for (Item it : hardCodeArray) {
            if (it.getBarcode().equals(c)) {
                return it;
            }
        }
        int id = idcount;
        if (idcount >= 2) {
            id = idcount - 1;
        }
        idcount++;
        return hardCodeArray[id];
    }

    private Item findBarcode(Barcode barcode) {
        Item item = null;
        for (Item it : hardCodeArray) {
            if (it.getBarcode().equals(barcode.displayValue)) {
                item = it;
                break;
            }
        }
        int id = idcount;
        if (item == null) {
            if (idcount >= 2) {
                id = idcount - 1;
            }
        }
        idcount++;
        return hardCodeArray[id];
  //      return new Item(barcode.displayValue, barcode.displayValue, 2.0, 2.99);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            cameraSource.release();
        } catch (NullPointerException e) {
            //ignored
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_REQUEST_CODE) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shoppingwrapper.setVisibility(View.GONE);
                        bannerwrapper.setVisibility(View.VISIBLE);
                        findViewById(R.id.remove_wrapper).setVisibility(View.GONE);
                        findViewById(R.id.confirm_main_wrapper).setVisibility(View.VISIBLE);
                        ((TextView)findViewById(R.id.top_banner_title)).setText(R.string.confirm_card);
                        ((TextView)findViewById(R.id.top_banner_tv)).setText(R.string.confirm_ok);
                    }
                });
                delayedHide(5000);
            } else {
                try {
                    cameraSource.start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SecurityException se) {
                    se.printStackTrace();
                }
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }


//    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
//                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
//                if (granted) {
//                    UsbDeviceConnection connection = usbManager.openDevice(device);
//                    UsbSerialDevice serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
//                    if (serialPort != null) {
//                        if (serialPort.open()) { //Set Serial Connection Parameters.
//                            serialPort.setBaudRate(9600);
//                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
//                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
//                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
//                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
//                            serialPort.read(mCallback);
//
//                        } else {
//                            Log.d("SERIAL", "PORT NOT OPEN");
//                        }
//                    } else {
//                        Log.d("SERIAL", "PORT IS NULL");
//                    }
//                } else {
//                    Log.d("SERIAL", "PERM NOT GRANTED");
//                }
//            }
//        }
//    };
//
//    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
//        @Override
//        public void onReceivedData(byte[] arg0) {
//            String data = null;
//            try {
//                data = new String(arg0, "UTF-8");
//                Toast.makeText(activity, data, Toast.LENGTH_SHORT).show();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }
//    };

}
