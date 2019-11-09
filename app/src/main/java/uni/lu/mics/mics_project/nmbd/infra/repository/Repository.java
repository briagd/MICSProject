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

import uni.lu.mics.mics_project.nmbd.domain.model.Entity;


public class Repository<T extends Entity> {

    protected CollectionReference collectionRef;
    // class object whose is model (event or user)
    private Class<T> modelClass;

    public Repository(CollectionReference collectionRef, Class<T> modelClass) {
        this.collectionRef = collectionRef;
        this.modelClass = modelClass;
    }

    public String generateId() {
        return this.collectionRef.document().getId();
    }

    public void list(final RepoMultiCallback<T> repoCallback){
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<T> models = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        models.add(document.toObject(modelClass));
                    repoCallback.onCallback(models);
                }
            }
        });
    }

    public void findById(final String docId, final RepoCallback<T> repoCallback) {
        DocumentReference docRef = this.collectionRef.document(docId);
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
                        Log.e("Repository", String.format("Entity (%s) not found ", docId));
                        repoCallback.onCallback(null);
                    }
                });;
    }


    public void add(final T model, final RepoCallback<Void> repoCallback) {
        DocumentReference doc = this.collectionRef.document(model.getId());
        doc.set(model)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void v) {
                    repoCallback.onCallback(v);
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
