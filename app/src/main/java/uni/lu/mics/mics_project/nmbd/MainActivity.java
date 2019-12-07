package uni.lu.mics.mics_project.nmbd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.database.annotations.Nullable;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 123;
    //TAG used for Log and debugging
    private final String TAG = "Main Activity";

    AppGlobalState globalState;
    UserRepository userRepo;
    Authentification authService;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Retrieve the global variables for the database
        globalState = (AppGlobalState) getApplicationContext();
        userRepo = globalState.getRepoFacade().userRepo();
        authService = globalState.getServiceFacade().authentificationService();


        //Check if a user is already signed in, if not go to sign in page
        if (authService.isUserSignedIn()) {
            userRepo.findById(authService.getAuthUid(), new RepoCallback<User>() {
                @Override
                public void onCallback(User model) {
                    Toast.makeText(MainActivity.this, "Welcome back " + model.getName(), Toast.LENGTH_LONG).show();
                    Intent intent = getIntent(model);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            startActivityForResult(authService.createSignInIntent(), MY_REQUEST_CODE);
        }
    }

    //Service result if user correctly signed-in
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                //Get the id of user from authorization database
                final String authId = authService.getAuthUid();
                userRepo.findById(authId, new RepoCallback<User>() {
                    @Override
                    public void onCallback(User user) {
                        if (user == null) {
                            //if the user did not exist in the database (new user) the a new user is added to the database with corresponding email and name
                            user = new User(authId, authService.getAuthEmail(), authService.getAuthDisplayName());
                            userRepo.set(authId, user);
                            Log.d(TAG, "User added to database");
                        }
                        Intent intent = getIntent(user);
                        //go to homepage activity
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Welcome " + user.getName(), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private Intent getIntent(User user) {
        Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", user);
        return intent;
    }
}
