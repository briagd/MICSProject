package uni.lu.mics.mics_project.nmbd.app.service;


import com.google.firebase.storage.StorageReference;

public  interface StorageUploadCallback {
    void onSuccess(StorageReference storageReference, String filename);
    void onFailure();
    void onProgress();
}
