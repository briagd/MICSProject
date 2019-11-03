package uni.lu.mics.mics_project.nmbd;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.storage.StorageReference;

import uni.lu.mics.mics_project.nmbd.adapters.FriendListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendRequestListAdapter ;
import uni.lu.mics.mics_project.nmbd.adapters.FriendSearchListAdapter ;
import uni.lu.mics.mics_project.nmbd.app.service.Authentification;
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

import java.util.LinkedList;

public class FriendsActivity extends AppCompatActivity {

    final private String TAG = "FriendsActivity";

    DbManager dbManager = new DbManager(new Factory());
    RepoFacade repoFacade = dbManager.connect();
    UserRepository userRepo = repoFacade.userRepo();

    //Reference to the user logged in
    private User currentUser;
    private String currentUserID;

    //Friend search results
    private final LinkedList<String> mSearchResultList = new LinkedList<>();
    private final LinkedList<String> mSearchResultIDList = new LinkedList<>();
    private RecyclerView mSearchResultRecyclerView;
    private FriendSearchListAdapter mFriendSearchListAdapter;
    private EditText searchEdit;

    //Friend Requests received
    private final LinkedList<String> mFriendReqList = new LinkedList<>();
    private final LinkedList<String> mFriendReqListIDList = new LinkedList<>();
    private RecyclerView mFriendReqListRecyclerView;
    private FriendRequestListAdapter mFriendRequestListAdapter;
    private TextView frReqPendingLabel;

    //Friend List
    private final LinkedList<String> mFriendList = new LinkedList<>();
    private final LinkedList<String> mFriendListIDList = new LinkedList<>();
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

        updateFriendReqLists();
        updateFriendList();

        //initialize the friend search recyclerView, friend request recycler view
        initializeSearchRecyclerView();

        //Add listener to search edit view so Recycler view can be updated when there is a change
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, s.toString() +" is being searched");
                //If the text in the search edit view has changed and the it is not an empty string
                if(s.length()!=0) {
                    //Delete all the elements in the searchResultList to make space for the new results of the search
                    mSearchResultList.clear();
                    mSearchResultIDList.clear();
                    //Compare entered string with database and returns matching results
                    userRepo.whereGreaterThanOrEqualTo("name", s.toString(), new RepoMultiCallback<User>() {
                        @Override
                        public void onCallback(ArrayList<User> models) {
                            for (User u:models) {
                                String nameSearched = u.getName();
                                String idOfSearched = u.getId();
                                //check that returned user is not current user and that invitation from that user has not been received/sent
                                Boolean isSearchedSameAsCurrentUser = nameSearched.equals(currentUser.getName());
                                Boolean isReqAlreadySent = currentUser.getFriendReqSentList().contains(idOfSearched);
                                Boolean isReqAlreadyReceived = currentUser.getFriendReqReceivedList().contains(idOfSearched);
                                Boolean isAlreadyFriend = currentUser.getFriendList().contains(idOfSearched);
                                if (!isSearchedSameAsCurrentUser && !isReqAlreadySent && !isReqAlreadyReceived && !isAlreadyFriend) {
                                    //add the the name and user id to the lists send to the recyclerview
                                    mSearchResultList.addLast(nameSearched);
                                    mSearchResultIDList.addLast(idOfSearched);
                                    //Notifies the adapter that the data in array has changed
                                    mFriendSearchListAdapter.notifyDataSetChanged();
                                    Log.d(TAG, u.getName() + " of userID " + idOfSearched + "was found in search");

                                }
                            }
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    public void updateFriendReqLists(){
        //If there are no friend request then the friend request label and recycler view can be set invisible
        if (currentUser.getFriendReqReceivedList().size()==0){
            frReqPendingLabel.setVisibility(View.INVISIBLE);
            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
        }else {
            for (String id : currentUser.getFriendReqReceivedList()) {
                mFriendReqListIDList.add(id);
                userRepo.findById(id, new RepoCallback<User>() {
                    @Override
                    public void onCallback(User model) {
                        mFriendReqList.add(model.getName());
                        Log.d(TAG, "Added " + model.getName() + " to the friend requests list");
                        initializeFriendReqRecyclerView();
                    }
                });
            }
        }
    }

    public void updateFriendList(){
        //If there are no current friends then change the display message
        if (currentUser.getFriendList().size()==0){
            friendsLabel.setText("You do not have any friend.");
            mFriendListRecyclerView.setVisibility(View.INVISIBLE);

        }else {
            friendsLabel.setText("Friends: ");
            mFriendListRecyclerView.setVisibility(View.VISIBLE);
            for (String id : currentUser.getFriendList()) {
                mFriendListIDList.add(id);
                userRepo.findById(id, new RepoCallback<User>() {
                    @Override
                    public void onCallback(User model) {
                        mFriendList.add(model.getName());
                        Log.d(TAG, "Added " + model.getName() + " to the friend requests list");
                        initializeFriendRecyclerView();
                    }
                });
            }
        }
    }




    public void initializeFriendRecyclerView(){
        final LinkedList<StorageReference> storageReferences = new LinkedList<StorageReference>();
        for (String id : mFriendListIDList){
            final String gsUrl = this.getString(R.string.gsTb64ProfPicUrl);
            userRepo.findById(id, new RepoCallback<User>() {
                @Override
                public void onCallback(User model) {
                    storageService.getStorageReference(gsUrl, model.getProfilePicUrl(), new StorageCallback() {
                        @Override
                        public void onSuccess(StorageReference storageReference) {
                            storageReferences.add(storageReference);
                            mFriendListAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onFailure() {

                        }
                    });
                }
            });
        }
        mFriendListAdapter = new FriendListAdapter(FriendsActivity.this, mFriendList, mFriendListIDList, storageReferences, new FriendListAdapter.MyClickListener() {

            @Override
            public void onUnfriendRequest(int p) {

                String unfriendUserID = mFriendListIDList.get(p);
                //Removes the friend ID from the list of current user object list of req received
                currentUser.removeFriendFromFriendList(unfriendUserID);
                //Updates the database
                userRepo.removeElement(currentUserID,"friendList", unfriendUserID );
                userRepo.removeElement(unfriendUserID,"friendList", currentUserID );
                //Show toast on successful completion
                Toast.makeText(FriendsActivity.this, "Unfriended: " + mFriendList.get(p) , Toast.LENGTH_SHORT  ).show();
                //Refresh the the list for the recycler view
                mFriendListIDList.remove(p);
                mFriendList.remove(p);
                mFriendListAdapter.notifyDataSetChanged();
                if (currentUser.getFriendList().size()==0){
                    friendsLabel.setText("You do not have any friend.");
                    mFriendListRecyclerView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Connect the adapter with the recycler view.
        mFriendListRecyclerView.setAdapter(mFriendListAdapter);
        // Give the recycler view a default layout manager.
        mFriendListRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));



    }

    public void initializeFriendReqRecyclerView(){

        //Create an adapter and supply the data to be displayed
        mFriendRequestListAdapter = new FriendRequestListAdapter(FriendsActivity.this, mFriendReqList, mFriendReqListIDList,
                new FriendRequestListAdapter.MyClickListener() {
                    @Override
                    public void onAcceptRequest(int p) {
                        String requestUserID = mFriendReqListIDList.get(p);
                        //Add the friend ID to the list of friends of the current user object
                        currentUser.addFriendToFriendList(mFriendReqListIDList.get(p));
                        //Removes the friend ID from the list of current user object list of req received
                        currentUser.removeFriendFromReqReceivedList(requestUserID);
                        //Updates the database
                        userRepo.removeElement(currentUserID, "friendReqReceivedList", requestUserID);
                        userRepo.addElement(currentUserID, "friendList", requestUserID );
                        userRepo.removeElement(requestUserID, "friendReqReceivedList", currentUserID);
                        userRepo.addElement(requestUserID, "friendList", currentUserID );
                        //Show toast on successful completion
                        Toast.makeText(FriendsActivity.this, "You are now friend with " + mFriendReqList.get(p), Toast.LENGTH_SHORT  ).show();
                        //Refresh the the list for the recycler view
                        mFriendReqListIDList.remove(p);
                        mFriendReqList.remove(p);
                        mFriendRequestListAdapter.notifyDataSetChanged();

                        //Updates the friend list
                        updateFriendList();

                        if (currentUser.getFriendReqReceivedList().size()==0) {
                            frReqPendingLabel.setVisibility(View.INVISIBLE);
                            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onDeclineRequest(int p) {
                        String requestUserID = mFriendReqListIDList.get(p);
                        //Removes the friend ID from the list of current user object list of req received
                        currentUser.removeFriendFromReqReceivedList(requestUserID);
                        //Updates the database
                        userRepo.removeElement(currentUserID, "friendReqReceivedList",requestUserID );
                        //Show toast on successful completion
                        Toast.makeText(FriendsActivity.this, "Friend request from: " + mFriendReqList.get(p) + " declined.", Toast.LENGTH_SHORT  ).show();
                        //Refresh the the list for the recycler view
                        mFriendReqListIDList.remove(p);
                        mFriendReqList.remove(p);
                        mFriendRequestListAdapter.notifyDataSetChanged();

                        if (currentUser.getFriendReqReceivedList().size()==0) {
                            frReqPendingLabel.setVisibility(View.INVISIBLE);
                            mFriendReqListRecyclerView.setVisibility(View.INVISIBLE);
                        }
                    }
                });
        // Connect the adapter with the recycler view.
        mFriendReqListRecyclerView.setAdapter(mFriendRequestListAdapter);
        // Give the recycler view a default layout manager.
        mFriendReqListRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
    }

    public void initializeSearchRecyclerView(){
        // Create an adapter and supply the data to be displayed.
        mFriendSearchListAdapter = new FriendSearchListAdapter(FriendsActivity.this, mSearchResultList, mSearchResultIDList,
                new FriendSearchListAdapter.MyClickListener() {
                    //Add Friend button pressed procedure
                    @Override
                    public void onAddFriend(int p) {
                        //Add friend request to the currentUser object and update database
                        String sendUserID = mSearchResultIDList.get(p);
                        currentUser.addFriendToReqSentList(sendUserID);
                        userRepo.addElement(currentUserID, "friendReqSentList", sendUserID);
                        userRepo.addElement(sendUserID, "friendReqReceivedList", currentUserID);


                        //Display a toast for success on sending friend request
                        Toast.makeText(FriendsActivity.this, "Friend request sent to "+mSearchResultList.get(p),Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Friend request sent to "+mSearchResultList.get(p));
                        //Clear the search results and removes the search results from screen
                        mSearchResultList.clear();
                        mSearchResultIDList.clear();
                        mFriendSearchListAdapter.notifyDataSetChanged();
                    }
                });
        // Connect the adapter with the recycler view.
        mSearchResultRecyclerView.setAdapter(mFriendSearchListAdapter);
        // Give the recycler view a default layout manager.
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(FriendsActivity.this));
    }






}
