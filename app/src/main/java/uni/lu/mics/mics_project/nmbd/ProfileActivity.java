package uni.lu.mics.mics_project.nmbd;

import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.service.Authentification;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    final String TAG = "ProfileActivity";

    //reference to to the user signed in the database
    //private FirebaseUser firebaseUser;
    private Authentification auth;
    //reference to the firestore database
    private FirebaseFirestore mDatabase;
    //Reference to the storage
    private StorageReference mStorageRef;

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
    private Button uploadPicButton;
    private ImageView profileImageView;
    private Uri imageUri;
    private ProgressBar uploadProgressBar;


    //Reference to the user logged in
    private User currentUser;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        //initialize firebaseUser to Auth user

        auth = new Authentification();
        //TODO should get from user intent once uid have been set as a field
        currentUserID = auth.getAuthUid();

        // Initialize the different textEditViews
        nameEdit = findViewById(R.id.profile_activity_name_edit_view);
        dobEdit = findViewById(R.id.profile_activity_dob_edit);
        passwordEdit = findViewById(R.id.profile_activity_password_edit);
        confirmPasswordEdit = findViewById(R.id.profile_activity_confirmpassword_edit);
        confirmPasswordTextView = findViewById(R.id.profile_activity_confirmpassword_label);
        chooseImageButton = findViewById(R.id.profile_activity_choose_image_button);
        profileImageView = findViewById(R.id.profile_activity_profile_picture_view);
        uploadPicButton = findViewById(R.id.profile_activity_upload_picture_button);
        uploadProgressBar = findViewById(R.id.profile_activity_upload_progressbar);
        //Set Upload button and upload progress bar to invisible as no picture has been chosen
        uploadPicButton.setVisibility(View.INVISIBLE);
        uploadProgressBar.setVisibility(View.INVISIBLE);


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
                                String dateOfB = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                dobEdit.setText(dateOfB);

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });




        //initializing the database
        mDatabase = FirebaseFirestore.getInstance();
        //get the database reference corresponding to the auth user
        DocumentReference docRef = mDatabase.collection("users").document(currentUserID);

        //Retrieves the user info from database

        nameEdit.setText(currentUser.getName());

        if (currentUser.getDateOfBirth() == null) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
            dobEdit.setText(df.format(c));
        } else {
            dobEdit.setText(currentUser.getDateOfBirth());
        }

        //Initialize Storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();


    }



    //Save data to database when save button pressed
    public void saveOnClick(View view) {
        //Updates the currentUser user object from the field
        String name = nameEdit.getText().toString();
        currentUser.setName(name);
        //Name needs to also be updated to FirebaseAuth - Probably not necessary
//        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                .setDisplayName(name)
//                .build();
//        firebaseUser.updateProfile(profileUpdates)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//
//                        }
//                    }
//                });
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
            auth.updatePassword(newPassword);

        } else if (!newPassword.equals(newConfirmPassword)) {
            Log.d(TAG, "Passwords do not match!");
            //Changes the confirm password label to notify user that passwords do not match
            confirmPasswordTextView.setText("Passwords must match.");
            confirmPasswordTextView.setTextColor(Color.RED);
        }

        //TODO:update other fields of the username object


        //Updates the data base with the currentuser object
        mDatabase.collection("users").document(currentUserID).set(currentUser);

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


    @SuppressLint("IntentReset")
    private void openImageChooser(){
        //TODO: Get photos also from camera
        //Opens the images from Gallery saved on the phone to choose a profile picture
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Sets the image view to the image chosen when intent is received
        if (requestCode == PICK_IMAGE_REQUEST && resultCode ==RESULT_OK && data!=null && data.getData()!= null){
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            //Make the upload button visible
            uploadPicButton.setVisibility(View.VISIBLE);
        }
    }



    //
    public void uploadPictureOnClick(View view) {
        //Hides button so no attempt to upload multiple times possible
        uploadPicButton.setVisibility(View.INVISIBLE);
        uploadFile();
    }

    //Method to get the file extension as string
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(imageUri!=null){
            //TODO: if there is an existing profile pic, it should be removed from storage
            //TODO: Compress picture before uploading


            //Creates a reference for the file to store
            final StorageReference fileReference = mStorageRef.child("profilePics/" + currentUserID + "." + getFileExtension(imageUri));

            //uploads file to firestore
            fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "Profile picture upload successful");
                    //Hides progress bar and upload button
                    uploadProgressBar.setVisibility(View.INVISIBLE);

                    //Displays toast on success
                    Toast.makeText(ProfileActivity.this, "Profile Picture updated", Toast.LENGTH_LONG).show();

                    //Upload the file ProfilePicUrl information to the database
                    String picUrl = fileReference.getDownloadUrl().toString();
                    currentUser.setProfilePicUrl(picUrl);
                    mDatabase.collection("users").document(currentUserID).set(currentUser);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.getMessage());
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    //Shows the progress bar
                    uploadProgressBar.setVisibility(View.VISIBLE);
//
                }
            });
        } else{
            Toast.makeText(this, "No profile picture selected", Toast.LENGTH_SHORT ).show();
        }
    }


}
