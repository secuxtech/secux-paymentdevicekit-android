package com.secuxtech.paymentdevicekit;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.util.Pair;

import com.example.apple1.mylibrary.PaymentPeripheral;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.ServiceData;

import java.util.Arrays;
import java.util.List;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-26
 */
public class SecuXBLEManager extends BLEManager{

    private static SecuXBLEManager instance = null;

    public static SecuXBLEManager getInstance(){
        if (instance == null){
            instance = new SecuXBLEManager();
        }
        //manager = instance;
        return instance;
    }

    private SecuXBLEManager(){
        setScanCallback();
    }

    public String mDeviceID = "";
    public long mScanTimeout = 0;
    public long mConnTimeout = 0;

    private BluetoothDevice mDevice = null;
    private PaymentPeripheral mPaymentPeripheral = null;
    private static Object mScanDevDoneLockObject = new Object();

    public Pair<BluetoothDevice, PaymentPeripheral> scanForTheDevice(String devID, long timeout){
        synchronized (mScanDevDoneLockObject) {
            mDevice = null;
            startScan();
            try{
                mScanDevDoneLockObject.wait(timeout);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return new Pair<>(mDevice, mPaymentPeripheral);
    }

    private void setScanCallback(){
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                byte[] scanData=result.getScanRecord().getBytes();
                //把byte数组转成16进制字符串，方便查看

                //Log.e("TAG","onScanResult :"+result.getScanRecord().toString());

                BluetoothDevice device = result.getDevice();

                byte[] scanResult = result.getScanRecord().getBytes();

                int rssi = result.getRssi();


                BLEDevice dev = new BLEDevice();
                dev.Rssi = rssi;
                dev.device = device;

                boolean bFindDev = false;
                if (device!=null && device.getName()!=null && device.getName().length()!=0){
                    for (int i = 0; i < mBleDevArrList.size(); i++) {
                        //System.out.println(cars.get(i));
                        BLEDevice devItem = mBleDevArrList.get(i);
                        if (devItem.device.getName().compareToIgnoreCase(device.getName())==0){
                            bFindDev = true;

                            if (devItem.Rssi != rssi){
                                devItem.Rssi = rssi;
                                if (mBleCallback!=null){
                                    mBleCallback.updateBLEDeviceRssi(i);
                                }
                            }


                            break;
                        }

                    }
                    if (!bFindDev && device.getName() != null) {

                        mBleDevArrList.add(dev);
                        Log.i("BLEManager", "add new device " + mBleDevArrList.size() + " " + device.getName() );

                        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanResult);
                        for (ADStructure adStructure : structures) {
                            Log.d("","adStructure: " + adStructure);
                            //PaymentUtil.debug("adStructure: " + adStructure);

                            if (adStructure instanceof ServiceData) {
                                ServiceData serviceData = (ServiceData) adStructure;
                                //String text_1 = serviceData.toString();
                                byte[] raw = serviceData.getData();
                                byte[] advertisedData = Arrays.copyOfRange(raw, 2, raw.length);
                                //scannedDevice.setAdvertisedData(advertisedData);

                                com.secux.payment.cpp.MyNDK ndk = new com.secux.payment.cpp.MyNDK();
                                if( advertisedData != null ) {
                                    PaymentPeripheral paymentPeripheral = ndk.createPaymentPeripheralObjectFromNative(advertisedData);
                                    String uid = paymentPeripheral.getUniqueId();
                                    Log.i("", uid);

                                    if (uid.compareToIgnoreCase(mDeviceID)==0){
                                        mDevice = device;
                                        mPaymentPeripheral = paymentPeripheral;
                                        synchronized (mScanDevDoneLockObject) {
                                            mScanDevDoneLockObject.notify();
                                        }
                                    }
                                }
                            }
                        }



                        if (mBleCallback != null){
                            mBleCallback.newBLEDevice();
                        }

                    /*
                    for(int i=0; i<mBleDevArrList.size(); i++){
                        BLEDevice devItem = mBleDevArrList.get(i);
                        Log.d("info", "Device " + devItem.device.getName() + " rssi=" + devItem.Rssi);
                    }

                   */
                    }
                }
            }
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }
}
