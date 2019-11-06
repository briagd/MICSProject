package uni.lu.mics.mics_project.nmbd.app.service;

public class ServiceFacade {

    private Authentification authService;
    private Storage strgService;
    // Other attributes will be other services

    public ServiceFacade(ServiceFactory serviceFactory){
        this.authService = serviceFactory.makeAuthentification();
        this.strgService = serviceFactory.makeStorage();
    }

    public Authentification authentificationService(){
        return this.authService;
    }
    public Storage storageService(){return this.strgService;}
}
