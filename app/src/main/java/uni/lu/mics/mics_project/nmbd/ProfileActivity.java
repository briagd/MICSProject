package uni.lu.mics.mics_project.nmbd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    final String TAG = "ProfileActivity";

    //reference to to the user signed in the database
    private FirebaseUser firebaseUser;

    //reference to the database
    private FirebaseFirestore mDatabase;

    //Name Edit Text view
    private EditText nameEdit;
    //Birthday calendar
    private DatePickerDialog datePickerDialog;
    private EditText dobEdit;
    //Password text
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;
    private TextView confirmPasswordTextView;
    //Profile pic
    private static final int PICK_IMAGE_REQUEST = 1;
    private Button chooseImageButton;
    private ImageView profileImageView;
    private Uri imageUri;


    //Reference to the user logged in
            private User currentUser;

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
        chooseImageButton = findViewById(R.id.profile_activity_choose_image_button);
        profileImageView = findViewById(R.id.profile_activity_profile_picture_view);

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


        //TODO: initialize from intent rather than from database
        currentUser = new User();

        //initializing the database
        mDatabase = FirebaseFirestore.getInstance();
        //get the database reference corresponding to the auth user
        DocumentReference docRef = mDatabase.collection("users").document(firebaseUser.getUid());

        //Retrieves the user info from database
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "User retrieved from database");
                        //Updates the currentuser Object from database info
                        currentUser = document.toObject(User.class);

                        nameEdit.setText(currentUser.getName());
                        if(currentUser.getDateOfBirth()==null){
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
                            dobEdit.setText(df.format(c));
                        } else{
                            dobEdit.setText(currentUser.getDateOfBirth());
                        }

                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });

        //updates the textviews according to user
        //Log.d(TAG, currentUser.getName());


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
        mDatabase.collection("users").document(firebaseUser.getUid()).set(currentUser);

        //Display Toast to confirm that data was saved
        Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show();
    }

    //Sends back to homepage with the user as extra of intent
    public void backOnclick(View view) {
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUserObject", currentUser);
        startActivity(intent);
    }


    public void chooseImageOnClick(View view) {
        openImageChooser();
    }


    private void openImageChooser(){
        Intent intent = new Intent();
        //Opens the images saved on the phone to choose a profile picture
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Sets the image view to the image chosen when intent is received
        if (requestCode == PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data!=null && data.getData()!= null){
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    //TODO: save picture to storage and retrieve image when opening the page
}
