package uni.lu.mics.mics_project.nmbd.app.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.StorageReference;

import uni.lu.mics.mics_project.nmbd.ProfileActivity;
import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;


//Check https://bumptech.github.io/glide/ to implement other functions

public class ImageViewUtils {

    static final private String TAG ="ImageViewUtils";

    @SuppressLint("RestrictedApi")
    public static void displayUserCirclePic(final Context context, User user, final ImageView imgView){
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        String currentUserProfilePicUrl = user.getProfilePicUrl();
        final String gsUrl = context.getString(R.string.gsTb64ProfPicUrl);
        storageService.getStorageReference(gsUrl, currentUserProfilePicUrl, new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                ImageViewUtils.displayCirclePic(context, storageReference, imgView);
                Log.d(TAG, "User profile picture correctly retrieved");
            }
            @Override
            public void onFailure() {
            }
        });
    }

    @SuppressLint("RestrictedApi")
    public static void displayUserCirclePicID(final Context context, String uid, final ImageView imgView){
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        UserRepository userRepo= globalState.getRepoFacade().userRepo();
        userRepo.findById(uid, new RepoCallback<User>() {
            @Override
            public void onCallback(User model) {
                String currentUserProfilePicUrl = model.getProfilePicUrl();
                final String gsUrl = context.getString(R.string.gsTb64ProfPicUrl);
                storageService.getStorageReference(gsUrl, currentUserProfilePicUrl, new StorageCallback() {
                    @Override
                    public void onSuccess(StorageReference storageReference) {
                        ImageViewUtils.displayCirclePic(context, storageReference, imgView);
                        Log.d(TAG, "User profile picture correctly retrieved");
                    }
                    @Override
                    public void onFailure() { }
                });
            }

            @Override
            public void onGetField(String str) { }
        });


    }


    public static void displayCirclePic(Context context, StorageReference strgRef, ImageView imgView){
        Glide.with(context)
                .load(strgRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_icons)
                .into(imgView);
    }

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



    public static void displayCircleAvatarPic(final Context context, Storage storageService, final ImageView imgView){
        final String defaulPictUrl = context.getString(R.string.avatarPicUrl);
        final String defaultPicId = context.getString(R.string.avatarPicId);
        storageService.getStorageReference(defaulPictUrl,defaultPicId , new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                Log.d(TAG, "Using default avatar to display profile picture");
                ImageViewUtils.displayCirclePic(context, storageReference, imgView);
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "No profile picture to display");

            }
        });
    }

    @SuppressLint("RestrictedApi")
    public static void displayEventPicID(final Context context, String eventId, final ImageView imgView){
         AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        EventRepository eventRepo= globalState.getRepoFacade().eventRepo();
        eventRepo.findById(eventId, new RepoCallback<Event>() {
            @Override
            public void onCallback(Event model) {
                String eventPicUrl = model.getCoverPicUrl();
                final String gsUrl = context.getString(R.string.gsTb256EventPicUrl);
                storageService.getStorageReference(gsUrl, eventPicUrl, new StorageCallback() {
                    @Override
                    public void onSuccess(StorageReference storageReference) {
                        ImageViewUtils.displayPic(context, storageReference, imgView);
                        Log.d(TAG, "Event picture correctly retrieved");
                    }
                    @Override
                    public void onFailure() { }
                });
            }

            @Override
            public void onGetField(String str) { }
        });
    }


    public static void displayPic(Context context, StorageReference strgRef, ImageView imgView){
        Glide.with(context)
                .load(strgRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.event_avatar)
                .into(imgView);
    }


    public static void displayPicUri(Context context, Uri uri, ImageView imgView){
        Glide.with(context)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .placeholder(R.drawable.event_avatar)
                .into(imgView);
    }


    @SuppressLint("RestrictedApi")
    public static void displayEventUploadPic(final Context context,  final ImageView imgView){
        final String defaulPictUrl = context.getString(R.string.gsEventsPicsUrl);
        final String defaultPicId = context.getString(R.string.eventUploadPicId);
        AppGlobalState globalState = (AppGlobalState) getApplicationContext();
        final Storage storageService = globalState.getServiceFacade().storageService();
        storageService.getStorageReference(defaulPictUrl,defaultPicId , new StorageCallback() {
            @Override
            public void onSuccess(StorageReference storageReference) {
                Log.d(TAG, "Using default avatar to display profile picture");
                ImageViewUtils.displayPic(context, storageReference, imgView);
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "No profile picture to display");

            }
        });
    }
}
