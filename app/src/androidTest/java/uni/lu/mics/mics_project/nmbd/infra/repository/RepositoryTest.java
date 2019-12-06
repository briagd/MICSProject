package uni.lu.mics.mics_project.nmbd.infra.repository;

import org.junit.Test;

import uni.lu.mics.mics_project.nmbd.app.AppGlobalState;
import uni.lu.mics.mics_project.nmbd.domain.model.Event;
import uni.lu.mics.mics_project.nmbd.domain.model.User;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;
import static org.junit.Assert.*;

public class RepositoryTest {

    //Checks that user is correctly retrieved from the database
    @Test
    public void retrieveUserFromDB(){
        AppGlobalState globalState;
        UserRepository userRepo;
        globalState = (AppGlobalState) getApplicationContext();
        userRepo = globalState.getRepoFacade().userRepo();

       final String userID = "5RrScbYjC8aNvUlKNcEhoyKp4Mz2";
       final String email = "d@d.com";
       final String friend = "8DnHzuZcEKalAGAswt1xv4yG2882";
       final int numFriend = 2;
       final String friendReq = "tvt4BXTnSckTNbQom3NaOBp1DD3";
       final String name = "Niels Bohr";

       userRepo.findById(userID, new RepoCallback<User>() {
           @Override
           public void onCallback(User user) {
               assertEquals(user.getId(),userID);
               assertEquals(user.getEmail(),email);
               assertTrue(user.getFriendList().contains(friend));
               assertEquals(user.getFriendList().size(),numFriend);
               assertTrue(user.getFriendReqReceivedList().contains(friendReq));
               assertEquals(user.getName(),name);
           }
       });
    }

    //Checks that event is correctly retrieved from the database
    @Test
    public void retrieveEventFromDB(){
        AppGlobalState globalState;
        EventRepository eventRepo;
        globalState = (AppGlobalState) getApplicationContext();
        eventRepo = globalState.getRepoFacade().eventRepo();

        final String eventID = "ST2i8fSxLlRfqrNllvSE";
        final String category = "TRIP";
        final String coverPicUrl = "ST2i8fSxLlRfqrNllvSE";
        final String description = "Come and join us in a pilgrimage into the 80's anime world. Come dress up as the character of your choice. ";
        final String address = "Harajuku, Japan";
        final String participant1 = "4YcCCZxQgnUpa4hx40nZVebwwWZ2";
        final String participant2 = "82eCw6lp3aVyTboOOfdiqnhmN4T2";
        final double gpsLat = 35.66996765136719;
        final double gpsLong = 139.70901489257812;
        final String name = "Cosplay Event";
        final Boolean isPrivate = false;
        final String endTime = "18:00";
        final String startTime = "09:00";

        eventRepo.findById(eventID, new RepoCallback<Event>() {
            @Override
            public void onCallback(Event event) {
                assertEquals(event.getId(),eventID);
                assertEquals(event.getCategory(),category);
                assertEquals(event.getCoverPicUrl(),coverPicUrl);
                assertEquals(event.getDescription(),description);
                assertEquals(event.getEventAddress(),address);
                assertTrue(event.getEventParticipants().contains(participant1));
                assertTrue(event.getEventParticipants().contains(participant2));
            }
        });
    }

}