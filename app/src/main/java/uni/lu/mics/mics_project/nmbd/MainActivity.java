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

        //Check if a user is already signed in, if not go to sign in page
        if (authService.isUserSignedIn()) {
            Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
            //TODO retrieve current user from database and put as extra
            String currentUserUid = authService.getAuthUid();
            //intent.putExtra("currentUser", currentUser);
            //go to homepage activity
            startActivity(intent);
            Toast.makeText(this, "Welcome back " + authService.getAuthDisplayName(), Toast.LENGTH_LONG).show();
        } else {
            startActivityForResult(authService.createSignInIntent(), MY_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final MainActivity self = this;
                userRepo.findById(authService.getAuthUid(), new RepoCallback<User>() {
                    @Override
                    public void onCallback(User model) {
                        if (model == null) {
                            model = new User(authService.getAuthUid(), authService.getAuthEmail(), authService.getAuthDisplayName());
                            userRepo.add(model);
                            Log.d(TAG, "User added to database");
                        }
                        Intent intent = new Intent(MainActivity.this, HomepageActivity.class);
                        //adds the currentUser object as extra to the intent to be retrieved
                        intent.putExtra("currentUser", model);
                        //go to homepage activity
                        startActivity(intent);
                        Toast.makeText(self, "Welcome " + model.getName(), Toast.LENGTH_LONG).show();
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
