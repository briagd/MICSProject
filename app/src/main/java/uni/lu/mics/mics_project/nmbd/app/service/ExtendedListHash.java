package uni.lu.mics.mics_project.nmbd.app.service;

import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.LinkedList;

public class ExtendedListHash{

        private LinkedList<String> names;
        private LinkedList<String> ids;
        private HashMap<String, StorageReference> strgReferences;

        public ExtendedListHash(){
            this.names = new LinkedList<>();
            this.ids = new LinkedList<>();
            this.strgReferences = new HashMap<>();
        }

        public String getName(int pos){
            return names.get(pos);
        }

        public void removeElement(int pos){
            strgReferences.remove(ids.get(pos));
            names.remove(pos);
            ids.remove(pos);
        }

        public void clearLists(){
            names.clear();
            ids.clear();
            strgReferences.clear();
        }

        public String getId(int pos){
            return ids.get(pos);
        }

        public LinkedList<String> getNameList(){
            return names;
        }

        public LinkedList<String> getIdList(){
            return ids;
        }

        public HashMap<String, StorageReference> getStrgRefList(){
            return strgReferences;
        }

        public StorageReference getStrgRef(int pos){
            return strgReferences.get(ids.get(pos));
        }

        public void addNameID(String name, String id){
            names.add(name);
            ids.add(id);
        }

        public void addStrgRef(String id, StorageReference stRef){
            strgReferences.put(id, stRef);
        }

        public int getIdIndexOfLast(){
            return ids.size()-1;
        }
}
