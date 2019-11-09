package uni.lu.mics.mics_project.nmbd.app.service;

import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.LinkedList;

public class ExtendedListUser {

        private LinkedList<String> names;
        private LinkedList<String> ids;

        public ExtendedListUser(){
            this.names = new LinkedList<>();
            this.ids = new LinkedList<>();
        }

        public String getName(int pos){
            return names.get(pos);
        }

        public String getId(int pos){
        return ids.get(pos);
    }

        public void removeElement(int pos){
            names.remove(pos);
            ids.remove(pos);
        }

        public void clearLists(){
            names.clear();
            ids.clear();
        }

        public int getSize(){
            return ids.size();
        }


        public LinkedList<String> getNameList(){
            return names;
        }

        public LinkedList<String> getIdList(){
            return ids;
        }

        public void addNameID(String name, String id){
            names.add(name);
            ids.add(id);
        }

        public int getIdIndexOfLast(){
            return ids.size()-1;
        }
}
