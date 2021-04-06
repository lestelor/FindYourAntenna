
package lestelabs.antenna.ui.main.scanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lestelabs.antenna.R;

public class SpeedAdapter extends ArrayAdapter<SpeedTest> {


    public SpeedAdapter(Context context, ArrayList<SpeedTest> tests) {
        super(context, 0, tests);
    }

    @NotNull
    @Override
    public View getView(int position, View convertView, @NotNull ViewGroup parent) {
        // Get the data item for this position
        SpeedTest speedTest = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_speed, parent, false);
        }
        // Lookup view for data population
        //TextView tvNetwork = (TextView) convertView.findViewById(R.id.tvF1Network);
        TextView tvSpeedUp = (TextView) convertView.findViewById(R.id.tvF1SpeedUp);
        TextView tvSpeedDown = (TextView) convertView.findViewById(R.id.tvF1SpeedDown);
        TextView tvPing = (TextView) convertView.findViewById(R.id.tvF1Ping);
        TextView tvDate = (TextView) convertView.findViewById(R.id.tvF1Date);

        // Populate the data into the template view using the data object
        assert speedTest != null;
        //tvNetwork.setText(speedTest.getNetwork());
        tvSpeedUp.setText(speedTest.getSpeedUp());
        tvSpeedDown.setText(speedTest.getSpeedDown());
        tvPing.setText(speedTest.getLatency());
        tvDate.setText(speedTest.getDate());
        // Return the completed view to render on screen
        return convertView;
    }

}