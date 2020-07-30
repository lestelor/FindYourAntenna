
package lestelabs.antenna.ui.main.scanner;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import lestelabs.antenna.R;

public class WifiAdapter extends ArrayAdapter<DeviceWiFi> {


    public WifiAdapter(Context context, ArrayList<DeviceWiFi> wiFis) {
        super(context, 0, wiFis);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        DeviceWiFi deviceWiFi = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_wifi, parent, false);
        }
        // Lookup view for data population
        TextView tvSsid = (TextView) convertView.findViewById(R.id.tv_wifi_ssid);
        TextView tvLevel = (TextView) convertView.findViewById(R.id.tv_wifi_level);
        TextView tvMac = (TextView) convertView.findViewById(R.id.tv_wifi_mac);
        TextView tvSec = (TextView) convertView.findViewById(R.id.tv_wifi_sec);
        TextView tvChannel = (TextView) convertView.findViewById(R.id.tv_wifi_channel);
        TextView tvChannelInt = (TextView) convertView.findViewById(R.id.tv_wifi_channelInt);
        TextView tvFreq = (TextView) convertView.findViewById(R.id.tv_wifi_frequency);
        ImageView ivIconLevel = (ImageView)  convertView.findViewById(R.id.iv_wifi_signalIcon);
        // Populate the data into the template view using the data object
        tvSsid.setText(deviceWiFi.getSsid());
        tvLevel.setText(deviceWiFi.getLevel().toString() + " dBm");
        tvMac.setText("mac: " + deviceWiFi.getMac().toString());
        tvSec.setText(deviceWiFi.getSecurity().toString());
        tvChannel.setText(R.string.channel);
        Integer freq = deviceWiFi.getCenterFreq2();
        tvFreq.setText(freq.toString() + " MHz");

        Integer channel;
        if (freq>5000) {
            channel = ((freq-5180)/5+36);
        } else {
            channel = ((freq-2412)/5+1);
        }

        tvChannelInt.setText(channel.toString());

        if (deviceWiFi.getLevel()>=-67) {
            Log.d("cfauli","GREEN " + deviceWiFi.getLevel() + "dBm");
            ivIconLevel.setImageResource(R.drawable.ic_network_green);
        } else if (deviceWiFi.getLevel()>=-80) {
            Log.d("cfauli","YELLOW " + deviceWiFi.getLevel() + "dBm");
            ivIconLevel.setImageResource(R.drawable.ic_network_yellow);
        } else {
            Log.d("cfauli","RED " + deviceWiFi.getLevel() + "dBm");
            ivIconLevel.setImageResource(R.drawable.ic_network_red);
        }

        // Return the completed view to render on screen
        return convertView;
    }

}