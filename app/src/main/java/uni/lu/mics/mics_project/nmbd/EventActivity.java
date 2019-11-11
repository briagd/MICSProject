package uni.lu.mics.mics_project.nmbd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.lang3.text.WordUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
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
    private User currentUser;
    private String currentUserID;
    private Event currentEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        userRepo = globalState.getRepoFacade().userRepo();
        imgView = findViewById(R.id.eventImageBtn);
//        Retrieve Intent and get current user and event
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();
        currentEvent =  (Event) intent.getSerializableExtra("currentEvent");
//        Check if there is an image uri,this would happen if the event was just created and the picture is still being
//        uploaded
//        , otherwise display the picture from the eventID.
//        imgView: is the imageView object where you want the picture to be displayed


        if(intent.hasExtra("image")){
            Uri imageUri = Uri.parse(intent.getStringExtra("image"));

            ImageViewUtils.displayPicUri(this,imageUri,imgView);
        } else {
            ImageViewUtils.displayEventPicID(this, currentEvent.getId(), imgView);
        }








                setEventName(currentEvent.getName());
                setDate(currentEvent.getDate());
                setAdress(currentEvent.getEventAddress());
                setCategory(currentEvent.getCategory());
                String creatorId = currentEvent.getCreator();
                Log.d(TAG, "Listing Users");
                userRepo.list(new RepoMultiCallback<User>() {
                    @Override
                    public void onCallback(ArrayList<User> users) {
                        for (User u : users) {
                            Log.d(TAG, "listing user: " + u.getId());
                            Toast.makeText(EventActivity.this, u.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                userRepo.findById(creatorId, new RepoCallback<User>() {
                    @Override
                    public void onCallback(User user) {
                        host=(TextView)findViewById(R.id.hosted_by);
                        host.setText(user.getName());
                        Log.d(TAG, "I am finding the user");
                        //setHost("Hosted by " + model.getName());
                        //Toast.makeText(EventActivity.this, model.getName(), Toast.LENGTH_LONG).show();
                    }
            });

        Toast.makeText(EventActivity.this, currentEvent.getId(), Toast.LENGTH_LONG).show();
    }

    private void setEventName(String name){
        eventTitle = (TextView) findViewById(R.id.eventTitle);
        eventTitle = (TextView) findViewById(R.id.eventTitle);
        eventTitle.setText(name);
    }

    private void setDate(String dateString){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatterToStr = new SimpleDateFormat("EEEE, MMMM dd");
        dateField = (TextView)findViewById(R.id.day_monthId);
        try {
            Date date = formatter.parse(dateString);
            dateField.setText(formatterToStr.format(date));
        }catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void setAdress(String adress){
        place = (TextView)findViewById(R.id.placeId);
        place.setText(adress);
    }

    private  void setCategory(String category){
        eventType = (TextView) findViewById(R.id.categoryId);
        eventType.setText(WordUtils.capitalize(category.toLowerCase()));
    }

    private void setHost(String hostName){
        host = (TextView)findViewById(R.id.hosted_by);
        host.setText(hostName);

    }

//         Uncomment to be able to pass user as intent when back button is pressed
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }


}


