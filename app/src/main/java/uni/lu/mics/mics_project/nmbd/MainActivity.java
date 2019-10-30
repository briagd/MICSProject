package uni.lu.mics.mics_project.nmbd;

import uni.lu.mics.mics_project.nmbd.domain.model.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //TAG used for Log and debugging
    final String TAG = "Main Activity";

    private static final int MY_REQUEST_CODE = 123;
    Authentification auth;

    //reference to the database
    private FirebaseFirestore mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing the database
        mDatabase = FirebaseFirestore.getInstance();
        auth = new Authentification();

        //Check if a user is already signed in, if not go to sign in page
        if (auth.isUserSignedIn()){
            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
            //TODO retrieve current user from database and put as extra
            String currentUserUid = auth.getAuthUid();

            //intent.putExtra("currentUser", currentUser);
            //go to homepage activity
            startActivity(intent);
            Toast.makeText(this, "Welcome back " + auth.getAuthDisplayName(), Toast.LENGTH_LONG).show();
        } else {
            startActivityForResult(auth.createSignInIntent(), MY_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {

                //Instantiate a user object for the current user to be initialized from database and passed as intent to next activity
                final User currentUser = new User();
                //Creates a reference for the id of users
                DocumentReference usersRef = mDatabase.collection("users").document(auth.getAuthUid());

                //Check if user already exists in the database
                usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "User retrieved from database");
                                //Updates the currentuser Object from database info
                                //TODO: Check if all fields are correct
                                currentUser.setName(document.get("name").toString());
                                currentUser.setEmail(document.get("email").toString());
                                currentUser.setAge(Integer.parseInt(document.get("age").toString()));

                                if(document.get("username")!=null) {
                                    currentUser.setUsername(document.get("username").toString());
                                }

                            } else {
                                //If user does not exists then add user to database
                                currentUser.setName(auth.getAuthDisplayName());
                                currentUser.setEmail(auth.getAuthEmail());
                                String uid = auth.getAuthUid();
                                currentUser.setUserId(uid);
                                mDatabase.collection("users").document(uid).set(currentUser);
                                Log.d(TAG, "User added to database");
                            }
                            //Creates intent
                            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                            //adds the currentUser object as extra to the intent to be retrieved
                            intent.putExtra("currentUser", currentUser);
                            //go to homepage activity
                            startActivity(intent);

                            finish();

                        } else {
                            Log.d(TAG, "Failed with: ", task.getException());
                        }
                    }
                });



                //display toast if correctly signed in
                Toast.makeText(this, "Welcome " + currentUser.getName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private Intent getIntent(User user){
        Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", user);
        return intent;
    }
}
