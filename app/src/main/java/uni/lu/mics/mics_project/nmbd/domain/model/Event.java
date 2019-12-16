package uni.lu.mics.mics_project.nmbd.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Entity, Serializable {


    private String id;
    private String name;
    private String description;
    private String date;
    private String startTime;
    private String endTime;
    private String category;
    private String coverPicUrl;

    // To be changed to coordinates type
    private String eventAddress;
    private float gpsLat;
    private float gpsLong;

    // List of users listed by UIDs
    private List<String> eventParticipants;
    private List<String> eventInvited;
    // List of admins by UIDs
    private List<String> eventAdmins;
    private String creator;

    private float rating;

    private int likes;
    private boolean isPrivate;


    public Event(){
        this.eventParticipants = new ArrayList<>();
        this.eventInvited = new ArrayList<>();
        this.eventAdmins = new ArrayList<>();
        this.coverPicUrl = "event_avatar.jpg";
        this.rating = 0f;
        this.startTime = "00:00";
        this.endTime = "00:01";
        this.isPrivate = true;
    }

    public Event(String name, String description, String date, String creator, String category) throws DomainException {
        if (name == null || name.isEmpty()){
            throw new DomainException("name can't be empty");
        }

        //this.id = id;

        this.name = name;
        this.description = description;
        this.date = date;
        this.creator = creator;
        this.eventParticipants = new ArrayList<>();
        this.eventParticipants.add(creator);
        this.eventInvited = new ArrayList<>();
        this.eventAdmins = new ArrayList<>();
        this.eventAdmins.add(creator);
        this.category = category;
        this.coverPicUrl = "event_avatar.jpg";

        this.rating = 0f;

        this.likes = 0;
        this.isPrivate = true;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getEventAddress() {
        return eventAddress;
    }

    public List<String> getEventParticipants() {
        return eventParticipants;
    }

    public List<String> getEventAdmins() {
        return eventAdmins;
    }

    public String getCreator() {
        return creator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEventAddress(String eventAddress) {
        this.eventAddress = eventAddress;
    }

    public void setEventParticipants(List<String> eventParticipants) {
        this.eventParticipants = eventParticipants;
    }

    public void setEventAdmins(List<String> eventAdmins) {
        this.eventAdmins = eventAdmins;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCoverPicUrl() {
        return coverPicUrl;
    }

    public void setCoverPicUrl(String coverPicUrl) {
        this.coverPicUrl = coverPicUrl;
    }

    public float getGpsLat() {
        return gpsLat;
    }

    public void setGpsLat(float gpsLat) {
        this.gpsLat = gpsLat;
    }

    public float getGpsLong() {
        return gpsLong;
    }

    public void setGpsLong(float gpsLong) {
        this.gpsLong = gpsLong;
    }

    public List<String> getEventInvited() {
        return eventInvited;
    }

    public void setEventInvited(List<String> eventInvited) {
        this.eventInvited = eventInvited;
    }

    public void addEventInvited(String uid){
        if (!eventInvited.contains(uid)) {
            eventInvited.add(uid);
        }
    }

    public enum EventCategory{
        GENERAL,
        PARTY,
        BIRTHDAY,
        CONCERT,
        TRIP

    }

    public float getRating() {
        return this.rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void addParticipant(String uid){
        if (!eventParticipants.contains(uid)) {
            eventParticipants.add(uid);
        }
    }

    public void addAdmin(String uid) {
        if (!eventAdmins.contains(uid)) {
            eventAdmins.add(uid);
        }
    }

    public void removeParticipant(String uid){
        eventParticipants.remove(uid);
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
