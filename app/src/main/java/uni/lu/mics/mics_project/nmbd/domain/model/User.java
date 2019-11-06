package uni.lu.mics.mics_project.nmbd.domain.model;

import android.content.res.Resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import uni.lu.mics.mics_project.nmbd.R;

public class User implements Serializable {


    private String username;

    private String id;
    private String name;
    private String dateOfBirth;
    private int age;
    private String email;
    private String profilePicUrl;


    // List of friends of one user
    private List<String> friendList;
    private List<String> friendReqReceivedList;
    private List<String> friendReqSentList;

    public User() {
    }

    public User(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.friendList = new ArrayList<>();
        this.friendReqReceivedList = new ArrayList<>();
        this.friendReqSentList = new ArrayList<>();
        this.profilePicUrl = "eventzy_user.png";
    }

    public String getId() {
        return this.id;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public List<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<String> friendList) {
        this.friendList = friendList;
    }

    public void addFriendToFriendList(String friendId){
        friendList.add(friendId);
    }

    public void removeFriendFromFriendList(String friendId){
        friendList.remove(friendId);
    }

    public List<String> getFriendReqReceivedList() {
        return friendReqReceivedList;
    }

    public void setFriendReqReceivedList(List<String> friendReqReceivedList) {
        this.friendReqReceivedList = friendReqReceivedList;
    }

    public void addFriendToReqReceivedList(String friendId){
        friendReqReceivedList.add(friendId);
    }

    public void removeFriendFromReqReceivedList(String friendReqID){
        friendReqReceivedList.remove(friendReqID);
    }

    public List<String> getFriendReqSentList() {
        return friendReqSentList;
    }

    public void setFriendReqSentList(List<String> friendReqSentList) {
        this.friendReqSentList = friendReqSentList;
    }

    public void addFriendToReqSentList(String friendId){
        friendReqSentList.add(friendId);
    }
}
