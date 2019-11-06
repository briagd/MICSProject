package uni.lu.mics.mics_project.nmbd.infra.repository;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;


public class Repository<T> {

    protected CollectionReference collectionRef;
    // class object whose is model (event or user)
    private Class<T> modelClass;

    public Repository(CollectionReference collectionRef, Class<T> modelClass) {
        this.collectionRef = collectionRef;
        this.modelClass = modelClass;
    }

    public void findById(String docID, final RepoCallback<T> repoCallback) {
        DocumentReference docRef = this.collectionRef.document(docID);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        T model = documentSnapshot.toObject(modelClass);
                        repoCallback.onCallback(model);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //repoCallback.onCallback();
                    }
                });;
    }

    public void add(T model, final RepoCallback repoCallback) {
        this.collectionRef.add(model)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("DocAdding", "DocumentSnapshot written with ID: " + documentReference.getId());
                        repoCallback.onGetField(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocAdding", "Error adding document", e);
                    }
                });
    }

    /*

     */
    public void set(String uid, T model){
        this.collectionRef.document(uid).set(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("DocSetting", "Model correctly set");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("DocAdding", "Error adding document", e);
            }
        });
    }

    public void update(String modelUid, Map<String, Object> updates) {
        DocumentReference docRef = this.collectionRef.document(modelUid);
        docRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DocUpdates", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocUpdates", "Error updating document", e);
                    }
                });
    }

    public void update(String modelUid, @NonNull String field, Object value, Object... moreFieldsAndValues){
        DocumentReference docRef = this.collectionRef.document(modelUid);
        docRef.update(field, value, moreFieldsAndValues)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DocUpdates", "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocUpdates", "Error updating document", e);
                    }
                });
    }

    public void addElement(String modelUid, String listName, Object value){
        this.collectionRef.document(modelUid).update(listName, FieldValue.arrayUnion(value));

    }

    public void removeElement(String modelUid, String listName, Object value){
        this.collectionRef.document(modelUid).update(listName, FieldValue.arrayRemove(value));

    }


    public void delete(String modelUid) {
        this.collectionRef.document(modelUid).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("DocDelete", "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DocDelete", "Error deleting document", e);
                    }
                });
    }

    public void whereGreaterThanOrEqualTo(String field, String toCompare, final RepoMultiCallback<T> repoCallback){
        this.collectionRef.whereGreaterThanOrEqualTo(field, toCompare).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<T> models = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    models.add(document.toObject(modelClass));
                }
                repoCallback.onCallback(models);
            }
        });
    }

}
