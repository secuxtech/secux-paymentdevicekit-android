package com.secuxtech.paymentdevicekit;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;


import com.example.apple1.mylibrary.PaymentPeripheral;
import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.ServiceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.secuxtech.paymentdevicekit.BLEManagerCallback.*;

public class BLEManager {

    public ArrayList<BLEDevice> mBleDevArrList = new ArrayList<BLEDevice>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothScanner;

    private BluetoothGatt mBluetoothGatt = null;
    private BluetoothGattCharacteristic mBluetoothRxCharacter = null;
    private BluetoothGattCharacteristic mBluetoothTxCharacter = null;

    protected BLEManagerCallback mBleCallback = null;
    protected ScanCallback mScanCallback = null;

    /*
    public static final String ServiceUUID =  "6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String TXCharacteristicUUID = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
    private static final String RXCharacteristicUUID = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E";

     */

    private static final String ServiceUUID =  "BC280001-610E-4C94-A5E2-0F352D4B5256";
    private static final String TXCharacteristicUUID = "BC280003-610E-4C94-A5E2-0F352D4B5256";
    private static final String RXCharacteristicUUID = "BC280002-610E-4C94-A5E2-0F352D4B5256";

    private static Object mWriteDoneLockObject = new Object();
    private static Object mReadDoneLockObject = new Object();
    private static Object mConnectDoneLockObject = new Object();


    private List<Byte> mRecvData = new ArrayList<Byte>();

    public Context mContext = null;


    BLEManager(){

    }

    public void setBleCallback(BLEManagerCallback callback){
        mBleCallback = callback;
    }

    public void setBLEManager(BluetoothManager bleMgr){
        mBluetoothManager = bleMgr;
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public boolean isSupportBle(){
        return mBluetoothAdapter != null;
    }

    public boolean isBleEnabled(){
        return isSupportBle() && mBluetoothAdapter.isEnabled();
    }

    public void openBlueAsyn(){
        if (isSupportBle()) {
            mBluetoothAdapter.enable();
        }
    }

    public void startScan(){
        Log.d("info", "Start ble scan");

        mBleDevArrList.clear();
        //mBluetoothScanner.startScan(scanCallback);

        ScanFilter scanFilter = (new ScanFilter.Builder()).setServiceUuid(new ParcelUuid(UUID.fromString(ServiceUUID))).build();
        ArrayList<ScanFilter> filters = new ArrayList();
        filters.add(scanFilter);
        android.bluetooth.le.ScanSettings.Builder scanSettingsBuilder = new android.bluetooth.le.ScanSettings.Builder();
        scanSettingsBuilder.setScanMode(1);
        scanSettingsBuilder.setCallbackType(1);

        ScanSettings scanSettings = scanSettingsBuilder.build();
        mBluetoothScanner.startScan(filters, scanSettings, mScanCallback);
    }

    public void stopScan(){
        Log.d("info", "Stop ble scan");
        mBluetoothScanner.stopScan(mScanCallback);
    }

    BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        //當連線狀態發生改變
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.d("BLEManager", "onConnectionStateChange: thread "
                    + Thread.currentThread() + " status " + newState);



            if (status != BluetoothGatt.GATT_SUCCESS) {
                String err = "Cannot connect device with error status: " + status;
                // 當嘗試連線失敗的時候呼叫 disconnect 方法是不會引起這個方法回撥的，所以這裡
                //   直接回調就可以了。
                gatt.close();
                mBluetoothGatt = null;
                Log.e("BLEManager", err);

                if (mBleCallback != null){
                    mBleCallback.updateConnDevStatus(BlEDEV_ConnFailed);
                }
                return;
            }


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //getDevServiceAndCharacteristicsAsyn();
                gatt.discoverServices();



            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mBluetoothGatt.close();
                mBluetoothGatt = null;

                if (mBleCallback != null){
                    mBleCallback.updateConnDevStatus(BlEDEV_Disconnected);
                }
            }

        }

        //發現新服務，即呼叫了mBluetoothGatt.discoverServices()後，返回的資料
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            boolean findService = false;
            List<BluetoothGattService> services = mBluetoothGatt.getServices();
            for (BluetoothGattService gattService : services) {

                final String uuid = gattService.getUuid().toString();
                Log.i("BLEManager", "Service discovered: " + uuid);

                if (uuid.compareToIgnoreCase(BLEManager.ServiceUUID)!=0){
                    continue;
                }

                findService = true;
                Log.i("BLEManager", "find service");

                new ArrayList<HashMap<String, String>>();
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                boolean findRx = false, findTx = false;
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {

                    final String charUuid = gattCharacteristic.getUuid().toString();
                    Log.i("BLEManager", "Characteristic discovered for service: " + charUuid);

                    if (charUuid.compareToIgnoreCase(BLEManager.RXCharacteristicUUID) == 0){
                        Log.i("BLEManager", "Find Rx");
                        findRx = true;
                        mBluetoothRxCharacter = gattCharacteristic;
                    }else if (charUuid.compareToIgnoreCase(BLEManager.TXCharacteristicUUID)==0){
                        Log.i("BLEManager", "Find Tx");
                        findTx = true;
                        mBluetoothTxCharacter = gattCharacteristic;
                    }

                    if (findRx && findTx){
                        synchronized (mConnectDoneLockObject) {
                            mConnectDoneLockObject.notify();
                        }

                        if (mBleCallback != null){

                            mBluetoothTxCharacter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                            mBluetoothGatt.setCharacteristicNotification(mBluetoothRxCharacter,true);

                            List<BluetoothGattDescriptor> descriptorList = mBluetoothRxCharacter.getDescriptors();
                            if(descriptorList != null && descriptorList.size() > 0) {
                                for(BluetoothGattDescriptor descriptor : descriptorList) {
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    mBluetoothGatt.writeDescriptor(descriptor);

                                    //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                    //mBluetoothGatt.writeDescriptor(descriptor);
                                }
                            }
                        }
                        break;
                    }

                }

                if (!findRx || !findTx){
                    if (mBleCallback != null){
                        mBleCallback.updateConnDevStatus(BlEDEV_FindCharacteristicsFailed);
                    }
                }
            }

            if (!findService){
                if (mBleCallback != null){
                    mBleCallback.updateConnDevStatus(BlEDEV_FindServiceFailed);
                }
            }
        }

        //呼叫mBluetoothGatt.readCharacteristic(characteristic)讀取資料回撥，在這裡面接收資料
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            /*
            mRecvData = characteristic.getValue();

            if (mRecvData != null){
                for (byte b: mRecvData){
                    Log.i("myactivity", String.format("%x", b));
                }
            }


            if (status != BluetoothGatt.GATT_SUCCESS){
                if (mBleCallback!=null){
                    mBleCallback.updateConnDevStatus(BlEDEV_ReadFailed);
                }

                synchronized (mReadDoneLockObject) {
                    mReadDoneLockObject.notify();
                }
                return;
            }

            synchronized (mReadDoneLockObject){
                mReadDoneLockObject.notify();
            }

             */

        }

        //傳送資料後的回撥
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            synchronized (mWriteDoneLockObject) {
                mWriteDoneLockObject.notify();
            }
            if (status != BluetoothGatt.GATT_SUCCESS){
                if (mBleCallback!=null){
                    mBleCallback.updateConnDevStatus(BlEDEV_WriteFailed);
                    Log.i("BLEManager", "Write failed" + status);
                }
                return;
            }

            Log.i("BLEManager", "writ done");
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            final byte[] received = characteristic.getValue();


            if (received != null && received.length > 0){
                /*
                for (byte b: mRecvData){
                    Log.i("BLEManager", String.format("%x", b));
                }
                */

                new Thread(new Runnable() {
                    byte[] recvByte = received;

                    @Override
                    public void run() {
                        synchronized (mReadDoneLockObject){

                        String strData = "";
                        for(int i=0; i<recvByte.length; i++){
                            strData += String.format("%d, ", recvByte[i]);

                            mRecvData.add(Byte.valueOf(recvByte[i]));
                        }

                        Log.i("BLEManager", "received data:" + strData);

                        /*
                        mRecvData = new Byte[recvByte.length];
                        for (int i=0; i<recvByte.length; i++){
                            mRecvData[i] = Byte.valueOf(recvByte[i]);
                        }
                        */

                        Integer[] recvData = new Integer[mRecvData.size()];
                        int i=0;
                        for(Byte btData : mRecvData){
                            recvData[i] = Integer.valueOf(btData);
                            i+=1;
                        }



                            mReadDoneLockObject.notify();
                        }
                    }
                }).start();

            }


        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {//descriptor讀
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {//descriptor寫
            super.onDescriptorWrite(gatt, descriptor, status);

            mBleCallback.updateConnDevStatus(BlEDEV_ConnDone);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        /*
        //呼叫mBluetoothGatt.readRemoteRssi()時的回撥，rssi即訊號強度
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {//讀Rssi
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }


         */
    };

    public void connectWithDevice(String devName, Context context){
        this.stopScan();
        for(int i=0; i<mBleDevArrList.size(); i++){
            BLEDevice devItem = mBleDevArrList.get(i);

            String name = devItem.device.getName();
            if (name.compareTo(devName) == 0){
                this.mBluetoothGatt = devItem.device.connectGatt(context, true, mBluetoothGattCallback);
                break;
            }
        }
    }

    public void disconnectWithDevice(){
        if (this.mBluetoothGatt != null){
            this.mBluetoothGatt.disconnect();
        }
    }

    public synchronized void sendData(byte[] data){
        this.mRecvData.clear();

        if (this.mBluetoothGatt != null && this.mBluetoothTxCharacter!=null){
            Log.i("BLEManager", "send data ");
            String strData = "";
            for(int i=0; i<data.length; i++){
                strData += String.format("%d,", data[i]);
            }
            Log.i("BLEManager", strData);
            this.mBluetoothTxCharacter.setValue(data);
            mBluetoothGatt.writeCharacteristic(this.mBluetoothTxCharacter);
        }
    }


    public synchronized void sendData(String str){
        this.mRecvData.clear();

        if (this.mBluetoothGatt != null && this.mBluetoothTxCharacter!=null){
            this.mBluetoothTxCharacter.setValue(str);
            mBluetoothGatt.writeCharacteristic(this.mBluetoothTxCharacter);
        }
    }

    public boolean connectWithDevice(BluetoothDevice device, long connectTimeout){
        if (mContext!=null){
            synchronized (mConnectDoneLockObject) {
                device.connectGatt(mContext, true, mBluetoothGattCallback);

                try{
                    mConnectDoneLockObject.wait(connectTimeout);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }


        }

        return (mBluetoothTxCharacter!=null && mBluetoothRxCharacter!=null);
    }

    public byte[] sendCmdRecvData(String cmd){
        return sendCmdRecvData(cmd.getBytes());
    }

    public byte[] sendCmdRecvData(byte[] cmd){
        this.mRecvData.clear();

        if (this.mBluetoothGatt != null && this.mBluetoothTxCharacter!=null){

            synchronized (mWriteDoneLockObject){
                this.mBluetoothTxCharacter.setValue(cmd);
                mBluetoothGatt.writeCharacteristic(this.mBluetoothTxCharacter);

                try {
                    mWriteDoneLockObject.wait(2000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            BluetoothGattCharacteristic blechar = mBluetoothGatt.getService(UUID.fromString(BLEManager.ServiceUUID)).getCharacteristic(UUID.fromString(BLEManager.RXCharacteristicUUID));
            //boolean ret = mBluetoothGatt.readCharacteristic(this.mBluetoothRxCharacter);

            synchronized (mReadDoneLockObject){
                try {
                    mReadDoneLockObject.wait(2000);

                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        }


        byte[] recvData = new byte[this.mRecvData.size()];
        for(int i=0; i<this.mRecvData.size(); i++){
            recvData[i] = this.mRecvData.get(i);
        }

        return recvData;
    }
}
