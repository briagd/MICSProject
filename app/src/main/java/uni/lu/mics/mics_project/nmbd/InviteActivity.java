package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import uni.lu.mics.mics_project.nmbd.adapters.AdapterCallBack;
import uni.lu.mics.mics_project.nmbd.adapters.FriendInviteAdapter;
import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedList.ExtendedListUser;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.repository.EventRepository;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

public class InviteActivity extends AppCompatActivity {

    final String TAG = "InviteActivity";

    AppGlobalState globalState;
    EventRepository eventRepo;
    UserRepository userRepo;

    private User currentUser;
    private String currentUserID;
    private Event currentEvent;

    //Friend List
    private final ExtendedListUser friendInviteList = new ExtendedListUser();

    //Recyclerview variables
    private RecyclerView mFriendInvirteRecyclerView;
    private FriendInviteAdapter mFriendInviteAdapter;
    private TextView friendsLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        //Initialize the global state and database variables
        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();
        userRepo = globalState.getRepoFacade().userRepo();
        //Retrieve intent and set the user and event objects accordingly
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();
        currentEvent = (Event) intent.getSerializableExtra("event");
        //Setup the toolbar at the top of screen
        setupToolbar();

        //
        mFriendInvirteRecyclerView = findViewById(R.id.friends_invite_recyclerview);
        friendsLabel = findViewById(R.id.friends_activity_friends_list_label2);
        initializeFriendRecyclerView();
    }

    public void initializeFriendRecyclerView() {
        mFriendInviteAdapter = new FriendInviteAdapter(InviteActivity.this,
                friendInviteList, new AdapterCallBack() {

            @Override
            public void onClickCallback(int p) {
                String inviteUserID = friendInviteList.getId(p);
                //add the user selected to the list of invited
                currentEvent.addEventInvited(inviteUserID);
                eventRepo.addElement(currentEvent.getId(), "eventInvited", inviteUserID);

                //Show toast on successful completion
                Toast.makeText(InviteActivity.this, "Sent Invitation to: " + friendInviteList.getName(p), Toast.LENGTH_SHORT).show();
                //Refresh the the list for the recycler view
                friendInviteList.removeElement(p);
                mFriendInviteAdapter.notifyItemRemoved(p);
                if (currentUser.getFriendList().size() == 0) {
                    friendsLabel.setText("You do not have any friend to invite.");
                    mFriendInvirteRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
        // Connect the adapter with the recycler view.
        mFriendInvirteRecyclerView.setAdapter(mFriendInviteAdapter);
        // Give the recycler view a default layout manager.
        mFriendInvirteRecyclerView.setLayoutManager(new LinearLayoutManager(InviteActivity.this));
        updateFriendList();
    }

    public void updateFriendList() {
        //If there are no current friends then change the display message
        if (currentUser.getFriendList().size() == 0) {
            friendsLabel.setText("You do not have any friend to invite.");
            mFriendInvirteRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            friendsLabel.setText("Friends: ");
            mFriendInvirteRecyclerView.setVisibility(View.VISIBLE);
            friendInviteList.clearLists();
            mFriendInviteAdapter.notifyDataSetChanged();
            for (String id : currentUser.getFriendList()) {
                if (!currentEvent.getEventInvited().contains(id) && !currentEvent.getEventParticipants().contains(id)) {
                    addFriendToExtendedList(id, friendInviteList, mFriendInviteAdapter);
                }
            }
        }
    }

    public void addFriendToExtendedList(String id, final ExtendedListUser extList, final RecyclerView.Adapter adapter) {
        //Access database to find user corresponding to an id
        userRepo.findById(id, new RepoCallback<User>() {
            @Override
            public void onCallback(final User model) {
                //add the found model to the list
                extList.addNameID(model.getName(), model.getId());
                //Notifies adapter that the list has been updated so recyclerview can be updated
                adapter.notifyItemInserted(extList.getIdIndexOfLast());
            }
        });
    }



    private void setupToolbar() {
        ImageView profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),profileImageView );
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InviteActivity.this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                finish();
            }
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InviteActivity.this, HomepageActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        backToHomepage();
    }

    public void backToHomepage(){
        Intent intent = new Intent(InviteActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }
}
