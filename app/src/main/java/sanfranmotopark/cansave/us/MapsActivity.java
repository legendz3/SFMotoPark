package sanfranmotopark.cansave.us;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static String ARG_SHOW_CLOSEST_LOCATION = "ShowClosestLocation";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private ParkingLocationDataSource dataSource;
    private LocationClient locationClient;
    private ParkingLocation[] locations;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup infoWindow;
    private TextView infoAddress;
    private TextView infoPrice;
    private ImageView infoTakesCardImage;
    private ImageButton infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;
    private FragmentActivity myContext;
    private static View view;

    private final Handler handler = new Handler();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Map<Marker, ParkingLocation> allMarkersMap = new HashMap<Marker, ParkingLocation>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.activity_maps, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        Boolean showCloeset = getArguments().getBoolean(ARG_SHOW_CLOSEST_LOCATION);
        setUpMap(inflater, showCloeset);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        myContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        if (isGooglePlayServicesAvailable()) {
            locationClient.connect();
        }
    }


    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap(LayoutInflater inflater, Boolean showCloeset) {
        locationClient = new LocationClient(myContext, this, this);
        dataSource = new ParkingLocationDataSource(myContext);
        try {
            dataSource.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        locations = dataSource.getAllLocations().toArray(new ParkingLocation[0]);
        LatLng sf = new LatLng(37.785737, -122.418074);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mapWrapperLayout = (MapWrapperLayout) myContext.findViewById(R.id.map_relative_layout);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sf, 13));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                handler.removeCallbacks(addMarkers);
                handler.post(addMarkers);

            }
        });

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(mMap, getPixelsFromDp(myContext, 39 + 20));

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        this.infoWindow = (ViewGroup) inflater.inflate(R.layout.info_window, null);
        this.infoAddress = (TextView) infoWindow.findViewById(R.id.address);
        this.infoPrice = (TextView) infoWindow.findViewById(R.id.price);
        this.infoButton = (ImageButton) infoWindow.findViewById(R.id.imageButton);
        this.infoTakesCardImage = (ImageView) infoWindow.findViewById(R.id.takes_credit_card);


        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton,
                getResources().getDrawable(R.drawable.ic_menu_directions),
                getResources().getDrawable(R.drawable.ic_menu_directions)) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", marker.getPosition().latitude, marker.getPosition().longitude, "Meter");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        };
        this.infoButton.setOnTouchListener(infoButtonListener);

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                ParkingLocation location = allMarkersMap.get(marker);
                infoAddress.setText(location.getAddress());
                infoPrice.setText(location.getCost().toString());
                int visibility = location.isSmartMeter() ? View.VISIBLE : View.GONE;
                infoTakesCardImage.setVisibility(visibility);
                infoButtonListener.setMarker(marker);

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

    }

    private final Runnable addMarkers = new Runnable() {
        @Override
        public void run() {
            MarkerOptions markerPOI;

            Projection projection = mMap.getProjection();
            LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
            for (ParkingLocation location : locations) {
                if (bounds.contains(location.getLatLng())) {
                    markerPOI = new MarkerOptions();
                    markerPOI.position(location.getLatLng());
                    switch (location.getArea()) {
                        case MC1:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                            break;
                        case MC2:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            break;
                        case MC3:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                            break;
                        case MC5:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            break;
                        case PortMC1:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                            break;
                        case PortMC2:
                            markerPOI.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            break;
                    }
                    Marker marker = mMap.addMarker(markerPOI);

                    allMarkersMap.put(marker, location);
                }
            }
        }
    };

    /*
    * Called by Location Services when the request to connect the
    * client finishes successfully. At this point, you can
    * request the current location or start periodic updates
    */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(myContext, "Connected", Toast.LENGTH_SHORT).show();
        Location location = locationClient.getLastLocation();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
        mMap.animateCamera(cameraUpdate);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(myContext, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    /*
     * Google Play services can resolve some errors it detects.
     * If the error has a resolution, try sending an Intent to
     * start a Google Play services activity that can resolve
     * error.
     */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        myContext,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            /*
            * Thrown if Google Play services canceled the original
            * PendingIntent
            */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(myContext.getApplicationContext(), "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(myContext);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode,
                    myContext,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(myContext.getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
