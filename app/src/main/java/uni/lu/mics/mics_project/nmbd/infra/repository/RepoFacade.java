package uni.lu.mics.mics_project.nmbd.infra.repository;

import com.google.firebase.firestore.FirebaseFirestore;

public class RepoFacade {

    // This class is a facade exposes elements of repository level
    // used for adding one level of abstraction (kind of storing the repos)
    private UserRepository userRepo;
    private EventRepository eventRepo;
    private CommentRepository commentRepo;



    public RepoFacade(Factory repoFactory, FirebaseFirestore db){

        this.userRepo = repoFactory.makeUserRepository(db);
        this.eventRepo = repoFactory.makeEventRepository(db);
        this.commentRepo = repoFactory.makeCommentRepository(db);
    }

    public UserRepository userRepo(){
        return this.userRepo;
    }

    public EventRepository eventRepo(){
        return this.eventRepo;
    }

    public CommentRepository commentRepo(){
        return this.commentRepo;
    }

}
