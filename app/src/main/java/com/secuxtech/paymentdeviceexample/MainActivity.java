package com.secuxtech.paymentdeviceexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.secuxtech.paymentdevicekit.BLEDevice;
import com.secuxtech.paymentdevicekit.BLEManagerCallback;
import com.secuxtech.paymentdevicekit.MachineIoControlParam;
import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXBLEManager;

import org.json.JSONObject;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static com.secuxtech.paymentdevicekit.PaymentPeripheralManager.SecuX_Peripheral_Operation_OK;


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



        SecuXBLEManager.getInstance().startScan();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView_payment_devices);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public byte[] getEncryptMobilePaymentCommand(String terminalId, String amount, String ivKey, String cryptKey){
        Log.d(TAG,"getEncryptMobilePaymentCommand()");
        String plainTransaction = getMobilePaymentCommand(terminalId, amount);

        // AES 256 crypt
        byte[] encrytedTransactionData = null;
        try {
            byte[] ivKeyData = ivKey.getBytes();
            encrytedTransactionData = encrypt(ivKeyData, cryptKey.getBytes(), plainTransaction.getBytes());
        }catch (Exception ex) {
            encrytedTransactionData = null;
        }

        return encrytedTransactionData;
    }

    public byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public String getMobilePaymentCommand(String terminalId, String amount) {
        Log.d(TAG,"======= getMobilePaymentCommand() =======");
        Calendar c = Calendar.getInstance();
        SimpleDateFormat format1 = new SimpleDateFormat("yyyyMMddHHmmss");
        String transactionTime = format1.format(c.getTime());
        String paymentID = "P12345678912" + transactionTime.substring(transactionTime.length() - 4);
        String amountString = convertAmountToFourDigits(amount);
        String amountCurrency = "DCT";
        if(amountCurrency.length() == 0) amountCurrency = "TWD";
        String plainTransaction = transactionTime + "," + paymentID + "," + terminalId + "," + amountString+","+amountCurrency;

        Log.d(TAG,"plainTransaction:"+plainTransaction);
        return plainTransaction;
    }

    private String convertAmountToFourDigits(String amount) {
        int length = String.valueOf(amount).length();

        String amountStr = amount+"";
        int Remaining = 8-length;
        for(int i=0;i<Remaining;i++){
            amountStr = " "+amountStr;
        }

//        if (length == 1) {
//            return "   " + amount;
//        }
//
//        if (length == 2) {
//            return "  " + amount;
//        }
//
//        if (length == 3) {
//            return " " + amount;
//        }

        // length is 8
        Log.d(TAG,"convertAmountToFourDigits: "+amountStr);
        Log.d(TAG,"convertAmountToFourDigits: "+amountStr.length());
        return amountStr;
    }

    public void onRescanButtonClick(View v){
        SecuXBLEManager.getInstance().stopScan();
        mPaymentDevList.clear();
        mAdapter.clearDeviceList();
        mAdapter.notifyDataSetChanged();

        SecuXBLEManager.getInstance().startScan();
    }

    public boolean payToDevice(List<PaymentDevice> devList, String amount){
        boolean payToDevRet = false;
        for (PaymentDevice dev : devList) {
            if (!dev.deviceSelected) {
                //Log.i(TAG, "Test " + dev.paymentDev.deviceID);
                continue;
            }

            final String devID = dev.paymentDev.deviceID;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    CommonProgressDialog.setProgressSubTip(devID);
                }
            });

            PaymentPeripheralManager peripheralManager = new PaymentPeripheralManager();
            //peripheralManager.doGetIVKey(mContext, 10000, "4ab10000726b", -80, 10000);
            Pair<Integer, String> getIVKeyret = peripheralManager.doGetIVKeyWithoutStartScan(mContext, 10, devID, -80, 30);
            String ivKey = "";
            if (getIVKeyret.first == SecuX_Peripheral_Operation_OK) {
                ivKey = getIVKeyret.second;
                Log.i(TAG, "ivKey=" + ivKey);
            } else {
                Log.i(TAG, "Get ivKey failed");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        CommonProgressDialog.dismiss();
                    }
                });
                return false;
            }

            try {
                JSONObject ioCtrlParamJson = new JSONObject("{\"uart\":\"0\",\"gpio1\":\"0\",\"gpio2\":\"0\",\"gpio31\":\"0\",\"gpio32\":\"0\",\"gpio4\":\"0\",\"gpio4c\":\"0\",\"gpio4cInterval\":\"0\",\"gpio4cCount\":\"0\",\"gpio4dOn\":\"0\",\"gpio4dOff\":\"0\",\"gpio4dInterval\":\"0\",\"runStatus\":\"0\",\"lockStatus\":\"0\"}");

                final MachineIoControlParam machineIoControlParam = new MachineIoControlParam();
                machineIoControlParam.setGpio1(ioCtrlParamJson.getString("gpio1"));
                machineIoControlParam.setGpio2(ioCtrlParamJson.getString("gpio2"));
                machineIoControlParam.setGpio31(ioCtrlParamJson.getString("gpio31"));
                machineIoControlParam.setGpio32(ioCtrlParamJson.getString("gpio32"));
                machineIoControlParam.setGpio4(ioCtrlParamJson.getString("gpio4"));
                machineIoControlParam.setGpio4c(ioCtrlParamJson.getString("gpio4c"));
                machineIoControlParam.setGpio4cCount(ioCtrlParamJson.getString("gpio4cCount"));
                machineIoControlParam.setGpio4cInterval(ioCtrlParamJson.getString("gpio4cInterval"));
                machineIoControlParam.setGpio4dOn(ioCtrlParamJson.getString("gpio4dOn"));
                machineIoControlParam.setGpio4dOff(ioCtrlParamJson.getString("gpio4dOff"));
                machineIoControlParam.setGpio4dInterval(ioCtrlParamJson.getString("gpio4dInterval"));
                machineIoControlParam.setUart(ioCtrlParamJson.getString("uart"));
                machineIoControlParam.setRunStatus(ioCtrlParamJson.getString("runStatus"));
                machineIoControlParam.setLockStatus(ioCtrlParamJson.getString("lockStatus"));

                //String encryptedStr = "91sGnngVALh6Ep3RsEJKhGQEQM2ntJiZxR0cwiQNLT\\/SbZcCVux1WHabIrzqkICsPz3PudpRHnEFwcbiMO7qhA==";
                //final byte[] encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT);
                final byte[] encryptedData = getEncryptMobilePaymentCommand(devID.substring(devID.length() - 8, devID.length()), amount, ivKey, "PA123456789012345678901234567890");

                Pair<Integer, String> ret = peripheralManager.doPaymentVerification(encryptedData, machineIoControlParam);
                if (ret.first != 0) {
                    Log.i(TAG, "Payment failed! " + ret.second);
                } else {
                    Log.i(TAG, "Payment done");
                    payToDevRet = true;
                }
            } catch (Exception e) {
                Log.i(TAG, "Generate io configuration failed!");
            }
        }
        return payToDevRet;
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

                    /*
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
                            if (ret.first != SecuX_Peripheral_Operation_OK) {
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


                     */

                    if (!payToDevice(devList, String.valueOf(idx))){
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
