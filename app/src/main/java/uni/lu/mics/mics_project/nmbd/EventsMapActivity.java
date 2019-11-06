package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationCallBack;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationUtils;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;

public class EventsMapActivity extends AppCompatActivity {


    private final String TAG = "EventsMapActivity";

    AppGlobalState globalState;
    EventRepository eventRepo;
    Storage storageService;
    private User currentUser;

    //Open Map variables
    MapView map = null;
    //Open Map variables
    IMapController mapController;
    GeoPoint startPoint;
    Marker startMarker;

    //Location variable
    Location userlocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load/initialize the osmdroid configuration, this can be done, should be done before setContentView
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setContentView(R.layout.activity_events_map);

        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        storageService = globalState.getServiceFacade().storageService();

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        userlocation = new Location("");

        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);



        // Logic to handle location object
        mapController = map.getController();
        mapController.setZoom(17.);
        //sets default start position to MNO
        startPoint = new GeoPoint(49.503723, 5.947590);
        mapController.setCenter(startPoint);
        startMarker = new Marker(map);

        LocationUtils.getLastLocation(this, new LocationCallBack() {
            @Override
            public void onSuccess(Location location) {
                GeoPoint userGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(userGeoPoint);
            }
        });

        displayEventMarkers();
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

    private void displayEventMarkers(){
        final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

        eventRepo.getAll(new RepoMultiCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> models) {
                Log.d(TAG, String.valueOf(models.size()));
                for (Event e:models) {
                    items.add(new OverlayItem(e.getName(), e.getDescription(), new GeoPoint(e.getGpsLat(), e.getGpsLong())));
                }
                ItemizedOverlayWithFocus<OverlayItem> mOverlay;
                mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                        new OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                //do something
                                return true;
                            }
                            @Override
                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                return false;
                            }
                        }, EventsMapActivity.this);
                Log.d(TAG, String.valueOf(items.size()));
                mOverlay.setFocusItemsOnTap(true);
                map.getOverlays().add(mOverlay);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }


}
