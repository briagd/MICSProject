package uni.lu.mics.mics_project.nmbd;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
import uni.lu.mics.mics_project.nmbd.app.service.StorageUploadCallback;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class ProfileActivity extends AppCompatActivity {

    final String TAG = "ProfileActivity";

    AppGlobalState globalState;
    UserRepository userRepo;
    Authentification authService;
    Storage storageService;

    //Name Edit Text view
    private EditText nameEdit;
    private Button saveNameButton;
    //Birthday calendar
    private DatePickerDialog datePickerDialog;
    private EditText dobEdit;
    private Button saveDobButton;
    //Password text
    private EditText passwordEdit;
    private EditText confirmPasswordEdit;
    private TextView confirmPasswordTextView;
    private Button savePasswordButton;
    //Profile pic
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView thmbProfileImageView;
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

        globalState = (AppGlobalState) getApplicationContext();
        userRepo = globalState.getRepoFacade().userRepo();
        authService = globalState.getServiceFacade().authentificationService();
        storageService = globalState.getServiceFacade().storageService();


        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();

        setNameFields();
        setDobFields();
        setPasswordFields();
        setPicFields();

        displayProfilePic();
    }

    private void setPicFields() {
        thmbProfileImageView = findViewById(R.id.profile_activity_thmb_imageView);
        profileImageView = findViewById(R.id.profile_activity_profile_picture_view);
        uploadPicButton = findViewById(R.id.profile_activity_upload_picture_button);
        uploadProgressBar = findViewById(R.id.profile_activity_upload_progressbar);
        //Set Upload button and upload progress bar to invisible as no picture has been chosen
        uploadPicButton.setVisibility(View.INVISIBLE);
        uploadProgressBar.setVisibility(View.INVISIBLE);
        profileImageView.setVisibility(View.VISIBLE);
    }


    public void setNameFields(){
        nameEdit = findViewById(R.id.profile_activity_name_edit_view);
        saveNameButton = findViewById(R.id.profile_activity_name_button);
        saveNameButton.setVisibility(View.INVISIBLE);
        nameEdit.setText(currentUser.getName());
        nameEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If the name has been changed then the save button is made visible
                saveNameButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public void saveNameOnclick(View view) {
        //Updates the currentUser user object from the field
        String name = nameEdit.getText().toString();
        currentUser.setName(name);
        //Updates the data base with the currentuser object
        userRepo.set(currentUserID,currentUser);
        saveNameButton.setVisibility(View.INVISIBLE);
        //Display Toast to confirm that data was saved
        Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
    }

    public void setDobFields(){
        //Configures the date picker
        saveDobButton = findViewById(R.id.profile_activity_dob_button);
        saveDobButton.setVisibility(View.INVISIBLE);
        dobEdit = findViewById(R.id.profile_activity_dob_edit);
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
                                saveDobButton.setVisibility(View.VISIBLE);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        if (currentUser.getDateOfBirth() == null) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy");
            dobEdit.setText(df.format(c));
        } else {
            dobEdit.setText(currentUser.getDateOfBirth());
        }
    }

    public void saveDobOnClick(View view) {
        String dob = dobEdit.getText().toString();
        currentUser.setDateOfBirth(dob);
        userRepo.update(currentUserID,"dateOfBirth", dob);
        saveDobButton.setVisibility(View.INVISIBLE);
        Toast.makeText(this, "Birthday updated", Toast.LENGTH_SHORT).show();
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
        //Check if text has been entered in the password field
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
                    //If passwords match then the save button is displayed
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
        //check that passwords match
        if(!newPassword.matches("") && newPassword.equals(newConfirmPassword)) {
            //updates passwords on the database
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

    private void displayProfilePic() {
        String currentUserProfilePicUrl = currentUser.getProfilePicUrl();
        final String gsUrl = this.getString(R.string.gsTb64ProfPicUrl);
        storageService.getStorageReference(gsUrl, currentUserProfilePicUrl, new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                ImageViewUtils.displayCirclePic(ProfileActivity.this, storageReference, thmbProfileImageView);
                Log.d(TAG, "User profile picture correctly retrieved");
            }
            @Override
            public void onFailure() {
                ImageViewUtils.displayCircleAvatarPic(ProfileActivity.this, storageService, thmbProfileImageView);
            }
        });
    }

    public void chooseImageOnClick(View view) {
        openImageChooser();
    }

    @SuppressLint("IntentReset")
    private void openImageChooser(){
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
            profileImageView.setVisibility(View.VISIBLE);
            profileImageView.setImageURI(imageUri);
            //Make the upload button visible
            uploadPicButton.setVisibility(View.VISIBLE);
        }
    }

    public void uploadPictureOnClick(View view) {
        //Hides button so no attempt to upload multiple times possible
        uploadPicButton.setVisibility(View.INVISIBLE);
        uploadFile();
    }

    private void uploadFile(){
        if(imageUri!=null) {
            storageService.uploadPic(this, imageUri, this.getString(R.string.gsProfilePicsStrgFldr), currentUserID, new StorageUploadCallback() {
                @Override
                public void onProgress() {
                    uploadProgressBar.setVisibility(View.VISIBLE);
                }
                @Override
                public void onSuccess(StorageReference storageReference, String filename) {
                    uploadProgressBar.setVisibility(View.INVISIBLE);
                    //Displays toast on success
                    Toast.makeText(ProfileActivity.this, "Profile Picture updated", Toast.LENGTH_LONG).show();
                    //If the new picture has a different filename than the previous one it will not be replaced so it should be deleted
                    if (currentUser.getProfilePicUrl()!=null && !filename.equals(currentUser.getProfilePicUrl())){
                        storageService.deleteFile(ProfileActivity.this.getString(R.string.gsProfilePicsStrgFldr), currentUser.getProfilePicUrl());
                    }
                    //Hide the imageview to display the image chosen
                    profileImageView.setVisibility(View.INVISIBLE);
                    //updates current user and repo
                    currentUser.setProfilePicUrl(filename);
                    userRepo.update(currentUserID, "profilePicUrl", filename);
                    //Updates the profile pic displayer
                    displayProfilePic();
                }
                @Override
                public void onFailure() {
                    Log.d(TAG, "Upload failed");
                }
            });
        }
    }
}
