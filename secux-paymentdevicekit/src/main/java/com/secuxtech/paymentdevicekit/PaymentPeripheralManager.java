package com.secuxtech.paymentdevicekit;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.example.apple1.mylibrary.PaymentPeripheral;

import java.util.Arrays;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-26
 */
public class PaymentPeripheralManager {

    public static final Integer SecuX_Peripheral_Operation_OK = 0;
    public static final Integer SecuX_Peripheral_Operation_fail = 1;

    public PaymentPeripheralManager() {

    }

    public Pair<Integer, String> doGetIVKey(Context context, long scanTimeout, String connectDeviceId, int checkRSSI, final int connectionTimeout) {
        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");

        SecuXBLEManager.getInstance().mDeviceID = connectDeviceId;
        SecuXBLEManager.getInstance().mScanTimeout = scanTimeout;
        SecuXBLEManager.getInstance().mConnTimeout = connectionTimeout;
        SecuXBLEManager.getInstance().mContext = context;

        Pair<BluetoothDevice, PaymentPeripheral> devInfo = SecuXBLEManager.getInstance().scanForTheDevice(connectDeviceId, scanTimeout);
        if (devInfo.first==null || devInfo.second==null){

            Log.i("", "find device failed!");
        }

        if (SecuXBLEManager.getInstance().connectWithDevice(devInfo.first, connectionTimeout)){
            com.secux.payment.cpp.MyNDK myNDK = new com.secux.payment.cpp.MyNDK();
            final byte[] validatePeripheralCommand = myNDK.getValidatePeripheralCommand(connectionTimeout, devInfo.second);

            byte[] recvData = SecuXBLEManager.getInstance().sendCmdRecvData(validatePeripheralCommand);

            PaymentPeripheral paymentPeripheral = devInfo.second;
            if(paymentPeripheral.isActivated()) {

                if (paymentPeripheral.isValidPeripheralIvKey(recvData)) {
                    Log.d("", "true__" + recvData.toString());
                    byte[] ivKeyData = Arrays.copyOfRange(recvData, 5, recvData.length);
                    String ivKey = dataToHexString(ivKeyData);
                    ivKey = ivKey.toUpperCase();
                    Log.d("", ivKey);
                }
            }
        }


        return ret;
    }

    public Pair<Integer, String> doPaymentVerification(byte[] encryptedTransactionData, MachineIoControlParam machineControlParam) {
        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");


        return ret;
    }

    private static String dataToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        String result = stringBuilder.toString();
        return result;
    }
}
