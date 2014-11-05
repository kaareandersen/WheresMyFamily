package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Child implements Serializable {

    public String name;
    public String phone;
    public String eMail;
    public String deviceID;
    public String status;

    public Child() {};

    public Child(String _name, String _phone)
    {
        name = _name;
        phone = _phone;
    }

    // region Get and set
    public String getChildName() {
        return name;
    }
    public void setChildName(String childName) {
        this.name = childName;
    }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String geteMail() { return eMail; }
    public void seteMail(String eMail) { this.eMail = eMail; }
    public String getChildStatus() {
        return status;
    }
    public void setChildStatus(String childStatus) { this.status = childStatus; }
    //endregion
}
