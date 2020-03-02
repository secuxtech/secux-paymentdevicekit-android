package com.secuxtech.paymentdeviceexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.secuxtech.paymentdevicekit.BLEDevice;
import com.secuxtech.paymentdevicekit.BLEManagerCallback;
import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXBLEManager;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final String TAG = "P20P22 Test Tool";

    private Context mContext = this;
    private PaymentDevListAdapter mAdapter;

    private PaymentPeripheralManager mPeripheralManager = new PaymentPeripheralManager();

    public ArrayList<BLEDevice> mPaymentDevList = new ArrayList<>();

    private boolean mTestRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorTitle)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
        getWindow().setStatusBarColor(ContextCompat.getColor(this,R.color.colorTitle));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorTitle)));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            }
        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            SecuXBLEManager.getInstance().setBleCallback(mBLECallback);
            SecuXBLEManager.getInstance().setBLEManager((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE));
            if (!SecuXBLEManager.getInstance().isBleEnabled()){
                Log.i("BaseActivity", "BLE is not enabled!!");

                //BLEManager.getInstance().openBlueAsyn();
            }else{
                //BLEManager.getInstance().setBleCallback(bleCallback);
            }
        }else{
            Toast.makeText(this, "The phone DOES NOT support BLE!", Toast.LENGTH_SHORT).show();
        }

/*
        new Thread(new Runnable() {
            @Override
            public void run() {
                PaymentPeripheralManager peripheralManager = new PaymentPeripheralManager();
                peripheralManager.doGetIVKey(mContext, 10000, "4ab10000726b", -80, 10000);
                //peripheralManager.doGetIVKey(mContext, 10000, "001a00000048", -80, 10000);
            }
        }).start();



 */

        SecuXBLEManager.getInstance().startScan();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView_payment_devices);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));


    }

    public void onRescanButtonClick(View v){
        SecuXBLEManager.getInstance().stopScan();
        mPaymentDevList.clear();
        mAdapter.clearDeviceList();
        mAdapter.notifyDataSetChanged();

        SecuXBLEManager.getInstance().startScan();
    }


    public void onStartTestButtonClick(View v) {
        //SecuXBLEManager.getInstance().stopScan();

        boolean devSelected = false;
        for (PaymentDevice dev : mAdapter.getDevices()) {
            if (dev.deviceSelected) {
                devSelected = true;
                break;
            }
        }

        if (!devSelected){
            Toast toast = Toast.makeText(mContext, "Please select the device for testing", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }

        mTestRun = true;
        CommonProgressDialog.showProgressDialog(mContext, "Round 1"); // / " + Setting.getInstance().mTestCount);
        new Thread(new Runnable() {

            @Override
            public void run() {
                int idx = 2;
                boolean runFlag = true;

                List<PaymentDevice> devList = new ArrayList<>();
                devList.addAll(mAdapter.getDevices());
                while(true){
                    //Log.i(TAG, "Round " + i);

                    for (PaymentDevice dev : devList) {
                        if (dev.deviceSelected) {
                            Log.i(TAG, "Test " + dev.paymentDev.deviceID);
                            final PaymentDevice theDev = dev;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CommonProgressDialog.setProgressSubTip("Test " + theDev.paymentDev.deviceID);
                                }
                            });

                            Pair<Integer, String> ret = mPeripheralManager.doGetIVKeyWithoutStartScan(mContext, 10000, dev.paymentDev.deviceID, -80, 20000);
                            if (ret.first != PaymentPeripheralManager.SecuX_Peripheral_Operation_OK) {
                                final PaymentDevice pdev = dev;
                                final String errorMsg = ret.second;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(mContext, "GetIVKey " + pdev.paymentDev.deviceID + " failed! Error: " + errorMsg, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                });
                                runFlag = false;
                                break;
                            }else{
                                Log.i(TAG, "Get IVKey done");
                            }
                        }

                        if (!mTestRun)
                            break;
                    }


                    if (!runFlag || !mTestRun)
                        break;

                    final int currIdx = idx;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CommonProgressDialog.setProgressTip("Round " + currIdx); // + " / " + Setting.getInstance().mTestCount);
                        }
                    });

                    idx += 1;

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonProgressDialog.dismiss();
                    }
                });
            }
        }).start();
    }

    public void onStopTestButtonClick(View v) {
        mTestRun = false;
    }

    private BLEManagerCallback mBLECallback = new BLEManagerCallback() {
        @Override
        public void newBLEDevice(BLEDevice device) {
            super.newBLEDevice(device);

            int lastCount = mPaymentDevList.size();
            mPaymentDevList.add(device);
            if (lastCount==0){
                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView_payment_devices);
                mAdapter = new PaymentDevListAdapter(mContext, mPaymentDevList);
                recyclerView.setAdapter(mAdapter);
            }else {

                mAdapter.addNewDevice(device);
                mAdapter.notifyItemRangeInserted(lastCount, mPaymentDevList.size());
            }
        }

        @Override
        public void updateBLEDeviceRssi(int devIdx){

        }

        @Override
        public void updateConnDevStatus(@ConnDevStatus int status){

        }
    };

}
