package dk.projekt.bachelor.wheresmyfamily.fragments;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import dk.projekt.bachelor.wheresmyfamily.R;



public class MapFragment extends SupportMapFragment implements GoogleMap.OnMyLocationChangeListener {

    public static interface SupportMapFragmentListener
    {
        void onMapCreated(GoogleMap map);
    }

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView mAddress;
    private ProgressBar mActivityIndicator;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static final String MAP_OPTIONS = "MapOptions";

    GoogleMap map;
    LatLng currentPosition;
    Location myLocation;
    LocationClient locationClient;
    private MapView mapView;
    private Handler handler;
    MapFragment mapFragment;
    private SupportMapFragmentListener listener;


    // TODO: Rename and change types and number of parameters
    public static SupportMapFragment newInstance(GoogleMapOptions mapOptions) {
        SupportMapFragment fragment = new SupportMapFragment();

        Bundle args = new Bundle();
        args.putParcelable(MAP_OPTIONS, mapOptions);
        fragment.setArguments(args);
        return fragment;
    }
    public static SupportMapFragment newInstance(){
        // Required empty public constructor
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        return supportMapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.fragment_map);

        // map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map = SupportMapFragment.newInstance().getMap();
        if(map == null)
            MapsInitializer.initialize(getActivity());

        Toast.makeText(getActivity(), "Mapfragment onCreate", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        // super.onResume();

        mapFragment = (MapFragment) MapFragment.newInstance();
        mapFragment.setListener(new MapFragment.SupportMapFragmentListener() {
            @Override
            public void onMapCreated(GoogleMap googleMap) {
                // TODO Auto-generated method stub
                if (googleMap != null) {
                    // service is unaviable
                    Log.e("Noservice", ARG_PARAM1);
                }
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View overView =  inflater.inflate(R.layout.fragment_map, container, false);

        // ((ViewGroup) overView.findViewById(R.id.mapViewHolder)).addView(this.mapView);

        mAddress = (TextView) overView.findViewById(R.id.address);
        mActivityIndicator =
                (ProgressBar) overView.findViewById(R.id.address_progress);

        // map = MapFragment.newInstance().getMap();
        if(listener != null)
            listener.onMapCreated(getMap());

        /*if(map != null)
            map.setMyLocationEnabled(true);

        myLocation = map.getMyLocation();
        LatLng myLatLng = new LatLng(myLocation.getLatitude(),
                myLocation.getLongitude());

        map.addMarker(new MarkerOptions().position(myLatLng));*/

        return overView;
    }



    @Override
    public void onMyLocationChange(Location location) {

        myLocation = location;
    }

    /**
     * A subclass of AsyncTask that calls getFromLocation() in the
     * background. The class definition has these generic types:
     * Location - A Location object containing
     * the current location.
     * Void     - indicates that progress units are not used
     * String   - An address passed to onPostExecute()
     */
    private class GetAddressTask extends
            AsyncTask<Location, Void, String> {
        Context mContext;
        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }

        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @params params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
        @Override
        protected String doInBackground(Location... params) {
            Geocoder geocoder =
                    new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            // Create a list to contain the result address
            List<Address> addresses = null;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(loc.getLatitude()) +
                        " , " +
                        Double.toString(loc.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());

                Toast.makeText(mContext, addressText, Toast.LENGTH_SHORT).show();
                // Return the text
                return addressText;
            } else {
                return "No address found";
            }
        }

        /**
         * A method that's called once doInBackground() completes. Turn
         * off the indeterminate activity indicator and set
         * the text of the UI element that shows the address. If the
         * lookup failed, display the error message.
         */
        @Override
        protected void onPostExecute(String address) {
            // Set activity indicator visibility to "gone"
            mActivityIndicator.setVisibility(View.GONE);
            // Display the results of the lookup.
           Toast.makeText(mContext, address, Toast.LENGTH_SHORT).show();
        }

        /**
         * The "Get Address" button in the UI is defined with
         * android:onClick="getAddress". The method is invoked whenever the
         * user clicks the button.
         *
         * @param v The view object associated with this method,
         * in this case a Button.
         */
        public void getAddress(View v) {
            // Ensure that a Geocoder services is available
            if (Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.GINGERBREAD
                    &&
                    Geocoder.isPresent()) {
                // Show the activity indicator
                mActivityIndicator.setVisibility(View.VISIBLE);

                // Get the current location
                Location currentLocation = locationClient.getLastLocation();
            /*
             * Reverse geocoding is long-running and synchronous.
             * Run it on a background thread.
             * Pass the current location to the background task.
             * When the task finishes,
             * onPostExecute() displays the address.
             */
                (new GetAddressTask(mContext)).execute(myLocation);
            }
        }
    }

    /**
     * @return the listener
     */
    public SupportMapFragmentListener getListener() {
        return listener;
    }
    /**
     * @param listener the listener to set
     */
    public void setListener(SupportMapFragmentListener listener) {
        this.listener = listener;
    }
}

