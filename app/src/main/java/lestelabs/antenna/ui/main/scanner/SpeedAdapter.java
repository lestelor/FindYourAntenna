
package lestelabs.antenna.ui.main.scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import lestelabs.antenna.R;

public class SpeedAdapter extends ArrayAdapter<SpeedTest> {


    public SpeedAdapter(Context context, ArrayList<SpeedTest> tests) {
        super(context, 0, tests);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        SpeedTest speedTest = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_speed, parent, false);
        }
        // Lookup view for data population
        TextView tvNetwork = (TextView) convertView.findViewById(R.id.tvF1Network);
        TextView tvSpeed = (TextView) convertView.findViewById(R.id.tvF1Speed);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvF1Date);

        // Populate the data into the template view using the data object
        tvNetwork.setText(speedTest.getNetwork());
        tvSpeed.setText(speedTest.getSpeed());
        tvDate.setText(speedTest.getDate());
        // Return the completed view to render on screen
        return convertView;
    }

}