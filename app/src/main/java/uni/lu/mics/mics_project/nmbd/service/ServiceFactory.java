package uni.lu.mics.mics_project.nmbd.service;

import com.google.android.gms.auth.api.Auth;

public class ServiceFactory {

    public ServiceFactory(){}

    public Authentification makeAuthentification(){
        return new Authentification();
    }

    // Similarly other services will be created here
}
