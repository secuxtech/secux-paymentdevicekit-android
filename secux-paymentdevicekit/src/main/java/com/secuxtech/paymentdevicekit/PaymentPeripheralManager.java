package com.secuxtech.paymentdevicekit;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;

import com.example.apple1.mylibrary.PaymentPeripheral;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-26
 */
public class PaymentPeripheralManager {

    public static final Integer SecuX_Peripheral_Operation_OK = 0;
    public static final Integer SecuX_Peripheral_Operation_fail = 1;

    private byte[] ivKeyData;

    public PaymentPeripheralManager() {

    }

    /*
    public Pair<Integer, String> doGetIVKey(Context context, BLEDevice device, int connectionTimeout){
        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");

        SecuXBLEManager.getInstance().mContext = context;
        if (SecuXBLEManager.getInstance().connectWithDevice(device.device, connectionTimeout)){
            Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " Connect with the device done!");
            //com.secux.payment.cpp.MyNDK myNDK = new com.secux.payment.cpp.MyNDK();
            //final byte[] validatePeripheralCommand = myNDK.getValidatePeripheralCommand(connectionTimeout, devInfo.second);
            //Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " getValidatePeripheralCommand");

            byte[] recvData = SecuXBLEManager.getInstance().sendCmdRecvData(device.peripheralCommand);

            if (recvData!=null && recvData.length>0) {
                PaymentPeripheral paymentPeripheral = device.peripheral;
                if (paymentPeripheral.isActivated()) {
                    Log.i(TAG, "device is activated! " + recvData.toString());
                    if (paymentPeripheral.isValidPeripheralIvKey(recvData)) {
                        Log.d(TAG, "true__" + recvData.toString());
                        byte[] ivKeyData = Arrays.copyOfRange(recvData, 5, recvData.length);
                        String ivKey = dataToHexString(ivKeyData);
                        ivKey = ivKey.toUpperCase();
                        Log.d(TAG, ivKey);
                    } else {
                        Log.i(TAG, "Invalid peripheral ivkey");
                        ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid peripheral ivkey");
                    }
                } else {
                    Log.i(TAG, "device is not activated!");
                    ret = new Pair<>(SecuX_Peripheral_Operation_fail, "device is not activated");
                }
            }else{
                Log.i(TAG, String.valueOf(SystemClock.uptimeMillis())  + " receive data failed!");
                ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Receive data timeout");
            }
        }else{
            Log.i(TAG, "connect with the device failed!");
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Connect with device timeout");
        }

        return ret;
    }

     */

    public Pair<Integer, String> doGetIVKeyWithoutStartScan(Context context, long scanTimeout, String connectDeviceId, int checkRSSI, final int connectionTimeout) {
        Log.i(TAG, "doGetIVKeyWithoutStartScan");
        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");

        SecuXBLEManager.getInstance().mContext = context;
        SecuXBLEManager.getInstance().setBLEManager((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));

        Pair<BluetoothDevice, PaymentPeripheral> devInfo = SecuXBLEManager.getInstance().findTheDevice(connectDeviceId, scanTimeout, checkRSSI);
        if (devInfo.first==null || devInfo.second==null){
            //SecuXBLEManager.getInstance().stopScan();
            Log.i(TAG, "find device failed!");
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Can't find device");
            return ret;
        }

        Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " find the device");
        if (SecuXBLEManager.getInstance().connectWithDevice(devInfo.first, connectionTimeout)){
            Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " Connect with the device done!");
            //com.secux.payment.cpp.MyNDK myNDK = new com.secux.payment.cpp.MyNDK();
            //final byte[] validatePeripheralCommand = myNDK.getValidatePeripheralCommand(connectionTimeout, devInfo.second);
            //Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " getValidatePeripheralCommand");

            byte[] recvData = SecuXBLEManager.getInstance().sendCmdRecvData(SecuXBLEManager.getInstance().getValidatePeripheralCommand());

            if (recvData!=null && recvData.length>0) {
                PaymentPeripheral paymentPeripheral = devInfo.second;
                if (paymentPeripheral.isActivated()) {
                    Log.i(TAG, "device is activated! " + recvData.toString());
                    if (paymentPeripheral.isValidPeripheralIvKey(recvData)) {
                        Log.d(TAG, SystemClock.uptimeMillis()  + " recv: " + recvData.toString());
                        byte[] ivKeyData = Arrays.copyOfRange(recvData, 5, recvData.length);
                        String ivKey = dataToHexString(ivKeyData);
                        ivKey = ivKey.toUpperCase();
                        Log.d(TAG, SystemClock.uptimeMillis()  + " " + ivKey);

                        ret = new Pair<>(SecuX_Peripheral_Operation_OK, ivKey);


                    } else {
                        Log.i(TAG, "Invalid peripheral ivkey");
                        ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid peripheral ivkey");
                    }
                } else {
                    Log.i(TAG, "device is not activated!");
                    ret = new Pair<>(SecuX_Peripheral_Operation_fail, "device is not activated");
                }
            }else{
                Log.i(TAG, String.valueOf(SystemClock.uptimeMillis())  + " receive data failed!");
                ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Receive data timeout");
            }

            SecuXBLEManager.getInstance().disconnectWithDevice();
        }else{
            Log.i(TAG, "connect with the device failed!");
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Connect with device timeout");
        }


        return ret;
    }

    public Pair<Integer, String> doGetIVKey(Context context, long scanTimeout, String connectDeviceId, int checkRSSI, final int connectionTimeout) {
        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");

        SecuXBLEManager.getInstance().mContext = context;
        SecuXBLEManager.getInstance().setBLEManager((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));

        Pair<BluetoothDevice, PaymentPeripheral> devInfo = SecuXBLEManager.getInstance().scanForTheDevice(connectDeviceId, scanTimeout, checkRSSI);
        if (devInfo.first==null || devInfo.second==null){
            //SecuXBLEManager.getInstance().stopScan();
            Log.i(TAG, "find device failed!");
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Can't find device");
            return ret;
        }

        Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " find the device");
        if (SecuXBLEManager.getInstance().connectWithDevice(devInfo.first, connectionTimeout)){
            Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " Connect with the device done!");
            //com.secux.payment.cpp.MyNDK myNDK = new com.secux.payment.cpp.MyNDK();
            //final byte[] validatePeripheralCommand = myNDK.getValidatePeripheralCommand(connectionTimeout, devInfo.second);
            //Log.i(TAG, String.valueOf(SystemClock.uptimeMillis()) + " getValidatePeripheralCommand");

            byte[] recvData = SecuXBLEManager.getInstance().sendCmdRecvData(SecuXBLEManager.getInstance().getValidatePeripheralCommand());

            if (recvData!=null && recvData.length>0) {
                PaymentPeripheral paymentPeripheral = devInfo.second;
                if (paymentPeripheral.isActivated()) {
                    Log.i(TAG, "device is activated! " + recvData.toString());
                    if (paymentPeripheral.isValidPeripheralIvKey(recvData)) {
                        Log.d(TAG, "true__" + recvData.toString());
                        ivKeyData = Arrays.copyOfRange(recvData, 5, recvData.length);
                        String ivKey = dataToHexString(ivKeyData);
                        ivKey = ivKey.toUpperCase();
                        Log.d(TAG, ivKey);

                        ret = new Pair<>(SecuX_Peripheral_Operation_OK, ivKey);

                    } else {
                        Log.i(TAG, "Invalid peripheral ivkey");
                        ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid peripheral ivkey");
                        SecuXBLEManager.getInstance().disconnectWithDevice();
                    }
                } else {
                    Log.i(TAG, "device is not activated!");
                    ret = new Pair<>(SecuX_Peripheral_Operation_fail, "device is not activated");
                    SecuXBLEManager.getInstance().disconnectWithDevice();
                }
            }else{
                Log.i(TAG, String.valueOf(SystemClock.uptimeMillis())  + " receive data failed!");
                ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Receive data timeout");
                SecuXBLEManager.getInstance().disconnectWithDevice();
            }
        }else{
            Log.i(TAG, "connect with the device failed!");
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Connect with device timeout");
        }


        return ret;
    }

    public Pair<Integer, String>  doPaymentVerification(byte[] encryptedTransactionData, MachineIoControlParam machineControlParam) {
        Map<String, String> ioControlParams = this.getIoControlParamMap(machineControlParam);
        return this.doPaymentVerification(encryptedTransactionData, ioControlParams);
    }

    public Pair<Integer, String>  doPaymentVerification(byte[] encryptedTransactionData, final Map<String, String> machineControlParams) {

        Map<String, Integer> ioControlParams = new HashMap<String, Integer>();
        ioControlParams.put("uart", 0);
        ioControlParams.put("relay", 0);

        byte[] sendData = getEncryptPaymentData(encryptedTransactionData, ioControlParams);
        if (sendData == null){
            SecuXBLEManager.getInstance().disconnectWithDevice();
            return new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid transaction data");
        }

        Pair<Integer, String> ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Unknown reason");
        byte[] recvData = SecuXBLEManager.getInstance().sendCmdRecvData(sendData);
        if (recvData!=null && recvData.length>0) {
            try {
                String responseString = new String(recvData, "UTF-8");

                if (responseString.charAt(0) == 'E') {
                    ret = new Pair<>(SecuX_Peripheral_Operation_fail, responseString);
                }else {

                    byte[] returnMoneyData = Arrays.copyOfRange(recvData, 0, 8);

                    //NSData *returnMoneyData = [returnData subdataWithRange:NSMakeRange(0, 5)];
                    String returnMoneyString = new String(returnMoneyData, "UTF-8");
                    Log.d("returnMoneyString", returnMoneyString);
                    //NSString *returnMoney = [NSString stringWithUTF8String:[returnMoneyData bytes]];

                    byte[] sequenceNumData = Arrays.copyOfRange(recvData, 8, 9);
                    int sequenceNumInt = (sequenceNumData[0] & 0xFF);
                    //NSData *sequenceNoData = [returnData subdataWithRange:NSMakeRange(5, 1)];
                    //int sequenceNumInt = getTwoByteInteger(sequenceNumData);
                    String sequenceNumString = "" + sequenceNumInt;//new String(sequenceNumData, "UTF-8");
                    //int *b = (int *)sequenceNoData.bytes;
                    //NSString *sequenceNoStr = [NSString stringWithFormat:@"%d",*b];

                    final Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("amount", returnMoneyString);
                    dataMap.put("sequenceNo", sequenceNumString);

                    byte[] machineControlData = getMachineControlData(machineControlParams);
                    int sendStartOffset = 0;
                    do {
                        int sendEndOffset = sendStartOffset + 20;
                        if (sendEndOffset > machineControlData.length) {
                            sendEndOffset = machineControlData.length;
                        }
                        byte[] ioConfigData = Arrays.copyOfRange(machineControlData, sendStartOffset, sendEndOffset);

                        if (ioConfigData[0] == 0x74){
                            SecuXBLEManager.getInstance().sendData(ioConfigData);
                        }else {
                            byte[] reply = SecuXBLEManager.getInstance().sendCmdRecvData(ioConfigData);
                            if (reply != null && reply.length > 0) {
                                responseString = new String(recvData, "UTF-8");

                                if (responseString.charAt(0) == 'E') {
                                    ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid io config response from device");
                                    break;
                                }
                            }
                        }

                        sendStartOffset += 20;
                    }while (sendStartOffset < machineControlData.length);

                    if (sendStartOffset >= machineControlData.length){
                        ret = new Pair<>(SecuX_Peripheral_Operation_OK, "");
                    }
                    /*
                    byte[] reply = SecuXBLEManager.getInstance().sendCmdRecvData(machineControlData);
                    if (reply != null && reply.length > 0) {
                        responseString = new String(recvData, "UTF-8");

                        if (responseString.charAt(0) == 'E') {
                            ret = new Pair<>(SecuX_Peripheral_Operation_fail, responseString);
                        } else {
                            ret = new Pair<>(SecuX_Peripheral_Operation_OK, "");
                        }
                    }

                     */
                }
            }catch (Exception e){
                ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Invalid payment amount response from device");
            }

        }else{
            ret = new Pair<>(SecuX_Peripheral_Operation_fail, "Receive response from device timeout");
        }

        SecuXBLEManager.getInstance().disconnectWithDevice();
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

    private Map<String,String> getIoControlParamMap(MachineIoControlParam machineControlParam){
        Map<String, String> ioControlParams = new HashMap<String, String>();
        if(machineControlParam.getUart()!=null&&machineControlParam.getUart().length()>0){
            ioControlParams.put("uart",machineControlParam.getUart());
        }
        if(machineControlParam.getGpio1()!=null&&machineControlParam.getGpio1().length()>0){
            ioControlParams.put("gpio1",machineControlParam.getGpio1());
        }
        if(machineControlParam.getGpio2()!=null&&machineControlParam.getGpio2().length()>0){
            ioControlParams.put("gpio2",machineControlParam.getGpio2());
        }
        if(machineControlParam.getGpio31()!=null&&machineControlParam.getGpio31().length()>0){
            ioControlParams.put("gpio31",machineControlParam.getGpio31());
        }
        if(machineControlParam.getGpio32()!=null&&machineControlParam.getGpio32().length()>0){
            ioControlParams.put("gpio32",machineControlParam.getGpio32());
        }
        if(machineControlParam.getGpio4()!=null&&machineControlParam.getGpio4().length()>0){
            ioControlParams.put("gpio4",machineControlParam.getGpio4());
        }
        if(machineControlParam.getGpio4c()!=null&&machineControlParam.getGpio4c().length()>0){
            ioControlParams.put("gpio4c",machineControlParam.getGpio4c());
        }
        if(machineControlParam.getGpio4cInterval()!=null&&machineControlParam.getGpio4cInterval().length()>0){
            ioControlParams.put("gpio4cInterval",machineControlParam.getGpio4cInterval());
        }
        if(machineControlParam.getGpio4cCount()!=null&&machineControlParam.getGpio4cCount().length()>0){
            ioControlParams.put("gpio4cCount",machineControlParam.getGpio4cCount());
        }
        if(machineControlParam.getGpio4dOn()!=null&&machineControlParam.getGpio4dOn().length()>0){
            ioControlParams.put("gpio4dOn",machineControlParam.getGpio4dOn());
        }
        if(machineControlParam.getGpio4dOff()!=null&&machineControlParam.getGpio4dOff().length()>0){
            ioControlParams.put("gpio4dOff",machineControlParam.getGpio4dOff());
        }
        if(machineControlParam.getGpio4dInterval()!=null&&machineControlParam.getGpio4dInterval().length()>0){
            ioControlParams.put("gpio4dInterval",machineControlParam.getGpio4dInterval());
        }
        if(machineControlParam.getRunStatus()!=null&&machineControlParam.getRunStatus().length()>0){
            ioControlParams.put("runStatus",machineControlParam.getRunStatus());
        }
        if(machineControlParam.getLockStatus()!=null&&machineControlParam.getLockStatus().length()>0){
            ioControlParams.put("lockStatus",machineControlParam.getLockStatus());
        }
        return ioControlParams;
    }

    private byte[] getEncryptPaymentData(byte[] encrytedTransactionData, Map<String, Integer> ioControlParams) {


        // 組合 byte array
        byte[] data = new byte[80];

        try {
            //- Pack 1 - 0~19
            data[0] = (byte) 0x81;
            data[1] = (byte) 0x10;
            System.arraycopy(encrytedTransactionData, 0, data, 2, 18);
//            data[18] = (byte) 0x00;
//            data[19] = (byte) 0x00;

            //- Pack 2 - 20~39
            data[20] = (byte) 0x82;
            data[21] = (byte) 0x10;
            System.arraycopy(encrytedTransactionData, 18, data, 22, 18);
//            data[38] = (byte) 0x00;
//            data[39] = (byte) 0x00;

            //- Pack 3 - 40~59
            data[40] = (byte) 0x83;
            data[41] = (byte) 0x10;
            System.arraycopy(encrytedTransactionData, 36, data, 42, 18);

            //- Pack 4 - 60~71
            data[60] = (byte) 0x84;
            data[61] = (byte) 0x00;
            System.arraycopy(encrytedTransactionData, 54, data, 62, 10);

//            int uart = ioControlParams.get("uart");
//            data[60] = (byte) uart;
//            int relay = ioControlParams.get("relay");
//            data[61] = (byte) relay;

            //EOF------------------------------------
//            data[62] = (byte) 0x84;

            byte byteZero = (byte) 0x00;
            //byte[] eofData = {(byte)0x64,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero,zero};
            //int eofIndex = 0;
            for (int i = 72; i < 80; i++) {
                data[i] = byteZero;
            }

        } catch (Exception ex) {
            Log.d("CryptCommand", ex.getMessage());
            data = null;
        }

        return data;
    }

    private byte[] getMachineControlData(Map<String, String> machineControlParams) {
        //PaymentUtil.debug("Start");

        Log.d(TAG,"BuildConfig.DEBUG=["+BuildConfig.DEBUG+"] machineControlParams.size=["+machineControlParams.size()+"]");

        if (BuildConfig.DEBUG) {
            for (String string : machineControlParams.keySet()) {
                //PaymentUtil.debug("getMachineControlData key=[" + string + "] value=[" + machineControlParams.get(string) + "]");
            }
        }

        final int BIT0 = 1;
        final int BIT1 = 2;
        final int BIT2 = 4;
        final int BIT3 = 8;
        final int BIT4 = 16;
        final int BIT5 = 32;
        final int BIT6 = 64;
        final int BIT7 = 128;

        String uartStr = machineControlParams.get("uart");
        String gpio1Str = machineControlParams.get("gpio1");
        String gpio2Str = machineControlParams.get("gpio2");
        String gpio31Str = machineControlParams.get("gpio31");
        String gpio32Str = machineControlParams.get("gpio32");
        String gpio4Str = machineControlParams.get("gpio4");

        //檢查是不是有設定 4d 的值
        String gpio4dOn = machineControlParams.get("gpio4dOn");
        if((gpio4dOn!=null&&gpio4dOn.length()>0&&!"0".equals(gpio4dOn))){
            return getMachineControlData4c4d(machineControlParams);
        }

        String runStatusStr = SecuXPaymentUtility.getDefaultValue(machineControlParams.get("runStatus"),"0");
        String lockStatusStr = SecuXPaymentUtility.getDefaultValue(machineControlParams.get("lockStatus"),"0");

        int b1, b2, b3, b4;
        int fun = 0x00;
        int funInt;

        // random numbers 1~255
        int r1Int = (int) ((Math.random() * 255) + 1);
        String r1Str = "" + r1Int;
        int r2Int = (int) ((Math.random() * 255) + 1);
        String r2Str = "" + r2Int;

        //PaymentUtil.debug("r1Str=["+r1Str+"] r2Str=["+r2Str+"]");

        // 組合 byte array
        byte[] data = new byte[80];
        //------------------------------------
        // 0, 1
        data[0] = (byte) 0x71;
        data[1] = (byte) 0x00;

        fun = fun + BIT0;

        // gpio-1
        int gpio1Int = SecuXPaymentUtility.str2Int(gpio1Str);
        if (gpio1Int != 0) {
            fun = fun + BIT1;
        }

        b1 = gpio1Int & 0xFF;
        b2 = (gpio1Int >> 8) & 0xFF;
        b3 = (gpio1Int >> 16) & 0xFF;

        // 2, 3, 4
        data[2] = (byte) b1;
        data[3] = (byte) b2;
        data[4] = (byte) b3;

        // gpio-2
        int gpio2Int = SecuXPaymentUtility.str2Int(gpio2Str);
        if (gpio2Int != 0) {
            fun = fun + BIT2;
        }

        b1 = gpio2Int & 0xFF;
        b2 = (gpio2Int >> 8) & 0xFF;
        b3 = (gpio2Int >> 16) & 0xFF;

        // 5, 6, 7
        data[5] = (byte) b1;
        data[6] = (byte) b2;
        data[7] = (byte) b3;

        // gpio-31
        int gpio31Int = SecuXPaymentUtility.str2Int(gpio31Str);
        if (gpio31Int != 0) {
            fun = fun + BIT3;
        }

        b1 = gpio31Int & 0xFF;
        b2 = (gpio31Int >> 8) & 0xFF;

        // 8, 9
        data[8] = (byte) b1;
        data[9] = (byte) b2;

        // gpio-32
        int gpio32Int = SecuXPaymentUtility.str2Int(gpio32Str);
        b1 = gpio32Int & 0xFF;
        b2 = (gpio32Int >> 8) & 0xFF;

        // 10, 11
        data[10] = (byte) b1;
        data[11] = (byte) b2;

        // gpio-4
        int gpio4Int = SecuXPaymentUtility.str2Int(gpio4Str);
        if (gpio4Int != 0) {
            fun = fun + BIT4;
        }

        b1 = gpio4Int & 0xFF;
        b2 = (gpio4Int >> 8) & 0xFF;
        b3 = (gpio4Int >> 16) & 0xFF;

        // 12, 13, 14
        data[12] = (byte) b1;
        data[13] = (byte) b2;
        data[14] = (byte) b3;

        // uart
        int uartInt = SecuXPaymentUtility.str2Int(uartStr);
        if (uartInt != 0) {
            fun = fun + BIT5;
        }

        b1 = uartInt & 0xFF;
        b2 = (uartInt >> 8) & 0xFF;
        b3 = (uartInt >> 16) & 0xFF;
        b4 = (uartInt >> 24) & 0xFF;

        // 15, 16, 17, 18, 19
        data[15] = (byte) b1;
        data[16] = (byte) b2;
        data[17] = (byte) b3;
        data[18] = (byte) b4;
        data[19] = (byte) fun;


        // 20, 21
        data[20] = (byte) 0x72;
        data[21] = (byte) 0x00;

        // ivKeyData, length=8, 22~29
        System.arraycopy(ivKeyData, 0, data, 22, ivKeyData.length);
        Log.d("ivkey_length", ":" + ivKeyData.length);

        final String RUN_IO = "0";
        final String STOP_IO = "1";
        final String REVERSE_IO = "2";

        final String LOCK_IO = "1";
        final String UNLOCK_IO = "0";

        int run;
//        int runInt;

        if (runStatusStr.equals(RUN_IO)) {
            run = fun ^ 0xFF;
        } else if (runStatusStr.equals(STOP_IO)) {
            run = fun;
        } else if (runStatusStr.equals(REVERSE_IO)) {
            run = fun + 1;
        } else {
            return null;
        }

        // 30
        data[30] = (byte) run;

        int r1 = SecuXPaymentUtility.str2Int(r1Str);
        int r2 = SecuXPaymentUtility.str2Int(r2Str);

        // 31, 32
        data[31] = (byte) r1;
        data[32] = (byte) r2;

        // 33
        if (lockStatusStr.equals(UNLOCK_IO)) {  // unlock iv 0+2+3+5
            int i = ivKeyData[0] + ivKeyData[2] + ivKeyData[3] + ivKeyData[5];
            data[33] = (byte) i;
        } else {                                // lock iv 0+1+6+7
            int i = ivKeyData[0] + ivKeyData[1] + ivKeyData[6] + ivKeyData[7];
            data[33] = (byte) i;
        }

        // gpio4c  34~39 是 4c 的資料
        String gpio4cStr = machineControlParams.get("gpio4c");
        String gpio4cIntervalStr = machineControlParams.get("gpio4cInterval");
        String gpio4cCountStr = machineControlParams.get("gpio4cCount");
        if(gpio4cStr!=null&&gpio4cStr.length()>0) { // gpio4cStr has value
            int i = ivKeyData[0]+ivKeyData[4]+ivKeyData[5]+ivKeyData[7];
            data[34] = (byte)i;
            // count
            data[35]= (byte)SecuXPaymentUtility.str2Int(gpio4cCountStr);
            // Paulse time
            data[36] = (byte)(((SecuXPaymentUtility.str2Int(gpio4cStr))&0xFF));
            data[37] = (byte)((SecuXPaymentUtility.str2Int(gpio4cStr)>>8)&0xFF);
            // Interval time
            data[38] = (byte)((SecuXPaymentUtility.str2Int(gpio4cIntervalStr))&0xFF);
            data[39] = (byte)((SecuXPaymentUtility.str2Int(gpio4cIntervalStr)>>8)&0xFF);
        }
        else {
            data[34]=0x00;
            data[35]=0x00;
            data[36]=0x00;
            data[37]=0x00;
            data[38]=0x00;
            data[39]=0x00;
        }

        //捕上結尾 EOF
        data[40]=0x73;
        data[41]=0x00;
        data[42]=0x00;
        data[43]=0x00;
        data[44]=0x00;
        data[45]=0x00;
        data[46]=0x00;
        data[47]=0x00;
        data[48]=0x00;
        data[49]=0x00;
        data[50]=0x00;
        data[51]=0x00;
        data[52]=0x00;
        data[53]=0x00;
        data[54]=0x00;
        data[55]=0x00;
        data[56]=0x00;
        data[57]=0x00;
        data[58]=0x00;
        data[59]=0x00;

        data[60]=0x74;
        data[61]=0x00;
        data[62]=0x00;
        data[63]=0x00;
        data[64]=0x00;
        data[65]=0x00;
        data[66]=0x00;
        data[67]=0x00;
        data[68]=0x00;
        data[69]=0x00;
        data[70]=0x00;
        data[71]=0x00;
        data[72]=0x00;
        data[73]=0x00;
        data[74]=0x00;
        data[75]=0x00;
        data[76]=0x00;
        data[77]=0x00;
        data[78]=0x00;
        data[79]=0x00;

        //PaymentUtil.debug("End");

        return data;
    }

    private byte[] getMachineControlData4c4d(Map<String, String> machineControlParams) {
        //PaymentUtil.debug("getMachineControlData4c4d Start");
        final int BIT0 = 1;
        final int BIT1 = 2;
        final int BIT2 = 4;
        final int BIT3 = 8;
        final int BIT4 = 16;
        final int BIT5 = 32;
        final int BIT6 = 64;
        final int BIT7 = 128;

        String gpio4cStr = machineControlParams.get("gpio4c");
        String gpio4cIntervalStr = machineControlParams.get("gpio4cInterval");
        String gpio4cCountStr = machineControlParams.get("gpio4cCount");
        String gpio4dOnStr = machineControlParams.get("gpio4dOn");
        String gpio4dOffStr = machineControlParams.get("gpio4dOff");
        String gpio4dIntervalStr = machineControlParams.get("gpio4dInterval");

        int b1, b2, b3, b4;

        // 組合 byte array
        byte[] data = new byte[34];
        //------------------------------------
        // 0, 1
        data[0] = (byte) 0x73;
        data[1] = (byte) 0x00;

        // gpio4c
        if(gpio4cStr==null||gpio4cStr.length()==0){
            gpio4cStr="0";
            gpio4cIntervalStr="0";
            gpio4cCountStr="0";
        }

        int gpio4cInt = SecuXPaymentUtility.str2Int(gpio4cStr);
        int i=0;
        if (gpio4cInt == 0) {
            i = ivKeyData[0]+ivKeyData[4]+ivKeyData[5]+ivKeyData[7];
            i++;
        }else{
            i = ivKeyData[0]+ivKeyData[4]+ivKeyData[5]+ivKeyData[7];
        }
        data[2]=(byte)i;

        // count
        data[3]=(byte)SecuXPaymentUtility.str2Int(gpio4cCountStr);

        // Paulse time
        b1 = (SecuXPaymentUtility.str2Int(gpio4cStr))&0xFF;
        b2 = (SecuXPaymentUtility.str2Int(gpio4cStr)>>8)&0xFF;
        data[4]=(byte)b1;
        data[5]=(byte)b2;
        // Interval time
        b1 = (SecuXPaymentUtility.str2Int(gpio4cIntervalStr))&0xFF;
        b2 = (SecuXPaymentUtility.str2Int(gpio4cIntervalStr)>>8)&0xFF;
        data[6]=(byte)b1;
        data[7]=(byte)b2;

        // gpio4d
        if(gpio4dOnStr == null||gpio4dOnStr.length()==0) {
            gpio4dOnStr = "0";
            gpio4dOffStr = "0";
            gpio4dIntervalStr = "0";
        }
        int gpio4dOnInt = SecuXPaymentUtility.str2Int(gpio4dOnStr);
        if(gpio4dOnInt==0) { // don't execute, so change ivkey add value to other value
            i = ivKeyData[2]+ivKeyData[4]+ivKeyData[6]+ivKeyData[7];
            i++;
        }
        else {
            i = ivKeyData[2]+ivKeyData[4]+ivKeyData[6]+ivKeyData[7];
        }
        data[8]=(byte)i;
        // gpio4d On time
        data[9]=(byte) SecuXPaymentUtility.str2Int(gpio4dOnStr);
        // gpio4d Off time
        data[10]=(byte) SecuXPaymentUtility.str2Int(gpio4dOffStr);
        // Interval time
        b1 = (SecuXPaymentUtility.str2Int(gpio4dIntervalStr))&0xFF;
        b2 = (SecuXPaymentUtility.str2Int(gpio4dIntervalStr)>>8)&0xFF;
        data[11]=(byte)b1;
        data[12]=(byte)b2;

        //PaymentUtil.debug("End");

        return data;
    }
}
