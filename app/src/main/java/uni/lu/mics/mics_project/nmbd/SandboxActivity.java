package uni.lu.mics.mics_project.nmbd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class SandboxActivity extends AppCompatActivity {

    //TODO get city from location see https://developer.android.com/training/location/display-address


    private final String TAG = "SanboxActivity";
    //GPS variables
    private FusedLocationProviderClient fusedLocationClient;

    //Variables for permission request
    private static final int RC_LOCATION_PERM = 124;
    private static final String[] LOCATION =
            {Manifest.permission.ACCESS_FINE_LOCATION};

    //Open Map variables
    MapView map = null;
    //Open Map variables
    IMapController mapController;
    GeoPoint startPoint;
    Marker startMarker;

    //Location variable
    //https://developer.android.com/reference/android/location/Location.html
    //double getLatitude() : Get the latitude, in degrees. Setter also exists
    //double getLongitude(): Get the longitude, in degrees.
    //float	distanceTo(Location dest): Returns the approximate distance in meters between this location and the given location.
    //void 	set(Location l): Sets the contents of the location to the values from the given location.
    Location userlocation;


    TextView currentCityTextView;
    TextView currentGPSTextView;
    TextView clickedCityTextView;
    TextView clickedGPSTextView;
    TextView distanceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done, should be done before setContentView
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.sandbox_activity);

        currentCityTextView = findViewById(R.id.sandbox_current_city);
        currentGPSTextView = findViewById(R.id.sandbox_current_gps);
        currentCityTextView.setText("City from location not implemented");
        clickedCityTextView = findViewById(R.id.sandbox_clicked_city);
        clickedGPSTextView = findViewById(R.id.sandbox_clicked_gps);
        clickedCityTextView.setText("City from location not implemented");
        clickedGPSTextView.setText("Click on map");
        distanceTextView = findViewById(R.id.sanbox_distance);
        distanceTextView.setText("Click on map");

        userlocation = new Location("");

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Logic to handle location object
        mapController = map.getController();
        mapController.setZoom(17.);
        //sets default start position to MNO
        startPoint = new GeoPoint(49.503723, 5.947590);
        mapController.setCenter(startPoint);
        startMarker = new Marker(map);


        getLastLocation();


        //Add a marker when map clicked and get Geopoint
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                clickedGPSTextView.setText("Clicked GPS co-ordinates: "+ p.getLatitude() + " "+ p.getLongitude());
                Location loc = new Location("");
                loc.setLatitude(p.getLatitude());
                loc.setLongitude(p.getLongitude());
                distanceTextView.setText("Distance from current location is "+userlocation.distanceTo(loc) +"meters");

                startMarker.setPosition(p);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay( mReceive);
        map.getOverlays().add(OverlayEvents);


        //To display items from ArrayList on Map, could be events for which we retrieve the geo coordinates
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title", "Description", new GeoPoint(49.607877,6.131961))); // Lat/Lon decimal degrees
        items.add(new OverlayItem("Title", "Description", new GeoPoint(49.623933,6.112286)));
        items.add(new OverlayItem("Title", "Description", new GeoPoint(49.518730,5.951482)));
        items.add(new OverlayItem("Title", "Description", new GeoPoint(49.480862,6.058156)));
        items.add(new OverlayItem("Title", "Description", new GeoPoint(30.791286,34.937419)));
        //the overlay
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);
        mOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(mOverlay);



    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @AfterPermissionGranted(RC_LOCATION_PERM)
    public void getLastLocation(){
        //Will be executed if permission has already been granted
        //gets the current location
        if (hasLocationPermission()) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                userlocation.set(location);
                                GeoPoint userGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                mapController.animateTo(userGeoPoint);
                                currentCityTextView.setText("Accept location permission to see current city");
                                currentGPSTextView.setText("Your current GPS location is"+location.getLatitude() +" "+ location.getLongitude());
                            }
                        }
                    });
        } else {
            //Asks for permission
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location),
                    RC_LOCATION_PERM,
                    LOCATION);
        }
    }

    //Permission requests Helper function
    private boolean hasLocationPermission() {
        return EasyPermissions.hasPermissions(this, LOCATION);
    }









}
