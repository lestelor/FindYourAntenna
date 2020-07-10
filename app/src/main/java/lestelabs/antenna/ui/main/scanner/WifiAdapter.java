package lestelabs.antenna.ui.main.scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
            TextView tvSsid = (TextView) convertView.findViewById(R.id.tvSsid);
            TextView tvLevel = (TextView) convertView.findViewById(R.id.tvLevel);
            // Populate the data into the template view using the data object
            tvSsid.setText(deviceWiFi.getSsid());
            tvLevel.setText(deviceWiFi.getLevel().toString());
            // Return the completed view to render on screen
            return convertView;
        }

}
