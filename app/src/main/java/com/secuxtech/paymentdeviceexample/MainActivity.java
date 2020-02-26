package com.secuxtech.paymentdeviceexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXBLEManager;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    private Context mContext = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
            }
        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            //有藍芽功能模組

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                PaymentPeripheralManager peripheralManager = new PaymentPeripheralManager();
                peripheralManager.doGetIVKey(mContext, 5000, "4ab10000726b", -80, 10000);
            }
        }).start();

    }
}
