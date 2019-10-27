package uni.lu.mics.mics_project.nmbd.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private String eventId;
    private String eventName;
    private String eventDescription;

    // To be changed to coordinates type
    private String eventLocation;
    // List of users listed by UIDs
    private List<String> eventParticipants;
    // List of admins by UIDs
    private List<String> eventAdmins;
    private String eventCreator;

    public Event(){}

    public Event(String eventName, String eventLocation, String eventCreator) {
        this.eventName = eventName;
        this.eventLocation = eventLocation;
        this.eventParticipants = new ArrayList<>();
        this.eventAdmins = new ArrayList<>();
        this.eventAdmins.add(eventCreator);
        this.eventCreator = eventCreator;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public List<String> getEventParticipants() {
        return eventParticipants;
    }

    public List<String> getEventAdmins() {
        return eventAdmins;
    }

    public String getEventCreator() {
        return eventCreator;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }

    public void setEventParticipants(List<String> eventParticipants) {
        this.eventParticipants = eventParticipants;
    }

    public void setEventAdmins(List<String> eventAdmins) {
        this.eventAdmins = eventAdmins;
    }

    public void setEventCreator(String eventCreator) {
        this.eventCreator = eventCreator;
    }
}
