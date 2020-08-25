
package lestelabs.antenna.ui.main.rest.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Towers {

    @SerializedName("result")
    @Expose
    private Integer result;

    @SerializedName("data")
    @Expose
    private DataCell data = null;

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public DataCell getData() {
        return data;
    }

    public void setData(DataCell data) {
        this.data = data;
    }

}
