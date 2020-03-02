package com.secuxtech.paymentdeviceexample;

/**
 * Created by maochuns.sun@gmail.com on 2020-03-02
 */
public class Setting {

    private static Setting instance = null;


    public static Setting getInstance(){
        if (instance == null){
            instance = new Setting();
        }

        return instance;
    }

    public int mTestCount = 100;

}
