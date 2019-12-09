package uni.lu.mics.mics_project.nmbd.infra.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import uni.lu.mics.mics_project.nmbd.domain.model.Comment;

public class CommentRepository extends Repository<Comment> {
    public CommentRepository(FirebaseFirestore dbRef){
        super(dbRef.collection("comments"), Comment.class);

    }

    public void findByEventId(String eventId, final RepoMultiCallback<Comment> repoCallback){
        this.collectionRef.whereEqualTo("eventId", eventId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<Comment> models = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult())
                        models.add(document.toObject(CommentRepository.super.modelClass));
                    repoCallback.onCallback(models);
                }
            }
        });
    }
    }
