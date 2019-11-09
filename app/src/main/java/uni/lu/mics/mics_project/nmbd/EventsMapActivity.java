package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import uni.lu.mics.mics_project.nmbd.adapters.EventMapCallBack;
import uni.lu.mics.mics_project.nmbd.adapters.EventMapListAdapter;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedListEvent;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
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



    //Location variable
    Location userlocation;
    final ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();

    //Event Ids
    final ArrayList<String> eventIds = new ArrayList<>();

    //Recycler view
    private RecyclerView mEventListRecyclerView;
    private EventMapListAdapter mEventListAdapter;
    //Extended list to pass to the adapter
    final private ExtendedListEvent eventExtList = new ExtendedListEvent();



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
        mEventListRecyclerView = findViewById(R.id.events_recyclerview);

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

        //Move to the phone last know position
        LocationUtils.getLastLocation(this, new LocationCallBack() {
            @Override
            public void onSuccess(Location location) {
                GeoPoint userGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                mapController.setCenter(userGeoPoint);
            }
        });

        displayEventMarkers();
        setupToolbar();
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


        eventRepo.getAll(new RepoMultiCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> models) {
                Log.d(TAG, String.valueOf(models.size()));
                for (Event e:models) {
                    //add markers
                    OverlayItem overlayItem = new OverlayItem(e.getName(), e.getDescription(), new GeoPoint(e.getGpsLat(), e.getGpsLong()));
                    overlayItem.setMarker(getDrawable(R.drawable.map_marker));
                    overlayItems.add(overlayItem);
                    eventIds.add(e.getEventId());
                    //add event to the extended list to display cards in recycler view
                    eventExtList.addElement(e.getName(),e.getEventId(), e.getDate(), e.getCategory(), e.getEventAddress());
                }
                //Initialize recycler view
                initializeEventsRecyclerView();
                ItemizedOverlayWithFocus<OverlayItem> mOverlay;
                mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(overlayItems,
                        new OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                                //Moves Recycler view to the selected event
                                mEventListRecyclerView.smoothScrollToPosition(index);
                                return true;
                            }
                            @Override
                            public boolean onItemLongPress(final int index, final OverlayItem item) {
                                return false;
                            }
                        }, EventsMapActivity.this);
                Log.d(TAG, String.valueOf(overlayItems.size()));
                map.getOverlays().add(mOverlay);
            }
        });

    }

    private void initializeEventsRecyclerView() {
        //Initialize the adatpter for the recycler view
        mEventListAdapter = new EventMapListAdapter(this, eventExtList, new EventMapCallBack() {
            @Override
            public void onViewEventClick(int p) {
                //Takes to event activty when button clicked
                startEventActivity(eventExtList.getId(p));
            }

            @Override
            public void onShowOnMapClick(int p) {
                //Move to corresponding point on Map
                mapController.animateTo(overlayItems.get(p).getPoint());
                mapController.setZoom(15.);
            }
        });
        //Make the recycler view snap on the current card view
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mEventListRecyclerView);
        // Connect the adapter with the recycler view.
        mEventListRecyclerView.setAdapter(mEventListAdapter);
        // Give the recycler view a default layout manager.
        mEventListRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }


    public void startEventActivity (String eventId){
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
            public void onGetField(String str) { }
        });
    }

    public void backToHomepage(){
        Intent intent = new Intent(EventsMapActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        backToHomepage();
    }

    private void setupToolbar() {
        ImageView profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),profileImageView );
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventsMapActivity.this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                finish();
            }
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHomepage();
            }
        });
    }

}
