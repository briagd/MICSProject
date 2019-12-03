package uni.lu.mics.mics_project.nmbd.app.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uni.lu.mics.mics_project.nmbd.domain.model.User;

public class FriendRecommender {
    ArrayList<String> friendIdsList;

    public FriendRecommender(ArrayList<String> friendIdsList){
        this.friendIdsList = friendIdsList;
    }

    public String getRecommendation(){
        if (friendIdsList.size()==0){
            return null;
        } else {
            HashMap<String, Integer> map = new HashMap<String, Integer>();
            String result = "";
            for (String i : this.friendIdsList) {
                Integer j = map.get(i);
                map.put(i, (j == null) ? 1 : j + 1);
            }

            int maxValueInMap = (Collections.max(map.values()));  // This will return max value in the Hashmap
            for (Map.Entry<String, Integer> entry : map.entrySet()) {  // Iterate through hashmap
                if (entry.getValue() == maxValueInMap) {
                    result = entry.getKey();
                }
            }
            return result;
        }
    }




}
