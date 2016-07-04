package fiveship.vn.fiveship.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sonnd on 05/12/2015.
 */
public class StepDirectionItem {
    int DurationValue;
    String Duration;
    int DistanceValue;
    String Distance;
    String StartAddress;
    String EndAddress;
    String Summary;
    LatLng StartPosition;
    LatLng EndPosition;

    public StepDirectionItem() {
    }

    public StepDirectionItem(int durationValue, String duration, int distanceValue, String distance, String startAddress, String endAddress,String summary) {
        DurationValue = durationValue;
        Duration = duration;
        DistanceValue = distanceValue;
        Distance = distance;
        StartAddress = startAddress;
        EndAddress = endAddress;
        Summary = summary;
    }

    public StepDirectionItem(int durationValue, String duration, int distanceValue, String distance, String startAddress, String endAddress, String summary, LatLng startPosition, LatLng endPosition) {
        DurationValue = durationValue;
        Duration = duration;
        DistanceValue = distanceValue;
        Distance = distance;
        StartAddress = startAddress;
        EndAddress = endAddress;
        Summary = summary;
        StartPosition = startPosition;
        EndPosition = endPosition;
    }

    public LatLng getStartPosition() {
        return StartPosition;
    }

    public LatLng getEndPosition() {
        return EndPosition;
    }

    public void setStartPosition(LatLng startPosition) {
        StartPosition = startPosition;
    }

    public void setEndPosition(LatLng endPosition) {
        EndPosition = endPosition;
    }

    public String getSummary() {
        return Summary;
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public int getDurationValue() {
        return DurationValue;
    }

    public void setDurationValue(int durationValue) {
        DurationValue = durationValue;
    }

    public String getDuration() {
        return Duration;
    }

    public void setDuration(String duration) {
        Duration = duration;
    }

    public int getDistanceValue() {
        return DistanceValue;
    }

    public void setDistanceValue(int distanceValue) {
        DistanceValue = distanceValue;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    public String getStartAddress() {
        return StartAddress;
    }

    public void setStartAddress(String startAddress) {
        StartAddress = startAddress;
    }

    public String getEndAddress() {
        return EndAddress;
    }

    public void setEndAddress(String endAddress) {
        EndAddress = endAddress;
    }
}
