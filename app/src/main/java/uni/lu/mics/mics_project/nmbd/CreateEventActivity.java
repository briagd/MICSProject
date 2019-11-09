package uni.lu.mics.mics_project.nmbd;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
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

    private Button saveButton;
    private Button cancelBtn;

    private HashMap<String, Uri> resourceMap;
    ;
    // Location picker
    // TO DO: find out location

    private User currentUser;
    private Event currentEvent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        storageService = globalState.getServiceFacade().storageService();

        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");

        eventCategory = (Spinner) findViewById(R.id.SpinnerEvent);
        cancelBtn = (Button) findViewById(R.id.cancelBtn);
        setDobFields();
        setSpinner();


    }

    public void imageOnclick(View view) {
        openImageChooser();
    }


    public void onClickSave(View v) {

        saveButton = (Button) findViewById(R.id.SaveBtn);
        nameEdit = (EditText) findViewById(R.id.eventName);
        descriptionEdit = (EditText) findViewById(R.id.descriptionText);

        String name = nameEdit.getText().toString();
        String descr = descriptionEdit.getText().toString();
        String strDate = dobEdit.getText().toString();
        String category = eventCategory.getSelectedItem().toString();
        final Bitmap image = ((BitmapDrawable) eventImageButton.getDrawable()).getBitmap();
//        getImageUri(image);

        try {
            String id = eventRepo.generateId();
            final Event event = new Event(id, name, descr, strDate, currentUser.getId(), category);
            eventRepo.add(event, new RepoCallback<Void>() {
                @Override
                public void onCallback(Void v) {
                    Toast.makeText(CreateEventActivity.this, "Event Saved", Toast.LENGTH_SHORT).show();

                    //go to Event activity
                    Intent intent = getIntent(currentEvent);
                    startActivity(intent);
                    finish();
                }

//                @Override
//                public void onGetField(String str) {
//                    event.setEventId(str);
//                    CreateEventActivity.this.currentEvent = event;
//                    eventRepo.set(str, event);
//                    storageService.uploadPic(CreateEventActivity.this, resourceMap.get("ImageUri"), getResources().getString(R.string.gsEventPicsStrgFldr), str, new StorageUploadCallback() {
//                        @Override
//                        public void onProgress() {
//                        }
//
//                        @Override
//                        public void onSuccess(StorageReference storageReference, String filename) {
//                            //Displays toast on success
//                            Toast.makeText(CreateEventActivity.this, "Event picture updated", Toast.LENGTH_LONG).show();
//                            //If the new picture has a different filename than the previous one it will not be replaced so it should be deleted
//                            if (currentEvent.getCoverPicUrl() != null && !filename.equals(currentEvent.getCoverPicUrl())) {
//                                storageService.deleteFile(CreateEventActivity.this.getString(R.string.gsEventPicsStrgFldr), currentEvent.getCoverPicUrl());
//                            }
//                            //updates current user and repo
//                            currentEvent.setCoverPicUrl(filename);
//                            // TO DO: Change field names to R strings
//                            eventRepo.update(currentEvent.getEventId(), "coverPicUrl", filename);
//                            //Updates the profile pic displayer
//                            displayEventPic();
//                        }
//
//                        @Override
//                        public void onFailure() {
//                            Log.d(TAG, "Upload failed");
//                        }
//                    });

//                    Toast.makeText(CreateEventActivity.this, "Event Saved", Toast.LENGTH_SHORT).show();
//
//                    //go to Event activity
//                    Intent intent = getIntent(currentEvent);
//                    startActivity(intent);
//                    finish();
//                }
            });

        } catch (DomainException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Intent for when the user saves an event
    public Intent getIntent(Event event) {
        Intent intent = new Intent(CreateEventActivity.this, EventActivity.class);
        intent.putExtra("currentEvent", event);
        return intent;
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

        if (dobEdit.getText().toString().isEmpty()) {
            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            dobEdit.setText(df.format(c));
        }
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
            this.resourceMap = new HashMap<>(1);
            this.resourceMap.put("ImageUri", data.getData());
            eventImageButton = (ImageButton) findViewById(R.id.eventImage);
            eventImageButton.setImageURI(data.getData());
        } else {
            Toast.makeText(CreateEventActivity.this, "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }


    private void displayEventPic() {
        String currentEventPicUrl = currentEvent.getCoverPicUrl();
        final String gsUrl = this.getString(R.string.gsTb64EventPicUrl);
        storageService.getStorageReference(gsUrl, currentEventPicUrl, new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                ImageViewUtils.displayAvatarPic(CreateEventActivity.this, storageService, eventImageButton);
                Log.d(TAG, "User profile picture correctly retrieved");
            }

            @Override
            public void onFailure() {
                ImageViewUtils.displayAvatarPic(CreateEventActivity.this, storageService, eventImageButton);
            }
        });
    }


    public void cancelOnclick(View view) {
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

}