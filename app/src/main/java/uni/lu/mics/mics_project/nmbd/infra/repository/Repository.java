package uni.lu.mics.mics_project.nmbd.infra.repository;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.core.Repo;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;


public class Repository{

    protected CollectionReference collectionRef;
    // class object whose is model (event or user)
    private Class modelClass;


    public Repository(CollectionReference collectionRef, Class modelClass){
        this.collectionRef = collectionRef;
        this.modelClass = modelClass;
    }


    }

    //public void update()



    // Create repository

    // Delete repository

    // Update repository

    // Find by id


