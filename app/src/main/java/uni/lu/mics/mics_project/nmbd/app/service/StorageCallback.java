package uni.lu.mics.mics_project.nmbd.app.service;

import com.google.firebase.storage.StorageReference;

public interface StorageCallback {
    void onSuccess(StorageReference storageReference);
    void onFailure();
}
