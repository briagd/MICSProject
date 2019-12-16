package uni.lu.mics.mics_project.nmbd.domain.model;

import java.io.Serializable;

public class Comment implements Entity, Serializable {

    private String id;
    private String ownerId;
    private String ownerName;
    private String ownerPic;
    private String eventId;
    private String date;
    private String text;

    public Comment(){}

    public Comment(String ownerId, String ownerName, String ownerPic, String eventId, String date, String text){
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPic = ownerPic;
        this.eventId = eventId;
        this.date = date;
        this.text = text;
    }

    public void setId(String id){
        this.id = id;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerName(String ownerName){
        this.ownerName = ownerName;
    }

    public void setOwnerPic(String ownerPic){
        this.ownerPic = ownerPic;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getOwnerName(){
        return this.ownerName;
    }

    public String getOwnerPic(){
        return this.ownerPic;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

}
