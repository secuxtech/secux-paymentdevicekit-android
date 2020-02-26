package com.secux.payment.cpp;


import com.example.apple1.mylibrary.PaymentPeripheral;

/**
 * Created by apple1 on 2017/6/21.
 */


public class MyNDK {
    private final int MAX_ADV_LEN = 21;
    private final int TIMESTAMP_CNT_LEN = 1;
    private final int MAC_ADDR_LEN = 4;
    private final int DEVICE_HWID_LEN = 6;
    private final int CODING_KEY_LEN = 4;
    private final int WORK_MODE_LEN = 2;
    private final int WORK_VALUE_LEN = 2;
    private final int IV_KEY_LEN = 8;


    private byte[] timestamp_cnt_l = new byte[TIMESTAMP_CNT_LEN];
    private byte[] timestamp_cnt_h = new byte[TIMESTAMP_CNT_LEN];
    private byte[] timestamp_cnt = new byte[TIMESTAMP_CNT_LEN+TIMESTAMP_CNT_LEN];
    private byte[] mac_addr = new byte[MAC_ADDR_LEN];
    private byte[] device_hwid = new byte[DEVICE_HWID_LEN];
    private byte[] coding_key = new byte[CODING_KEY_LEN];
    private byte[] work_mode = new byte[WORK_MODE_LEN];
    private byte[] work_value = new byte[WORK_VALUE_LEN];
    private int timer;

    static {
        System.loadLibrary("MyLibrary");
    }

    public native PaymentPeripheral createPaymentPeripheralObjectFromNative(byte[] data);
    public native byte[] getValidatePeripheralCommand(int timer, PaymentPeripheral peripheralObject);

}
