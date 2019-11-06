package uni.lu.mics.mics_project.nmbd.app.service.uploadService;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import uni.lu.mics.mics_project.nmbd.R;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageUploadCallback;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class UploadIntentService extends IntentService {
    private static final String TAG = "UploadIntentService";

    /**
     * The receiver where results are forwarded from this service.
     */
    private ResultReceiver mReceiver;

    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();
    EventRepository eventRepo = repoFacade.eventRepo();
    ServiceFacade serviceFacade = new ServiceFacade(new ServiceFactory());
    final Storage storageService = serviceFacade.storageService();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public UploadIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mReceiver = intent.getParcelableExtra(UploadConstants.RECEIVER);

        // Check if receiver was properly registered.
        if (mReceiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.");
            return;
        }

        String strgFld = intent.getStringExtra(UploadConstants.STRG_FLDR_EXTRA);
        Uri imageUri = Uri.parse(intent.getStringExtra(UploadConstants.FILE_URI_EXTRA));

        final String fileUid = intent.getStringExtra(UploadConstants.FILE_UID_EXTRA);
        final String fileType = intent.getStringExtra(UploadConstants.FILE_TYPE_EXTRA);

        //Set-up Notification
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), getString(R.string.UpldNotifId))
                .setSmallIcon(R.drawable.ic_eventzy_notification)
                .setContentTitle(getString(R.string.UpldNotifTitle))
                .setContentText(getString(R.string.UpldNotifContent))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        // Issue the initial notification with zero progress
        final int PROGRESS_MAX = 100;
        final   int PROGRESS_CURRENT = 0;
        final int NOTIFICATION_ID = 1;
        builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        storageService.uploadPic(this, imageUri, strgFld, fileUid, new StorageUploadCallback() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                int progress = (int)((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                builder.setContentText(progress+"% completed")
                        .setProgress(PROGRESS_MAX, progress, false);
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
            @Override
            public void onSuccess(StorageReference storageReference, String filename) {
                builder.setContentText("Upload complete")
                        .setProgress(0,0,false);
                notificationManager.notify(NOTIFICATION_ID, builder.build());

                if(fileType.equals(UploadConstants.PROFILE_TYPE)) {

                    userRepo.update(fileUid, "profilePicUrl", filename);

                } else if(fileType.equals(UploadConstants.EVENT_TYPE)) {
                    //Update evenetRepo when an event picture is uploaded
                    eventRepo.update(fileUid, "coverPicUrl", filename);

                }

                //Delivers the filename as a string back to the activity that started the intent service
                deliverResultToReceiver(UploadConstants.SUCCESS_RESULT, filename);
            }
            @Override
            public void onFailure() {
                Log.d(TAG, "Upload failed");
                builder.setContentText("Upload failed, please try again.");
                notificationManager.notify(NOTIFICATION_ID, builder.build());
            }
        });

    }

    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String filename) {
        Bundle bundle = new Bundle();
        bundle.putString(UploadConstants.FILE_NAME, filename);
        mReceiver.send(resultCode, bundle);
    }
}
