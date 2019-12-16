package uni.lu.mics.mics_project.nmbd.infra.repository;

import com.google.firebase.firestore.FirebaseFirestore;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;

public class EventRepository extends Repository<Event>{

    public EventRepository(FirebaseFirestore dbRef){
        super(dbRef.collection("events"), Event.class);

    }

}
