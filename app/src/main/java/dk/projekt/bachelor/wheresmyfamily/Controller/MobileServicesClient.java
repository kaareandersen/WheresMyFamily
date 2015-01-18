package dk.projekt.bachelor.wheresmyfamily.Controller;

/**
 * Created by KaareAndersen on 25/09/14.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.ApiJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceJsonTable;
import com.microsoft.windowsazure.mobileservices.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.TableDeleteCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonOperationCallback;
import com.microsoft.windowsazure.mobileservices.TableJsonQueryCallback;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import dk.projekt.bachelor.wheresmyfamily.DataModel.WmfGeofence;
import dk.projekt.bachelor.wheresmyfamily.activities.Main;


public class MobileServicesClient {

    private MobileServiceClient mClient;
    private MobileServiceJsonTable mTableAccounts;
    private MobileServiceJsonTable mTableAuthData;
    private MobileServiceJsonTable mTableCalendarEvents;
    private MobileServiceJsonTable mTablePushNotification;
    private Context mContext;
    private final String TAG = "MobileServicesClient";

    public MobileServicesClient(Context context) {
        mContext = context;
        try {
            mClient = new MobileServiceClient("https://wheresmyfamilums.azure-mobile.net/",
                    "NEZImchCPkZquedmjaxCBZzeplOtqR99", mContext);
            mTableAccounts = mClient.getTable("Accounts");
            mTableAuthData = mClient.getTable("AuthData");
            mTableCalendarEvents = mClient.getTable("CalendarEvents");
            mTablePushNotification = mClient.getTable("PushNotifacation");
        } catch (MalformedURLException e) {
            Log.e(TAG, "There was an error creating the Mobile Service.  Verify the URL");
        }
    }

    public void setContext(Context context) {
        mClient.setContext(context);
    }

    public String getUserId() {
        return mClient.getCurrentUser().getUserId();
    }

    public void callApi()
    {
        mClient.invokeApi("pushnotificationapi",
                new ApiJsonOperationCallback() {
                    @Override
                    public void onCompleted(JsonElement jsonData, Exception error,
                                            ServiceFilterResponse response) {
                        Log.i("JsonData", jsonData.getAsJsonObject().get("message").getAsString());
                    }
                });
    }


    @SuppressWarnings("unchecked")
    public void login(final String email, final String password, final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
            try {
                JsonObject customUser = new JsonObject();
                customUser.addProperty("email", email);
                customUser.addProperty("password", password);
                List<Pair<String, String>> parameters = new ArrayList<Pair<String, String>>();
                parameters.add(new Pair<String, String>("login", "true"));

                mTableAccounts.insert(customUser, parameters, callback);
            } catch (Exception e) {
                Log.e(TAG, "Issue registering with hub: " + e.getMessage());
                return e;
            }
            return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void sendEmailPassW(final String email, final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    JsonObject emailPassWord = new JsonObject();
                    emailPassWord.addProperty("email", email);
                    List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
                    parameters.add(new Pair<String, String>("email", "true"));

                    mTableAccounts.insert(emailPassWord, parameters, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue sending email: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void getAuthData(final TableJsonQueryCallback callback) {
        new AsyncTask() {
                @Override
                protected Object doInBackground(Object... params) {
                    try {
                        mTableAuthData.where().execute(callback);
                    } catch (Exception e) {
                        Log.e(TAG, "Issue gettin auth data: " + e.getMessage());
                        return e;
                    }
                    return null;
                }
        }.execute(null, null, null);
    }

    /**
     * Checks to see if we have userId and token stored on the device and sets them if so
     */
    public boolean isUserAuthenticated() {
        SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        if (settings != null) {
            String userId = settings.getString("userid", null);
            String token = settings.getString("token", null);
            if (userId != null && !userId.equals("")) {
                setUserData(userId, token);
                return true;
            }
        }
        return false;
    }

    /**
     * Creates a new MobileServiceUser using a userId and token passed in.
     * Also sets the current provider
     */
    @SuppressWarnings("unchecked")
    public void setUserData(final String userId, final String token) {
        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mClient.setCurrentUser(user);
    }

    /***
     * Pulls the user ID and token out of a json object from the server
     */
    public void setUserAndSaveData(JsonObject jsonObject) {
        String userId = jsonObject.getAsJsonPrimitive("userId").getAsString();
        String token = jsonObject.getAsJsonPrimitive("token").getAsString();
        setUserData(userId, token);
        saveUserData();
    }

    /**
     * Saves userId and token to SharedPreferences.
     * NOTE:  This is not secure and is just used as a storage mechanism.  In reality, you would want to
     * come up with a more secure way of storing this information.
     */
    public void saveUserData() {
        SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
        SharedPreferences.Editor preferencesEditor = settings.edit();
        preferencesEditor.putString("userid", mClient.getCurrentUser().getUserId());
        preferencesEditor.putString("token", mClient.getCurrentUser().getAuthenticationToken());
        preferencesEditor.commit();
    }

    /**
     * Register the user if they're creating a custom auth account
     */
    @SuppressWarnings("unchecked")
    public void registerUser(final String username, final String password, String confirm,
                             final String email, final String phone, final boolean child,
                             final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    JsonObject newUser = new JsonObject();
                    newUser.addProperty("username", username);
                    newUser.addProperty("password", password);
                    newUser.addProperty("email", email);
                    newUser.addProperty("phone", phone);
                    newUser.addProperty("child", child);
                    mTableAccounts.insert(newUser, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue saving user data: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

   @SuppressWarnings("unchecked")
   public void deleteUser() {
       new AsyncTask() {
           @Override
           protected Object doInBackground(Object... params) {
               try {
                    String id = getUserId();
                    String[] sep = id.split(":");

                    mTableAccounts.delete(sep[1], new TableDeleteCallback() {
                    @Override
                    public void onCompleted(Exception exception, ServiceFilterResponse response) {
                        if (exception == null) {
                            Log.i(TAG, "Object deleted");
                            //logout
                            logout(true);
                        }
                    }
                    });
               } catch (Exception e) {
                   Log.e(TAG, "Issue saving user data: " + e.getMessage());
                   return e;
               }
               return null;
           }
       }.execute(null, null, null);
   }

    /**
     * Handles logging the user out including:
     * -deleting cookies so their login with a provider won't be cached in the web view
     * -removing the userdata from the shared preferences
     * -setting the current user object on the client to logged out
     * -optionally redirects to the login page if requested
     */
    @SuppressWarnings("unchecked")
    public void logout(final boolean shouldRedirectToLogin) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    //Clear the cookies so they won't auto login to a provider again
                    CookieSyncManager.createInstance(mContext);
                    CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.removeAllCookie();
                    //Clear the user id and token from the shared preferences
                    SharedPreferences settings = mContext.getSharedPreferences("UserData", 0);
                    SharedPreferences.Editor preferencesEditor = settings.edit();
                    preferencesEditor.clear();
                    preferencesEditor.commit();
                    //Clear the user and return to the auth activity
                    mClient.logout();
                    //Take the user back to the auth activity to relogin if requested
                        if (shouldRedirectToLogin) {
                            Intent logoutIntent = new Intent(mContext, Main.class);
                            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(logoutIntent);
                        }
                } catch (Exception e) {
                    Log.e(TAG, "Issue logging out: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void newCalEvent(final String partitionkey, final String rowkey,
                             final String eventname, final WmfGeofence location, final String child,
                             final String startdate, final String starttime,
                             final String enddate, final String endtime, final long expiration, final String repeat,
                             final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    JsonObject newEvent = new JsonObject();
                    newEvent.addProperty("PartitionKey", partitionkey);
                    newEvent.addProperty("RowKey", rowkey);
                    newEvent.addProperty("EventName", eventname);
                    /*newEvent.addProperty("GeofenceId", location.getGeofenceId());
                    newEvent.addProperty("Latitude", location.getLatitude());
                    newEvent.addProperty("Longitude", location.getLongitude());
                    newEvent.addProperty("Radius", location.getRadius());*/
                    newEvent.addProperty("Child", child);
                    newEvent.addProperty("StartDate", startdate);
                    newEvent.addProperty("StartTime", starttime);
                    newEvent.addProperty("EndDate", enddate);
                    newEvent.addProperty("EndTime", endtime);
                    newEvent.addProperty("Expiration", expiration);
                    newEvent.addProperty("Repeat", repeat);
                    mTableCalendarEvents.insert(newEvent, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue adding new calendar event: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void getCalendarEvent(final String id, final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    mTableCalendarEvents.lookUp(id,  callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue getting calendar event: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void getLocation(final String childEmail, final TableJsonOperationCallback callback){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    JsonObject getLoc = new JsonObject();
                    getLoc.addProperty("email", childEmail);
                    List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
                    parameters.add(new Pair<String, String>("getlocation", "true"));

                    mTablePushNotification.insert(getLoc, parameters, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue getting child location: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    @SuppressWarnings("unchecked")
    public void sendLocation(final String parentEmail, final String location, final TableJsonOperationCallback callback) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                try {
                    JsonObject sendLoc = new JsonObject();
                    sendLoc.addProperty("email", parentEmail);
                    sendLoc.addProperty("location", location);
                    List<Pair<String,String>> parameters = new ArrayList<Pair<String, String>>();
                    parameters.add(new Pair<String, String>("sendlocation", "true"));

                    mTablePushNotification.insert(sendLoc, parameters, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Issue sending child location: " + e.getMessage());
                    return e;
                }
                return null;
            }
        }.execute(null, null, null);
    }
}
