package uni.lu.mics.mics_project.nmbd;

import java.util.ArrayList;
import java.util.List;

public class User {

    // to be discussed
    private String userId;
    private String username;
    private String name;
    private int age;
    private String email;

    // List of friends of one user
    private List<String> userFriendsList;


    public User(){

    }

    public User(String username, String name, int age, String email){
        this.username = username;
        this.name = name;
        this.age = age;
        this.email = email;
        this.userFriendsList = new ArrayList<>();

    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
