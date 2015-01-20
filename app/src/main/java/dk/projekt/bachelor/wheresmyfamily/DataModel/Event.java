package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.io.Serializable;

/**
 * Created by Tommy on 20-01-2015.
 */
public class Event implements Serializable {

    private String eventName;
    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;
    private String childName;

    public Event() {}

    public Event(String _eventName, String _startDate, String _endDate, String _startTime, String _endTime, String _childName)
    {
        setEventName(_eventName);
        setStartDate(_startDate);
        setEndDate(_endDate);
        setStartTime(_startTime);
        setEndTime(_endTime);
        setChildName("");
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    public String getStartDate() {
        return startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getChildName() {
        return childName;
    }
    public void setChildName(String childName) {
        this.childName = childName;
    }
}
