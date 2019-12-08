package uni.lu.mics.mics_project.nmbd.infra.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import uni.lu.mics.mics_project.nmbd.domain.model.Rating;

public class RatingRepository extends Repository<Rating> {
    private static final String TAG = "RatingRepository";
    public RatingRepository(FirebaseFirestore dbRef){
        super(dbRef.collection("Ratings"), Rating.class);
    }

    public void findByEventId(String eventId, final RepoMultiCallback<Rating> repoCallback){
        this.collectionRef.whereEqualTo("eventId", eventId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Rating> models = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        models.add(document.toObject(RatingRepository.super.modelClass));
                    repoCallback.onCallback(models);
                }
            }
        });
    }

    public void findByEventAndOwnerIds(String eventId, String ownerId, final RepoMultiCallback<Rating> repoCallback){
        this.collectionRef.whereEqualTo("eventId", eventId).whereEqualTo("ownerId", ownerId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Rating> models = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        models.add(document.toObject(RatingRepository.super.modelClass));
                    }
                    repoCallback.onCallback(models);
                }
            }
        });
    }

}
