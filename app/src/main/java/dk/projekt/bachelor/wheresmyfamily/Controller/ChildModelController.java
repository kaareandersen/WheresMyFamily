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

    private int childListCount;

    public ChildModelController(Context context)
    {
        myChildren = userInfoStorage.loadChildren(context);
    }

    public Child getCurrentChild()
    {
        Child temp = new Child();
        // ArrayList<Child> tempList;
        // tempList = myChildren;

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

    public int getChildListCount() {

        childListCount = myChildren.size();
        return childListCount;
    }

    public ArrayList<Child> getMyChildren() {
        return myChildren;
    }

    public void setCurrentUserToNone()
    {
        for(int i = 0; i < myChildren.size(); i++)
        {
            myChildren.get(i).setIsCurrent(false);
        }
    }
}
