package com.secuxtech.paymentdevicekit;

import java.io.Serializable;

/**
 * Created by Brian on 2018/5/14.
 */

public class MachineIoControlParam implements Serializable{

    /**
     * uart 的數值
     */
    private String uart;

    /**
     * gpio1 的參數值
     */
    private String gpio1;

    /**
     * gpio2 的參數值
     */
    private String gpio2;

    /**
     * gpio31 的參數值
     */
    private String gpio31;

    /**
     * gpio32 的參數值
     */
    private String gpio32;

    /**
     * gpio4 的參數值
     */
    private String gpio4;

    /**
     * gpio4c
     */
    private String gpio4c;

    /**
     *gpio4cInterval
     */
    private String gpio4cInterval;

    /**
     * gpio4cCount
     */
    private String gpio4cCount;

    /**
     * gpio4cCount
     */
    private String gpio4dOn;

    /**
     * gpio4cCount
     */
    private String gpio4dOff;

    /**
     * gpio4cCount
     */
    private String gpio4dInterval;

    /**
     * 執行設定?
     * 0: Run
     * ?1: Pause
     * ?2: Cancel
     */
    private String runStatus;

    /**
     * 邏輯訊號?
     * 0: Low
     * ?1: High
     */
    private String lockStatus;


    public String getUart() {
        return uart;
    }

    public void setUart(String uart) {
        this.uart = uart;
    }

    public String getGpio1() {
        return gpio1;
    }

    public void setGpio1(String gpio1) {
        this.gpio1 = gpio1;
    }

    public String getGpio2() {
        return gpio2;
    }

    public void setGpio2(String gpio2) {
        this.gpio2 = gpio2;
    }

    public String getGpio31() {
        return gpio31;
    }

    public void setGpio31(String gpio31) {
        this.gpio31 = gpio31;
    }

    public String getGpio32() {
        return gpio32;
    }

    public void setGpio32(String gpio32) {
        this.gpio32 = gpio32;
    }

    public String getGpio4() {
        return gpio4;
    }

    public void setGpio4(String gpio4) {
        this.gpio4 = gpio4;
    }

    public String getGpio4c() {
        return gpio4c;
    }

    public void setGpio4c(String gpio4c) {
        this.gpio4c = gpio4c;
    }

    public String getGpio4cInterval() {
        return gpio4cInterval;
    }

    public void setGpio4cInterval(String gpio4cInterval) {
        this.gpio4cInterval = gpio4cInterval;
    }

    public String getGpio4cCount() {
        return gpio4cCount;
    }

    public void setGpio4cCount(String gpio4cCount) {
        this.gpio4cCount = gpio4cCount;
    }

    public String getGpio4dOn() {
        return gpio4dOn;
    }

    public void setGpio4dOn(String gpio4dOn) {
        this.gpio4dOn = gpio4dOn;
    }

    public String getGpio4dOff() {
        return gpio4dOff;
    }

    public void setGpio4dOff(String gpio4dOff) {
        this.gpio4dOff = gpio4dOff;
    }

    public String getGpio4dInterval() {
        return gpio4dInterval;
    }

    public void setGpio4dInterval(String gpio4dInterval) {
        this.gpio4dInterval = gpio4dInterval;
    }

    public String getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(String runStatus) {
        this.runStatus = runStatus;
    }

    public String getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(String lockStatus) {
        this.lockStatus = lockStatus;
    }

    @Override
    public String toString() {
        return "MachineIoControlParam{" +
                "uart='" + uart + '\'' +
                ", gpio1='" + gpio1 + '\'' +
                ", gpio2='" + gpio2 + '\'' +
                ", gpio31='" + gpio31 + '\'' +
                ", gpio32='" + gpio32 + '\'' +
                ", gpio4='" + gpio4 + '\'' +
                ", gpio4c='" + gpio4c + '\'' +
                ", gpio4cInterval='" + gpio4cInterval + '\'' +
                ", gpio4cCount='" + gpio4cCount + '\'' +
                ", gpio4dOn='" + gpio4dOn + '\'' +
                ", gpio4dOff='" + gpio4dOff + '\'' +
                ", gpio4dInterval='" + gpio4dInterval + '\'' +
                ", runStatus='" + runStatus + '\'' +
                ", lockStatus='" + lockStatus + '\'' +
                '}';
    }
}
