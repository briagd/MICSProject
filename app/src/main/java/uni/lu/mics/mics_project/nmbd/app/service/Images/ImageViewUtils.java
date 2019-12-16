package uni.lu.mics.mics_project.nmbd.app.service.Images;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.StorageReference;

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


//Check https://bumptech.github.io/glide/ to implement other functions

public class ImageViewUtils {

    static final private String TAG ="ImageViewUtils";

    //Function to display a profile picture as a circle given a user object and an imageView
    @SuppressLint("RestrictedApi")
    public static void displayUserCirclePic(final Context context, User user, final ImageView imgView){
        //Retrieve the Storage from global state
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        //Get the user's profile picture filename and location
        String currentUserProfilePicUrl = user.getProfilePicUrl();
        final String gsUrl = context.getString(R.string.gsTb64ProfPicUrl);
        //Get the storage reference of the picture
        storageService.getStorageReference(gsUrl, currentUserProfilePicUrl, new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                //Display the picture in the image view
                ImageViewUtils.displayCirclePic(context, storageReference, imgView);
                Log.d(TAG, "User profile picture correctly retrieved");
            }
            @Override
            public void onFailure() {
            }
        });
    }

    //Displays the profile picture of a user as a circle pic given a user id and an imageView
    @SuppressLint("RestrictedApi")
    public static void displayUserCirclePicID(final Context context, String uid, final ImageView imgView){
        //Retrieve the Storage from global state
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        //Retrieve the user object from the database
        UserRepository userRepo= globalState.getRepoFacade().userRepo();
        userRepo.findById(uid, new RepoCallback<User>() {
            @Override
            public void onCallback(User model) {
                //Get the user's profile picture filename and location
                String currentUserProfilePicUrl = model.getProfilePicUrl();
                final String gsUrl = context.getString(R.string.gsTb64ProfPicUrl);
                storageService.getStorageReference(gsUrl, currentUserProfilePicUrl, new StorageCallback() {
                    @Override
                    public void onSuccess(StorageReference storageReference) {
                        //Display the picture in the image view
                        ImageViewUtils.displayCirclePic(context, storageReference, imgView);
                        Log.d(TAG, "User profile picture correctly retrieved");
                    }
                    @Override
                    public void onFailure() { }
                });
            }
        });


    }

    //use Glide to display a picture into an imageview given a storage reference as a circle
    public static void displayCirclePic(Context context, StorageReference strgRef, ImageView imgView){
        Glide.with(context)
                .load(strgRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_icons)
                .into(imgView);
    }

    //use Glide to display a picture into an imageview given a uri as a circle
    public static void displayCirclePicUri(Context context, Uri uri, ImageView imgView){
        Log.d(TAG,uri.getPath());
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_icons)
                .into(imgView);
    }


    //Display the default avatar profile picture in an imageview
    public static void displayCircleAvatarPic(final Context context, Storage storageService, final ImageView imgView){
        final String defaulPictUrl = context.getString(R.string.avatarPicUrl);
        final String defaultPicId = context.getString(R.string.avatarPicId);
        //Get the storage reference of the avatar picture
        storageService.getStorageReference(defaulPictUrl,defaultPicId , new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                Log.d(TAG, "Using default avatar to display profile picture");
                //Display the avatar picture in image view
                ImageViewUtils.displayCirclePic(context, storageReference, imgView);
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "No profile picture to display");

            }
        });
    }

    //Display an event picture given an eventID and an imageview
    @SuppressLint("RestrictedApi")
    public static void displayEventPicID(final Context context, String eventId, final ImageView imgView){
        //Retrieve the Storage from global state
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        //Retrieve the event object from the eventRepo from its id
        EventRepository eventRepo= globalState.getRepoFacade().eventRepo();
        eventRepo.findById(eventId, new RepoCallback<Event>() {
            @Override
            public void onCallback(Event model) {
                //Get the event picture url
                String eventPicUrl = model.getCoverPicUrl();
                final String gsUrl = context.getString(R.string.gsTb256EventPicUrl);
                //Get the event picture storage reference
                storageService.getStorageReference(gsUrl, eventPicUrl, new StorageCallback() {
                    @Override
                    public void onSuccess(StorageReference storageReference) {
                        //Display the picture into the image view
                        ImageViewUtils.displayPic(context, storageReference, imgView);
                        Log.d(TAG, "Event picture correctly retrieved");
                    }
                    @Override
                    public void onFailure() { }
                });
            }
        });
    }

    //use Glide to display a picture into an imageview given a storage reference
    public static void displayPic(Context context, StorageReference strgRef, ImageView imgView){
        Glide.with(context)
                .load(strgRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.event_avatar)
                .into(imgView);
    }

    //use Glide to display a picture into an imageview given a uri
    public static void displayPicUri(Context context, Uri uri, ImageView imgView){
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.event_avatar)
                .into(imgView);
    }

    //Function to display the default event picture into an image view
    @SuppressLint("RestrictedApi")
    public static void displayEventUploadPic(final Context context,  final ImageView imgView){
        //The url of the default picture in the database
        final String defaulPictUrl = context.getString(R.string.gsEventsPicsUrl);
        final String defaultPicId = context.getString(R.string.eventUploadPicId);
        //retrieve the global state and storage variable
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        //Get the storage reference of the picture
        storageService.getStorageReference(defaulPictUrl,defaultPicId , new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                Log.d(TAG, "Using default avatar to display profile picture");
                //Display the picture into the image view
                ImageViewUtils.displayPic(context, storageReference, imgView);
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "No profile picture to display");

            }
        });
    }
}
