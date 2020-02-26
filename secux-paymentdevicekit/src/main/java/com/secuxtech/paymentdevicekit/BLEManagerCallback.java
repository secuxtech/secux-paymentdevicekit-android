package com.secuxtech.paymentdevicekit;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/*
enum ConnDevStatus : Int{
    case BLEOff = 0
    case ConnFailed
    case FindServiceFailed
    case FindCharacteristicsFailed
    case ConnDone
    case UnsupportedDev
    case WriteFailed
    case WriteDone
    case ReadFailed
    case Disconnected
}
 */
public abstract class BLEManagerCallback {

    public static final int BlEDEV_Off = 0;
    public static final int BlEDEV_ConnFailed = 1;
    public static final int BlEDEV_FindServiceFailed = 2;
    public static final int BlEDEV_FindCharacteristicsFailed = 3;
    public static final int BlEDEV_ConnDone = 4;
    public static final int BlEDEV_UnsupportedDev = 5;
    public static final int BlEDEV_WriteFailed = 6;
    public static final int BlEDEV_ReadFailed = 7;
    public static final int BlEDEV_Disconnected = 8;

    @IntDef({BlEDEV_Off, BlEDEV_ConnFailed,BlEDEV_FindServiceFailed,BlEDEV_FindCharacteristicsFailed,
            BlEDEV_ConnDone,BlEDEV_UnsupportedDev,BlEDEV_WriteFailed,BlEDEV_ReadFailed,BlEDEV_Disconnected})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnDevStatus {}

    public void newBLEDevice(){

    }

    public void updateBLEDeviceRssi(int devIdx){

    }

    public void updateConnDevStatus(@ConnDevStatus int status){

    }
}
