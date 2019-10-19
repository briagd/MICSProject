package uni.lu.mics.mics_project.nmbd;

public class User {

    String username;
    String name;
    int age;
    String email;


    public User(){

    }

    public User(String username, String name, int age, String email){
        this.username = username;
        this.name = name;
        this.age = age;
        this.email = email;

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
