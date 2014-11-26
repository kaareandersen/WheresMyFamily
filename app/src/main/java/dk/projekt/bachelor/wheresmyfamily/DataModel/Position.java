package dk.projekt.bachelor.wheresmyfamily.DataModel;

/**
 * Created by Tommy on 25-11-2014.
 */
public class Position {

    //region Fields
    double latitude;
    double longitude;
    //endregion

    public Position(){}

    public Position(double _latitude, double _longitude)
    {
        setLatitude(_latitude);
        setLongitude(_longitude);
    }

    //region Get and set
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    //endregion
}
