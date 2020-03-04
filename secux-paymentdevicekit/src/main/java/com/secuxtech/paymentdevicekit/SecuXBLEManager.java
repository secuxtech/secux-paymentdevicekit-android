package com.secuxtech.paymentdevicekit;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.SystemClock;
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

        return instance;
    }

    private SecuXBLEManager(){
        setScanCallback();
    }

    private String mDeviceID = "";
    private int mScanRSSI = -90;

    private PaymentPeripheral mPaymentPeripheral = null;
    private byte[] mValidatePeripheralCommand = null;

    private com.secux.payment.cpp.MyNDK mNdk = new com.secux.payment.cpp.MyNDK();

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

            startScan(false);
            try{
                mScanDevDoneLockObject.wait(timeout);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            stopScan();
        }
        return new Pair<>(mDevice, mPaymentPeripheral);
    }

    public Pair<BluetoothDevice, PaymentPeripheral> findTheDevice(String devID, long timeout, int rssi){
        synchronized (mScanDevDoneLockObject) {
            mDeviceID = devID;
            mPaymentPeripheral = null;
            mDevice = null;
            mValidatePeripheralCommand = null;
            mScanRSSI = rssi;
            mScanWithCallbackFlag = false;
            try{
                mScanDevDoneLockObject.wait(timeout);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            mDeviceID = "";
            mScanWithCallbackFlag = true;
        }
        return new Pair<>(mDevice, mPaymentPeripheral);
    }

    public void startScan(){
        mPaymentPeripheral = null;
        startScan(true);
    }

    public boolean connectWithDevice(BluetoothDevice device, long connectTimeout){
        if (mContext!=null){

            synchronized (mConnectDoneLockObject) {
                Log.i(TAG, SystemClock.uptimeMillis() + " ConnectWithDevice");
                mDevice = device;
                mConnectDone = false;
                mBluetoothRxCharacter = null;
                mBluetoothTxCharacter = null;

                this.mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);

                try{
                    mConnectDoneLockObject.wait(connectTimeout);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }

        mConnectDone = true;
        return (mBluetoothTxCharacter!=null && mBluetoothRxCharacter!=null);
    }

    private void setScanCallback(){
        mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {

                int rssi = result.getRssi();
                if (rssi < mScanRSSI)
                    return;

                ScanRecord scanRecord = result.getScanRecord();
                byte[] scanResult = result.getScanRecord().getBytes();

                BluetoothDevice device = result.getDevice();
                BLEDevice dev = new BLEDevice();
                dev.Rssi = rssi;
                dev.device = device;

                boolean bFindDev = false;
                if (device!=null && scanResult!=null && scanResult.length>0){ // && device.getName()!=null && device.getName().length()!=0){
                    //Log.i(TAG, "device " + scanRecord.getDeviceName() );

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

                    if ((!bFindDev || !mScanWithCallbackFlag) && mPaymentPeripheral==null) {

                        if (!bFindDev) {
                            mBleDevArrList.add(dev);
                            Log.i(TAG, "new device " + mBleDevArrList.size() + " " + device.getName());
                        }

                        List<ADStructure> structures = ADPayloadParser.getInstance().parse(scanResult);
                        for (ADStructure adStructure : structures) {
                            Log.d(TAG,"adStructure: " + adStructure);
                            //PaymentUtil.debug("adStructure: " + adStructure);

                            if (adStructure instanceof ServiceData) {
                                ServiceData serviceData = (ServiceData) adStructure;
                                //String text_1 = serviceData.toString();
                                byte[] raw = serviceData.getData();
                                if (raw.length < 3){
                                    Log.i(TAG, "Invalid service data!");
                                    continue;
                                }
                                byte[] advertisedData = Arrays.copyOfRange(raw, 2, raw.length);
                                //scannedDevice.setAdvertisedData(advertisedData);

                                if( advertisedData != null && advertisedData.length > 0 ) {
                                    String strMsg = "";
                                    for (byte b: advertisedData){
                                        strMsg += String.format("%02x ", b);
                                    }
                                    Log.i(TAG, "advertisedData " + strMsg);

                                    //com.secux.payment.cpp.MyNDK mNdk = new com.secux.payment.cpp.MyNDK();
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
                                        Log.d(TAG, "mValidatePeripheralCommand " + strMsg);

                                        synchronized (mScanDevDoneLockObject) {

                                            mDevice = device;
                                            mPaymentPeripheral = paymentPeripheral;
                                            mScanDevDoneLockObject.notify();
                                        }
                                    }
                                }
                            }
                        }

                        if (mBleCallback != null && !bFindDev){
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
                Log.e(TAG, "scan error " + errorCode);
            }
        };
    }
}
