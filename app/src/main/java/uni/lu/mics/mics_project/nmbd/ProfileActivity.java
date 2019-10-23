package uni.lu.mics.mics_project.nmbd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    //reference to to the user signed in the database
    FirebaseUser firebaseUser;

    //reference to the database
    private DatabaseReference mDatabase;

    //Name Edit Text view
    EditText nameEdit;
    //Birthday calendar
    DatePickerDialog datePickerDialog;
    EditText dobEdit;
    //Password text
    EditText passwordEdit;
    EditText confirmPasswordEdit;
    TextView confirmPasswordTextView;



    //Reference to the user logged in
    User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialize firebaseUser to Auth user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize the different textEditViews
        nameEdit = findViewById(R.id.profile_activity_name_edit_view);
        dobEdit = findViewById(R.id.profile_activity_dob_edit);
        passwordEdit = findViewById(R.id.profile_activity_password_edit);
        confirmPasswordEdit = findViewById(R.id.profile_activity_confirmpassword_edit);
        confirmPasswordTextView = findViewById(R.id.profile_activity_confirmpassword_label);

        //Configures the date picker
        dobEdit.setInputType(InputType.TYPE_NULL);
        dobEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datePickerDialog = new DatePickerDialog(ProfileActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dobEdit.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });


        //TODO: initialize from intent
        currentUser = new User();

        //initializing the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //get the database reference corresponding to the auth user
        DatabaseReference nameRef = mDatabase.child("users").child(firebaseUser.getUid());
        nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //copy the user object from database to currentUser object
                currentUser = dataSnapshot.getValue(User.class);
                //Sets the text fields from user
                //TODO: once currentUser will be instantiated from intent this will need to be moved out of the addListener function
                nameEdit.setText(currentUser.getName());
                if(currentUser.getDateOfBirth()==null){
                    Date c = Calendar.getInstance().getTime();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
                    dobEdit.setText(df.format(c));
                } else{
                    dobEdit.setText(currentUser.getDateOfBirth());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }



    //Save data to database when save button pressed
    public void saveOnClick(View view) {
        //Updates the currentUser user object from the field
        String name = nameEdit.getText().toString();
        currentUser.setName(name);
        //Name needs to also be updated to FirebaseAuth
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                        }
                    }
                });
        //Updates date of birth of current User Object
        currentUser.setDateOfBirth(dobEdit.getText().toString());


        //Updates password if a new password has been entered
        //Stores the two passwords entered to be able to compare them as strings
        String newPassword = passwordEdit.getText().toString();
        String newConfirmPassword = confirmPasswordEdit.getText().toString();


        if(!TextUtils.isEmpty(newPassword) && newPassword.equals(newConfirmPassword)){
            //Rewerite the confirm password label in case it was changed because of not matching passwords
            confirmPasswordTextView.setText("Confirm Password:");
            confirmPasswordTextView.setTextColor(Color.BLACK);
            firebaseUser.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("****************", "User password updated.");
                            }
                        }
                    });

        } else if (!newPassword.equals(newConfirmPassword)) {
            //Changes the confirm password label to notify user that passwords do not match
            confirmPasswordTextView.setText("Passwords must match.");
            confirmPasswordTextView.setTextColor(Color.RED);
        }

        //TODO:update other fields of the username object


        //Updates the data base with the currentuser object
        mDatabase.child("users").child(firebaseUser.getUid()).setValue(currentUser);

        //Display Toast to confirm that data was saved
        Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show();
    }

    //Sends back to homepage with the user as extra of intent
    public void backOnclick(View view) {
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUserObject", currentUser);
        startActivity(intent);
    }


}
