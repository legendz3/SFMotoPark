package sanfranmotopark.cansave.us;

import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;

/**
 * Created by kembp on 8/4/14.
 */
public class ParkingLocation {
    public ParkingLocation() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private int id;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(float latitude, float longitude) {
        this.latLng = new LatLng(latitude, longitude);
    }

    private LatLng latLng;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    private double distance;

    public Money getCost() {

        switch (getArea()) {
            case MC1:
                return Money.dollars(BigDecimal.valueOf(0.70));
            case MC2:
                return Money.dollars(BigDecimal.valueOf(0.60));
            case MC3:
                return Money.dollars(BigDecimal.valueOf(0.40));
            case MC5:
                return Money.dollars(BigDecimal.valueOf(0.25), BigDecimal.valueOf(6.00));
            case PortMC1:
                return Money.dollars(BigDecimal.valueOf(0.50));
            case PortMC2:
                return Money.dollars(BigDecimal.valueOf(0.50));
            default:
                return Money.dollars(BigDecimal.valueOf(0.25), BigDecimal.valueOf(6.00));
        }

    }

    public boolean isMultiSpace() {
        return isMultiSpace;
    }

    public void setMultiSpace(String meterType) {
        if (meterType == "MS")
            isMultiSpace = true;
        else
            isMultiSpace = false;
    }

    private boolean isMultiSpace;

    public boolean isSmartMeter() {
        return isSmartMeter;
    }

    public void setSmartMeter(String smartMeter) {
        if (smartMeter == "Y")
            isSmartMeter = true;
        else
            isSmartMeter = false;
    }

    private boolean isSmartMeter;

    private RateArea Area;

    public RateArea getArea() {
        return Area;
    }

    public void setArea(String area) {
        Area = RateArea.valueOf(area);
    }
}

