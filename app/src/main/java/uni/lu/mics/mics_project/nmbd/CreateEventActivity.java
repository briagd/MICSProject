package uni.lu.mics.mics_project.nmbd;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

import org.osmdroid.util.GeoPoint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
import uni.lu.mics.mics_project.nmbd.app.service.StorageUploadCallback;
import uni.lu.mics.mics_project.nmbd.app.service.location.LocationUtils;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadConstants;
import uni.lu.mics.mics_project.nmbd.app.service.uploadService.UploadStartIntentService;
import uni.lu.mics.mics_project.nmbd.domain.model.DomainException;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;

public class CreateEventActivity extends AppCompatActivity {

    final String TAG = "CreateEventActivity";
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

    private Uri imgUri;
    private Boolean isImagePicked = false;
    private EditText addressEdit;

    private User currentUser;
    private Event currentEvent;

    private UploadResultReceiver mUpldRessultReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        storageService = globalState.getServiceFacade().storageService();

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        addressEdit = findViewById(R.id.LocationPicker);
        eventCategory = (Spinner) findViewById(R.id.SpinnerEvent);
        eventImageButton = (ImageButton) findViewById(R.id.eventImage);
        nameEdit = findViewById(R.id.eventName);
        descriptionEdit = findViewById(R.id.descriptionText);
        ImageViewUtils.displayEventUploadPic(this, eventImageButton);
        setDobFields();
        setSpinner();

        //Instantiate the receiver for the upload service
        mUpldRessultReceiver = new UploadResultReceiver(new Handler());


    }

    public void imageOnclick(View view) {
        openImageChooser();
    }


    public void setSpinner() {
        eventCategory.setAdapter(new ArrayAdapter<Event.EventCategory>(this, android.R.layout.simple_spinner_dropdown_item, Event.EventCategory.values()));
    }

    public void setDobFields() {
        //Configures the date picker
        dobEdit = (EditText) findViewById(R.id.DatePicker);
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
                                dobEdit.setText(eventDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

    }

    @SuppressLint("IntentReset")
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            imgUri = data.getData();
            isImagePicked = true;
            ImageViewUtils.displayPicUri(this,imgUri,eventImageButton);
        } else {
            Toast.makeText(CreateEventActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    public Boolean isFormFilled(){
        Boolean isAllFilled = true;

        if(TextUtils.isEmpty(nameEdit.getText().toString())) {
            isAllFilled = false;
            TextView nameLabel = findViewById(R.id.EventNameTag);
            nameLabel.setTextColor(Color.RED);
            nameLabel.setText("Please enter a name for your event.");
        }

        if(TextUtils.isEmpty(descriptionEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.DescriptionNameTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter a description for your event.");
        }

        if(TextUtils.isEmpty(dobEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.DateTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter a date for your event.");
        }

        if(TextUtils.isEmpty(addressEdit.getText().toString())) {
            isAllFilled = false;
            TextView label = findViewById(R.id.AddressTag);
            label.setTextColor(Color.RED);
            label.setText("Please enter an address for your event.");
        }

        if(!isImagePicked){
            Log.d(TAG, "No image chosen");
            Toast.makeText(this, "Please choose a picture for your event", Toast.LENGTH_LONG).show();
            isAllFilled = false;
        }

        return isAllFilled;
    }

    public void onClickSave(View v) {

        if (isFormFilled()) {
            String name = nameEdit.getText().toString();
            String descr = descriptionEdit.getText().toString();
            String strDate = dobEdit.getText().toString();
            String category = eventCategory.getSelectedItem().toString();

            try {
                final Event event = new Event(name, descr, strDate, currentUser.getId(), category);

                String address = addressEdit.getText().toString();
                if (!TextUtils.isEmpty(address)) {
                    event.setEventAddress(address);
                    GeoPoint p = LocationUtils.getLocationFromAddress(this, address);
                    event.setGpsLat((float) p.getLatitude());
                    event.setGpsLong((float) p.getLongitude());
                }

                eventRepo.add(event, new RepoCallback() {

                    @Override
                    public void onCallback(Object model) {
                    }

                    @Override
                    public void onGetField(String id) {
                        event.setEventId(id);
                        CreateEventActivity.this.currentEvent = event;
                        eventRepo.set(id, event);
                        uploadFile(imgUri);
                        Log.d(TAG, currentEvent.getEventId());
                        Toast.makeText(CreateEventActivity.this, "Event Saved", Toast.LENGTH_SHORT).show();
                        //go to Event activity
                        Intent intent = setIntent(currentEvent, imgUri);
                        startActivity(intent);
                        finish();
                    }
                });

            } catch (DomainException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Intent for when the user saves an event
    public Intent setIntent(Event event, Uri imgUri) {
        Intent intent = new Intent(CreateEventActivity.this, EventActivity.class);
        intent.putExtra("currentUser", currentUser);
        intent.putExtra("currentEvent", event);
        intent.putExtra("image", imgUri.toString());
        return intent;
    }

    private void uploadFile(Uri imgUri){
        if(imgUri!=null) {
            Log.d(TAG, "Starting upload service");
            UploadStartIntentService.startIntentService(this, mUpldRessultReceiver, imgUri,
                    getString(R.string.gsEventPicsStrgFldr), currentEvent.getEventId(), UploadConstants.EVENT_TYPE);
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