package uni.lu.mics.mics_project.nmbd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadConstants;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadStartIntentService;
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
    private Uri imageUri;
    //Reference to the user logged in
    private User currentUser;
    private String currentUserID;

    //Receiver from UploadServiceIntent
    private UploadResultReceiver mUpldRessultReceiver;

    //Variables for selecting picture
    private String currentPhotoPath;
    private final int PICFROMGALLERY = 1;
    private final int PICFROMCAMERA = 0;

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

        //Instantiate the receiver for the upload service
        mUpldRessultReceiver = new UploadResultReceiver(new Handler());


        setNameFields();
        setDobFields();
        setPasswordFields();
        setPicFields();

        displayProfilePic();
    }

    private void setPicFields() {
        thmbProfileImageView = findViewById(R.id.profile_activity_thmb_imageView);
        uploadPicButton = findViewById(R.id.profile_activity_upload_picture_button);
        //Set Upload button and upload progress bar to invisible as no picture has been chosen
        uploadPicButton.setVisibility(View.INVISIBLE);
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

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
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

    public void pictureOnClick(View view) {
        selectImage();
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if(resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_OK ) {
                switch (reqCode) {
                    case PICFROMCAMERA:
                        File file = new File(currentPhotoPath);
                        imageUri  = Uri.fromFile(file);
                        break;
                    case PICFROMGALLERY:
                        imageUri = data.getData();
                        break;
                }
                uploadPicButton.setVisibility(View.VISIBLE);
                ImageViewUtils.displayCirclePicUri(this, imageUri, thmbProfileImageView);
            }else {
                Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void selectImage() {
        //Check if camera permission has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            final CharSequence[] options = {"Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose your profile picture");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, PICFROMGALLERY);

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }else {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Choose your profile picture");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (options[item].equals("Take Photo")) {
                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            File image = null;
                            try {
                                image = createImageFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (image != null) {
                                Uri photoURI = FileProvider.getUriForFile(ProfileActivity.this,
                                        "com.example.android.fileprovider",
                                        image);
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                startActivityForResult(takePictureIntent, PICFROMCAMERA);
                            }
                        }
                    } else if (options[item].equals("Choose from Gallery")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, PICFROMGALLERY);

                    } else if (options[item].equals("Cancel")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }
    }
    //Create a file when a picture with a unique name to be stored locally
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new android.icu.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public void uploadPictureOnClick(View view) {
        //Hides button so no attempt to upload multiple times possible
        uploadPicButton.setVisibility(View.INVISIBLE);
        uploadFile();
    }


    private void displayProfilePicAfterUpload(){
        if (imageUri!=null) {
            ImageViewUtils.displayCirclePicUri(ProfileActivity.this, imageUri, thmbProfileImageView);
        } else{
            displayProfilePic();
        }
    }

    private void displayProfilePic() {
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),thmbProfileImageView );
    }

    private void uploadFile(){
        if(imageUri!=null) {
            UploadStartIntentService.startIntentService(this, mUpldRessultReceiver, imageUri,
                    getString(R.string.gsProfilePicsStrgFldr), currentUserID, UploadConstants.PROFILE_TYPE);

        }
    }



    //Intent service to upload file
    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private class UploadResultReceiver extends ResultReceiver {
        UploadResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            //UI things to complete when service intent completed
            displayProfilePicAfterUpload();
            String filename = resultData.getString(UploadConstants.FILE_NAME);
            //If the new picture has a different filename than the previous one it will not be replaced so it should be deleted
            if (currentUser.getProfilePicUrl()!=null && !filename.equals(currentUser.getProfilePicUrl())
            && !filename.equals("eventzy_user.png") ){
                storageService.deleteFile(ProfileActivity.this.getString(R.string.gsProfilePicsStrgFldr), currentUser.getProfilePicUrl());
                storageService.deleteFile(ProfileActivity.this.getString(R.string.gsProfilePicsStrgFldrtb256), currentUser.getProfilePicUrl());
                storageService.deleteFile(ProfileActivity.this.getString(R.string.gsProfilePicsStrgFldrtb128), currentUser.getProfilePicUrl());
                storageService.deleteFile(ProfileActivity.this.getString(R.string.gsProfilePicsStrgFldrtb64), currentUser.getProfilePicUrl());
            }
            //Hide the imageview to display the image chosen
            //updates current user and repo
            currentUser.setProfilePicUrl(filename);

            //Displays toast on success
            Toast.makeText(ProfileActivity.this, "Profile Picture updated", Toast.LENGTH_LONG).show();
        }
    }

}
