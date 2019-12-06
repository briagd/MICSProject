package uni.lu.mics.mics_project.nmbd.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uni.lu.mics.mics_project.nmbd.domain.model.User;


public class FriendRecommender {

    private ArrayList<String> friendIdsList;
    private String currentUserID;
    private User currentUser;
    private String friendReco;
    private int numCommon;

    public FriendRecommender(User currentUser, ArrayList<String> friendIdsList){
        this.friendIdsList = friendIdsList;
        this.currentUserID = currentUser.getId();
        this.currentUser = currentUser;
        if(friendIdsList.size()!=0) {
            makeRecom();
        } else {
            friendReco = null;
            numCommon = 0;
        }
    }

    private boolean isSuitableRecom(String recomID){
        return !recomID.equals(this.currentUserID) && !currentUser.getFriendReqReceivedList().contains(recomID)
                && !currentUser.getFriendReqSentList().contains(recomID) && !currentUser.getFriendList().contains(recomID);
    }

    private void makeRecom(){
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        for (String friend : this.friendIdsList) {
            if (isSuitableRecom(friend)) {
                Integer j = map.get(friend);
                map.put(friend, (j == null) ? 1 : j + 1);
            }
        }
        if (map.isEmpty()){
            friendReco = null;
            numCommon = 0;
        } else {
            int maxValueInMap = (Collections.max(map.values()));  // This will return max value in the Hashmap
            numCommon = maxValueInMap;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {  // Iterate through hashmap
                if (entry.getValue() == maxValueInMap) {
                    friendReco = entry.getKey();
                }
            }
        }
    }

    public int getNumCommon(){
        return numCommon;
    }

    public String getRecommendation(){
        if (friendIdsList.size()==0){
            return null;
        } else {
            return friendReco;
        }

    }




}
