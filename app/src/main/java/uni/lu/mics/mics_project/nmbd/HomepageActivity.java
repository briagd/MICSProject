package uni.lu.mics.mics_project.nmbd;

import uni.lu.mics.mics_project.nmbd.domain.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public class HomepageActivity extends AppCompatActivity {


    Button btn_sign_out;
    //currentUser object retrieved from intent
    User currentUser;
    Authentification auth;

    final String TAG = "HomepageActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        auth = new Authentification();
        //retrieves intent
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
        Intent intent = createIntent(InviteFriendsActivity.class, currentUser);
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
}
