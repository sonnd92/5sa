package fiveship.vn.fiveship.service;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import fiveship.vn.fiveship.utility.Config;
import fiveship.vn.fiveship.utility.SessionManager;
import fiveship.vn.fiveship.utility.Utils;

/**
 * Created by sonnd on 08/10/2015.
 */
public class BaseService {
    private final int TIME_OUT_MILLISECOND = 10000;
    private final String DISCONNECTED_DIALOG_FRAGMENT_TAG = "WarningDisconnectedDialogFragment";
    private Context mContext;
    private SessionManager mSessionManager;

    public BaseService(Context mContext) {
        this.mContext = mContext;
        this.mSessionManager = new SessionManager(mContext);
    }

    public String readJSONWS(String targetUrl, Map<String, String> params) {

        String query = "";

        StringBuilder builder = new StringBuilder();
        InputStream inputStream;
        HttpURLConnection urlConnection = null;
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {

                query += ("".equals(query) ? "" : "&") + entry.getKey() + "=" + URLEncoder.encode(entry.getValue() != null ? String.valueOf(entry.getValue()) : "", "UTF-8");

            }
            URL url = new URL(Config.API_LINK + targetUrl + "?" + query);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty(
                    "clientId", Utils.getImei(mContext)
                            + "_"
                            + Utils.getOSVersion()
                            + "_"
                            + Utils.getManufacturer()
                            + "_"
                            + Utils.getModel()
                            + "_"
                            + mSessionManager.getShipperId()
                            + "_"
                            + mSessionManager.getShopId()
            );
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(TIME_OUT_MILLISECOND);
            urlConnection.setReadTimeout(TIME_OUT_MILLISECOND);

            /* 200 represents HTTP OK */
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
                builder.append('\r');
            }
            /* Close Stream */
            inputStream.close();
            bufferedReader.close();
        } catch (Exception e) {

            if (!Utils.isConnectingToInternet(mContext)
                    && ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentByTag(DISCONNECTED_DIALOG_FRAGMENT_TAG) == null) {
                Utils.getDialogNoInternetConnection(mContext)
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), DISCONNECTED_DIALOG_FRAGMENT_TAG);
            }

            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return builder.toString();
    }

    public String PostJson(String targetURL, JSONObject jsonObject) {

        URL url;
        HttpURLConnection connection = null;
        try {

            //Create connection
            url = new URL(Config.API_LINK + targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty(
                    "clientId",
                    Utils.getImei(mContext)
                            + "_"
                            + Utils.getOSVersion()
                            + "_"
                            + Utils.getManufacturer()
                            + "_"
                            + Utils.getModel()
                            + "_"
                            + mSessionManager.getShipperId()
                            + "_"
                            + mSessionManager.getShopId()
            );
            connection.setConnectTimeout(TIME_OUT_MILLISECOND);
            connection.setReadTimeout(TIME_OUT_MILLISECOND);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            byte[] data = jsonObject.toString().getBytes("UTF-8");
            wr.write(data);
            wr.flush();
            wr.close();

            //Get Response

            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            is.close();
            return response.toString();
        } catch (Exception e) {
            if (!Utils.isConnectingToInternet(mContext)
                    && ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentByTag(DISCONNECTED_DIALOG_FRAGMENT_TAG) == null) {
                Utils.getDialogNoInternetConnection(mContext)
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), DISCONNECTED_DIALOG_FRAGMENT_TAG);
            }
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    // Open an input stream to a file available on the internet.
    private InputStream openISToFile(String urlString) {
        InputStream is = null;
        try {
            URL url = new URL(Config.API_LINK + urlString);
            URLConnection conn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                is = httpConn.getInputStream();
            } else {
                if (!Utils.isConnectingToInternet(mContext)
                        && ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentByTag(DISCONNECTED_DIALOG_FRAGMENT_TAG) == null) {
                    Utils.getDialogNoInternetConnection(mContext)
                            .show(((AppCompatActivity) mContext).getSupportFragmentManager(), DISCONNECTED_DIALOG_FRAGMENT_TAG);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return is;
    }

    public Document readDocumentWithGet(String url, Map<String, String> params) {
        URL targetUrl;
        HttpURLConnection connection = null;
        try {
            String query = "";

            for (Map.Entry<String, String> entry : params.entrySet()) {
                query += ("".equals(query) ? "" : "&") + entry.getKey() + "=" + URLEncoder.encode(entry.getValue() != null ? String.valueOf(entry.getValue()) : "", "UTF-8");
            }
            targetUrl = new URL(url + "?" + query);
            connection = (HttpURLConnection) targetUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(
                    "clientId", Utils.getImei(mContext)
                            + "_"
                            + Utils.getOSVersion()
                            + "_"
                            + Utils.getManufacturer()
                            + "_"
                            + Utils.getModel()
                            + "_"
                            + mSessionManager.getShipperId()
                            + "_"
                            + mSessionManager.getShopId()
            );
            connection.setConnectTimeout(TIME_OUT_MILLISECOND);
            connection.setReadTimeout(TIME_OUT_MILLISECOND);

            InputStream is = new BufferedInputStream(connection.getInputStream());
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document dcm = builder.parse(is);
            is.close();
            return dcm;
        } catch (Exception e) {
            if (!Utils.isConnectingToInternet(mContext)
                    && ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentByTag(DISCONNECTED_DIALOG_FRAGMENT_TAG) == null) {
                Utils.getDialogNoInternetConnection(mContext)
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), DISCONNECTED_DIALOG_FRAGMENT_TAG);
            }
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public String readJSONWSWithPost(String targetURL, Map<String, String> params) {
        String query = "";

        URL url;
        HttpURLConnection connection = null;
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {

                query += ("".equals(query) ? "" : "&") + entry.getKey() + "=" + URLEncoder.encode(entry.getValue() != null ? String.valueOf(entry.getValue()) : "", "UTF-8");

            }
            //Create connection
            url = new URL(Config.API_LINK + targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty(
                    "clientId",
                    Utils.getImei(mContext)
                            + "_"
                            + Utils.getOSVersion()
                            + "_"
                            + Utils.getManufacturer()
                            + "_"
                            + Utils.getModel()
                            + "_"
                            + mSessionManager.getShipperId()
                            + "_"
                            + mSessionManager.getShopId()
            );
            connection.setConnectTimeout(TIME_OUT_MILLISECOND);
            connection.setReadTimeout(TIME_OUT_MILLISECOND);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(query);
            wr.flush();
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            is.close();
            return response.toString();
        } catch (Exception e) {
            if (!Utils.isConnectingToInternet(mContext)
                    && ((AppCompatActivity) mContext).getSupportFragmentManager().findFragmentByTag(DISCONNECTED_DIALOG_FRAGMENT_TAG) == null) {
                Utils.getDialogNoInternetConnection(mContext)
                        .show(((AppCompatActivity) mContext).getSupportFragmentManager(), DISCONNECTED_DIALOG_FRAGMENT_TAG);
            }
            return null;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
