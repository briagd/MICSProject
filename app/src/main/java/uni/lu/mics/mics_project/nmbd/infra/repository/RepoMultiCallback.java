package uni.lu.mics.mics_project.nmbd.infra.repository;

import java.util.ArrayList;

public interface RepoMultiCallback<T> {
    void onCallback(ArrayList<T> models);
}


