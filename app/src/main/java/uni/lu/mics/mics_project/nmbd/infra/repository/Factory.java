package uni.lu.mics.mics_project.nmbd.infra.repository;

import com.google.firebase.firestore.FirebaseFirestore;

public class Factory {

    // This is a common pattern for object creation
    // This is an object that allows the creation of objects from the same family

    public Factory(){}

    public UserRepository makeUserRepository(FirebaseFirestore dbRef){
        return new UserRepository(dbRef);
    }

    public EventRepository makeEventRepository(FirebaseFirestore dbRef){
        return new EventRepository(dbRef);
    }

    public CommentRepository makeCommentRepository(FirebaseFirestore dbRef){
        return new CommentRepository(dbRef);
    }

    public RatingRepository makeRatingRepository(FirebaseFirestore dbRef){
        return new RatingRepository(dbRef);
    }
}
