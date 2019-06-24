package com.example.template_conections.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.template_conections.App;
import com.sap.mobile.lib.configuration.IPreferences;
import com.sap.mobile.lib.configuration.Preferences;
import com.sap.mobile.lib.configuration.PreferencesException;
import com.sap.mobile.lib.parser.IODataEntry;
import com.sap.mobile.lib.parser.IODataProperty;
import com.sap.mobile.lib.parser.IODataSchema;
import com.sap.mobile.lib.parser.IODataServiceDocument;
import com.sap.mobile.lib.parser.Parser;
import com.sap.mobile.lib.parser.ParserException;
import com.sap.mobile.lib.request.ConnectivityParameters;
import com.sap.mobile.lib.request.INetListener;
import com.sap.mobile.lib.request.IRequest;
import com.sap.mobile.lib.request.IRequestStateElement;
import com.sap.mobile.lib.request.IResponse;
import com.sap.mobile.lib.request.RequestManager;
import com.sap.mobile.lib.supportability.Logger;
import com.sap.smp.rest.AppSettings;
import com.sap.smp.rest.ClientConnection;
import com.sap.smp.rest.SMPException;
import com.sap.smp.rest.UserManager;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Connection implements INetListener {

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // This class handles the connection to SAP backends using SMP //
    // Developed by: Santiago Martin Janse //
    // 15 May 2015 //
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static String mAppCID;
    private static String mAppToken;
    private static String mAppEndPoint;
    private static AppSettings mAppSett;
    private static final String TAG = Connection.class.getName();
    private RequestManager mRequestManager = null;
    private Preferences mPreferences = null;
    private Logger mLogger = null;
    private ConnectivityParameters mConnectivityParameters = null;
    private Parser mParser = null;
    private IODataSchema mSchema = null;
    private int pendingMethod = 0;

    public void setmServiceDocument(IODataServiceDocument mServiceDocument) {
        this.mServiceDocument = mServiceDocument;
    }

    private IODataServiceDocument mServiceDocument;
    private boolean mUseJSONFormat = false;
    private Context mContext;
    private Handler mHandler;
    private static int mPendingNotif;
    private static int mPendingLog;
    private static int mPendingImage;

    public void setReconnectCount(int reconnectCount) {
        this.reconnectCount = reconnectCount;
    }

    private int reconnectCount = 0;

    public Connection(Context context) {

        mContext = context;
        // Create logger
        mLogger = new Logger();

        // Create connection parameters
        mConnectivityParameters = new ConnectivityParameters();
        mConnectivityParameters.setLanguage(mContext.getResources()
                .getConfiguration().locale.getLanguage());
        mConnectivityParameters.enableXsrf(true);

        // Create preferences
        try {
            mPreferences = new Preferences(mContext, mLogger);
            mPreferences.setBooleanPreference(IPreferences.PERSISTENCE_SECUREMODE, false);
            mPreferences.setIntPreference(IPreferences.CONNECTIVITY_HTTPS_PORT, Constants.HTTPS_PORT);
            mPreferences.setIntPreference(IPreferences.CONNECTIVITY_CONNTIMEOUT, Constants.TIMEOUT_MS);
            mPreferences.setIntPreference(IPreferences.CONNECTIVITY_SCONNTIMEOUT, Constants.TIMEOUT_MS);
        } catch (PreferencesException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        try {
            mParser = new Parser(mPreferences, getLogger());
        } catch (ParserException e) {
            mLogger.e(TAG, "Error inicializando el parser", e);
        }

    }

    // Getters y setters

    public static String getmAppEndPoint() {
        return mAppEndPoint;
    }

    public static void setmAppEndPoint(String mAppEndPoint) {
        Connection.mAppEndPoint = mAppEndPoint;
    }

    public static String getappCID() {
        return mAppCID;
    }

    public static String getAppToken() {
        return mAppToken;
    }

    public static void setappCID(String app) {
        Connection.mAppCID = app;
    }

    public static AppSettings getmAppSett() {
        return mAppSett;
    }

    public static void setappSet(AppSettings newAppSett) {
        mAppSett = newAppSett;
    }

    public Logger getLogger() {
        return mLogger;
    }

    public Preferences getPreferences() {
        return mPreferences;
    }

    public void setUsername(String username) {
        mConnectivityParameters.setUserName(username);
    }

    public String getUsername() {
        return mConnectivityParameters.getUserName();
    }

    public void setPassword(String password) {
        mConnectivityParameters.setUserPassword(password);
    }

    public RequestManager getRequestManager() {
        if (mRequestManager == null) {
            mRequestManager = new RequestManager(mLogger, mPreferences,
                    mConnectivityParameters, Constants.NUMBER_OF_THREADS);
        }
        return mRequestManager;
    }

    public Parser getParser() {
        return this.mParser;
    }

    public boolean useJSONFormat() {
        return this.mUseJSONFormat;
    }

    public void setJSONFormat(boolean useJSONFormat) {
        this.mUseJSONFormat = useJSONFormat;
    }

    public IODataSchema getODataSchema() {
        return mSchema;
    }

    public void setODataSchema(IODataSchema aSchema) {
        mSchema = aSchema;
    }

    public void clearODataSchema() {
        mSchema = null;
    }

    public void setHandler(Handler newHandler) {
        mHandler = newHandler;
    }

    public Handler getHandler() {
        return mHandler;
    }

    // Registration

    // Register the application with server

    public void registerApp(String serviceName) {
        try {
            setUsername(Constants.SERVER_USERNAME);
            setPassword(Constants.SERVER_PASSWORD);
            ClientConnection clientConnection =
                    new ClientConnection(mContext,
                            serviceName,
                            null,
                            Constants.APP_SEC_PROFILE,
                            getRequestManager());
            SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
            clientConnection.setConnectionProfile(false, Constants.SERVER_IP, Constants.SERVER_PORT_HTTPS, null, null);
            UserManager userManager = new UserManager(clientConnection);
            userManager.registerUser(true);
            String appCID = userManager.getApplicationConnectionId();
            Connection.setappCID(appCID);
            Connection.setappSet(new AppSettings(clientConnection));
            saveRegisterData();
        } catch (SMPException e) {
            if (getODataSchema() == null && reconnectCount == 0) {
                String error = e.getMessage();
                Message.obtain(mHandler, Constants.ERROR_REGISTER, 0, 0).sendToTarget();
            }
            return;
        }
        Message.obtain(mHandler, Constants.OK_REGISTER, 0, 0).sendToTarget();
    }

    public void saveRegisterData() {
        try {
            String appEndPoint = mAppSett.getApplicationEndPoint();
            Connection.setmAppEndPoint(appEndPoint);
            //Save registration data in shared preferences
            SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putString(Constants.APPCID, mAppCID);
            editor.commit();
        } catch (SMPException e) {
            //AppLogger.log("Error guardando datos de registración", e.getMessage());
        }
    }

    public void getServiceDocument() {

        this.initializeRequestBuilder();

        if (mServiceDocument == null) {
            IRequest request = RequestBuilder.getInstance().buildServiceDocumentRequest(this, Constants.REQUEST_SERVICE_DOCUMENT);
            getRequestManager().makeRequest(request);
        } else {
            getServiceSchema();
        }
    }

    public void getServiceSchema() {
        IODataSchema schema = getODataSchema();
        if (schema == null) {
            IRequest request = RequestBuilder.getInstance().buildMetaDataRequest(this, Constants.REQUEST_METADATA);
            getRequestManager().makeRequest(request);
        } else {
            RequestBuilder.getInstance().setSchema(schema);
        }
    }

    private void initializeRequestBuilder() {
        String appConnID = Connection.getappCID();
        String appToken = Connection.getAppToken();
        String endPointURL = Connection.getmAppEndPoint();
        Boolean isMAFLogon = false;
        Parser parser = getParser();
        IODataSchema schema = getODataSchema();
        SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
        RequestBuilder.getInstance().initialize(schema, parser, isMAFLogon, appConnID, appToken, endPointURL, false, Constants.MODE_HTTPS);
    }

    @Override
    public void onError(IRequest aRequest, IResponse aResponse, IRequestStateElement aRequestState) {
        try {

            if (aResponse == null) {
                String x = "X";
            }

            // Error after connection has been idle
            if (aResponse.getStatusLine().getStatusCode() == 403 && getODataSchema() != null) {
                switch (aRequest.getRequestTAG()) {

                }
            }

            // Response for WS Document
            if (aRequest.getRequestTAG().contentEquals(Constants.REQUEST_SERVICE_DOCUMENT)) {
                Message.obtain(mHandler, Constants.ERROR_SERVICE_DOCUMENT, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();
            }

            // Response for WS Metadata
            if (aRequest.getRequestTAG().contentEquals(Constants.REQUEST_METADATA)) {
                Message.obtain(mHandler, Constants.ERROR_SCHEMA, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();
            }
        } catch (NullPointerException ex) {
            String x = "X";
        }


        //Response para login
        if (aRequest.getRequestTAG().contentEquals(Constants.LOGIN)) {
            Message.obtain(mHandler, Constants.ERROR_LOGIN, 0, 0).sendToTarget();
        }

        //Response para sincronización
        if (aRequest.getRequestTAG().contentEquals(Constants.READ_DATA)) {
            Message.obtain(mHandler, Constants.ERROR_READ_DATA, 0, 0).sendToTarget();
        }


    }

    @Override
    public void onSuccess(IRequest aRequest, IResponse aResponse) {
        reconnectCount = 0;
        try {
            HttpEntity responseEntity = aResponse.getEntity();
            String responseString = EntityUtils.toString(responseEntity, Constants.UTF8);
            List<IODataEntry> entries;
            Parser parser = getParser();

            // Response for WS Document
            if (aRequest.getRequestTAG().contentEquals(Constants.REQUEST_SERVICE_DOCUMENT)) {
                mServiceDocument = parser.parseODataServiceDocument(responseString);
                //Save service document data in shared preferences
                SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putString(Constants.SERVICE_DOCUMENT, responseString);
                editor.commit();
                Message.obtain(mHandler, Constants.OK_SERVICE_DOCUMENT, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();

                getServiceSchema();
            }

            // Response for WS Metadata
            if (aRequest.getRequestTAG().contentEquals(Constants.REQUEST_METADATA)) {
                IODataSchema schema = parser.parseODataSchema(responseString, mServiceDocument);
                setODataSchema(schema);
                RequestBuilder.getInstance().setSchema(schema);
                //Save schema data in shared preferences
                SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
                SharedPreferences.Editor editor = mSharedPrefs.edit();
                editor.putString(Constants.SCHEMA, responseString);
                editor.commit();
                Message.obtain(mHandler, Constants.OK_SCHEMA, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();
            }

            //Response para login
            if (aRequest.getRequestTAG().contentEquals(Constants.LOGIN)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    if (response.equals(Constants.LOGIN_CORRECTO)) {
                        Message.obtain(mHandler, Constants.OK_LOGIN, 0, 0).sendToTarget();
                    } else {
                        Message.obtain(mHandler, Constants.ERROR_LOGIN, 0, 0).sendToTarget();
                    }
                }
            }


//            //Response para sincronización
            if (aRequest.getRequestTAG().contentEquals(Constants.READ_DATA)) {
//               IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {

                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue();

                    if (response.equals((Constants.VISITA))) {

                        ((App) mContext).getDataHandler().updateVisita(propertiesData);
                    }
                    if (response.equals((Constants.ULTIMAS_VISITAS))) {
                        ((App) mContext).getDataHandler().updateUltimasVisita(propertiesData);
                    }
                    if (response.equals((Constants.ULTIMAS_ACTAS))) {
                        ((App) mContext).getDataHandler().updateUltimasActas(propertiesData);
                    }
                    if (response.equals((Constants.CHECKLIST))) {
                        ((App) mContext).getDataHandler().updateChecklist(propertiesData);
                    }
                    if (response.equals((Constants.DATA_ADMIN))) {
                        ((App) mContext).getDataHandler().updateDataAdmin(propertiesData);
                    }
                    if (response.equals((Constants.OFICIO))) {
                        ((App) mContext).getDataHandler().updateOficio(propertiesData);
                    }
                    if (response.equals((Constants.SUACI))) {
                        ((App) mContext).getDataHandler().updateSuaci(propertiesData);
                    }
                    if (response.equals((Constants.ACTA_ADMIN))) {
                        ((App) mContext).getDataHandler().updateActaAdmin(propertiesData);
                    }
                }


                Message.obtain(mHandler, Constants.OK_READ_DATA, 0, 0).sendToTarget();
            }

        } catch (ParseException e) {
            //AppLogger.log("Error parseando response de request", e.getMessage());
        } catch (IOException e) {
            //AppLogger.log("Error parseando response de request", e.getMessage());
        } catch (IllegalArgumentException e) {
            //AppLogger.log("Error parseando response de request", e.getMessage());
        } catch (ParserException e) {
            //AppLogger.log("Error parseando response de request", e.getMessage());
        }
    }

    public boolean isOnline() {

        // Check if there is an internet connection available
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return isConnectionFast(netInfo.getType(), netInfo.getSubtype());
        }
        return false;
    }

    public boolean isConnectionFast(int type, int subType) {
        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    public void recconect(int source) {
        try {
            setUsername(Constants.SERVER_USERNAME);
            setPassword(Constants.SERVER_PASSWORD);
            ClientConnection clientConnection =
                    new ClientConnection(mContext,
                            Constants.APP_NAME,
                            null,
                            Constants.APP_SEC_PROFILE,
                            getRequestManager());
            SharedPreferences mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
            clientConnection.setConnectionProfile(false, Constants.SERVER_IP, Constants.SERVER_PORT_HTTPS, null, null);
            UserManager userManager = new UserManager(clientConnection);

            if (Connection.getappCID() != null) {
                userManager.deleteUser();
                setUsername(Constants.SERVER_USERNAME);
                setPassword(Constants.SERVER_PASSWORD);
                clientConnection = new ClientConnection(mContext,
                        Constants.APP_NAME,
                        null,
                        Constants.APP_SEC_PROFILE,
                        getRequestManager());
                mSharedPrefs = mContext.getSharedPreferences(Constants.SHARED_PREFS, 0);
                clientConnection.setConnectionProfile(false, Constants.SERVER_IP, Constants.SERVER_PORT_HTTPS, null, null);
                userManager = new UserManager(clientConnection);
            }

            userManager.registerUser(true);
            String appCID = userManager.getApplicationConnectionId();
            Connection.setappCID(appCID);
            Connection.setappSet(new AppSettings(clientConnection));
            saveRegisterData();
        } catch (SMPException e) {
            if (getODataSchema() == null) {
                Message.obtain(mHandler, Constants.ERROR_REGISTER, 0, 0).sendToTarget();
            }
            return;
        }

        pendingMethod = source;

    }
}