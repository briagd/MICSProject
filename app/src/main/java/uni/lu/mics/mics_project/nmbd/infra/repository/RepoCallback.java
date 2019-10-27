package uni.lu.mics.mics_project.nmbd.infra.repository;

// Interface for callback to be able to retrieve a retrieved object from Db
public interface RepoCallback<T> {
    void onCallback(T model);
}
