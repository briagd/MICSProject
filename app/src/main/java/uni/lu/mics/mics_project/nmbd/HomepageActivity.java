package uni.lu.mics.mics_project.nmbd;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.domain.model.User;

public class HomepageActivity extends AppCompatActivity {

    AppGlobalState globalState;
    Authentification auth;
    Button btn_sign_out;
    //currentUser object retrieved from intent
    User currentUser;

    final String TAG = "HomepageActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        globalState = (AppGlobalState) getApplicationContext();
        auth = globalState.getServiceFacade().authentificationService();
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        if(currentUser!=null){
            Log.d(TAG, currentUser.getName());
        }


        btn_sign_out = findViewById(R.id.sign_out_button);

        //Sets up the sign out button to take action if pressed
        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut(HomepageActivity.this, MainActivity.class);
            }
        });

        //Create the Notification channel to be able to display notification
        createNotificationChannel();
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

    public void inviteFriendOnClick(View view) {
        Intent intent = createIntent(SandboxActivity.class, currentUser);
        startActivity(intent);
    }

    public void viewProfileOnClick(View view) {
        Intent intent = createIntent(ProfileActivity.class, currentUser);
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
