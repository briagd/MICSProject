package uni.lu.mics.mics_project.nmbd;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;
import uni.lu.mics.mics_project.nmbd.adapters.AdapterCallBack;
import uni.lu.mics.mics_project.nmbd.adapters.EventInvitationAdapter;
import uni.lu.mics.mics_project.nmbd.adapters.EventListAdapter;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListEvent;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListInvitation;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class HomepageActivity extends AppCompatActivity {

    AppGlobalState globalState;
    UserRepository userRepo;
    EventRepository eventRepo;
    Authentification authService;
    Storage storageService;
    Button btn_sign_out;
    //currentUser object retrieved from intent
    User currentUser;


    final String TAG = "HomepageActivity";

    private ImageView profileImageView;
    private ImageView mapImageView;

    //Variables for permission request
    private static final int RC_CAMERA_AND_LOCATION_STORAGE = 124;
    private static final String[] PERMISSIONS =
            {Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    //Event recyclerview
    private RecyclerView mEventInviteListRecyclerView;
    private EventInvitationAdapter mEventInviteListAdapter;
    //Event invites received
    private final ExtendedListInvitation eventInviteList = new ExtendedListInvitation();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        //initialize the global variables
        globalState = (AppGlobalState) getApplicationContext();
        userRepo = globalState.getRepoFacade().userRepo();
        eventRepo = globalState.getRepoFacade().eventRepo();
        storageService = globalState.getServiceFacade().storageService();
        authService = globalState.getServiceFacade().authentificationService();

        //Gets current user object from intent
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        if(currentUser!=null){
            Log.d(TAG, currentUser.getName());
        }

        mapImageView = findViewById(R.id.map_imageview);
        mapImageView.setImageDrawable(getDrawable(R.drawable.openstreetmap));

        profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePic(this, currentUser,profileImageView );
        btn_sign_out = findViewById(R.id.sign_out_button);

        //Sets up the sign out button to take action if pressed
        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authService.signOut(HomepageActivity.this, MainActivity.class);
            }
        });

        //Create the Notification channel to be able to display notification
        createNotificationChannel();

        //Request permission to accept location
        EasyPermissions.requestPermissions(this, getString(R.string.rationale_camera_location),
                RC_CAMERA_AND_LOCATION_STORAGE,
                PERMISSIONS);

        setupToolbar();

        //Recycler view for invites:
        mEventInviteListRecyclerView = findViewById(R.id.eventInvitationRecyclerView);
        initializeEventsInviteRecyclerView();
    }





    public void setupToolbar(){
        profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),profileImageView );
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomepageActivity.this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                finish();
            }
        });
    }

    public void createEventOnClick(View view) {
        Intent intent = createIntent(CreateEventActivity.class, currentUser);
        startActivity(intent);
    }

    public void viewFriendsOnClick(View view) {
        Intent intent = createIntent(FriendsActivity.class, currentUser);
        startActivity(intent);
    }

    public void eventsMapOnClick(View view) {
        Intent intent = createIntent(EventsMapActivity.class, currentUser);
        startActivity(intent);
    }

    public void viewProfileOnClick(View view) {
        Intent intent = createIntent(ProfileActivity.class, currentUser);
        startActivity(intent);
    }

    public void viewEventsOnClick(View view) {
        Intent intent = createIntent(ViewEventsActivity.class, currentUser);
        startActivity(intent);
    }

    private Intent createIntent(Class targetActivity, User user){
        Intent intent = new Intent(this, targetActivity);
        intent.putExtra("currentUser", user);
        return intent;
    }

    //Set-up notification
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.UpldNotifName);
            String description = getString(R.string.UpldNotifDescritption);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.UpldNotifId), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //Invitation recyclerview
    private void initializeEventsInviteRecyclerView(){
        mEventInviteListAdapter = new EventInvitationAdapter(this, eventInviteList, new AdapterCallBack() {
            @Override
            public void onClickCallback(int p) {
                startEventActivity(eventInviteList.getId(p));
            }
        });
        //Make the recycler view snap on the current card view
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(mEventInviteListRecyclerView);
        // Connect the adapter with the recycler view.
        mEventInviteListRecyclerView.setAdapter(mEventInviteListAdapter);
        // Give the recycler view a default layout manager.
        mEventInviteListRecyclerView.setLayoutManager(new LinearLayoutManager(HomepageActivity.this, RecyclerView.HORIZONTAL, false));
        updateEventsInviteList();
    }

    private void updateEventsInviteList() {
        eventRepo.whereArrayContains("eventInvited", currentUser.getId(), new RepoMultiCallback<Event>() {
            @Override
            public void onCallback(ArrayList<Event> models) {
                for (Event event:models){
                    Log.d(TAG, "Events invited:"+event.getName());
                    addEventToExtendedList(event.getId(), eventInviteList, mEventInviteListAdapter);
                }
                if (models.size()==0){
                    mEventInviteListRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void addEventToExtendedList(String id, final ExtendedListInvitation extList, final RecyclerView.Adapter adapter) {
        //Access database to find event corresponding to an id

        eventRepo.findById(id, new RepoCallback<Event>() {
            @Override
            public void onCallback(final Event model) {
                //add the found model to the list
                //String eventName, String eventId, String eventOrganizorId, String eventOrganizorName
                userRepo.findById(model.getCreator(), new RepoCallback<User>() {
                    @Override
                    public void onCallback(User user) {
                        extList.addElement(model.getName(), model.getId(), model.getCreator(), user.getName());
                        //Notifies adapter that the list has been updated so recyclerview can be updated
                        adapter.notifyItemInserted(extList.getIdIndexOfLast());
                    }
                });

            }
        });
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

        });
    }


}
