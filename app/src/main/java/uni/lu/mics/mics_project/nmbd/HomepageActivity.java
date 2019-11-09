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

import pub.devrel.easypermissions.EasyPermissions;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class HomepageActivity extends AppCompatActivity {

    AppGlobalState globalState;
    UserRepository userRepo;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        globalState = (AppGlobalState) getApplicationContext();
        userRepo = globalState.getRepoFacade().userRepo();
        storageService = globalState.getServiceFacade().storageService();
        authService = globalState.getServiceFacade().authentificationService();
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

    public void viewEventOnClick(View view) {
        Intent intent = createIntent(EventActivity.class, currentUser);
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





}
