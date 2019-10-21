package uni.lu.mics.mics_project.nmbd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

public class HomepageActivity extends AppCompatActivity {

    Button btn_sign_out;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);btn_sign_out = findViewById(R.id.sign_out_button);
        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance()
                        .signOut(HomepageActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                signOut();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(HomepageActivity.this, ""+ e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void signOut() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void createEventOnClick(View view) {
        Intent intent = new Intent(this, CreateEventActivity.class);
        startActivity(intent);
    }

    public void viewEventOnClick(View view) {
        Intent intent = new Intent(this, EventActivity.class);
        startActivity(intent);
    }

    public void viewFriendsOnClick(View view) {
        Intent intent = new Intent(this, FriendsActivity.class);
        startActivity(intent);
    }

    public void inviteFriendOnClick(View view) {
        Intent intent = new Intent(this, InviteFriendsActivity.class);
        startActivity(intent);
    }

    public void viewProfileOnClick(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
}
