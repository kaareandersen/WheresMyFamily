package dk.projekt.bachelor.wheresmyfamily;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.DataModel.Child;
import dk.projekt.bachelor.wheresmyfamily.DataModel.Parent;

/**
 * Created by Tommy on 20-11-2014.
 */

public final class UserInfoStorage
{
    private static final String PREFIX = "json";
    public static final String CHILD_PREFS_NAME = "CHILD_PREFS";
    public static final String CHILD_FAVORITES = "CHILD_FAVORITES";
    public static final String PARENT_PREFS_NAME = "PARENT_PREFS";
    public static final String PARENT_FAVORITES = "PARENT_FAVORITES";

    public UserInfoStorage()
    {
        super();
    };

    /*public boolean saveChildArray(Child[] array, String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("childPrefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(arrayName +"_size", array.length);
        for(int i=0;i<array.length;i++)
            editor.putString(arrayName + "_" + i, array[i]);
        return editor.commit();
    }

    public Child[] loadChildArray(String arrayName, Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences("childPrefs", 0);
        int size = prefs.getInt(arrayName + "_size", 0);
        Child array[] = new Child[][size];
        for(int i=0;i<size;i++)
            array[i] = prefs.getString(arrayName + "_" + i, null);
        return array;
    }*/

    /*public static void saveJSONObject(Context c, String prefName, String key, JSONObject object) {

        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(UserInfoStorage.PREFIX + key, object.toString());
        editor.commit();
    }

    public static void saveChildArray(Context c, String prefName, String key, ArrayList<Child> array) {

        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(UserInfoStorage.PREFIX + key, array.toString());
        editor.commit();
    }

    public static JSONObject loadJSONObject(Context c, String prefName, String key) throws JSONException {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        return new JSONObject(settings.getString(UserInfoStorage.PREFIX + key, "{}"));
    }

    public static ArrayList<Child> loadChildArray(Context c, String prefName, String key) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        return new ArrayList<Child>((java.util.Collection<? extends Child>) settings.getAll()); // (UserInfoStorage.PREFIX + key, "[]")));
    }

    public static void saveParentArray(Context c, String prefName, String key, ArrayList<Parent> array) {

        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(UserInfoStorage.PREFIX + key, array.toString());
        editor.commit();
    }

    public static ArrayList<Parent> loadParentArray(Context c, String prefName, String key) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        return new ArrayList<Parent>((java.util.Collection<? extends Parent>) settings.getAll()); // Integer.parseInt(settings.getString(UserInfoStorage.PREFIX + key, "[]")));
    }

    public static void remove(Context c, String prefName, String key) {
        SharedPreferences settings = c.getSharedPreferences(prefName, 0);
        if (settings.contains(UserInfoStorage.PREFIX + key)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(UserInfoStorage.PREFIX + key);
            editor.commit();
        }
    }*/

    // These eight methods are used for maintaining favorites.
    public void saveChildren(Context context, List<Child> children) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(CHILD_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonParentFavorites = gson.toJson(children);

        editor.putString(CHILD_FAVORITES, jsonParentFavorites);

        editor.commit();
    }

    public ArrayList<Child> loadChildren(Context context) {
        SharedPreferences settings;
        List<Child> children;

        settings = context.getSharedPreferences(CHILD_PREFS_NAME,
                Context.MODE_PRIVATE);

        if (!settings.contains(CHILD_FAVORITES)) {
            return new ArrayList<Child>();
        } else {
            String jsonFavorites = settings.getString(CHILD_FAVORITES, null);
            Gson gson = new Gson();
            Child[] favoriteItems = gson.fromJson(jsonFavorites,
                    Child[].class);

            children = Arrays.asList(favoriteItems);
            children = new ArrayList<Child>(children);
            return (ArrayList<Child>) children;
        }
    }

    public void saveParents(Context context, List<Parent> parents) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;

        settings = context.getSharedPreferences(PARENT_PREFS_NAME,
                Context.MODE_PRIVATE);
        editor = settings.edit();

        Gson gson = new Gson();
        String jsonParentFavorites = gson.toJson(parents);

        editor.putString(PARENT_FAVORITES, jsonParentFavorites);

        editor.commit();
    }

    public ArrayList<Parent> loadParents(Context context) {
        SharedPreferences settings;
        List<Parent> parents;

        settings = context.getSharedPreferences(PARENT_PREFS_NAME,
                Context.MODE_PRIVATE);

        if (settings.contains(PARENT_FAVORITES))
        {
            String jsonFavorites = settings.getString(PARENT_FAVORITES, null);
            Gson gson = new Gson();
            Parent[] favoriteItems = gson.fromJson(jsonFavorites,
                    Parent[].class);

            parents = Arrays.asList(favoriteItems);
            parents = new ArrayList<Parent>(parents);

            return (ArrayList<Parent>) parents;
        }
        else
            return new ArrayList<Parent>();
    }

    public void addParent(Context context, Parent parent) {
        List<Parent> parents = loadParents(context);
        if (parents == null)
            parents = new ArrayList<Parent>();
        parents.add(parent);
        saveParents(context, parents);
    }

    public void removeParent(Context context, Parent parent) {
        ArrayList<Parent> parents = loadParents(context);
        if (parents != null) {
            parents.remove(parent);
            saveParents(context, parents);
        }
    }

    public void addChild(Context context, Child child) {
        List<Child> favorites = loadChildren(context);
        if (favorites == null)
            favorites = new ArrayList<Child>();
        favorites.add(child);
        saveChildren(context, favorites);
    }

    public void removeChild(Context context, Child child) {
        ArrayList<Child> favorites = loadChildren(context);
        if (favorites != null) {
            favorites.remove(child);
            saveChildren(context, favorites);
        }
    }
}
