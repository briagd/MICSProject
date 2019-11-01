package uni.lu.mics.mics_project.nmbd.app.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.EnumMap;
import java.util.HashMap;

import uni.lu.mics.mics_project.nmbd.R;

public class Storage {
    private StorageReference mStorageRef;
    private FirebaseStorage storage;

    public Storage(){
        this.storage = FirebaseStorage.getInstance();
        this.mStorageRef = this.storage.getReference();
    }

    public void uploadProfilePic(Context context, Uri imgUri, String userUid, final StorageCallback strgCallBack){
        final String filename ="profilePic/"+ userUid + "." + getFileExtension(context, imgUri);
        final StorageReference fileReference = mStorageRef.child(filename);
        fileReference.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String url = "gs://mics-android-project.appspot.com/"+filename;
                StorageReference gsReference = storage.getReferenceFromUrl(url);
                strgCallBack.onSuccess(gsReference);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                strgCallBack.onFailure(e);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                strgCallBack.onProgress();
            }
        });
    }

//

    private String getFileExtension(Context context, Uri uri){
        ContentResolver cR = context.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

//    private String getProfilePicUrl(String filename){
//        StorageReference fileReference = mStorageRef.child(R.string.profilePicsStrgFldr + filename);
//        return fileReference.getDownloadUrl().toString();
//    }
//
//    private EnumMap<StrgPicSizesEnum, String> getProfilePicUrlsEnumMap(String flname){
//
//        EnumMap<StrgPicSizesEnum, String> imgUrls = new EnumMap<>(StrgPicSizesEnum.class);
//        String picUrl = getProfilePicUrl(flname);
//        String picSUrl = getProfilePicUrl("thumb@64" + flname);
//        String picMUrl = getProfilePicUrl("thumb@128" + flname);
//        String picLUrl = getProfilePicUrl("thumb@256" + flname);
//        imgUrls.put(StrgPicSizesEnum.FULLSIZE, picUrl);
//        imgUrls.put(StrgPicSizesEnum.THUMB64, picSUrl);
//        imgUrls.put(StrgPicSizesEnum.THUMB128, picMUrl);
//        imgUrls.put(StrgPicSizesEnum.THUMB256, picLUrl);
//        return imgUrls;
//    }


}
