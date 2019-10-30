package uni.lu.mics.mics_project.nmbd.app.service;

public class ServiceFactory {

    public ServiceFactory(){}

    public Authentification makeAuthentification(){
        return new Authentification();
    }

    // Similarly other services will be created here
}
