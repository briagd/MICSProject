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
import java.text.DecimalFormat;
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
import uni.lu.mics.mics_project.nmbd.domain.model.Rating;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.CommentRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RatingRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class EventActivity extends AppCompatActivity {

    final String TAG = "EventActivity";

    AppGlobalState globalState;
    EventRepository eventRepo;
    UserRepository userRepo;
    CommentRepository commentRepo;
    RatingRepository ratingRepo;
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
    private RatingBar ratingBar;
    private TextView eventRating;
    private TextView scoreText;

    private Button inviteButton;

    private Button refuseInvButton;


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
        //Initialize the global state and database variables
        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        userRepo = globalState.getRepoFacade().userRepo();
        commentRepo = globalState.getRepoFacade().commentRepo();
        ratingRepo = globalState.getRepoFacade().ratingRepo();
        storageService = globalState.getServiceFacade().storageService();

        imgView = findViewById(R.id.eventImageBtn);
        numberOfParticipants = findViewById(R.id.number_peeps_going);
        //Retrieve Intent and get current user and event
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();
        currentEvent = (Event) intent.getSerializableExtra("currentEvent");

        //Add the join or leave button or edit button if necessary
        setJoinLeaveButton();
        setEditInviteButton();

        //Checks that whether the image of the event was uploaded or retrieves it from uri if the event was just created
        if (intent.hasExtra("image")) {
            Log.d(TAG, "retrieving image extra from intent");
            Uri imageUri = Uri.parse(intent.getStringExtra("image"));
            ImageViewUtils.displayPicUri(this, imageUri, imgView);
        } else {
            Log.d(TAG, "retrieving image from event id");
            ImageViewUtils.displayEventPicID(this, currentEvent.getId(), imgView);
        }

        //Sets the different fields
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

        setupRating();
        setCommenterName(currentUser.getName());
        setCommenterPic();
        getComments();
        //getRating();

        navigationView = findViewById(R.id.navigationView);
        commentBody = findViewById(R.id.commentBody);
        handleCommentField(commentBody);
        handleRatings();

        setRefuseInviteButton();


    }


    //If user was invited to event then the refuse button should be displayed
    private void setRefuseInviteButton() {
        refuseInvButton = findViewById(R.id.refuse_inv_button);
        if (currentEvent.getEventInvited().contains(currentUserID) && !currentEvent.getEventAdmins().contains(currentUserID)) {
            refuseInvButton.setVisibility(View.VISIBLE);
        } else {
            refuseInvButton.setVisibility(View.INVISIBLE);
        }
    }

    //If the refuse button is clicked, the user is removed from invited list in the database and the user is brought back to the homepage
    public void refuseOnClick(View view) {
        eventRepo.removeElement(currentEvent.getId(), "eventInvited", currentUserID);
        returnToHomepage();
    }

    //Set the profile picture of the event organizer/host
    private void setProfilePics(String creatorId) {
        userRepo.findById(creatorId, new RepoCallback<User>() {
            @Override
            public void onCallback(User user) {
                setHost(user.getName());
                Log.d(TAG, "Setting Event creator's Name");
                hostProfileImgView = findViewById(R.id.hostProfileImgView);
                ImageViewUtils.displayUserCirclePic(EventActivity.this, user, hostProfileImgView);
            }
        });
    }

    private void setCommenterPic(){
        commenterPic = findViewById(R.id.userPic);
        Log.d(TAG, "setCommenterPic: " +  (commenterPic == null));
        Log.d(TAG, "setCommenterPic: is called" + currentUserID);
        ImageViewUtils.displayUserCirclePicID(EventActivity.this, currentUserID, commenterPic);
    }





    //Sets-up the map to be displayed
    private void setMap() {

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
        //Add a pin at the location of the event
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

    //Set the event name
    private void setEventName(String name) {
        eventTitle = (TextView) findViewById(R.id.eventTitle);
        eventTitle.setText(name);
    }

    //Sets the event date
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

    //Sets the event address
    private void setAdress(String adress) {
        place = (TextView) findViewById(R.id.placeId);
        place.setText(adress);
    }

    //Sets the category of the event
    private void setCategory(String category) {
        eventType = (TextView) findViewById(R.id.categoryId);
        eventType.setText(WordUtils.capitalize(category.toLowerCase()));
    }

    //Sets the name of the host/organizer
    private void setHost(String hostName) {
        host = (TextView) findViewById(R.id.hosted_by);
        host.setText("Created by " + hostName);
    }

    //Adds the description
    private void setDescription(String description) {
        descriptionBox = findViewById(R.id.description_box);
        descriptionBox.setMovementMethod(new ScrollingMovementMethod());
        descriptionBox.setText(description);
    }

    //Sets the time fields
    private void seTime() {
        time = findViewById(R.id.time_from_to);
        time.setText(currentEvent.getStartTime() + " - " + currentEvent.getEndTime());
    }

    //Display the join or leave button depending on whether the user is part a participant or not
    private void setJoinLeaveButton() {
        joinLeaveBtn = (Button) findViewById(R.id.participateBtn);
        if (currentEvent.getEventParticipants().contains(currentUser.getId())) {
            joinLeaveBtn.setText("Leave");
        } else {
            joinLeaveBtn.setText("Join");
        }
    }

    //If the user is the organizer/admin of the event then the option to invite (button) other users is made available
    private void setEditInviteButton() {
        editBtn = findViewById(R.id.edit_event);
        inviteButton = findViewById(R.id.invite_button);
        //Check if the user is an administrator of the event.
        if (currentEvent.getEventAdmins().contains(currentUser.getId())) {
            //display the edit button
            editBtn.setVisibility(View.VISIBLE);
            //display the invite button
            inviteButton.setVisibility(View.VISIBLE);
        } else {
            editBtn.setVisibility(View.INVISIBLE);
            inviteButton.setVisibility(View.INVISIBLE);
        }
    }

    //Action to be taken when the join or leave button is clicked
    public void joinLeaveEventOnClick(View view) {
        int numParticipants = currentEvent.getEventParticipants().size();
        //Check if the button is currently join
        if (joinLeaveBtn.getText() == "Join") {
            //Update event object and database to add user in the list of participants
            currentEvent.getEventParticipants().add(currentUser.getId());
            eventRepo.addElement(currentEvent.getId(), "eventParticipants", currentUserID);
            //If the user was invited to the event, its id is removed from the invited list and the refuse invitation button is set to invisible
            if (currentEvent.getEventInvited().contains(currentUserID)) {
                eventRepo.removeElement(currentEvent.getId(), "eventInvited", currentUserID);
                refuseInvButton.setVisibility(View.INVISIBLE);
            }
            //Change the text if the button to leave
            joinLeaveBtn.setText("Leave");
            numParticipants++;

        } else {
            //If the button was leave the user is removed from the list of participants and database is updated accordingly
            currentEvent.getEventParticipants().remove(currentUser.getId());
            eventRepo.removeElement(currentEvent.getId(), "eventParticipants", currentUser.getId());
            //change the button to join so that user can jon back the event
            joinLeaveBtn.setText("Join");
            numParticipants--;
        }
        //Updates the text field with the number of participants attending
        if (numParticipants == 1 || numParticipants == 0) {
            numberOfParticipants.setText(numParticipants + " person is going");
        } else {
            numberOfParticipants.setText(numParticipants + " people are going");
        }
        //Updates the currentEvent with database
        eventRepo.findById(currentEvent.getId(), new RepoCallback<Event>() {
            @Override
            public void onCallback(Event event) {
                currentEvent = event;
                //Updates the participants pictures
                setParticipants();
            }
        });
    }

    //Displays pictures of some participants
    private void setParticipants() {
        //Retrieve the list of participants
        List<String> participants = currentEvent.getEventParticipants();
        int numParticipants = participants.size();
        Log.d(TAG, "Number of participants:" + numParticipants);
        if (numParticipants == 1 || numParticipants == 0) {
            numberOfParticipants.setText(numParticipants + " person is going");
        } else {
            numberOfParticipants.setText(numParticipants + " people are going");
        }
        //Display the correct number of participants pictures. If the event has more than 3 participants then 3 pictures are chosen at random among the different participants
        ImageView prof1 = findViewById(R.id.profile1);
        ImageView prof2 = findViewById(R.id.profile2);
        ImageView prof3 = findViewById(R.id.profile3);
        if (numParticipants == 0) {
            prof1.setVisibility(View.INVISIBLE);
            prof2.setVisibility(View.INVISIBLE);
            prof3.setVisibility(View.INVISIBLE);
        } else if (numParticipants == 1) {
            prof1.setVisibility(View.VISIBLE);
            prof2.setVisibility(View.INVISIBLE);
            prof3.setVisibility(View.INVISIBLE);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(0), prof1);
        } else if (numParticipants == 2) {
            prof1.setVisibility(View.VISIBLE);
            prof2.setVisibility(View.VISIBLE);
            prof3.setVisibility(View.INVISIBLE);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(0), prof1);
            ImageViewUtils.displayUserCirclePicID(this, participants.get(1), prof2);
        } else if (numParticipants == 3) {
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


    //Retrun to homepage if the phone back button is pressed

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    //If the edit button is clicked then the user and event objects are passed as intents to the CreateEventActivity
    public void editOnClick(View view) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("event", currentEvent);
        startActivity(intent);
    }

    private void setupToolbar() {
        ImageView profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(), profileImageView);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToHomepage();
            }
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToHomepage();
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

    private void setupRating(){
        eventRating = findViewById(R.id.score);
        scoreText = findViewById(R.id.starscore);

        ratingRepo.findByEventId(currentEvent.getId(), new RepoMultiCallback<Rating>() {
            @Override
            public void onCallback(ArrayList<Rating> models) {
                Log.d(TAG, "onCallback: setupRating " + models.size());

                if (models.size() == 0){
                    eventRating.setText("0.0");
                    scoreText.setText("0 Stars out of 5");
                }
                else {
                    String retrievedRating = new DecimalFormat("##.##").format(currentEvent.getRating()/models.size());
                    Log.d(TAG, "onCallback: setupRating " + currentEvent.getRating() + retrievedRating);
                    eventRating.setText(retrievedRating);
                    scoreText.setText(retrievedRating + " Stars out of 5");
                }
            }
        });

        ratingRepo.findByEventAndOwnerIds(currentEvent.getId(), currentUserID, new RepoMultiCallback<Rating>() {
            @Override
            public void onCallback(ArrayList<Rating> models) {
                if (!models.isEmpty()){
                    ratingBar.setRating(models.get(0).getValue());
                }
            }
        });

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

    private void handleRatings(){
        ratingBar = findViewById(R.id.ratingbar);
        eventRating = findViewById(R.id.score);
        scoreText = findViewById(R.id.starscore);


        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                float ratingvalue = (float) ratingBar.getRating();

                if (ratingRepo.existsDoc("ownerId", currentUserID)){
                    ratingRepo.findByEventAndOwnerIds(currentEvent.getId(), currentUserID, new RepoMultiCallback<Rating>() {
                        @Override
                        public void onCallback(ArrayList<Rating> models) {
                            float totalRatings = currentEvent.getRating();
                            Log.d(TAG, "onCallback: Ratind exists in Db: The query is successful");
                            //Log.d(TAG, "onCallback: Current event Id "+ currentEvent.getId());
                            //Log.d(TAG, "onCallback: Current user Id "+ currentUserID);
                            if (!models.isEmpty()){
                                ratingRepo.update(models.get(0).getId(), "value", ratingvalue);
                                totalRatings = totalRatings - models.get(0).getValue() + ratingBar.getRating();
                                currentEvent.setRating(totalRatings);
                                eventRepo.update(currentEvent.getId(), "rating", totalRatings);
                                setupRating();
                                //Toast.makeText(getApplicationContext(), " RetrievedID : " + models.get(0).getId()+ "", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                final Rating userRating = new Rating(currentUserID, currentEvent.getId(), ratingvalue);
                                ratingRepo.addWithoutId(userRating, new RepoCallback<String>() {
                                    @Override
                                    public void onCallback(String model) {
                                        float totalRatings = currentEvent.getRating();
                                        Log.d(TAG, "onCallback: Adding rating to DB");
                                        ratingRepo.update(model, "id", model);
                                        Log.d(TAG, "onCallback: Updating rating's id field");
                                        userRating.setId(model);
                                        totalRatings += ratingBar.getRating();
                                        currentEvent.setRating(totalRatings);
                                        eventRepo.update(currentEvent.getId(), "rating", totalRatings);
                                        setupRating();
                                    }
                                });
                            }
                        }
                    });
                }

            }
        });
    }

    private void returnToHomepage() {
        Intent intent = new Intent(EventActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
        finish();
    }

    //Pass the user and event as intents if invite button clicked
    public void inviteOnClick(View view) {
        Intent intent = new Intent(this, InviteActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("event", currentEvent);
        startActivity(intent);
    }

    //Sets the get direction button on click with an implicit intent so that the user can choose app of choice
    public void getDirextionsOnClick(View view) {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + currentEvent.getGpsLat() + "," + currentEvent.getGpsLong());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

}


