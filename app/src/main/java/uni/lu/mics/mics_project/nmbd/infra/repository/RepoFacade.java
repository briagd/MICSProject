package uni.lu.mics.mics_project.nmbd.infra.repository;

import com.google.firebase.firestore.FirebaseFirestore;

public class RepoFacade {

    // This class is a facade exposes elements of repository level
    // used for adding one level of abstraction (kind of storing the repos)
    private UserRepository userRepo;
    private EventRepository eventRepo;



    public RepoFacade(Factory repoFactory, FirebaseFirestore db){ // This should take a db ref

        this.userRepo = repoFactory.makeUserRepository(db); // This should take a db ref
        this.eventRepo = repoFactory.makeEventRepository(db); // This should take a db ref
    }

    public UserRepository userRepo(){
        return this.userRepo;
    }

    public EventRepository eventRepo(){
        return this.eventRepo;
    }
}
