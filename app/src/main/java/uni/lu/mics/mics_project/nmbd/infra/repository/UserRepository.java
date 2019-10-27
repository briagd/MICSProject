package uni.lu.mics.mics_project.nmbd.infra.repository;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import uni.lu.mics.mics_project.nmbd.domain.model.User;

public class UserRepository extends Repository<User> {

    public UserRepository(FirebaseFirestore dbRef){
        super(dbRef.collection("users"), User.class);
    }
}
