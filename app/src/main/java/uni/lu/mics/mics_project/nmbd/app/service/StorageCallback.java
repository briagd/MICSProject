package uni.lu.mics.mics_project.nmbd.app.service;

import com.google.firebase.storage.StorageReference;

import java.util.EnumMap;

public interface StorageCallback {

    void onSuccess(StorageReference storageReference);
    void onFailure(Exception e);
    void onProgress();

}
