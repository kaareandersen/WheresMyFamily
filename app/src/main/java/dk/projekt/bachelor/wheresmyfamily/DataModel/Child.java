package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Child extends User implements Serializable {

    private String childName;
    private String childPhone;
    private String childEmail;
    private Boolean isCurrentChild;

    public Child() {}

    public Child(String _name, String _phone, String _email, Boolean _isCurrentChild)
    {
        setName(_name);
        setPhone(_phone);
        setEmail(_email);
        setIsCurrent(_isCurrentChild);
    }

    //region Get and set
    @Override
    public String getName() {
        return childName;
    }
    @Override
    public User setName(String name) {
        this.childName = name;
        return this;
    }
    @Override
    public String getPhone() {
        return childPhone;
    }
    @Override
    public User setPhone(String phone) {
        this.childPhone = phone;
        return this;
    }
    @Override
    public String getEmail() {
        return childEmail;
    }
    @Override
    public User setEmail(String eMail) {
        this.childEmail = eMail;
        return this;
    }
    @Override
    public Boolean getIsCurrent() {
        return isCurrentChild;
    }

    @Override
    public User setIsCurrent(Boolean isCurrent) {
        this.isCurrentChild = isCurrent;
        return this;
    }
    //endregion
}
