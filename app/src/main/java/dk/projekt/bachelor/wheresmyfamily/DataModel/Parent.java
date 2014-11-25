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
    public void setName(String name) {
        this.parentName = name;
    }
    @Override
    public String getPhone() {
        return parentPhone;
    }
    @Override
    public void setPhone(String phone) {
        this.parentPhone = phone;
    }
    @Override
    public String getEmail() {
        return parentEmail;
    }
    @Override
    public void setEmail(String email) {
        this.parentEmail = email;
    }
    @Override
    public Boolean getIsCurrent() {
        return isCurrentParent;
    }
    @Override
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrentParent = isCurrent;
    }
    //endregion
}
