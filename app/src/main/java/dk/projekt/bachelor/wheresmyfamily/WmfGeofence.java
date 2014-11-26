package dk.projekt.bachelor.wheresmyfamily;

/**
 * Created by Tommy on 25-11-2014.
 */

/**
 * A single WmfGeofence object, defined by its center and radius.
 */
public class WmfGeofence
{
    // Instance variables
    private final String mId;
    private final double mLatitude;
    private final double mLongitude;
    private final float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    /**
     * @param geofenceId The WmfGeofence's request ID
     * @param latitude Latitude of the WmfGeofence's center.
     * @param longitude Longitude of the WmfGeofence's center.
     * @param radius Radius of the geofence circle.
     * @param expiration WmfGeofence expiration duration
     * @param transition Type of WmfGeofence transition.
     */
    public WmfGeofence(String geofenceId, double latitude, double longitude, float radius,
                       long expiration, int transition)
    {
        // Set the instance fields from the constructor
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
    }

    // region Instance field getters
    public String getId(){
        return mId;
    }
    public double getLatitude()
    {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
    }
    //endregion
    /**
     * Creates a Location Services WmfGeofence object from a
     * WmfGeofence.
     *
     * @return A WmfGeofence object
     */
    public com.google.android.gms.location.Geofence toGeofence()
    {
        // Build a new WmfGeofence object
        return new com.google.android.gms.location.Geofence.Builder().setRequestId(getId()).setTransitionTypes(mTransitionType)
                .setCircularRegion(getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration).build();
    }
}