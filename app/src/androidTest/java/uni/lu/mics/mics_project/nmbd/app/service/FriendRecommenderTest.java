package uni.lu.mics.mics_project.nmbd.app.service;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class FriendRecommenderTest {

    private  FriendRecommender friendRecommender;

    @Test
    public void getRecommendation() {
        String currentUser = "c";

        ArrayList<String> names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");

        friendRecommender = new FriendRecommender(currentUser,names);
        // the string returned from the object under test...
        String result = friendRecommender.getRecommendation();
        //Test if input is not empty output is non null
        assertNotNull(result);
        //Test correct output
        assertEquals(result, "a");

        //Test empty list
        names = new ArrayList<>();
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getRecommendation();
        //Check that result is null
        assertNull(result);

        names = new ArrayList<>();
        names.add("a");
        names.add("b");
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getRecommendation();
        //Test if there is no string occuring more than other then an output is returned
        assertNotNull(result);

        //Test for different lengths of strings
        names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");
        names.add("foebofeboqwe");
        names.add("eqoepqew");
        names.add("11240431");
        names.add("9jgw9032HJION");
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getRecommendation();
        assertNotNull(result);
        assertEquals(result, "a");

        //Test if current user is present in the friends list
        names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");
        names.add("c");
        names.add("c");
        names.add("c");
        names.add("c");
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getRecommendation();
        assertNotNull(result);
        assertEquals(result, "a");
    }

    @Test
    public void getNumCommon() {
        String currentUser = "c";

        ArrayList<String> names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");

        friendRecommender = new FriendRecommender(currentUser,names);
        int result = friendRecommender.getNumCommon();
        //Test if input is not empty output is non null
        assertNotNull(result);
        //Test correct output
        assertEquals(result, 2);

        //Test if current user is present in the friends list
        names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");
        names.add("c");
        names.add("c");
        names.add("c");
        names.add("c");
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getNumCommon();
        assertNotNull(result);
        assertEquals(result, 2);


        names = new ArrayList<>();
        names.add("a");
        names.add("b");
        names.add("a");
        names.add("foebofeboqwe");
        names.add("eqoepqew");
        names.add("11240431");
        names.add("9jgw9032HJION");
        names.add("b");
        names.add("b");
        names.add("b");
        friendRecommender = new FriendRecommender(currentUser, names);
        result = friendRecommender.getNumCommon();
        assertNotNull(result);
        assertEquals(result, 4);

    }

}