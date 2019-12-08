package uni.lu.mics.mics_project.nmbd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.text.WordUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import uni.lu.mics.mics_project.nmbd.adapters.CommentRvAdapter;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.domain.model.Comment;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.CommentRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class EventActivity extends AppCompatActivity {

    final String TAG = "EventActivity";

    AppGlobalState globalState;
    EventRepository eventRepo;
    UserRepository userRepo;
    CommentRepository commentRepo;
    Storage storageService;

    private ImageButton imgView;
    private TextView eventTitle;
    private TextView dateField;
    private TextView place;
    private TextView eventType;
    private TextView host;
    private TextView numberOfParticipants;
    private TextView descriptionBox;
    private TextView time;
    private TextView currentUserName;
    private EditText commentBody;
    private Button joinLeaveBtn;
    private Button editBtn;
    private User currentUser;
    private String currentUserID;
    private Event currentEvent;
    private ImageView commenterPic;
    private ImageView hostProfileImgView;
    private NavigationView navigationView;

    //Open Map variables
    MapView map = null;
    //Open Map variables
    IMapController mapController;
    GeoPoint startPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        setContentView(R.layout.activity_event);

        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        userRepo = globalState.getRepoFacade().userRepo();
        commentRepo = globalState.getRepoFacade().commentRepo();
        storageService = globalState.getServiceFacade().storageService();

        imgView = findViewById(R.id.eventImageBtn);
        numberOfParticipants = findViewById(R.id.number_peeps_going);
//        Retrieve Intent and get current user and event
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();
        currentEvent = (Event) intent.getSerializableExtra("currentEvent");

        setJoinLeaveButton();
        setEditButton();

        if (intent.hasExtra("image")) {
            Log.d(TAG, "retrieving image extra from intent");
            Uri imageUri = Uri.parse(intent.getStringExtra("image"));
            ImageViewUtils.displayPicUri(this, imageUri, imgView);
        } else {
            Log.d(TAG, "retrieving image from event id");
            ImageViewUtils.displayEventPicID(this, currentEvent.getId(), imgView);
        }
        setEventName(currentEvent.getName());
        setDate(currentEvent.getDate());
        setAdress(currentEvent.getEventAddress());
        setCategory(currentEvent.getCategory());
        String creatorId = currentEvent.getCreator();
        setProfilePics(creatorId);
        setDescription(currentEvent.getDescription());
        setMap();
        setParticipants();
        setupToolbar();
        seTime();
        setCommenterName(currentUser.getName());
        setCommenterPic();
        getComments();
        //getRating();

        navigationView = findViewById(R.id.navigationView);
        commentBody = findViewById(R.id.commentBody);
        handleCommentField(commentBody);

    }


    private void setProfilePics(String creatorId){
        userRepo.findById(creatorId, new RepoCallback<User>() {
            @Override
            public void onCallback(User user) {
                setHost(user.getName());
                Log.d(TAG, "Setting Event creator's Name");
                hostProfileImgView = findViewById(R.id.hostProfileImgView);
                ImageViewUtils.displayUserCirclePic(EventActivity.this, user,hostProfileImgView );
            }
        });
    }

    private void setCommenterPic(){
        commenterPic = findViewById(R.id.userPic);
        Log.d(TAG, "setCommenterPic: " +  (commenterPic == null));
        Log.d(TAG, "setCommenterPic: is called" + currentUserID);
        ImageViewUtils.displayUserCirclePicID(EventActivity.this, currentUserID, commenterPic);
    }



    private void setMap(){
        //Map variables
        map = findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(false);
        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mapController = map.getController();
        mapController.setZoom(15.);
        final ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
        startPoint = new GeoPoint(currentEvent.getGpsLat(), currentEvent.getGpsLong());

        OverlayItem overlayItem = new OverlayItem("", "", startPoint);
        overlayItem.setMarker(getDrawable(R.drawable.map_marker));
        overlayItems.add(overlayItem);
        ItemizedOverlayWithFocus<OverlayItem> mOverlay;
        mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(overlayItems,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, EventActivity.this);
        Log.d(TAG, String.valueOf(overlayItems.size()));
        map.getOverlays().add(mOverlay);
        mapController.setCenter(new GeoPoint(startPoint));

    }

    private void setEventName(String name) {
        eventTitle = (TextView) findViewById(R.id.eventTitle);
        eventTitle = (TextView) findViewById(R.id.eventTitle);
        eventTitle.setText(name);
    }

    private void setDate(String dateString) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatterToStr = new SimpleDateFormat("EEEE, MMMM dd");
        dateField = (TextView) findViewById(R.id.day_monthId);
        try {
            Date date = formatter.parse(dateString);
            dateField.setText(formatterToStr.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void setAdress(String adress) {
        place = (TextView) findViewById(R.id.placeId);
        place.setText(adress);
    }

    private void setCategory(String category) {
        eventType = (TextView) findViewById(R.id.categoryId);
        eventType.setText(WordUtils.capitalize(category.toLowerCase()));
    }

    private void setHost(String hostName) {
        host = (TextView) findViewById(R.id.hosted_by);
        host.setText("Created by " + hostName);

    }

    

    private void setDescription(String description) {
        descriptionBox = findViewById(R.id.description_box);
        descriptionBox.setMovementMethod(new ScrollingMovementMethod());
        descriptionBox.setText(description);

    }

    private void setJoinLeaveButton() {
        joinLeaveBtn = (Button) findViewById(R.id.participateBtn);
        if (currentEvent.getEventParticipants().contains(currentUser.getId())) {
            joinLeaveBtn.setText("Leave");
        } else {
            joinLeaveBtn.setText("Join");
        }
    }

    private void setEditButton() {
        editBtn = findViewById(R.id.edit_event);
        if (currentEvent.getEventAdmins().contains(currentUser.getId())) {
            editBtn.setVisibility(View.VISIBLE);
        } else {
            editBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void joinLeaveEventOnClick(View view) {
        int numParticipants = currentEvent.getEventParticipants().size();
        if (joinLeaveBtn.getText() == "Join") {
            currentEvent.getEventParticipants().add(currentUser.getId());
            eventRepo.addElement(currentEvent.getId(), "eventParticipants", currentUser.getId());
            joinLeaveBtn.setText("Leave");
            numParticipants++;

        } else {
            currentEvent.getEventParticipants().remove(currentUser.getId());
            eventRepo.removeElement(currentEvent.getId(), "eventParticipants", currentUser.getId());
            joinLeaveBtn.setText("Join");
            numParticipants--;
        }
        if (numParticipants == 1 || numParticipants == 0){
            numberOfParticipants.setText(numParticipants + " person is going");
        } else {
            numberOfParticipants.setText(numParticipants + " people are going");
        }
        eventRepo.findById(currentEvent.getId(), new RepoCallback<Event>() {
            @Override
            public void onCallback(Event model) {
                currentEvent = model;
                setParticipants();
            }
        });

    }

    private void seTime(){
        time = findViewById(R.id.time_from_to);
        time.setText(currentEvent.getStartTime() + " - " + currentEvent.getEndTime());
    }

    private void setParticipants() {
        List<String> participants = currentEvent.getEventParticipants();
        int numParticipants = participants.size();
        Log.d(TAG, "Number of participants:"+ numParticipants);
        if (numParticipants == 1 || numParticipants == 0 ) {
            numberOfParticipants.setText(numParticipants + " person is going");
        } else {
            numberOfParticipants.setText(numParticipants + " people are going");
        }
        ImageView prof1 = findViewById(R.id.profile1);
        ImageView prof2 = findViewById(R.id.profile2);
        ImageView prof3 = findViewById(R.id.profile3);
        if (numParticipants==0){
          prof1.setVisibility(View.INVISIBLE);
          prof2.setVisibility(View.INVISIBLE);
            prof3.setVisibility(View.INVISIBLE);
        } else if (numParticipants==1){
            prof1.setVisibility(View.VISIBLE);
            prof2.setVisibility(View.INVISIBLE);
            prof3.setVisibility(View.INVISIBLE);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(0), prof1);
        } else if (numParticipants==2){
            prof1.setVisibility(View.VISIBLE);
            prof2.setVisibility(View.VISIBLE);
            prof3.setVisibility(View.INVISIBLE);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(0), prof1);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(1), prof2);
        } else if (numParticipants==3){
            prof1.setVisibility(View.VISIBLE);
            prof2.setVisibility(View.VISIBLE);
            prof3.setVisibility(View.VISIBLE);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(0), prof1);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(1), prof2);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(2), prof3);
        } else {
            Random rand = new Random();
            int r = rand.nextInt(participants.size());
            ImageViewUtils.displayUserCirclePicID(this, participants.get(r), prof1);
            participants.remove(r);
            r = rand.nextInt(participants.size());
            ImageViewUtils.displayUserCirclePicID(this, participants.get(r), prof2);
            participants.remove(r);
            r = rand.nextInt(participants.size());
            ImageViewUtils.displayUserCirclePicID(this, participants.get(r), prof3);
        }
    }

    private void setCommenterName(String name){
        currentUserName = findViewById(R.id.userCommenterName);
        currentUserName.setText(name);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
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


    public void editOnClick(View view) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("event", currentEvent);
        startActivity(intent);
    }

    private void setupToolbar() {
        ImageView profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),profileImageView );
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                finish();
            }
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EventActivity.this, HomepageActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void commentOnClick(View view){
        String commText = commentBody.getText().toString();

        if (commText.length() == 0){
            Toast.makeText(this, "Cannot send an empty comment", Toast.LENGTH_LONG).show();
        }

        String date = currentDateTimeToTimestamp();
        final Comment comment = new Comment(currentUserID, currentUser.getName(), currentUser.getProfilePicUrl(), currentEvent.getId(), date, commText);

        commentRepo.addWithoutId(comment, new RepoCallback<String>(){
            @Override
            public void onCallback(String model) {
                Log.d(TAG, "onCallback: Adding comment to DB");
                commentRepo.update(model, "id", model);
                Log.d(TAG, "onCallback: Updating comment's id field");
                comment.setId(model);
            }
        });

        commentBody.setText("");
        commentBody.setFocusable(false);
        commentBody.setFocusableInTouchMode(true);
        hideKeyboard();
        getComments();
    }

    public void getComments(){
        final RecyclerView recyclerView = findViewById(R.id.rvcomments);
        commentRepo.findByEventId(currentEvent.getId(), new RepoMultiCallback<Comment>() {
            @Override
            public void onCallback(ArrayList<Comment> dbComments) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd hh:mm:ss:SSS");

                    List<Comment> retrievedComments = dbComments;
                if (retrievedComments != null) {
                    retrievedComments.sort(Comparator.comparing(u -> u.getDate()));
                    List<String> ownerIds = retrievedComments.stream().map(comment -> comment.getOwnerId()).collect(Collectors.toList());
                    List<String> testNames = retrievedComments.stream().map(comment -> comment.getOwnerName()).collect(Collectors.toList());
                    List<String> testPics = retrievedComments.stream().map(comment -> comment.getOwnerPic()).collect(Collectors.toList());
                    List<String> testTexts = retrievedComments.stream().map(comment -> comment.getText()).collect(Collectors.toList());
                    List<String> testdates = retrievedComments.stream().map(comment -> (new Date(Timestamp.valueOf(comment.getDate().substring(0, 21)).getTime())).toString()).collect(Collectors.toList());

                    // Setting up comments recyclerView
                    CommentRvAdapter adapter = new CommentRvAdapter(EventActivity.this, testNames, testPics, testTexts, testdates, ownerIds);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(EventActivity.this));
                }

            }
        });
    }

    private void hideKeyboard(){
        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private String currentDateTimeToTimestamp(){
        Date currentTime = Calendar.getInstance().getTime();
        Timestamp ts = new Timestamp(currentTime.getTime());
        return ts.toString();
    }

    private void handleCommentField(View view){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setFocusable(true);
                Log.d(TAG, "onClick: comment body clicked" + v);
                //commentBody.requestFocusFromTouch();
            }
        });

        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    navigationView.setVisibility(View.INVISIBLE);

                } else {
                    navigationView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}


