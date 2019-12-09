package uni.lu.mics.mics_project.nmbd;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.text.WordUtils;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class EventActivity extends AppCompatActivity {

    final String TAG = "EventActivity";

    AppGlobalState globalState;
    EventRepository eventRepo;
    UserRepository userRepo;

    private ImageButton imgView;
    private TextView eventTitle;
    private TextView dateField;
    private TextView place;
    private TextView eventType;
    private TextView host;
    private TextView numberOfParticipants;
    private TextView descriptionBox;
    private Button joinLeaveBtn;
    private Button editBtn;
    private TextView time;
    private User currentUser;
    private String currentUserID;
    private Event currentEvent;
    private Button inviteButton;
    private ImageView hostProfileImgView;
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


