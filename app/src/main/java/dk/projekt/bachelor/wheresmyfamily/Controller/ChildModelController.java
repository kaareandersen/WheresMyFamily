package dk.projekt.bachelor.wheresmyfamily.Controller;

import android.content.Context;

import java.util.ArrayList;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.Storage.UserInfoStorage;

/**
 * Created by Tommy on 03-12-2014.
 */
public class ChildModelController {

    ArrayList<Child> myChildren = new ArrayList<Child>();
    UserInfoStorage userInfoStorage = new UserInfoStorage();

    public ChildModelController() {}

    public ArrayList<Child> getMyChildren(Context context)
    {
        myChildren = userInfoStorage.loadChildren(context);

        return myChildren;
    }

    public void setMyChildren(Context context, ArrayList<Child> _myChildren)
    {
        userInfoStorage.saveChildren(context, _myChildren);
    }

    public Child getCurrentChild()
    {
        Child temp = new Child();

        if(myChildren.size() > 0)
        {
            for(int i = 0; i < myChildren.size(); i++)
            {
                if(myChildren.get(i).getIsCurrent())
                    temp = myChildren.get(i);
            }
        }

        if(temp != null)
            return temp;
        else
        {
            temp.setName("Unknown");
            return temp;
        }
    }

    public void noCurrentChild(ArrayList<Child> _myChildren)
    {
        for(int i = 0; i < _myChildren.size(); i++)
        {
            _myChildren.get(i).setIsCurrent(false);
        }
    }
}
