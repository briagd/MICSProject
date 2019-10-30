package uni.lu.mics.mics_project.nmbd.service;

public class ServiceFacade {

    private Authentification authService;
    // Other attributes will be other services

    public ServiceFacade(ServiceFactory serviceFactory){
        this.authService = serviceFactory.makeAuthentification();
    }

    public Authentification authentificationService(){
        return this.authService;
    }
}
