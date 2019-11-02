package uni.lu.mics.mics_project.nmbd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.database.annotations.Nullable;

import java.io.Serializable;

import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;


public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 123;
    //TAG used for Log and debugging
    private final String TAG = "Main Activity";
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                final String authId = authService.getAuthUid();
                userRepo.findById(authId, new RepoCallback<User>() {
                    @Override
                    public void onCallback(User model) {
                        if (model == null) {
                            model = new User(authId, authService.getAuthEmail(), authService.getAuthDisplayName());
                            userRepo.set(authId, model);
                            Log.d(TAG, "User added to database");
                        }
                        Intent intent = getIntent(model);
                        //go to homepage activity
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Welcome " + model.getName(), Toast.LENGTH_LONG).show();
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
