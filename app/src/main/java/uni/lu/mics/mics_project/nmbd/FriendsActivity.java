package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import uni.lu.mics.mics_project.nmbd.adapters.FriendListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendRequestListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendSearchListAdapter ;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedListHash;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;
import uni.lu.mics.mics_project.nmbd.app.service.Storage;
import uni.lu.mics.mics_project.nmbd.app.service.StorageCallback;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {

    final private String TAG = "FriendsActivity";

    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();

    //Reference to the user logged in
    private User currentUser;
    private String currentUserID;

    //Friend search results
    private final ExtendedListHash searchResultList = new ExtendedListHash();
    private RecyclerView mSearchResultRecyclerView;
    private FriendSearchListAdapter mFriendSearchListAdapter;
    private EditText searchEdit;

    //Friend Requests received
    private final ExtendedListHash friendReqList = new ExtendedListHash();
    private RecyclerView mFriendReqListRecyclerView;
    private FriendRequestListAdapter mFriendRequestListAdapter;
    private TextView frReqPendingLabel;

    //Friend List
    private final ExtendedListHash friendList = new ExtendedListHash();
    private RecyclerView mFriendListRecyclerView;
    private FriendListAdapter mFriendListAdapter;
    private TextView friendsLabel;

    ServiceFacade serviceFacade = new ServiceFacade(new ServiceFactory());
    final Storage storageService = serviceFacade.storageService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //Retrieve current user
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();

        //Initialize the different views in layout
        searchEdit = findViewById(R.id.friends_activity_search_edit);
        frReqPendingLabel = findViewById(R.id.friends_activity_req_pending_label);
        friendsLabel = findViewById(R.id.friends_activity_friends_list_label);
        //Attaching the recycler views to their corresponding View items
        mFriendReqListRecyclerView = findViewById(R.id.friends_activity_req_pending_recyclerview);
        mFriendListRecyclerView = findViewById(R.id.friends_activity_friends_recyclerview);
        mSearchResultRecyclerView = findViewById(R.id.friends_activity_search_result_recyclerview);

        //initialize the friend search recyclerView, friend request recycler view
        initializeSearchRecyclerView();
        initializeFriendRecyclerView();
        initializeFriendReqRecyclerView();

        //Add listener to search edit view so Recycler view can be updated when there is a change
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, s.toString() + " is being searched");
                //If the text in the search edit view has changed and the it is not an empty string
                if (s.length() != 0) {
                    //Delete all the elements in the searchResultList to make space for the new results of the search
                    searchResultList.clearLists();
                    mFriendSearchListAdapter.notifyDataSetChanged();
                    //Compare entered string with database and returns matching results
                    userRepo.whereGreaterThanOrEqualTo("name", s.toString(), new RepoMultiCallback<User>() {
                        @Override
                        public void onCallback(ArrayList<User> models) {
                            for (User u : models) {
                                String nameSearched = u.getName();
                                String idOfSearched = u.getId();
                                //check that returned user is not current user and that invitation from that user has not been received/sent
                                Boolean isSearchedSameAsCurrentUser = nameSearched.equals(currentUser.getName());
                                Boolean isReqAlreadySent = currentUser.getFriendReqSentList().contains(idOfSearched);
                                Boolean isReqAlreadyReceived = currentUser.getFriendReqReceivedList().contains(idOfSearched);
                                Boolean isAlreadyFriend = currentUser.getFriendList().contains(idOfSearched);
                                if (!isSearchedSameAsCurrentUser && !isReqAlreadySent && !isReqAlreadyReceived && !isAlreadyFriend) {
                                    //add the the name and user id to the lists send to the recyclerview
                                    addFriendToExtendedListHash(idOfSearched, searchResultList, mFriendSearchListAdapter);
                                    Log.d(TAG, u.getName() + " of userID " + idOfSearched + "was found in search");
                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        Intent intent = new Intent(this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        Intent intent = getIntent();
        currentUser = (User) intent.getSerializableExtra("currentUser");
        currentUserID = currentUser.getId();
    }

    public void updateFriendList() {
        //If there are no current friends then change the display message
        if (currentUser.getFriendList().size() == 0) {
            friendsLabel.setText("You do not have any friend.");
            mFriendListRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            friendsLabel.setText("Friends: ");
            mFriendListRecyclerView.setVisibility(View.VISIBLE);
            friendList.clearLists();
            mFriendListAdapter.notifyDataSetChanged();
            for (String id : currentUser.getFriendList()) {
                addFriendToExtendedListHash(id, friendList, mFriendListAdapter);
            }
        }
    }

    public void initializeFriendRecyclerView() {
        addStrgRefs(friendList, mFriendListAdapter);

        mFriendListAdapter = new FriendListAdapter(FriendsActivity.this,
                friendList.getNameList(), friendList.getIdList(), friendList.getStrgRefList(), new FriendListAdapter.MyClickListener() {

            @Override
            public void onUnfriendRequest(int p) {
                String unfriendUserID = friendList.getId(p);//mFriendListIDList.get(p);
                //Removes the friend ID from the list of current user object list of req received
                currentUser.removeFriendFromFriendList(unfriendUserID);
                //Updates the database
                userRepo.removeElement(currentUserID, "friendList", unfriendUserID);
                userRepo.removeElement(unfriendUserID, "friendList", currentUserID);
                //Show toast on successful completion
                Toast.makeText(FriendsActivity.this, "Unfriended: " + friendList.getName(p), Toast.LENGTH_SHORT).show();
                //Refresh the the list for the recycler view
                friendList.removeElement(p);
                mFriendListAdapter.notifyItemRemoved(p);
                if (currentUser.getFriendList().size() == 0) {
                    friendsLabel.setText("You do not have any friend.");
                    mFriendListRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });
        // Connect the adapter with the recycler view.
        mFriendListRecyclerView.setAdapter(mFriendListAdapter);
        // Give the recycler view a default layout manager.
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
        updateFriendList();
    }

    public void initializeFriendReqRecyclerView() {
        addStrgRefs(friendReqList, mFriendRequestListAdapter);
        //Create an adapter and supply the data to be displayed
        mFriendRequestListAdapter = new FriendRequestListAdapter(FriendsActivity.this,
                friendReqList.getNameList(), friendReqList.getIdList(), friendReqList.getStrgRefList(),
                new FriendRequestListAdapter.MyClickListener() {
                    @Override
                    public void onAcceptRequest(int p) {
                        String requestUserID = friendReqList.getId(p);
                        //Add the friend ID to the list of friends of the current user object
                        currentUser.addFriendToFriendList(friendReqList.getId(p));
                        //Removes the friend ID from the list of current user object list of req received
                        currentUser.removeFriendFromReqReceivedList(requestUserID);
                        //Updates the database
                        userRepo.removeElement(currentUserID, "friendReqReceivedList", requestUserID);
                        userRepo.addElement(currentUserID, "friendList", requestUserID);
                        userRepo.removeElement(requestUserID, "friendReqSentList", currentUserID);
                        userRepo.addElement(requestUserID, "friendList", currentUserID);
                        //Show toast on successful completion
                        Toast.makeText(FriendsActivity.this, "You are now friend with " + friendReqList.getName(p), Toast.LENGTH_SHORT).show();
                        //Refresh the the list for the recycler view
                        friendReqList.removeElement(p);
                        mFriendRequestListAdapter.notifyItemRemoved(p);

                        //Updates the friend list
                        updateFriendList();

                        if (currentUser.getFriendReqReceivedList().size() == 0) {
                            frReqPendingLabel.setVisibility(View.INVISIBLE);
                            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onDeclineRequest(int p) {
                        String requestUserID = friendReqList.getId(p);
                        //Removes the friend ID from the list of current user object list of req received
                        currentUser.removeFriendFromReqReceivedList(requestUserID);
                        //Updates the database
                        userRepo.removeElement(currentUserID, "friendReqReceivedList", requestUserID);
                        //Show toast on successful completion
                        Toast.makeText(FriendsActivity.this, "Friend request from: " + friendReqList.getName(p) + " declined.", Toast.LENGTH_SHORT).show();
                        //Refresh the the list for the recycler view
                        friendReqList.removeElement(p);
                        mFriendRequestListAdapter.notifyDataSetChanged();
                        if (currentUser.getFriendReqReceivedList().size() == 0) {
                            frReqPendingLabel.setVisibility(View.INVISIBLE);
                            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }
                });
        // Connect the adapter with the recycler view.
        mFriendReqListRecyclerView.setAdapter(mFriendRequestListAdapter);
        // Give the recycler view a default layout manager.
        mFriendReqListRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
        updateFriendReqLists();
    }

    public void updateFriendReqLists() {
        //If there are no friend request then the friend request label and recycler view can be set invisible
        if (currentUser.getFriendReqReceivedList().size() == 0) {
            frReqPendingLabel.setVisibility(View.INVISIBLE);
            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
        } else {
            for (String id : currentUser.getFriendReqReceivedList()) {
                addFriendToExtendedListHash(id, friendReqList, mFriendRequestListAdapter);
            }
        }
    }

    public void initializeSearchRecyclerView() {
        // Create an adapter and supply the data to be displayed.
        mFriendSearchListAdapter = new FriendSearchListAdapter(FriendsActivity.this,
                searchResultList.getNameList(), searchResultList.getIdList(), searchResultList.getStrgRefList(),
                new FriendSearchListAdapter.MyClickListener() {
                    //Add Friend button pressed procedure
                    @Override
                    public void onAddFriend(int p) {
                        //Add friend request to the currentUser object and update database
                        String sendUserID = searchResultList.getId(p);
                        currentUser.addFriendToReqSentList(sendUserID);
                        userRepo.addElement(currentUserID, "friendReqSentList", sendUserID);
                        userRepo.addElement(sendUserID, "friendReqReceivedList", currentUserID);
                        //Display a toast for success on sending friend request
                        Toast.makeText(FriendsActivity.this, "Friend request sent to " + searchResultList.getName(p), Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Friend request sent to " + searchResultList.getName(p));
                        //Clear the search results and removes the search results from screen
                        searchResultList.removeElement(p);
                        mFriendSearchListAdapter.notifyItemRemoved(p);
                    }
                });
        // Connect the adapter with the recycler view.
        mSearchResultRecyclerView.setAdapter(mFriendSearchListAdapter);
        // Give the recycler view a default layout manager.
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
    }

    public void addStrgRefs(final ExtendedListHash extList, final RecyclerView.Adapter adapter) {
        for (final String id : extList.getIdList()) {
            final String gsUrl = this.getString(R.string.gsTb64ProfPicUrl);
            userRepo.findById(id, new RepoCallback<User>() {
                @Override
                public void onCallback(User model) {
                    storageService.getStorageReference(gsUrl, model.getProfilePicUrl(), new StorageCallback() {
                        @Override
                        public void onSuccess(StorageReference storageReference) {
                            extList.addStrgRef(id, storageReference);
                            adapter.notifyItemChanged(extList.getIdIndexOfLast());
                        }

                        @Override
                        public void onFailure() {
                        }
                    });
                }
            });
        }
    }

    public void addFriendToExtendedListHash(String id, final ExtendedListHash extListHash, final RecyclerView.Adapter adapter) {
        userRepo.findById(id, new RepoCallback<User>() {
            @Override
            public void onCallback(final User model) {
                extListHash.addNameID(model.getName(), model.getId());
                final String gsUrl = FriendsActivity.this.getString(R.string.gsTb64ProfPicUrl);
                storageService.getStorageReference(gsUrl, model.getProfilePicUrl(), new StorageCallback() {
                    @Override
                    public void onSuccess(StorageReference storageReference) {
                        extListHash.addStrgRef(model.getId(), storageReference);
                        adapter.notifyItemChanged(extListHash.getIdIndexOfLast());
                    }

                    @Override
                    public void onFailure() {
                    }
                });
                adapter.notifyItemInserted(extListHash.getIdIndexOfLast());
            }
        });
    }
}




