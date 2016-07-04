package fiveship.vn.fiveship.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fiveship.vn.fiveship.R;
import fiveship.vn.fiveship.model.StepDirectionItem;
import fiveship.vn.fiveship.service.GoogleService;

@SuppressLint("NewApi")
public class GoogleDirection {
    public final static String MODE_DRIVING = "driving";
    public final static String MODE_WALKING = "walking";
    public final static String MODE_BICYCLING = "bicycling";

    public final static String STATUS_OK = "OK";
    public final static String STATUS_NOT_FOUND = "NOT_FOUND";
    public final static String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
    public final static String STATUS_MAX_WAYPOINTS_EXCEEDED = "MAX_WAYPOINTS_EXCEEDED";
    public final static String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
    public final static String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    public final static String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
    public final static String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";

    public final static String VIETNAM_LANGUAGE_CODE = "vi";
    public final static String KEY_API = "AIzaSyCTDkSS8fZVh86d0KCdif0cq6nYOlHpDks";

    public final static int SPEED_VERY_VERY_FAST = 0;
    public final static int SPEED_VERY_FAST = 1;
    public final static int SPEED_FAST = 2;
    public final static int SPEED_NORMAL = 3;
    public final static int SPEED_SLOW = 4;
    public final static int SPEED_VERY_SLOW = 5;

    private OnDirectionResponseListener mDirectionListener = null;
    private OnAnimateListener mAnimateListener = null;
    private GoogleService service;

    private boolean isLogging = true;

    private LatLng animateMarkerPosition = null;
    private LatLng beginPosition = null;
    private LatLng endPosition = null;
    private ArrayList<LatLng> animatePositionList = null;
    private Marker animateMarker = null;
    private Polyline animateLine = null;
    private GoogleMap gm = null;
    private int step = -1;
    private int animateSpeed = -1;
    private int zoom = -1;
    private double animateDistance = -1;
    private double animateCamera = -1;
    private double totalAnimateDistance = 0;
    private boolean cameraLock = false;
    private boolean drawMarker = false;
    private boolean drawLine = false;
    private boolean flatMarker = false;
    private boolean isCameraTilt = false;
    private boolean isCameraZoom = false;
    private boolean isAnimated = false;
    private ArrayList<LatLng> wayPoints;

    private Context mContext = null;

    public GoogleDirection(Context context) {
        mContext = context;
    }

    public void request(LatLng start, LatLng end, String mode, ArrayList<LatLng> wayPoints) {

        this.service = GoogleService.get_instance(mContext);
        this.wayPoints = wayPoints;

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("origin", start.latitude + "," + start.longitude);
        hashMap.put("destination", end.latitude + "," + end.longitude);
        hashMap.put("sensor", String.valueOf(false));
        hashMap.put("units", "metric");
        hashMap.put("mode", mode);
        hashMap.put("language", VIETNAM_LANGUAGE_CODE);

        if (wayPoints != null && wayPoints.size() > 0)
            hashMap.put("waypoints", getWayPointsToString(wayPoints));

        hashMap.put("key", KEY_API);

        new RequestTask().execute(hashMap);
    }

    private class RequestTask extends AsyncTask<HashMap<String, String>, Void, Document> {

        @Override
        protected final Document doInBackground(HashMap<String, String>... params) {
            return service.directionRequest(params[0]);
        }

        @Override
        protected void onPostExecute(Document doc) {
            super.onPostExecute(doc);
            if (mDirectionListener != null
                    && doc != null
                    && doc.getChildNodes() != null
                    && doc.getChildNodes().getLength() > 0)
                mDirectionListener.onResponse(getStatus(doc), doc, GoogleDirection.this);
            else
                if(mAnimateListener != null)
                    mAnimateListener.onFinish(false);
        }

        private String getStatus(Document doc) {
            NodeList nl1 = doc.getElementsByTagName("status");
            if (nl1 != null) {
                Node node1 = nl1.item(0);
                if (isLogging)
                    Log.i("GoogleDirection", "Status : " + node1.getTextContent());
                return node1.getTextContent();
            }
            return "";
        }
    }

    public void setLogging(boolean state) {
        isLogging = state;
    }

    public double Haversine(LatLng from, LatLng to)//double lat1, double lon1, double lat2, double lon2
    {
        final double R = 6372.8; // Earth Radius in Kilometers

        double dLat = Deg2Rad(to.latitude - from.latitude);
        double dLon = Deg2Rad(to.longitude - from.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Deg2Rad(from.latitude)) * Math.cos(Deg2Rad(to.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;

        // Return Distance in Kilometers
        return d;
    }

    public double Deg2Rad(double deg) {
        return deg * Math.PI / 180;
    }

    public String getStatus(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("status");
        Node node1 = nl1.item(0);
        if (isLogging)
            Log.i("GoogleDirection", "Status : " + node1.getTextContent());
        return node1.getTextContent();
    }

    public String[] getDurationText(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        String[] arr_str = new String[nl1.getLength() - 1];
        for (int i = 0; i < nl1.getLength() - 1; i++) {
            Node node1 = nl1.item(i);
            NodeList nl2 = node1.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "text"));
            arr_str[i] = node2.getTextContent();
            if (isLogging)
                Log.i("GoogleDirection", "DurationText : " + node2.getTextContent());
        }
        return arr_str;
    }

    public int[] getDurationValue(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        int[] arr_int = new int[nl1.getLength() - 1];
        for (int i = 0; i < nl1.getLength() - 1; i++) {
            Node node1 = nl1.item(i);
            NodeList nl2 = node1.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "value"));
            arr_int[i] = Integer.parseInt(node2.getTextContent());
            if (isLogging)
                Log.i("GoogleDirection", "Duration : " + node2.getTextContent());
        }
        return arr_int;
    }

    public String getTotalDurationText(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        Node node1 = nl1.item(nl1.getLength() - 1);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "text"));
        if (isLogging)
            Log.i("GoogleDirection", "TotalDuration : " + node2.getTextContent());
        return node2.getTextContent();
    }

    public int getTotalDurationValue(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("duration");
        Node node1 = nl1.item(nl1.getLength() - 1);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "value"));
        if (isLogging)
            Log.i("GoogleDirection", "TotalDuration : " + node2.getTextContent());
        return Integer.parseInt(node2.getTextContent());
    }

    public String[] getDistanceText(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("distance");
        String[] arr_str = new String[nl1.getLength() - 1];
        for (int i = 0; i < nl1.getLength() - 1; i++) {
            Node node1 = nl1.item(i);
            NodeList nl2 = node1.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "text"));
            arr_str[i] = node2.getTextContent();
            if (isLogging)
                Log.i("GoogleDirection", "DurationText : " + node2.getTextContent());
        }
        return arr_str;
    }

    public int[] getDistanceValue(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("distance");
        int[] arr_int = new int[nl1.getLength() - 1];
        for (int i = 0; i < nl1.getLength() - 1; i++) {
            Node node1 = nl1.item(i);
            NodeList nl2 = node1.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "value"));
            arr_int[i] = Integer.parseInt(node2.getTextContent());
            if (isLogging)
                Log.i("GoogleDirection", "Duration : " + node2.getTextContent());
        }
        return arr_int;
    }

    public String getTotalDistanceText(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("distance");
        Node node1 = nl1.item(nl1.getLength() - 1);
        NodeList nl2 = node1.getChildNodes();
        Node node2 = nl2.item(getNodeIndex(nl2, "text"));
        if (isLogging)
            Log.i("GoogleDirection", "TotalDuration : " + node2.getTextContent());
        return node2.getTextContent();
    }

    public int getTotalDistanceValue(Document doc) {

        NodeList nl1 = doc.getElementsByTagName("distance");
        int totalDistance = 0;
        for (int i = 0; i < nl1.getLength() - 1; i++) {

            Node node = nl1.item(i);
            NodeList nl2 = node.getChildNodes();
            Node node2 = nl2.item(getNodeIndex(nl2, "value"));
            totalDistance += Integer.parseInt(node2.getTextContent());

        }
        if (isLogging)
            Log.i("GoogleDirection", "TotalDuration : " + totalDistance);
        return totalDistance;
    }

    public String getStartAddress(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("start_address");

        if (nl1 == null) return "";

        Node node1 = nl1.item(0);
        if (isLogging)
            Log.i("GoogleDirection", "StartAddress : " + node1.getTextContent());
        return node1.getTextContent();
    }

    public String getEndAddress(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("end_address");

        if(nl1 == null) return "";

        Node node1 = nl1.item(nl1.getLength() - 1);
        if (isLogging)
            Log.i("GoogleDirection", "EndAddress : " + node1.getTextContent());
        return node1.getTextContent();
    }

    public String getCopyRights(Document doc) {
        NodeList nl1 = doc.getElementsByTagName("copyrights");

        if(nl1 == null) return "";

        Node node1 = nl1.item(0);
        if (isLogging)
            Log.i("GoogleDirection", "CopyRights : " + node1.getTextContent());
        return node1.getTextContent();
    }

    public ArrayList<LatLng> getDirection(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));

                locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for (int j = 0; j < arr.size(); j++) {
                    listGeopoints.add(new LatLng(arr.get(j).latitude
                            , arr.get(j).longitude));
                }

                locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }

        return listGeopoints;
    }

    public ArrayList<StepDirectionItem> getSection(Document doc) {
        NodeList nl1, nl2, nl3, nl4, nl5, nl6;
        ArrayList<StepDirectionItem> listSteps = new ArrayList<>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationStartNode = nl2.item(getNodeIndex(nl2, "start_location"));
                nl3 = locationStartNode.getChildNodes();
                Node slatNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(slatNode.getTextContent());
                Node slngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(slngNode.getTextContent());

                Node locationEndNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl6 = locationEndNode.getChildNodes();
                Node elatNode = nl6.item(getNodeIndex(nl6, "lat"));
                double _lat = Double.parseDouble(elatNode.getTextContent());
                Node lngNode = nl6.item(getNodeIndex(nl6, "lng"));
                double _lng = Double.parseDouble(lngNode.getTextContent());

                Node durationNode = nl2.item(getNodeIndex(nl2, "duration"));
                nl4 = durationNode.getChildNodes();
                Node textDurationNode = nl4.item(getNodeIndex(nl4, "text"));
                String textDuration = textDurationNode.getTextContent();
                Node valueDurationNode = nl4.item(getNodeIndex(nl4, "value"));
                int valueDuration = Integer.parseInt(valueDurationNode.getTextContent());

                Node distanceNode = nl2.item(getNodeIndex(nl2, "distance"));
                nl5 = distanceNode.getChildNodes();
                Node textDistanceNode = nl5.item(getNodeIndex(nl5, "text"));
                String textDistance = textDistanceNode.getTextContent();
                Node valueDistanceNode = nl5.item(getNodeIndex(nl5, "value"));
                int valueDistance = Integer.parseInt(valueDistanceNode.getTextContent());

                Node summaryNode = nl2.item(getNodeIndex(nl2, "html_instructions"));
                String summary = summaryNode.getTextContent();


                listSteps.add(new StepDirectionItem(valueDuration, textDuration, valueDistance, textDistance, "", "", summary, new LatLng(lat, lng), new LatLng(_lat, _lng)));
            }
        }

        return listSteps;
    }

    public LatLngBounds getBound(Document doc) {
        NodeList nl;
        nl = doc.getElementsByTagName("bounds");

        if(nl == null) return null;

        LatLngBounds latLngBounds;
        if (nl.getLength() > 0) {
            NodeList bounds = nl.item(0).getChildNodes();
            NodeList southWest = bounds.item(getNodeIndex(bounds, "southwest")).getChildNodes();
            Node swLatNode = southWest.item(getNodeIndex(southWest, "lat"));
            Node swLngNode = southWest.item(getNodeIndex(southWest, "lng"));
            double swLat = Double.parseDouble(swLatNode.getTextContent());
            double swLng = Double.parseDouble(swLngNode.getTextContent());

            NodeList northEast = bounds.item(getNodeIndex(bounds, "northeast")).getChildNodes();
            Node neLatNode = northEast.item(getNodeIndex(northEast, "lat"));
            Node neLngNode = northEast.item(getNodeIndex(northEast, "lng"));
            double neLat = Double.parseDouble(neLatNode.getTextContent());
            double neLng = Double.parseDouble(neLngNode.getTextContent());

            latLngBounds = new LatLngBounds(new LatLng(swLat, swLng), new LatLng(neLat, neLng));
            return latLngBounds;
        }

        return null;
    }

    public PolylineOptions getPolyline(Document doc, int width, int color) {
        ArrayList<LatLng> arr_pos = getDirection(doc);
        PolylineOptions rectLine = new PolylineOptions().width(dpToPx(width)).color(color);
        for (int i = 0; i < arr_pos.size(); i++)
            rectLine.add(arr_pos.get(i));
        return rectLine;
    }

    private int getNodeIndex(NodeList nl, String nodename) {
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }

    /**
     * Encodes a sequence of LatLngs into an encoded path string.
     */

    public String getWayPointsToString(final List<LatLng> path) {

        StringBuilder result = new StringBuilder();
        result.append("optimize:true|");
        for (final LatLng point : path) {
            result.append(point.latitude + "," + point.longitude + "|");
        }
        return result.toString();
    }

    private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public void setOnDirectionResponseListener(OnDirectionResponseListener listener) {
        mDirectionListener = listener;
    }

    public void setOnAnimateListener(OnAnimateListener listener) {
        mAnimateListener = listener;
    }

    public interface OnDirectionResponseListener {
        void onResponse(String status, Document doc, GoogleDirection gd);
    }

    public interface OnAnimateListener {
        void onFinish(boolean mIsSuccess);

        void onStart();

        void onProgress(int progress, int total);
    }

    public void directDirection(GoogleMap gm, ArrayList<LatLng> direction, boolean drawMarker, MarkerOptions mo, boolean flatMarker
            , boolean drawLine, PolylineOptions po) {
        if (direction.size() > 1) {
            this.drawMarker = drawMarker;
            this.drawLine = drawLine;
            this.flatMarker = flatMarker;
            animatePositionList = direction;
            this.gm = gm;
            step = 0;
        }
        if (mAnimateListener != null)
            mAnimateListener.onStart();

        if (animatePositionList != null && animatePositionList.size() > 0) {
            beginPosition = animatePositionList.get(step);
            endPosition = animatePositionList.get(animatePositionList.size() - 1);

            if (drawMarker) {
                if (mo != null)
                    animateMarker = gm.addMarker(mo.position(beginPosition));
                else
                    animateMarker = gm.addMarker(new MarkerOptions().position(beginPosition));

                if (flatMarker) {
                    animateMarker.setFlat(true);
                }
            }

            if (drawLine) {
                if (po != null)
                    animateLine = gm.addPolyline(po.addAll(animatePositionList).width(dpToPx((int) po.getWidth())));
                else
                    animateLine = gm.addPolyline(new PolylineOptions().addAll(animatePositionList).width(dpToPx(5)));
            }

            if (drawMarker && wayPoints != null && wayPoints.size() > 0) {
                for (LatLng location : wayPoints) {
                    gm.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.end_flag))
                            .position(location));
                }
            }

            if (drawMarker)
                animateMarker.setPosition(endPosition);

            if (mAnimateListener != null)
                mAnimateListener.onFinish(true);
        } else {
            if (mAnimateListener != null)
                mAnimateListener.onFinish(false);
        }

    }

    public void animateDirection(GoogleMap gm, ArrayList<LatLng> direction, int speed
            , boolean cameraLock, boolean isCameraTilt, boolean isCameraZoom
            , boolean drawMarker, MarkerOptions mo, boolean flatMarker
            , boolean drawLine, PolylineOptions po) {
        if (direction.size() > 1) {
            isAnimated = true;
            animatePositionList = direction;
            animateSpeed = speed;
            this.drawMarker = drawMarker;
            this.drawLine = drawLine;
            this.flatMarker = flatMarker;
            this.isCameraTilt = isCameraTilt;
            this.isCameraZoom = isCameraZoom;
            step = 0;
            this.cameraLock = cameraLock;
            this.gm = gm;

            setCameraUpdateSpeed(speed);

            beginPosition = animatePositionList.get(step);
            endPosition = animatePositionList.get(step + 1);

            animateMarkerPosition = beginPosition;

            if (mAnimateListener != null)
                mAnimateListener.onProgress(step, animatePositionList.size());

            if (cameraLock) {
                float bearing = getBearing(beginPosition, endPosition);
                CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
                        .target(animateMarkerPosition).bearing(bearing);

                if (isCameraTilt)
                    cameraBuilder.tilt(90);
                else
                    cameraBuilder.tilt(gm.getCameraPosition().tilt);

                if (isCameraZoom)
                    cameraBuilder.zoom(zoom);
                else
                    cameraBuilder.zoom(gm.getCameraPosition().zoom);

                CameraPosition cameraPosition = cameraBuilder.build();
                gm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            if (drawMarker) {
                if (mo != null)
                    animateMarker = gm.addMarker(mo.position(beginPosition));
                else
                    animateMarker = gm.addMarker(new MarkerOptions().position(beginPosition));

                if (flatMarker) {
                    animateMarker.setFlat(true);

                    float rotation = getBearing(animateMarkerPosition, endPosition) + 180;
                    animateMarker.setRotation(rotation);
                }
            }


            if (drawLine) {
                if (po != null)
                    animateLine = gm.addPolyline(po.add(beginPosition)
                            .add(beginPosition).add(endPosition)
                            .width(dpToPx((int) po.getWidth())));
                else
                    animateLine = gm.addPolyline(new PolylineOptions()
                            .width(dpToPx(5)));
            }

            new Handler().postDelayed(r, speed);
            if (mAnimateListener != null)
                mAnimateListener.onStart();
        } else {
            if (mAnimateListener != null)
                mAnimateListener.onFinish(false);
        }
    }

    public void cancelAnimated() {
        isAnimated = false;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    private Runnable r = new Runnable() {
        public void run() {
            animateMarkerPosition = getNewPosition(animateMarkerPosition, endPosition);
            if (drawMarker)
                animateMarker.setPosition(animateMarkerPosition);


            if (drawLine) {
                List<LatLng> points = animateLine.getPoints();
                points.add(animateMarkerPosition);
                animateLine.setPoints(points);
            }

            if (animateMarkerPosition.latitude == endPosition.latitude
                    && animateMarkerPosition.longitude == endPosition.longitude) {
                if (step == animatePositionList.size() - 2) {
                    isAnimated = false;
                    totalAnimateDistance = 0;
                    if (mAnimateListener != null)
                        mAnimateListener.onFinish(true);
                } else {
                    step++;
                    beginPosition = animatePositionList.get(step);
                    endPosition = animatePositionList.get(step + 1);
                    animateMarkerPosition = beginPosition;

                    if (flatMarker && step + 3 < animatePositionList.size() - 1) {
                        float rotation = getBearing(animateMarkerPosition, animatePositionList.get(step + 3)) + 180;
                        animateMarker.setRotation(rotation);
                    }

                    if (mAnimateListener != null)
                        mAnimateListener.onProgress(step, animatePositionList.size());
                }
            }

            if (cameraLock && (totalAnimateDistance > animateCamera || !isAnimated)) {
                totalAnimateDistance = 0;
                float bearing = getBearing(beginPosition, endPosition);
                CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
                        .target(animateMarkerPosition).bearing(bearing);

                if (isCameraTilt)
                    cameraBuilder.tilt(90);
                else
                    cameraBuilder.tilt(gm.getCameraPosition().tilt);

                if (isCameraZoom)
                    cameraBuilder.zoom(zoom);
                else
                    cameraBuilder.zoom(gm.getCameraPosition().zoom);

                CameraPosition cameraPosition = cameraBuilder.build();
                gm.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            }

            Log.i("GoogleDirection", "LatLng : " + animateMarkerPosition.latitude);

            if (isAnimated) {
                new Handler().postDelayed(r, animateSpeed);
            }
        }
    };

    public Marker getAnimateMarker() {
        return animateMarker;
    }

    public Polyline getAnimatePolyline() {
        return animateLine;
    }

    private LatLng getNewPosition(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        double dis = Math.sqrt(Math.pow(lat, 2) + Math.pow(lng, 2));
        if (dis >= animateDistance) {
            double angle = -1;

            if (begin.latitude <= end.latitude && begin.longitude <= end.longitude)
                angle = Math.toDegrees(Math.atan(lng / lat));
            else if (begin.latitude > end.latitude && begin.longitude <= end.longitude)
                angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 90;
            else if (begin.latitude > end.latitude && begin.longitude > end.longitude)
                angle = Math.toDegrees(Math.atan(lng / lat)) + 180;
            else if (begin.latitude <= end.latitude && begin.longitude > end.longitude)
                angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 270;

            double x = Math.cos(Math.toRadians(angle)) * animateDistance;
            double y = Math.sin(Math.toRadians(angle)) * animateDistance;
            totalAnimateDistance += animateDistance;
            double finalLat = begin.latitude + x;
            double finalLng = begin.longitude + y;

            return new LatLng(finalLat, finalLng);
        } else {
            return end;
        }
    }

    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);
        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    public void setCameraUpdateSpeed(int speed) {
        if (speed == SPEED_VERY_SLOW) {
            animateDistance = 0.000005;
            animateSpeed = 20;
            animateCamera = 0.0004;
            zoom = 19;
        } else if (speed == SPEED_SLOW) {
            animateDistance = 0.00001;
            animateSpeed = 20;
            animateCamera = 0.0008;
            zoom = 18;
        } else if (speed == SPEED_NORMAL) {
            animateDistance = 0.00005;
            animateSpeed = 20;
            animateCamera = 0.002;
            zoom = 16;
        } else if (speed == SPEED_FAST) {
            animateDistance = 0.0001;
            animateSpeed = 20;
            animateCamera = 0.004;
            zoom = 15;
        } else if (speed == SPEED_VERY_FAST) {
            animateDistance = 0.0005;
            animateSpeed = 20;
            animateCamera = 0.004;
            zoom = 13;
        } else if (speed == SPEED_VERY_VERY_FAST) {
            animateDistance = 1;
            animateSpeed = 10;
            animateCamera = 5;
            zoom = 16;
        } else {
            animateDistance = 0.00005;
            animateSpeed = 20;
            animateCamera = 0.002;
            zoom = 16;
        }
    }

}

