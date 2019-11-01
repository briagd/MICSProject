package uni.lu.mics.mics_project.nmbd;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.HashMap;

import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class ProfileActivity extends AppCompatActivity {

    final String TAG = "ProfileActivity";

    //reference to to the user signed in the database
    //private FirebaseUser firebaseUser;

    //reference to the firestore database
    //private FirebaseFirestore mDatabase;

    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();
    ServiceFacade serviceFacade = new ServiceFacade(new ServiceFactory());
    Authentification authService = serviceFacade.authentificationService();
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
    private Button savePasswordButton;
    //Profile pic
    private static final int PICK_IMAGE_REQUEST = 1;
//    private Button chooseImageButton;
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
        currentUserID = currentUser.getId();

        // Initialize the different textEditViews
        nameEdit = findViewById(R.id.profile_activity_name_edit_view);
        dobEdit = findViewById(R.id.profile_activity_dob_edit);

        //chooseImageButton = findViewById(R.id.profile_activity_choose_image_button);
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


        //Retrieves the user info from database
        nameEdit.setText(currentUser.getName());

        if (currentUser.getDateOfBirth() == null) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
            dobEdit.setText(df.format(c));
        } else {
            dobEdit.setText(currentUser.getDateOfBirth());
        }

        setPasswordFields();

        //Initialize Storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();

    }



    //Save data to database when save button pressed
    public void saveOnClick(View view) {
        //Updates the currentUser user object from the field
        String name = nameEdit.getText().toString();
        currentUser.setName(name);
        currentUser.setDateOfBirth(dobEdit.getText().toString());
        //Updates the data base with the currentuser object
        userRepo.set(currentUserID,currentUser);

        //Display Toast to confirm that data was saved
        Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show();
    }



    //Sends back to homepage with the user as extra of intent
    public void backOnclick(View view) {
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    public void setPasswordFields(){
        passwordEdit = findViewById(R.id.profile_activity_password_edit);
        confirmPasswordEdit = findViewById(R.id.profile_activity_confirmpassword_edit);
        confirmPasswordTextView = findViewById(R.id.profile_activity_confirmpassword_label);
        savePasswordButton = findViewById(R.id.profile_activity_save_password_button);
        savePasswordButton.setVisibility(View.INVISIBLE);
        confirmPasswordEdit.setVisibility(View.INVISIBLE);
        confirmPasswordTextView.setVisibility(View.INVISIBLE);
        passwordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordTextView.setVisibility(View.VISIBLE);
                confirmPasswordEdit.setVisibility(View.VISIBLE);
                String newPassword = s.toString();
                String newConfirmPassword = confirmPasswordEdit.getText().toString();
                if (!newPassword.equals(newConfirmPassword)) {
                    Log.d(TAG, "Passwords do not match!");
                    //Changes the confirm password label to notify user that passwords do not match
                    confirmPasswordTextView.setText("Passwords must match");
                    confirmPasswordTextView.setTextColor(Color.RED);
                    savePasswordButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
        confirmPasswordEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newPassword = passwordEdit.getText().toString();
                String newConfirmPassword = s.toString();

                if (!newPassword.equals(newConfirmPassword)) {
                    Log.d(TAG, "Passwords do not match!");
                    //Changes the confirm password label to notify user that passwords do not match
                    confirmPasswordTextView.setText("Passwords must match");
                    confirmPasswordTextView.setTextColor(Color.RED);
                } else {
                    confirmPasswordTextView.setText("Passwords match");
                    confirmPasswordTextView.setTextColor(Color.BLACK);
                    savePasswordButton.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }


    public void savePasswordOnClick(View view) {

        String newPassword = passwordEdit.getText().toString();
        String newConfirmPassword = confirmPasswordEdit.getText().toString();

        if(!newPassword.matches("") && newPassword.equals(newConfirmPassword)) {
            authService.updatePassword(newPassword);
            passwordEdit.getText().clear();
            confirmPasswordEdit.getText().clear();
            confirmPasswordTextView.setVisibility(View.INVISIBLE);
            confirmPasswordEdit.setVisibility(View.INVISIBLE);
            savePasswordButton.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Password updated", Toast.LENGTH_LONG).show();
            Log.d(TAG, "Password updated");
        }
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

                    userRepo.update(currentUserID, "profilePicUrl", picUrl);
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
