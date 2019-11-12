package uni.lu.mics.mics_project.nmbd;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ParticipantsActivity extends AppCompatActivity {

    private static final String TAG = "ParticipantsActivity";

    private ArrayList<String> mProfileNames = new ArrayList<>();
    private ArrayList<String> mImagesUrl = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);
        Log.d(TAG, "onCreate: started");

        initImageBitmaps();
    }

    private void initImageBitmaps() {
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        mImagesUrl.add("https://c1.staticflickr.com/5/46/36/253116407448_de5fbf183d_o.jpg");
        mProfileNames.add("havasu falls");
        mImagesUrl.add("https://i.redd.it/tpsnoz5bzo501.jpg");
        mProfileNames.add("Trondheim");
        //initRecyclerView();
    }

//    private void initRecyclerView(){
//        RecyclerView recyclerView = findViewById(R.id.participants_recycler_view);
//        ParticipantsAdapter adapter = new ParticipantsAdapter(this, mProfileNames, mImagesUrl);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//    }
}
