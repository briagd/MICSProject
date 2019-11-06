package uni.lu.mics.mics_project.nmbd.app;

import android.app.Application;

import uni.lu.mics.mics_project.nmbd.app.service.ServiceFacade;
import uni.lu.mics.mics_project.nmbd.app.service.ServiceFactory;
import uni.lu.mics.mics_project.nmbd.infra.DbManager;
import uni.lu.mics.mics_project.nmbd.infra.repository.Factory;
import uni.lu.mics.mics_project.nmbd.infra.repository.RepoFacade;

public class AppGlobalState extends Application {
    private RepoFacade repoFacade;
    private ServiceFacade serviceFacade;

    @Override
    public void onCreate() {
        super.onCreate();
        DbManager dbManager = new DbManager(new Factory());
        this.repoFacade = dbManager.connect();
        this.serviceFacade = new ServiceFacade(new ServiceFactory());
    }

    public RepoFacade getRepoFacade() {
        return this.repoFacade;
    }

    public ServiceFacade getServiceFacade(){
        return this.serviceFacade;
    }
}
