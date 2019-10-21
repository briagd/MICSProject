package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int MY_REQUEST_CODE = 123;
    List<AuthUI.IdpConfig> providers;

    //reference to the database
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        //list of providers accepted for signing up
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                //new AuthUI.IdpConfig.PhoneBuilder().build(),
                //new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                //new AuthUI.IdpConfig.TwitterBuilder().build()
        );
        showSignInOptions();
    }

    private void showSignInOptions() {
        //TODO: Customize the login page with logo etc
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.MyTheme)
                        //.setLogo(R.drawable.firebase)
                        .build(), MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                //If user does not exists then add user to database
                DatabaseReference uidRef = mDatabase.child("users").child(user.getUid());
                ValueEventListener eventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {
                            //create new user object to be added to the database
                            User u = new User();
                            u.setName(user.getDisplayName());
                            u.setEmail(user.getEmail());
                            mDatabase.child("users").child(user.getUid()).setValue(u);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("DatabaseError", databaseError.getMessage()); //Don't ignore errors!
                    }
                };
                uidRef.addListenerForSingleValueEvent(eventListener);


                //go to homepage activity
                startActivity(new Intent(this, HomepageActivity.class));


                finish();

                //display toast if correctly signed in
                Toast.makeText(this, "Welcome! " + user.getDisplayName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
