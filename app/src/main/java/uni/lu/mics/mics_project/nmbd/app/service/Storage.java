package uni.lu.mics.mics_project.nmbd.app.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import uni.lu.mics.mics_project.nmbd.R;

public class Storage {
    private StorageReference mStorageRef;
    private FirebaseStorage storage;
    final private String TAG = "Storage Service Class";

    public Storage(){
        this.storage = FirebaseStorage.getInstance();
        this.mStorageRef = this.storage.getReference();
    }

    public void uploadPic(final Context context, Uri imgUri, String gsUrl ,String picUid, final StorageUploadCallback strgCallBack){
        final String filename = picUid + "." + getFileExtension(context, imgUri);
        final String fileLoc = gsUrl + picUid + "." + getFileExtension(context, imgUri);
        final String fileUrl = context.getString(R.string.gsUrl) + picUid + "." + getFileExtension(context, imgUri);
        final StorageReference fileReference = mStorageRef.child(fileLoc);
        fileReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference gsReference = storage.getReferenceFromUrl(fileUrl);
                strgCallBack.onSuccess(gsReference, filename);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                strgCallBack.onFailure();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                strgCallBack.onProgress();
            }
        });
    }

    public void getStorageReference(final String gsUrl , final String picUid, final StorageCallback strgCallBack){
        if (picUid==null){
            Log.d(TAG, "picUid is null");
            strgCallBack.onFailure();
        }else {
                    String url = gsUrl + picUid;
                    StorageReference gsReference = storage.getReferenceFromUrl(url);
                    strgCallBack.onSuccess(gsReference);
        }
    }

    public void deleteFile(final String gsFolderUrl , final String picUid){
            StorageReference storageReference = mStorageRef.child(gsFolderUrl+picUid);
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "File deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "File not deleted");
                }
            });
    }




    private String getFileExtension(Context context, Uri uri){
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }







}
