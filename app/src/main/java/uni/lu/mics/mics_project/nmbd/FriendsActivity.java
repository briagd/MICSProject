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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import uni.lu.mics.mics_project.nmbd.adapters.AdapterCallBack;
import uni.lu.mics.mics_project.nmbd.adapters.AdapterDoubleCallBack;
import uni.lu.mics.mics_project.nmbd.adapters.FriendListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendRequestListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendSearchListAdapter ;
import uni.lu.mics.mics_project.nmbd.app.service.ExtendedListUser;
import uni.lu.mics.mics_project.nmbd.app.service.Images.ImageViewUtils;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;
import uni.lu.mics.mics_project.nmbd.domain.model.User;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoMultiCallback;
import uni.lu.mics.mics_project.nmbd.infra.repository.UserRepository;

import java.util.ArrayList;

public class FriendsActivity extends AppCompatActivity {

    final private String TAG = "FriendsActivity";
    //Friend search results
    private final ExtendedListUser searchResultList = new ExtendedListUser();
    //Friend Requests received
    private final ExtendedListUser friendReqList = new ExtendedListUser();
    //Friend List
    private final ExtendedListUser friendList = new ExtendedListUser();
    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();
    ServiceFacade serviceFacade = new ServiceFacade(new ServiceFactory());
    //Reference to the user logged in
    private User currentUser;
    private String currentUserID;
    //View variables
    private RecyclerView mSearchResultRecyclerView;
    private FriendSearchListAdapter mFriendSearchListAdapter;
    private EditText searchEdit;
    private RecyclerView mFriendReqListRecyclerView;
    private FriendRequestListAdapter mFriendRequestListAdapter;
    private TextView frReqPendingLabel;
    private RecyclerView mFriendListRecyclerView;
    private FriendListAdapter mFriendListAdapter;
    private TextView friendsLabel;

    private LinearLayout friendsReqLayout;
    private View friendsReqDivider;
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
        friendsReqLayout =  findViewById(R.id.friend_req_layout);
        friendsReqDivider = findViewById(R.id.friends_divider);
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

        setupToolbar();
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
        mFriendListAdapter = new FriendListAdapter(FriendsActivity.this,
                friendList, new AdapterCallBack() {

            @Override
            public void onClickCallback(int p) {
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
        //Create an adapter and supply the data to be displayed
        mFriendRequestListAdapter = new FriendRequestListAdapter(FriendsActivity.this,
                friendReqList,
                new AdapterDoubleCallBack() {
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
                            friendsReqLayout.setVisibility(View.INVISIBLE);


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
                            friendsReqLayout.setVisibility(View.INVISIBLE);
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
            friendsReqLayout.setVisibility(View.INVISIBLE);
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
                searchResultList,
                new AdapterCallBack() {
                    //Add Friend button pressed procedure
                    @Override
                    public void onClickCallback(int p) {
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

    public void addFriendToExtendedListHash(String id, final ExtendedListUser extListHash, final RecyclerView.Adapter adapter) {
        //Access database to find user corresponding to an id
        userRepo.findById(id, new RepoCallback<User>() {
            @Override
            public void onCallback(final User model) {
                //add the found model to the list
                extListHash.addNameID(model.getName(), model.getId());
                //Notifies adapter that the list has been updated so recyclerview can be updated
                adapter.notifyItemInserted(extListHash.getIdIndexOfLast());
            }

            @Override
            public void onGetField(String str) { }
        });
    }

    public void backToHomepage(){
        Intent intent = new Intent(FriendsActivity.this, HomepageActivity.class);
        intent.putExtra("currentUser", currentUser);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        backToHomepage();
    }

    private void setupToolbar() {
        ImageView profileImageView = findViewById(R.id.profile_pic);
        ImageViewUtils.displayUserCirclePicID(this, currentUser.getId(),profileImageView );
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, ProfileActivity.class);
                intent.putExtra("currentUser", currentUser);
                startActivity(intent);
                finish();
            }
        });
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHomepage();
            }
        });
    }



}




