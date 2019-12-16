package uni.lu.mics.mics_project.nmbd.app.service.uploadService;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ResultReceiver;
import android.util.Log;

public class UploadStartIntentService {
    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    public static void startIntentService(Context context, ResultReceiver mResultReceiver, Uri fileUri, String strgFolder, String fileUid, String fileType) {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(context, UploadIntentService.class);

        intent.putExtra(UploadConstants.RECEIVER, mResultReceiver);
        // Pass the result receiver as an extra to the service.
        intent.putExtra(UploadConstants.FILE_URI_EXTRA, fileUri.toString());
        intent.putExtra(UploadConstants.STRG_FLDR_EXTRA, strgFolder);
        intent.putExtra(UploadConstants.FILE_UID_EXTRA, fileUid);
        intent.putExtra(UploadConstants.FILE_TYPE_EXTRA, fileType);

        Log.d("UploadIntentService", "UploadStartIntentService about to start service");
        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        context.startService(intent);
    }


}
