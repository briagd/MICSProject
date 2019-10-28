package uni.lu.mics.mics_project.nmbd;

import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class Authentification {

    private List<AuthUI.IdpConfig> providers;

    public Authentification(){
        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                //new AuthUI.IdpConfig.PhoneBuilder().build(),
                //new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
                //new AuthUI.IdpConfig.TwitterBuilder().build()
        );
    }

    public Intent createSignInIntent() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyTheme)
                //.setLogo(R.drawable.firebase)
                .build();
        return intent;
    }



    public String getAuthUid(){
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getAuthDisplayName(){
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }

    public String getAuthEmail(){
        return FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    }


}

