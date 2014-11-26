package dk.projekt.bachelor.wheresmyfamily.DataModel;

/**
 * Created by Tommy on 25-11-2014.
 */
public class Area {

    //region Fields
    Position northEast;
    Position southWest;
    //endregion

    public Area(){}

    public Area(Position _northEast, Position _southWest)
    {
        setNorthEast(_northEast);
        setSouthWest(_southWest);
    }

    //region Get and set
    public Position getNorthEast() {
        return northEast;
    }
    public void setNorthEast(Position northEast) {
        this.northEast = northEast;
    }
    public Position getSouthWest() {
        return southWest;
    }
    public void setSouthWest(Position southWest) {
        this.southWest = southWest;
    }

    public double getLatitudeSpan()
    {
        double maxLatitude = northEast.getLatitude();
        double minLatitude = southWest.getLatitude();
        return maxLatitude - minLatitude;
    }

    public double getLongitudeSpan()
    {
        double maxLongitude = northEast.getLongitude();
        double minLongitude = southWest.getLongitude();
        return (maxLongitude - minLongitude);
    }
    //endregion
}
