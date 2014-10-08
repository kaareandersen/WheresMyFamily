package dk.projekt.bachelor.wheresmyfamily;

import java.io.Serializable;

/**
 * Created by Tommy on 24-09-2014.
 */
public class Child implements Serializable {

    public String name;
    public String phone;
    public String deviceID;

    public Child(String _name, String _phone, String _deviceID)
    {
        name = _name;
        phone = _phone;
        deviceID = _deviceID;
    }
}
