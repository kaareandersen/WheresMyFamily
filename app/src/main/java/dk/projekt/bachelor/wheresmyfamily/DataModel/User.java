package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-11-2014.
 */
public class User implements Serializable {

    String userName;
    String userPhone;
    String userEmail;

    public User(){}

    public User(String _userName, String _userPhone, String _userEmail)
    {
        setName(_userName);
        setPhone(_userPhone);
        setEmail(_userEmail);
    }

    public String getName() {
        return userName;
    }
    public void setName(String userName) {
        this.userName = userName;
    }
    public String getPhone() {
        return userPhone;
    }
    public void setPhone(String userPhone) {
        this.userPhone = userPhone;
    }
    public String getEmail() {
        return userEmail;
    }
    public void setEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
