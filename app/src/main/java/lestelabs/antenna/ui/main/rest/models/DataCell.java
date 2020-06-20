
package lestelabs.antenna.ui.main.rest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataCell {

    @SerializedName("lat")
    @Expose
    private Double lat;

    @SerializedName("range")
    @Expose
    private Double range;

    @SerializedName("lon")
    @Expose
    private Double lon;

    @SerializedName("time")
    @Expose
    private Long time;


    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getRange() {
        return range;
    }

    public void setRange(Double range) {
        this.range = range;
    }

    public Double getLon() {return lon;}

    public void setLon(Double lon) {this.lon = lon;}

    public Long getTime() {return time;}

    public void setTime(Long time) {this.time = time;}
}
