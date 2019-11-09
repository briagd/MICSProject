package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
//        Retrieve Intent and get current user and event
//        Intent intent = getIntent();
//        currentUser = (User) intent.getSerializableExtra("currentUser");
//        currentUserID = currentUser.getId();
//        currentEvent =  (Event) intent.getSerializableExtra("currentEvent");
//        Check if there is an image uri,this would happen if the event was just created and the picture is still being
//        uploaded
//        , otherwise display the picture from the eventID.
//        imgView: is the imageView object where you want the picture to be displayed
//        if(intent.hasExtra("image")){
//            Uri imageUri = Uri.parse(intent.getStringExtra("image"));
//
//            ImageViewUtils.displayPicUri(this,imageUri,imgView);
//        } else {
//            ImageViewUtils.displayEventPicID(this, currentEvent.getEventId(), imgView);
//        }

    }


//     Uncomment to be able to pass user as intent when back button is pressed
//    @Override
//    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed Called");
//        Intent intent = new Intent(this, HomepageActivity.class);
//        intent.putExtra("currentUser", currentUser);
//        startActivity(intent);
//    }
}
