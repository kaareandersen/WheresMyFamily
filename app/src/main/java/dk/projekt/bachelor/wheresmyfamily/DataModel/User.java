package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-11-2014.
 */
public abstract class User implements Serializable {

    private String userName;
    private String userPhone;
    private String userEmail;
    private Boolean isCurrent;

    public User(){}

    public User(String _userName, String _userPhone, String _userEmail, boolean _isCurrent)
    {
        setName(_userName);
        setPhone(_userPhone);
        setEmail(_userEmail);
        setIsCurrent(_isCurrent);
    }

    //region Get and set
    public String getName() {
        return userName;
    }
    public User setName(String userName) {
        this.userName = userName;

        return this;
    }
    public String getPhone() {
        return userPhone;
    }
    public User setPhone(String userPhone) {
        this.userPhone = userPhone;
        return this;
    }
    public String getEmail() {
        return userEmail;
    }
    public User setEmail(String eMail) {
        this.userEmail = eMail;
        return this;
    }
    public Boolean getIsCurrent() {
        return isCurrent;
    }
    public User setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
        return this;
    }
    //endregion
}
