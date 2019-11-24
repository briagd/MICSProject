package uni.lu.mics.mics_project.nmbd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import org.osmdroid.util.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationUtils;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadConstants;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadStartIntentService;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;

public class CreateEventActivity extends AppCompatActivity {

    final String TAG = "CreateEventActivity";
    final private Event event = new Event();
    private final int PICFROMGALLERY = 1;
    private final int PICFROMCAMERA = 0;
    int PICK_IMAGE_REQUEST = 1;
    AppGlobalState globalState;
    EventRepository eventRepo;
    Storage storageService;
    // Image picker
    ImageButton eventImageButton;
    // Name
    private EditText nameEdit;
    // Description
    private EditText descriptionEdit;
    private Spinner eventCategory;
    // Date picker
    private DatePickerDialog datePickerDialog;
    private EditText dobEdit;


    //Time Picker
    private TimePickerDialog startTimePickerDialog;
    private EditText startTimeEdit;
    private TimePickerDialog endTimePickerDialog;
    private EditText endTimeEdit;

    private Uri imgUri;
    private boolean isImagePicked = false;
    private EditText addressEdit;
    private User currentUser;
    private UploadResultReceiver mUpldRessultReceiver;
    //Variables for selecting picture
    private String currentPhotoPath;

    //Checks if editing
    private boolean isEditing = false;
    private boolean isEditingPicPicked = false;
    private Event eventEditing;

    //Private event
    private Switch privateSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        //Initialize the repo and storage services
        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        storageService = globalState.getServiceFacade().storageService();

        //Get the intent and retrieve the current user object
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        addressEdit = findViewById(R.id.LocationPicker);
        eventCategory = (Spinner) findViewById(R.id.SpinnerEvent);
        eventImageButton = (ImageButton) findViewById(R.id.eventImage);
        nameEdit = findViewById(R.id.eventName);
        descriptionEdit = findViewById(R.id.descriptionText);
        startTimeEdit = findViewById(R.id.start_time_edit);
        endTimeEdit = findViewById(R.id.end_time_edit);
        dobEdit = (EditText) findViewById(R.id.DatePicker);
        privateSwitch = findViewById(R.id.private_switch);

        setTimeFields();
        setDobFields();
        setSpinner();

        //Instantiate the receiver for the upload service
        mUpldRessultReceiver = new UploadResultReceiver(new Handler());
        if (intent.getSerializableExtra("event")!=null){
            isEditing = true;
            eventEditing = (Event) intent.getSerializableExtra("event");
            setupFields();
            isImagePicked = true;
        }
        if (isEditing){
            try {
                ImageViewUtils.displayEventPicID(this, eventEditing.getId(), eventImageButton);
            } catch(Exception e){
                ImageViewUtils.displayEventUploadPic(this, eventImageButton);
            }
        } else {
            ImageViewUtils.displayEventUploadPic(this, eventImageButton);
        }

    }


    private void setupFields() {
        nameEdit.setText(eventEditing.getName());
        descriptionEdit.setText(eventEditing.getDescription());
        addressEdit.setText(eventEditing.getEventAddress());
        startTimeEdit.setText(eventEditing.getStartTime());
        endTimeEdit.setText(eventEditing.getEndTime());
        dobEdit.setText(eventEditing.getDate());

    }

    public void imageOnclick(View view) {
        selectImage();
    }

    public void setSpinner() {
        eventCategory.setAdapter(new ArrayAdapter<Event.EventCategory>(this, android.R.layout.simple_spinner_dropdown_item, Event.EventCategory.values()));
    }

    private void setTimeFields() {

        startTimeEdit.setInputType(InputType.TYPE_NULL);

        endTimeEdit.setInputType(InputType.TYPE_NULL);
        final int h = 0;
        final int m = 0;
        startTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimePickerDialog = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = setTimeFormat(Integer.toString(hourOfDay)) + ":" + setTimeFormat(Integer.toString(minute));
                        startTimeEdit.setText(time);
                    }
                }, h, m, true);
                startTimePickerDialog.show();
            }
        });
        endTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimePickerDialog = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String time = setTimeFormat(Integer.toString(hourOfDay)) + ":" + setTimeFormat(Integer.toString(minute));
                        endTimeEdit.setText(time);
                    }
                }, h, m, true);
                endTimePickerDialog.show();
            }
        });
    }


    public String setTimeFormat(String t){
        if (t.length() == 1){
            return("0" + t);
        }
        return t;
    }

    public void setDobFields() {
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
                datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                String eventDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                                try {
                                    Date enteredDate = sdf.parse(eventDate);
                                    Date currentDate = cldr.getTime();
                                    if (enteredDate.compareTo(currentDate)>=0){
                                        dobEdit.setText(eventDate);
                                    } else {
                                        Toast.makeText(CreateEventActivity.this, "Please set a date in the future for your event", Toast.LENGTH_LONG).show();
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

//        if (dobEdit.getText().toString().isEmpty()) {
//            Date c = Calendar.getInstance().getTime();
//            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
//            dobEdit.setText(df.format(c));
//        }
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (resultCode == RESULT_OK) {
                switch (reqCode) {
                    case PICFROMCAMERA:
                        File file = new File(currentPhotoPath);
                        imgUri = Uri.fromFile(file);
                        break;
                    case PICFROMGALLERY:
                        imgUri = data.getData();
                        break;
                }
                isImagePicked = true;
                isEditingPicPicked = true;
                ImageViewUtils.displayPicUri(this, imgUri, eventImageButton);
            } else {
                Toast.makeText(CreateEventActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
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
        } else {
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
                                Uri photoURI = FileProvider.getUriForFile(CreateEventActivity.this,
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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


    public Boolean isFormFilled() {
        Boolean isAllFilled = true;

        if (TextUtils.isEmpty(nameEdit.getText().toString())) {
            isAllFilled = false;
            TextView nameLabel = findViewById(R.id.EventNameTag);
            nameLabel.setTextColor(Color.RED);
            nameLabel.setText("Please enter a name for your event.");
        }

        if (TextUtils.isEmpty(descriptionEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.DescriptionNameTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter a description for your event.");
        }

        if (TextUtils.isEmpty(dobEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.DateTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter a date for your event.");
        }

        if (TextUtils.isEmpty(addressEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.AddressTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter an address for your event.");
        }

        if (!isImagePicked) {
            Log.d(TAG, "No image chosen");
            Toast.makeText(this, "Please choose a picture for your event", Toast.LENGTH_LONG).show();
            isAllFilled = false;
        }
        if (TextUtils.isEmpty(startTimeEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.start_time_label);
            label.setTextColor(Color.RED);
            label.setText("Please enter a start time.");
        }

        if (TextUtils.isEmpty(endTimeEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.end_time_label);
            label.setTextColor(Color.RED);
            label.setText("Please enter an end time.");
        }

        return isAllFilled;
    }

    public void onClickSave(View v) {

        if (isFormFilled()) {

                Button saveButton = findViewById(R.id.SaveBtn);
                saveButton.setVisibility(View.INVISIBLE);
                //Retrieve the information by the user
                String name = nameEdit.getText().toString();
                String descr = descriptionEdit.getText().toString();
                String strDate = dobEdit.getText().toString();
                String category = eventCategory.getSelectedItem().toString();
                String startTime = startTimeEdit.getText().toString();
                String endTime = endTimeEdit.getText().toString();
                Boolean isPrivate = privateSwitch.isChecked();
                //Sets all the fields for the event object to be created
                event.setName(name);
                event.setDescription(descr);
                event.setDate(strDate);
                event.setCreator(currentUser.getId());
                event.setCategory(category);
                event.setCreator(currentUser.getId());
                event.addParticipant(currentUser.getId());
                event.setStartTime(startTime);
                event.setEndTime(endTime);
                event.addAdmin(currentUser.getId());
                event.setPrivate(isPrivate);



                String address = addressEdit.getText().toString();
                if (!TextUtils.isEmpty(address)) {
                    event.setEventAddress(address);
                    GeoPoint p = LocationUtils.getLocationFromAddress(this, address);

                    if (p != null) {
                        event.setGpsLat((float) p.getLatitude());
                        event.setGpsLong((float) p.getLongitude());
                    }
                    event.setGpsLat((float) p.getLatitude());
                    event.setGpsLong((float) p.getLongitude());

                }
            if (isEditing) {
                event.setId(eventEditing.getId());
                if (isEditingPicPicked){
                    Log.d(TAG, "Image was picked");
                    uploadFile(imgUri);
                } else {
                    Log.d(TAG, "Image was not picked");
                    event.setCoverPicUrl(eventEditing.getCoverPicUrl());
                    event.setEventParticipants(eventEditing.getEventParticipants());
                    event.setLikes(eventEditing.getLikes());
                    event.setEventAdmins(eventEditing.getEventAdmins());
                }
                eventRepo.set(event.getId(), event);


            } else {

                eventRepo.addWithoutId(event, new RepoCallback<String>() {
                    @Override
                    public void onCallback(String model) {
                        eventRepo.update(model, "id", model);
                        event.setId(model);
                        uploadFile(imgUri);

                    }
                });
            }


            Toast.makeText(CreateEventActivity.this, "Event Saved", Toast.LENGTH_SHORT).show();
            //go to Event activity

            Intent intent = setIntent(event, imgUri);
            startActivity(intent);
            finish();
        }
    }

    // Intent for when the user saves an event
    public Intent setIntent(Event event, Uri imgUri) {
        Intent intent = new Intent(CreateEventActivity.this, EventActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("currentEvent", event);
        if(isImagePicked && imgUri!=null) {
            Log.d(TAG,"sending pic uri as intent");
            intent.putExtra("image", imgUri.toString());
        }
        return intent;
    }

    private void uploadFile(Uri imgUri) {
        if (imgUri != null) {
            Log.d(TAG, "Starting upload service");
            UploadStartIntentService.startIntentService(this, mUpldRessultReceiver, imgUri,
                    getString(R.string.gsEventPicsStrgFldr), event.getId(), UploadConstants.EVENT_TYPE);
        }
    }


    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    public void cancelOnClick(View view) {
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    //Intent service to upload file
    private class UploadResultReceiver extends ResultReceiver {
        UploadResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
        }
    }

}