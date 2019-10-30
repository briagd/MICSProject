package uni.lu.mics.mics_project.nmbd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.database.annotations.Nullable;

import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;
import uni.lu.mics.mics_project.nmbd.service.Authentification;
import uni.lu.mics.mics_project.nmbd.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.service.ServiceFactory;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 123;
    //TAG used for Log and debugging
    final String TAG = "Main Activity";
    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();
    ServiceFacade serviceFacade = new ServiceFacade(new ServiceFactory());
    Authentification authService = serviceFacade.authentificationService();


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
