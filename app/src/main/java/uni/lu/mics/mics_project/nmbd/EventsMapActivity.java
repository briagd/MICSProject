package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.HashMap;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationCallBack;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationUtils;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
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

    //Events
    ImageView imgView;
    TextView eventNameView;
    TextView eventDateView;
    TextView eventAddressView;
    Button eventButton;
    TextView eventCategory;
    //Id of event selected to be passed in intent
    String eventId;

    //Event Ids
    final ArrayList<String> eventIds = new ArrayList<>();



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

        //View variables
        imgView = findViewById(R.id.imageView);

        eventNameView = findViewById(R.id.event_name);
        eventNameView.setText("Click on a marker on the map to know about an event");
        eventDateView = findViewById(R.id.date_textview);

        eventAddressView = findViewById(R.id.address_view);
        eventButton = findViewById(R.id.eventButton);
        eventCategory = findViewById(R.id.event_type_textview);
        eventDateView.setVisibility(View.INVISIBLE);
        imgView.setVisibility(View.INVISIBLE);
        eventAddressView.setVisibility(View.INVISIBLE);
        eventButton.setVisibility(View.INVISIBLE);
        eventCategory.setVisibility(View.INVISIBLE);

        //Map variables
        userlocation = new Location("");
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Logic to handle location object
        mapController = map.getController();
        mapController.setZoom(10.);
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
                    eventIds.add(e.getEventId());
                }
                ItemizedOverlayWithFocus<OverlayItem> mOverlay;
                mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                        new OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                setFieldsVisible();
                                if (eventIds.get(index)!=null) {
                                    eventId= eventIds.get(index);
                                    eventRepo.findById(eventIds.get(index), new RepoCallback<Event>() {
                                        @Override
                                        public void onCallback(Event model) {
                                            storageService.getStorageReference(getString(R.string.gsTb256EventPicUrl), model.getCoverPicUrl(), new StorageCallback() {
                                                @Override
                                                public void onSuccess(StorageReference storageReference) {
                                                    ImageViewUtils.displayPic(EventsMapActivity.this, storageReference, imgView);
                                                }
                                                @Override
                                                public void onFailure() { }
                                            });
                                            eventCategory.setText(model.getCategory());
                                            eventNameView.setText(model.getName());
                                            eventDateView.setText(model.getDate());
                                            eventAddressView.setText(model.getEventAddress());
                                        }

                                        @Override
                                        public void onGetField(String str) {

                                        }
                                    });
                                }
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

    public void setFieldsVisible(){
        eventDateView.setVisibility(View.VISIBLE);
        imgView.setVisibility(View.VISIBLE);
        eventAddressView.setVisibility(View.VISIBLE);
        eventButton.setVisibility(View.VISIBLE);
        eventCategory.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }
    
    public void viewEventOnClick(View view) {
        final Intent intent = new Intent(this, EventActivity.class);
        intent.putExtra("currentUser", currentUser);
        eventRepo.findById(eventId, new RepoCallback<Event>() {
            @Override
            public void onCallback(Event model) {
                intent.putExtra("currentEvent", model);
                startActivity(intent);
                finish();
            }

            @Override
            public void onGetField(String str) {

            }
        });

    }
}
