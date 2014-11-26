package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Parent extends User implements Serializable {

    private String parentName;
    private String parentPhone;
    private String parentEmail;
    private Boolean isCurrentParent;

    public Parent(){}

    public Parent(String _name, String _phone, String _email, Boolean _isCurrentParent)
    {
        setName(_name);
        setPhone(_phone);
        setEmail(_email);
        setIsCurrent(_isCurrentParent);
    }

    //region Get and set
    @Override
    public String getName() {
        return parentName;
    }
    @Override
    public User setName(String name) {
        this.parentName = name;
        return this;
    }
    @Override
    public String getPhone() {
        return parentPhone;
    }
    @Override
    public User setPhone(String phone) {
        this.parentPhone = phone;
        return this;
    }
    @Override
    public String getEmail() {
        return parentEmail;
    }
    @Override
    public User setEmail(String email) {
        this.parentEmail = email;
        return this;
    }
    @Override
    public Boolean getIsCurrent() {
        return isCurrentParent;
    }
    @Override
    public User setIsCurrent(Boolean isCurrent) {
        this.isCurrentParent = isCurrent;
        return this;
    }
    //endregion
}
