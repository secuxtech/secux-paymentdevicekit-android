package com.secuxtech.paymentdeviceexample;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.secuxtech.paymentdevicekit.BLEDevice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-27
 */

class PaymentDevice{
    BLEDevice paymentDev;
    Boolean deviceSelected;
}


public class PaymentDevListAdapter extends RecyclerView.Adapter<PaymentDevListAdapter.ViewHolder>{

    private Context mContext;
    private List<PaymentDevice> mDevList = new ArrayList<>();

    public PaymentDevListAdapter(Context context, ArrayList<BLEDevice> devList) {
        this.mContext = context;

        for (BLEDevice dev:devList) {
            PaymentDevice pdev = new PaymentDevice();
            pdev.paymentDev = dev;
            pdev.deviceSelected = false;

            mDevList.add(pdev);
        }
    }

    @Override
    public PaymentDevListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater ll = LayoutInflater.from(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.cardview_bledevice, parent, false);

        return new PaymentDevListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final PaymentDevListAdapter.ViewHolder holder, int position) {
        final PaymentDevice devItem = mDevList.get(position);

        holder.textviewDevName.setText(devItem.paymentDev.deviceID);
        holder.textviewDevRssi.setText(String.valueOf(devItem.paymentDev.Rssi));


    }


    @Override
    public int getItemCount() {
        return mDevList.size();
    }

    public void addNewDevice(BLEDevice dev){
        PaymentDevice pdev = new PaymentDevice();
        pdev.paymentDev = dev;
        pdev.deviceSelected = false;

        mDevList.add(pdev);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textviewDevName, textviewDevRssi;
        ViewHolder(View itemView) {
            super(itemView);

            textviewDevName = itemView.findViewById(R.id.textView_device_name);
            textviewDevRssi = itemView.findViewById(R.id.textView_device_rssi);

            final CardView cardView = itemView.findViewById(R.id.cardView_device_info);

            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    PaymentDevice dev = mDevList.get(getAdapterPosition());
                    dev.deviceSelected = !dev.deviceSelected;
                    if (dev.deviceSelected)
                        cardView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDevSelected));
                    else
                        cardView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorDevUnselected));
                }
            });



        }

    }
}
