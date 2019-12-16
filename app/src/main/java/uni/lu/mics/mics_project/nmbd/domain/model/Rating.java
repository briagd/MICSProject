package uni.lu.mics.mics_project.nmbd.domain.model;

import java.io.Serializable;

public class Rating implements Entity, Serializable {

    private String id;
    private String ownerId;
    private String eventId;
    private float value;

    public Rating(){}

    public Rating(String ownerId, String eventId, float value) {
        this.ownerId = ownerId;
        this.eventId = eventId;
        this.value = value;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public String getEventId() {
        return eventId;
    }

    public float getValue() {
        return value;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id){
        this.id = id;
    }
}
