package net.kirno.videodemo;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * 列表适配器
 * Created by kirno on 2016/2/2.
 */
public class MListAdapter extends BaseAdapter{

    private final List<ScanResult> mSeanResultList;
    private final Context context;

    public MListAdapter(Context context, List<ScanResult> sceanResultList){
        mSeanResultList = sceanResultList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return mSeanResultList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSeanResultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.wifi_list_item, null);
            holder.address = (TextView) convertView.findViewById(R.id.item_ssid);
            holder.ssid = (TextView) convertView.findViewById(R.id.item_address);
            holder.other = (TextView) convertView.findViewById(R.id.item_other);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        ScanResult sr = mSeanResultList.get(position);
        holder.address.setText(sr.SSID);
        holder.ssid.setText(sr.BSSID);
        holder.other.setText(sr.capabilities);
        return convertView;
    }

    private class Holder {
        public TextView ssid;
        public TextView address;
        public TextView other;
    }
}
