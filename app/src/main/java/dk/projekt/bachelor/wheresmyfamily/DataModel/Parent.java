package dk.projekt.bachelor.wheresmyfamily.DataModel;

import android.content.Context;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Parent implements Serializable {

    public String name;
    public String phone;

    public Parent(){}

    public Parent(String _name, String _phone)
    {
        name = _name;
        phone = _phone;
    }

    //region Get and set
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    //endregion
}
