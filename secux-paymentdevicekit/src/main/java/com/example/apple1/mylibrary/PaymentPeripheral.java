package com.example.apple1.mylibrary;

import java.util.Arrays;

/**
 * Created by apple1 on 2017/6/29.
 */

public class PaymentPeripheral {

    private final int MAX_ADV_LEN = 21;
    private final int TIMESTAMP_CNT_LEN = 1;
    private final int MAC_ADDR_LEN = 4;
    private final int DEVICE_HWID_LEN = 6;
    private final int CODING_KEY_LEN = 4;
    private final int WORK_MODE_LEN = 2;
    private final int WORK_VALUE_LEN = 2;
    private final int IPHONE_TO_BLE_LEN = 20;
    private final int RESPONSE_CODE_LEN = 4;

    private byte[] timestamp_cnt_l = new byte[TIMESTAMP_CNT_LEN];
    private byte[] timestamp_cnt_h = new byte[TIMESTAMP_CNT_LEN];
    private byte[] timestamp_cnt = new byte[TIMESTAMP_CNT_LEN+TIMESTAMP_CNT_LEN];
    private byte[] mac_addr = new byte[MAC_ADDR_LEN];
    private byte[] device_hwid = new byte[DEVICE_HWID_LEN];
    private String uniqueId;
    private byte[] coding_key = new byte[CODING_KEY_LEN];
    private byte[] work_mode = new byte[WORK_MODE_LEN];
    private byte[] work_value = new byte[WORK_VALUE_LEN];
    private String firmware_version = new String("0.0");

    private byte[]  cipher_code_a =  new byte[IPHONE_TO_BLE_LEN];
    private byte[]  cipher_code_q1 =  new byte[IPHONE_TO_BLE_LEN];
    private byte[]  p_data =  new byte[RESPONSE_CODE_LEN];   // data to be checked
    int timer;

    // 2018/05/15 新增這些欄位
    private boolean isActivated = false;
    private boolean isGpioInputOneOn = false;
    private boolean isGpioInputTwoOn = false;
    private boolean isGpioOneRunning = false;
    private boolean isGpioTwoRunning = false;
    private boolean isGpioThreeRunning = false;
    private boolean isGpioOneLocking = false;
    private boolean isGpioTwoLocking = false;
    private byte partnerId = work_mode[1];



    public PaymentPeripheral(byte[] param_timestamp_cnt_l,byte[] param_timestamp_cnt_h, byte[] param_timestamp_cnt,byte[] param_mac_addr,
                             byte[] param_device_hwid, byte[] param_coding_key, byte[] param_work_mode,byte[] param_work_value,int paramTimer,String param_firmware_version,String param_uniqueId)
    {
        this.timestamp_cnt_l = param_timestamp_cnt_l;
        this.timestamp_cnt_h = param_timestamp_cnt_h;
        this.timestamp_cnt = param_timestamp_cnt;
        this.mac_addr = param_mac_addr;
        this.device_hwid = param_device_hwid;
        this.coding_key = param_coding_key;
        this.work_mode = param_work_mode;
        this.work_value = param_work_value;
        this.timer = paramTimer;
        this.firmware_version = param_firmware_version;
        this.uniqueId = param_uniqueId;
        // set up all flags
        isActivated = ((work_mode[0]&0x01)==0x01);
        isGpioInputOneOn = ((work_mode[0]&0x02)==0x02);        ;
        isGpioInputTwoOn = ((work_mode[0]&0x04)==0x04);
        isGpioOneRunning = ((work_mode[0]&0x08)==0x08);
        isGpioTwoRunning = ((work_mode[0]&0x10)==0x10);
        isGpioThreeRunning = ((work_mode[0]&0x20)==0x20);
        isGpioOneLocking = ((work_mode[0]&0x40)==0x40);
        isGpioTwoLocking = ((work_mode[0]&0x80)==0x80);
        partnerId = work_mode[1];
    }

    public void setCipherValue(byte[] param_cipher_code_a,byte[] param_cipher_code_q1, byte[] param_p_data)
    {

        this.cipher_code_a = param_cipher_code_a;
        this.cipher_code_q1 = param_cipher_code_q1;
        this.p_data = param_p_data;

    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isValidPeripheralIvKey(byte[] data){

        for(int i=0; i < RESPONSE_CODE_LEN; i++) {
            if(p_data[i] != data[i+1]) {
                return false;
            }
        }
        return true;
    }

    public boolean isActivated() {
        return isActivated;
    }

    public boolean isGpioInputOneOn() {
        return isGpioInputOneOn;
    }

    public boolean isGpioInputTwoOn() {
        return isGpioInputTwoOn;
    }

    public boolean isGpioOneRunning() {
        return isGpioOneRunning;
    }

    public boolean isGpioTwoRunning() {
        return isGpioTwoRunning;
    }

    public boolean isGpioThreeRunning() {
        return isGpioThreeRunning;
    }

    public boolean isGpioOneLocking() {
        return isGpioOneLocking;
    }

    public boolean isGpioTwoLocking() {
        return isGpioTwoLocking;
    }

    public byte getPartnerId() {
        return partnerId;
    }

    public String getFirmwareVersion() {
        return firmware_version;
    }

    public String toString() {
        return "PaymentPeripheral{" +
                "timestamp_cnt_l=" + Arrays.toString(timestamp_cnt_l) +
                ", timestamp_cnt_h=" + Arrays.toString(timestamp_cnt_h) +
                ", timestamp_cnt=" + Arrays.toString(timestamp_cnt) +
                ", mac_addr=" + Arrays.toString(mac_addr) +
                ", device_hwid=" + Arrays.toString(device_hwid) +
                ", uniqueId='" + uniqueId + '\'' +
                ", coding_key=" + Arrays.toString(coding_key) +
                ", work_mode=" + Arrays.toString(work_mode) +
                ", work_value=" + Arrays.toString(work_value) +
                ", firmware_version='" + firmware_version + '\'' +
                ", cipher_code_a=" + Arrays.toString(cipher_code_a) +
                ", cipher_code_q1=" + Arrays.toString(cipher_code_q1) +
                ", p_data=" + Arrays.toString(p_data) +
                ", timer=" + timer +
                ", isActivated=" + isActivated +
                ", isGpioInputOneOn=" + isGpioInputOneOn +
                ", isGpioInputTwoOn=" + isGpioInputTwoOn +
                ", isGpioOneRunning=" + isGpioOneRunning +
                ", isGpioTwoRunning=" + isGpioTwoRunning +
                ", isGpioThreeRunning=" + isGpioThreeRunning +
                ", isGpioOneLocking=" + isGpioOneLocking +
                ", isGpioTwoLocking=" + isGpioTwoLocking +
                ", partnerId=" + partnerId +
                '}';
    }
}
