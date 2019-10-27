package uni.lu.mics.mics_project.nmbd.infra;

import com.google.firebase.firestore.FirebaseFirestore;

import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;

public class DbManager {

    // Db Manager manages the database by establishing a connection to it
    // it takes as input a RepoFactory and ensures that a connection to the db is made
    // it creates a repofacade that that allows to create UserRepo anf EventRepo

    private FirebaseFirestore dbRef;
    private Factory repoFactory;

    // TO DO: Inject a logger here as 1st param (most common first)
    public DbManager(Factory repoFactory){
        this.dbRef = null;
        this.repoFactory = repoFactory;
    }

    public RepoFacade connect(){
        try {
            this.dbRef = FirebaseFirestore.getInstance();
        } catch (Exception e){
            // TO DO: setup a logger
            System.out.print("Could not get a database reference");
            return null;
        }
        return new RepoFacade(this.repoFactory, this.dbRef);

    }
}
