package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Child extends User implements Serializable {

    public String childName;
    public String childPhone;
    public String childEmail;
    public String childStatus;

    public Child() {}

    public Child(String _name, String _phone, String _email)
    {
        setName(_name);
        setPhone(_phone);
        seteMail(_email);
    }

    //region Get and set
    public String getName() {
        return childName;
    }
    public void setName(String name) {
        this.childName = name;
    }
    public String getPhone() {
        return childPhone;
    }
    public void setPhone(String phone) {
        this.childPhone = phone;
    }
    public String geteMail() {
        return childEmail;
    }
    public void seteMail(String eMail) {
        this.childEmail = eMail;
    }
    public String getStatus() {
        return childStatus;
    }
    public void setStatus(String status) {
        this.childStatus = status;
    }
    //endregion
}
