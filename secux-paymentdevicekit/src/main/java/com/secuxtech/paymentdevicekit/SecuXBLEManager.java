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

    private String mDeviceID = "";
    private long mConnTimeout = 0;
    private int mScanRSSI = -90;

    private BluetoothDevice mDevice = null;
    private PaymentPeripheral mPaymentPeripheral = null;
    private byte[] mValidatePeripheralCommand = null;
    private static Object mScanDevDoneLockObject = new Object();
    //private com.secux.payment.cpp.MyNDK mNdk = new com.secux.payment.cpp.MyNDK();

    public byte[] getValidatePeripheralCommand(){
        return mValidatePeripheralCommand;
    }

    public Pair<BluetoothDevice, PaymentPeripheral> scanForTheDevice(String devID, long timeout, int rssi){
        synchronized (mScanDevDoneLockObject) {
            mDeviceID = devID;
            mPaymentPeripheral = null;
            mDevice = null;
            mValidatePeripheralCommand = null;
            mScanRSSI = rssi;

            startScan();
            try{
                mScanDevDoneLockObject.wait(timeout);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            stopScan();
        }
        return new Pair<>(mDevice, mPaymentPeripheral);
    }

    public boolean connectWithDevice(BluetoothDevice device, long connectTimeout){
        if (mContext!=null){
            synchronized (mConnectDoneLockObject) {
                mConnTimeout = connectTimeout;
                this.mBluetoothGatt = device.connectGatt(mContext, true, mBluetoothGattCallback);

                try{
                    mConnectDoneLockObject.wait(connectTimeout);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }


        }

        return (mBluetoothTxCharacter!=null && mBluetoothRxCharacter!=null);
    }

    private void setScanCallback(){
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                int rssi = result.getRssi();
                if (rssi < mScanRSSI)
                    return;

                byte[] scanResult = result.getScanRecord().getBytes();

                BluetoothDevice device = result.getDevice();
                BLEDevice dev = new BLEDevice();
                dev.Rssi = rssi;
                dev.device = device;

                boolean bFindDev = false;
                if (device!=null && device.getName()!=null && device.getName().length()!=0){
                    for (int i = 0; i < mBleDevArrList.size(); i++) {
                        //System.out.println(cars.get(i));
                        BLEDevice devItem = mBleDevArrList.get(i);
                        if (devItem.device.equals(device)){
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
                    if (!bFindDev && mPaymentPeripheral==null) {

                        mBleDevArrList.add(dev);
                        Log.i(TAG, "new device " + mBleDevArrList.size() + " " + device.getName() );

                        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanResult);
                        for (ADStructure adStructure : structures) {
                            Log.d(TAG,"adStructure: " + adStructure);
                            //PaymentUtil.debug("adStructure: " + adStructure);

                            if (adStructure instanceof ServiceData) {
                                ServiceData serviceData = (ServiceData) adStructure;
                                //String text_1 = serviceData.toString();
                                byte[] raw = serviceData.getData();
                                byte[] advertisedData = Arrays.copyOfRange(raw, 2, raw.length);
                                //scannedDevice.setAdvertisedData(advertisedData);

                                if( advertisedData != null ) {
                                    String strMsg = "";
                                    for (byte b: advertisedData){
                                        strMsg += String.format("%02x ", b);
                                    }
                                    Log.i(TAG, "advertisedData " + strMsg);

                                    com.secux.payment.cpp.MyNDK mNdk = new com.secux.payment.cpp.MyNDK();
                                    PaymentPeripheral paymentPeripheral = mNdk.createPaymentPeripheralObjectFromNative(advertisedData);
                                    String fwVer = paymentPeripheral.getFirmwareVersion();
                                    String uid = paymentPeripheral.getUniqueId();
                                    Log.i(TAG, "Dev UUID=" + uid + " FW ver=" + fwVer);
                                    dev.deviceID = uid;

                                    if (uid.compareToIgnoreCase(mDeviceID)==0){
                                        mValidatePeripheralCommand = mNdk.getValidatePeripheralCommand(5, paymentPeripheral);
                                        strMsg = "";
                                        for (byte b: mValidatePeripheralCommand){
                                            strMsg += String.format("%x ", b);
                                        }
                                        Log.i(TAG, "mValidatePeripheralCommand " + strMsg);

                                        synchronized (mScanDevDoneLockObject) {
                                            mDevice = device;
                                            mPaymentPeripheral = paymentPeripheral;
                                            mScanDevDoneLockObject.notify();
                                        }
                                    }
                                }
                            }
                        }

                        if (mBleCallback != null){
                            mBleCallback.newBLEDevice(dev);
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
