package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Parent extends User implements Serializable {

    public String parentName;
    public String parentPhone;
    public String parentEmail;

    public Parent(){}

    public Parent(String _name, String _phone, String _email)
    {
        setName(_name);
        setPhone(_phone);
        setEmail(_email);
    }

    //region Get and set
    public String getName() {
        return parentName;
    }
    public void setName(String name) {
        this.parentName = name;
    }
    public String getPhone() {
        return parentPhone;
    }
    public void setPhone(String phone) {
        this.parentPhone = phone;
    }
    public String getEmail() {
        return parentEmail;
    }
    public void setEmail(String email) {
        this.parentEmail = email;
    }
    //endregion
}
