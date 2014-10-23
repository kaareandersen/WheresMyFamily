package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.FileNotFoundException;
import java.io.IOException;
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
}
