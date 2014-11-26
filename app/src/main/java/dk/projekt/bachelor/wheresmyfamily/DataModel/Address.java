package dk.projekt.bachelor.wheresmyfamily.DataModel;

import java.util.Locale;

/**
 * Created by Tommy on 25-11-2014.
 */
public class Address {

    //region Fields
    Locale locale;
    String streetNumber;
    String route;
    String premise;
    String subPremise;
    String floor;
    String room;
    String neighborhood;
    String locality;
    String subLocality;
    String adminArea;
    String subAdminArea;
    String subAdminArea2;
    String countryName;
    String countryCode;
    String postalCode;
    String formattedAddress;
    Area viewPort;
    Area bounds;
    double latitude;
    double longitude;
    //endregion

    public Address()
    {
        locale = Locale.getDefault();
    }

    public Address(Locale _locale)
    {
        locale = _locale;
    }

    //region Get and set
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    public String getStreetNumber() {
        return streetNumber;
    }
    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }
    public String getRoute() {
        return route;
    }
    public void setRoute(String route) {
        this.route = route;
    }
    public String getPremise() {
        return premise;
    }
    public void setPremise(String premise) {
        this.premise = premise;
    }
    public String getSubPremise() {
        return subPremise;
    }
    public void setSubPremise(String subPremise) {
        this.subPremise = subPremise;
    }
    public String getFloor() {
        return floor;
    }
    public void setFloor(String floor) {
        this.floor = floor;
    }
    public String getRoom() {
        return room;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public String getNeighborhood() {
        return neighborhood;
    }
    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }
    public String getLocality() {
        return locality;
    }
    public void setLocality(String locality) {
        this.locality = locality;
    }
    public String getSubLocality() {
        return subLocality;
    }
    public void setSubLocality(String subLocality) {
        this.subLocality = subLocality;
    }
    public String getAdminArea() {
        return adminArea;
    }
    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }
    public String getSubAdminArea() {
        return subAdminArea;
    }
    public void setSubAdminArea(String subAdminArea) {
        this.subAdminArea = subAdminArea;
    }
    public String getSubAdminArea2() {
        return subAdminArea2;
    }
    public void setSubAdminArea2(String subAdminArea2) {
        this.subAdminArea2 = subAdminArea2;
    }
    public String getCountryName() {
        return countryName;
    }
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    public String getPostalCode() {
        return postalCode;
    }
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    public String getFormattedAddress() {
        return formattedAddress;
    }
    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }
    public Area getViewPort() {
        return viewPort;
    }
    public void setViewPort(Area viewPort) {
        this.viewPort = viewPort;
    }
    public Area getBounds() {
        return bounds;
    }
    public void setBounds(Area bounds) {
        this.bounds = bounds;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    //endregion
}
