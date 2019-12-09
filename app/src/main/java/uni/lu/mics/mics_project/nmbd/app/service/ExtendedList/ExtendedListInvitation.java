package uni.lu.mics.mics_project.nmbd.app.service.ExtendedList;

import java.util.HashMap;
import java.util.LinkedList;

public class ExtendedListInvitation {
    private LinkedList<String> eventIds;
    private HashMap<String, String> eventNames;
    private HashMap<String, String> eventOrganizorsId;
    private HashMap<String, String> eventOrganizorsName;

    public ExtendedListInvitation(){
        this.eventIds = new LinkedList<>();
        this.eventNames = new HashMap<>();
        this.eventOrganizorsId = new HashMap<>();
        this.eventOrganizorsName = new HashMap<>();
    }

    public LinkedList<String> getIdList(){
        return eventIds;
    }

    public void removeElement(int pos){
        eventNames.remove(eventIds.get(pos));
        eventIds.remove(eventIds.get(pos));
        eventOrganizorsId.remove(eventIds.get(pos));
        eventOrganizorsName.remove(eventIds.get(pos));
    }

    public void clearLists(){
        eventNames.clear();
        eventIds.clear();
        eventOrganizorsId.clear();
        eventOrganizorsName.clear();
    }


    public String getId(int pos){
        return eventIds.get(pos);
    }
    public String getEventName (int pos){
        return eventNames.get(eventIds.get(pos));
    }
    public String geteventOrganizorsId (int pos){
        return eventOrganizorsId.get(eventIds.get(pos));
    }
    public String geteventOrganizorsName (int pos){
        return eventOrganizorsName.get(eventIds.get(pos));
    }


    public void addElement(String eventName, String eventId, String eventOrganizorId, String eventOrganizorName){
        eventIds.add(eventId);
        eventNames.put(eventId, eventName);
        eventOrganizorsId.put(eventId, eventOrganizorId);
        eventOrganizorsName.put(eventId, eventOrganizorName);
    }

    public int getSize(){
        return eventIds.size();
    }

    public int getIdIndexOfLast(){
        return eventIds.size()-1;
    }
}
