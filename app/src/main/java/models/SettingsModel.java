package models;

public class SettingsModel {
    private int indoorEdaThreshold = 8; // DEFAULT VALUE
    private int outdoorEdaThreshold = 14; // DEFAULT VALUE
    private String emgPhone1;
    private String emgPhone2;
    private String emgPhoneLocation;

    public int getIndoorEdaThreshold() {
        return indoorEdaThreshold;
    }

    public void setIndoorEdaThreshold(int indoorEdaThreshold) {
        this.indoorEdaThreshold = indoorEdaThreshold;
    }

    public int getOutdoorEdaThreshold() {
        return outdoorEdaThreshold;
    }

    public void setOutdoorEdaThreshold(int outdoorEdaThreshold) {
        this.outdoorEdaThreshold = outdoorEdaThreshold;
    }

    public String getEmgPhone1() {
        return emgPhone1;
    }

    public void setEmgPhone1(String emgPhone1) {
        this.emgPhone1 = emgPhone1;
    }

    public String getEmgPhone2() {
        return emgPhone2;
    }

    public void setEmgPhone2(String emgPhone2) {
        this.emgPhone2 = emgPhone2;
    }

    public String getEmgPhoneLocation() {
        return emgPhoneLocation;
    }

    public void setEmgPhoneLocation(String emgPhoneLocation) {
        this.emgPhoneLocation = emgPhoneLocation;
    }
}
