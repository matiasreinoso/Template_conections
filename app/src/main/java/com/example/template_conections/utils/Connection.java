package com.example.template_conections.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

import dtt.gcba.gochu.App;
import dtt.gcba.gochu.R;
import dtt.gcba.gochu.models.VisitaModel;
import dtt.gcba.gochu.screens.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;



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

            //If response is null
            if (aResponse == null) {
                String x = "X";
              /*  System.out.print(i.toString());
                Message.obtain(mHandler, Constants.ERROR_SERVICE_DOCUMENT, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();*/
            }

            // Error after connection has been idle
            if (aResponse.getStatusLine().getStatusCode() == 403 && getODataSchema() != null) {
                switch (aRequest.getRequestTAG()) {

                }
                //Message.obtain(mHandler, Constants.ERROR_CONNECTION_IDLE, 0, 0, new MessageData(aRequest.getRequestTAG())).sendToTarget();
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

        //Response para sincronización
//        if (aRequest.getRequestTAG().contentEquals(Constants.READ_DATA)) {
//            Message.obtain(mHandler, Constants.ERROR_READ_DATA_ORDEN, 0, 0).sendToTarget();
//        }

        //Response para datos admin
        if (aRequest.getRequestTAG().contentEquals(Constants.DATA_ADMIN)) {
            Message.obtain(mHandler, Constants.ERROR_ADMIN_DATA, 0, 0).sendToTarget();
        }

        //Response para send encuentas
        if (aRequest.getRequestTAG().contentEquals(Constants.POST_VISITA)) {
            Connection.remPendingNotif();

//            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo netInfo = cm.getActiveNetworkInfo();
//            if (netInfo != null) {
////                Message.obtain(mHandler, Constants.OK_SEND_VISITA, 0, 0).sendToTarget();
//                MessageData messageData = new MessageData(Constants.POST_VISITA);
//                messageData.addValue("value", Constants.MSG_S);
//                Message.obtain(mHandler, Constants.OK_SEND_VISITA, 0, 0, messageData).sendToTarget();
//
//            } else {una

            MessageData messageData = new MessageData(Constants.POST_VISITA);
            messageData.addValue("error", mContext.getResources().getString(R.string.error_conexion));
            Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0, messageData).sendToTarget();

            //            Message.obtain(mHandler, Constants.OK_RECONNECT_NOTIF, 0, 0).sendToTarget();
//                Message.obtain(mHandler, Constants.OK_SEND_VISITA, 0, 0).sendToTarget();
//            Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0).sendToTarget();
//            }
        }

        //Response para send actas
        if (aRequest.getRequestTAG().contentEquals(Constants.POST_ACTA)) {
            Connection.remPendingNotif();
            Message.obtain(mHandler, Constants.ERROR_SEND_ACTA, 0, 0).sendToTarget();
        }

        //Response para send visita
        if (aRequest.getRequestTAG().contentEquals(Constants.POST_FIN)) {
            Connection.remPendingNotif();
            Message.obtain(mHandler, Constants.ERROR_SEND_FIN_CHECKLIST, 0, 0).sendToTarget();
        }

        //Response para send visita texto
        if (aRequest.getRequestTAG().contentEquals(Constants.TEXTO)) {
//                        Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0).sendToTarget();
            Message.obtain(mHandler, Constants.ERROR_SEND_TEXTO, 0, 0).sendToTarget();
        }

        //Response para send Imagen
        if (aRequest.getRequestTAG().contentEquals(Constants.IMAGE)) {
            Connection.remPendingNotif();
//            Message.obtain(mHandler, Constants.ERROR_IMAGE, 0, 0).sendToTarget();
            Message.obtain(mHandler, Constants.OK_RECONNECT_IMG, 0, 0).sendToTarget();
        }

        //Response para send visita
        if (aRequest.getRequestTAG().contentEquals(Constants.RECOVERY)) {
            Connection.remPendingNotif();
            Message.obtain(mHandler, Constants.ERROR_RECOVERY, 0, 0).sendToTarget();
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

            //Response para login
            if (aRequest.getRequestTAG().contentEquals(Constants.RECOVERY)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(1).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    if (response.equals(Constants.LOGIN_CORRECTO)) {
                        Message.obtain(mHandler, Constants.OK_RECOVERY, 0, 0).sendToTarget();
                    } else {
                        Message.obtain(mHandler, Constants.ERROR_RECOVERY, 0, 0).sendToTarget();
                    }
                }
            }
            //Response para Checklist
            if (aRequest.getRequestTAG().contentEquals(Constants.POST_VISITA)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    Connection.remPendingNotif();
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    String instancia = propertiesData.get(1).getValue();
                    String posicion = propertiesData.get(2).getValue();

                    if (response.equals(Constants.LOGIN_CORRECTO)) {
                        ((App) mContext).getDataHandler().updateSentChecklist(instancia, posicion); //Marco que se envió para no volver a enviarlo
//                        Message.obtain(mHandler, Constants.OK_SEND_VISITA, 0, 0).sendToTarget();

                        MessageData messageData = new MessageData(Constants.POST_VISITA);
                        messageData.addValue("value", Constants.MSG_S);
                        Message.obtain(mHandler, Constants.OK_SEND_VISITA, 0, 0, messageData).sendToTarget();

                    } else {
                        MessageData messageData = new MessageData(Constants.POST_VISITA);
                        messageData.addValue("error", "Error al intentar guardar los datos en SAP");
                        Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0, messageData).sendToTarget();
                    }
                }
            }

            //Response para Actas
            if (aRequest.getRequestTAG().contentEquals(Constants.POST_ACTA)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    Connection.remPendingNotif();
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    String id_acta = propertiesData.get(2).getValue(); // Id del acta
                    String id_visita = propertiesData.get(3).getValue(); // id de la visita
                    String posicion = propertiesData.get(4).getValue(); // posicion del acta
                    String tipo = propertiesData.get(5).getValue(); // tipo de acta
                    if (response.equals(Constants.LOGIN_CORRECTO)) {

                        ((App) mContext).getDataHandler().deleteActa(id_acta, id_visita, posicion, tipo);
                        Message.obtain(mHandler, Constants.OK_SEND_ACTA, 0, 0).sendToTarget();
                    } else {
                        Message.obtain(mHandler, Constants.ERROR_SEND_ACTA, 0, 0).sendToTarget();
                    }
                }
            }
            //Response para Texto
            if (aRequest.getRequestTAG().contentEquals(Constants.TEXTO)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    if (response.equals(Constants.LOGIN_CORRECTO)) {
                        ((App) mContext).getDataHandler().updateTextoSent(propertiesData.get(1).getValue(), propertiesData.get(2).getValue());
                        Message.obtain(mHandler, Constants.OK_TEXTO, 0, 0).sendToTarget();
                    } else {
//                        Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0).sendToTarget();
                        Message.obtain(mHandler, Constants.ERROR_SEND_TEXTO, 0, 0).sendToTarget();
                    }
                }
            }

            //Response para Checklist
            if (aRequest.getRequestTAG().contentEquals(Constants.POST_FIN)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
                for (IODataEntry entry : entries) {
                    Connection.remPendingNotif();
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue(); // Valida que el logeo sea correcto en desde el campo TIPO
                    if (response.equals(Constants.LOGIN_CORRECTO)) {
                        Message.obtain(mHandler, Constants.OK_SEND_FIN_CHECKLIST, 0, 0).sendToTarget();
                    } else {

//                        MessageData messageData = new MessageData(Constants.POST_FIN);
//                        messageData.addValue("error", "Error al intentar guardar los datos en SAP");
//                        Message.obtain(mHandler, Constants.ERROR_SEND_VISITA, 0, 0, messageData).sendToTarget();

                        Message.obtain(mHandler, Constants.ERROR_SEND_FIN_CHECKLIST, 0, 0).sendToTarget();
                    }
                }
            }

//            //Response para sincronización
            if (aRequest.getRequestTAG().contentEquals(Constants.READ_DATA)) {
                ArrayList<VisitaModel> avisoArrayList = new ArrayList<VisitaModel>();
                boolean avisoTratado = false;
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
//                avisoArrayList = ((App) mContext).getDataHandler().deletePendingAvisosData();
//                if (avisoArrayList != null) {
//                    for (int i = 0; i < avisoArrayList.size(); i++) {
//                        boolean tratadoOk = false;
//                        String avisoNum = avisoArrayList.get(i).getAvisoNum();
//                        for (IODataEntry entry1 : entries) {
//                            List<IODataProperty> ioData = entry1.getPropertiesData();
//                            String order = ioData.get(2).getValue();
//                            if (avisoArrayList.get(i).getAvisoNum().equals(order)
//                                    || avisoArrayList.get(i).getAvisoNum().contains("M")) {
//                                tratadoOk = true;
//                            }
//                        }
//
//                        if (avisoArrayList.get(i).getAvisoNum().contains("M"))
//                            tratadoOk = true;
//                        if (!tratadoOk) {
//                            ((App) mContext).getDataHandler().deleteAvisoTratada(avisoNum);
//                        }
//                    }
//                }

//                int i = 1;
//                String b = String.valueOf(entries.size());
                for (IODataEntry entry : entries) {

//                    MessageData messageData = new MessageData(Constants.READ_DATA);
//                    messageData.addValue("value", "Se recibió: " + String.valueOf(i) + " : " + b);
//                    Message.obtain(mHandler, Constants.OK_HOLA, 0, 0, messageData).sendToTarget();
//
//                    i++;

                    avisoTratado = false;
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String response = propertiesData.get(0).getValue();
//                    for (int i = 0; i < avisoArrayList.size(); i++) {
////                        if (propertiesData.get(2).getValue().equals(avisoArrayList.get(i).getAvisoNum()))
////                            avisoTratado = true;
//                    }
//                    if (!avisoTratado) {
//                        if (clase != null)
                    //if (response.equals(Constants.READ_DATA)) {
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

//                        else
//                            ((App) mContext).getDataHandler().updateTextoExtendido(propertiesData);
//                    }
                }


                Message.obtain(mHandler, Constants.OK_READ_DATA, 0, 0).sendToTarget();
            }

//            //Response para sincronización
//            if (aRequest.getRequestTAG().contentEquals(Constants.READ_DATA_ORDEN)) {
//                ArrayList<OrdenModel> ordenArrayList = new ArrayList<OrdenModel>();
//                boolean ordenTratada = false;
//                IODataSchema schema = getODataSchema();
//                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
//                ordenArrayList = ((App) mContext).getDataHandler().deletePendingOrdenesData();
//                if (ordenArrayList != null) {
//                    for (int i = 0; i < ordenArrayList.size(); i++) {
//                        boolean tratadoOk = false;
//                        String avisoNum = ordenArrayList.get(i).getOrdenNum();
//                        for (IODataEntry entry1 : entries) {
//                            List<IODataProperty> propertiesData = entry1.getPropertiesData();
//                            String tipo = propertiesData.get(0).getValue();
//                            List<IODataProperty> ioData = entry1.getPropertiesData();
//                            String order = ioData.get(2).getValue();
//                            if (tipo.equals("OPERACION") || tipo.equals("PR"))
//                                tratadoOk = true;
//                            if (ordenArrayList.get(i).getOrdenNum().equals(order)) {
//                                tratadoOk = true;
//                            }
//                        }
//
////                        if(ordenArrayList.get(i).getOrdenNum().contains("M"))
////                            tratadoOk = true;
//                        if (!tratadoOk) {
//                            ((App) mContext).getDataHandler().deleteOrdenTratada(avisoNum);
//                        }
//                    }
//                }
//                for (IODataEntry entry : entries) {
//                    ordenTratada = false;
//                    List<IODataProperty> propertiesData = entry.getPropertiesData();
//                    String tipo = propertiesData.get(0).getValue();
//                    if (!tipo.equals("OPERACION") && !tipo.equals("PR") && !tipo.equals("TEXTO")) {
//                        for (int i = 0; i < ordenArrayList.size(); i++) {
//                            if (propertiesData.get(2).getValue().equals(ordenArrayList.get(i).getOrdenNum()))
//                                ordenTratada = true;
//                        }
//                    }
//                    if (!ordenTratada) {
//                        switch (tipo) {
//                            case "ORDEN":
//                                ((App) mContext).getDataHandler().updateOrden(propertiesData);
//                                break;
//                            case "OPERACION":
//                                ((App) mContext).getDataHandler().updateOperacion(propertiesData);
//                                break;
//                            case "PR":
//                                ((App) mContext).getDataHandler().updateProrroga(propertiesData);
//                                break;
//                            case "TEXTO":
//                                ((App) mContext).getDataHandler().updateTextoExtendido(propertiesData);
//                                break;
//                        }
////                        if (tipo != null)
////                            ((App) mContext).getDataHandler().updateAviso(propertiesData);
////                        else
////                            ((App) mContext).getDataHandler().updateTextoExtendido(propertiesData);
//                    }
//                }
//                Message.obtain(mHandler, Constants.OK_READ_DATA_ORDEN, 0, 0).sendToTarget();
//            }
//
//            //Response para datos admin
//            if (aRequest.getRequestTAG().contentEquals(Constants.DATA_ADMIN)) {
//                ((App) mContext).getDataHandler().deleteAdminData();
//                IODataSchema schema = getODataSchema();
//                entries = parser.parseODataEntries(responseString, Constants.OT_DATA, schema);
//                for (IODataEntry entry : entries) {
//                    List<IODataProperty> propertiesData = entry.getPropertiesData();
//                    String table = propertiesData.get(0).getValue();
//                    switch (table) {
//                        case Constants.CLASE_AVISO:
//                            ((App) mContext).getDataHandler().saveClaseAviso(propertiesData);
//                            break;
//                        case Constants.CLASE_ORDEN:
//                            ((App) mContext).getDataHandler().saveClaseOrden(propertiesData);
//                            break;
//                        case Constants.EDIFICIO:
//                            ((App) mContext).getDataHandler().saveEdificio(propertiesData);
//                            break;
//                        case Constants.ESTABLECIMIENTO:
//                            ((App) mContext).getDataHandler().saveEstablecimiento(propertiesData);
//                            break;
//                        case Constants.RUBRO:
//                            ((App) mContext).getDataHandler().saveRubro(propertiesData);
//                            break;
//                        case Constants.PRESTACION:
//                            ((App) mContext).getDataHandler().savePrestacion(propertiesData);
//                            break;
//                        case Constants.PRIORIDAD:
//                            ((App) mContext).getDataHandler().savePrioridad(propertiesData);
//                            break;
//                        case Constants.CODIFICACION:
//                            ((App) mContext).getDataHandler().saveCodificacion(propertiesData);
//                            break;
//                        case Constants.ESTADOS:
//                            ((App) mContext).getDataHandler().saveEstados(propertiesData);
//                            break;
//                    }
//
//                }
//                Message.obtain(mHandler, Constants.OK_ADMIN_DATA, 0, 0).sendToTarget();
//            }
//
//            //Response para send aviso
//            if (aRequest.getRequestTAG().contentEquals(Constants.POST_AVISO)) {
//                IODataSchema schema = getODataSchema();
//                entries = parser.parseODataEntries(responseString, Constants.IT_DATA, schema);
//                for (IODataEntry entry : entries) {
//                    List<IODataProperty> propertiesData = entry.getPropertiesData();
//                    String num = propertiesData.get(2).getValue();
//                    String numAnterior = propertiesData.get(22).getValue();
//                    String type = propertiesData.get(27).getValue();
//                    String cod = propertiesData.get(29).getValue();
////                    String type = propertiesData.get(1).getValue();
////                    String id = propertiesData.get(2).getValue();
////                    String cod = propertiesData.get(3).getValue();
////                    String value = propertiesData.get(4).getValue();
////                    String extra = propertiesData.get(5).getValue(); //Generated notif ID
//                    if (type.matches(Constants.MSG_S)) {
//                        if (cod.matches(Constants.AVISO)) {
//                            Connection.remPendingNotif();
////                            AppLogger.log(mContext.getResources().getString(R.string.succSMPCreate), extra);
//                            MessageData messageData = new MessageData(aRequest.getRequestTAG(), num);
////                            messageData.addValue("local", extra);
//                            Message.obtain(mHandler, Constants.OK_SEND_AVISOS
//                                    , 0, 0, messageData).sendToTarget();
//                            ((App) mContext).getDataHandler().updateAvisoNum(numAnterior, num);
//                            ((App) mContext).getDataHandler().updateAvisoStat(num, mContext.getResources().getStringArray(R.array.estados_num_array)[2]);
////                        }
//                        }
//                        if (cod.matches(Constants.TEXTO)) {
//                            //Message.obtain(mHandler, Constants.RESUME_NOTIF_THREAD , 0, 0, null).sendToTarget();
//                            ((App) mContext).getDataHandler().updateTextoSent(numAnterior, propertiesData.get(20).getValue());
//                            ((App) mContext).getDataHandler().isLastNotifRow(numAnterior);
////                            if (!sp.getString("CheckDoble", "").equals("X")) {
////                                ((App) mContext).getDataHandler().isLastOrderRow(id);
//                        }
//                    } else {
//                        Connection.remPendingNotif();
//                        ((App) mContext).getDataHandler().updateAvisoStat(num, mContext.getResources().getStringArray(R.array.estados_num_array)[3]);
//                        MessageData messageData = new MessageData(aRequest.getRequestTAG());
//                        messageData.addValue("local", num);
//                        Message.obtain(mHandler, Constants.ERROR_SEND_AVISOS, 0, 0, messageData).sendToTarget();
//                    }
//                }
//            }
//
//            //Response para send orden
//            if (aRequest.getRequestTAG().contentEquals(Constants.POST_ORDEN)) {
//                IODataSchema schema = getODataSchema();
//                entries = parser.parseODataEntries(responseString, Constants.IT_DATA, schema);
//                for (IODataEntry entry : entries) {
//                    List<IODataProperty> propertiesData = entry.getPropertiesData();
//                    String num = propertiesData.get(2).getValue();
//                    String numAnterior = propertiesData.get(22).getValue();
//                    String type = propertiesData.get(27).getValue();
//                    String cod = propertiesData.get(29).getValue();
//                    if (type.matches(Constants.MSG_S)) {
//                        if (cod.matches(Constants.ORDEN)) {
//                            Connection.remPendingNotif();
////                            AppLogger.log(mContext.getResources().getString(R.string.succSMPCreate), extra);
//                            MessageData messageData = new MessageData(aRequest.getRequestTAG(), num);
////                            messageData.addValue("local", extra);
//                            Message.obtain(mHandler, Constants.OK_SEND_ORDEN
//                                    , 0, 0, messageData).sendToTarget();
////                            ((App) mContext).getDataHandler().updateAvisoNum(numAnterior, num);
//                            ((App) mContext).getDataHandler().updateOrdenStat(numAnterior, mContext.getResources().getStringArray(R.array.estados_num_array)[2]);
////                        }
//                        }
//                        if (cod.matches(Constants.TEXTO)) {
//                            //Message.obtain(mHandler, Constants.RESUME_NOTIF_THREAD , 0, 0, null).sendToTarget();
//                            ((App) mContext).getDataHandler().updateTextoSent(numAnterior, propertiesData.get(20).getValue());
//                            ((App) mContext).getDataHandler().isLastOrderRow(numAnterior);
////                            if (!sp.getString("CheckDoble", "").equals("X")) {
////                                ((App) mContext).getDataHandler().isLastOrderRow(id);
//                        }
//                        if (cod.matches(Constants.AVISO_PR)) {
//                            ((App) mContext).getDataHandler().updateAvisoProrroga(num);
//                            ((App) mContext).getDataHandler().isLastOrderRow(numAnterior);
//                        }
//                        if (cod.matches(Constants.TEXTOPR)) {
//                            ((App) mContext).getDataHandler().updateTextoSent(numAnterior, propertiesData.get(20).getValue());
////                            ((App) mContext).getDataHandler().isLastOrderRow(numAnterior);
//                        }
//                    } else {
//                        Connection.remPendingNotif();
//                        ((App) mContext).getDataHandler().updateOrdenStat(numAnterior, mContext.getResources().getStringArray(R.array.estados_num_array)[3]);
//                        MessageData messageData = new MessageData(aRequest.getRequestTAG());
//                        messageData.addValue("local", num);
//                        Message.obtain(mHandler, Constants.ERROR_SEND_ORDEN, 0, 0, messageData).sendToTarget();
//                    }
//                }
//            }
//
//            //Response para send imagen
            if (aRequest.getRequestTAG().contentEquals(Constants.IMAGE)) {
                IODataSchema schema = getODataSchema();
                entries = parser.parseODataEntries(responseString, Constants.IT_DATA, schema);
                for (IODataEntry entry : entries) {
                    Connection.remPendingImage();
                    List<IODataProperty> propertiesData = entry.getPropertiesData();
                    String type = propertiesData.get(3).getValue();
                    String num = propertiesData.get(4).getValue();
                    String num_img = propertiesData.get(5).getValue();
                    String pos = propertiesData.get(6).getValue();
                    String tdline = propertiesData.get(7).getValue();


                    if (type.matches(Constants.MSG_S)) {
                        if (!tdline.equals(Constants.IMAGE_END)) {
                            ((App) mContext).getDataHandler().updateSentImage(num, num_img, pos);
                            Message.obtain(mHandler, Constants.RESUME_IMG_THREAD, 0, 0, "").sendToTarget();
                        } else {
                            ((App) mContext).getDataHandler().deleteImage(num, num_img); //TODO: comento el borrado de imagenes
                            MessageData messageData = new MessageData(aRequest.getRequestTAG());
                            messageData.addValue("num", num);
                            messageData.addValue("img", num_img);
                            Message.obtain(mHandler, Constants.OK_IMAGE, 0, 0, messageData).sendToTarget();
                        }
                    } else {
//                        AppLogger.log(mContext.getResources().getString(R.string.errSMPImage), id + " - " + desc + " - " + extra);
                        MessageData messageData = new MessageData(aRequest.getRequestTAG());
                        messageData.addValue("num", num);
                        messageData.addValue("img", num_img);
                        messageData.addValue("error", "Error al insertar la imagen en SAP");
                        Message.obtain(mHandler, Constants.ERROR_IMAGE, 0, 0, messageData).sendToTarget();
                    }
                }
            }

            //else {
////                        AppLogger.log(mContext.getResources().getString(R.string.errSMPCreate), id + " - " + value);
////                        ((App) mContext).getDataHandler().updateOrderStat(id, mContext.getResources().getStringArray(R.array.status_array)[3]);
//                        MessageData messageData = new MessageData(aRequest.getRequestTAG());
//                        messageData.addValue("local", id);
//                        Message.obtain(mHandler, Constants.ERROR_SEND_AVISOS, 0, 0, messageData).sendToTarget();
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

    public static void addPendingOrder() {
        mPendingNotif++;
    }

    public static void remPendingNotif() {
        mPendingNotif--;
    }

    public static void setPendingNotif(int newValue) {
        mPendingNotif = newValue;
    }

    public static int getPendingNotif() {
        return mPendingNotif;
    }

    public static void addPendingLog() {
        mPendingLog++;
    }

    public static void remPendingLog() {
        mPendingLog--;
    }

    public static void setPendingLog(int newValue) {
        mPendingLog = newValue;
    }

    public static int getPendingLog() {
        return mPendingLog;
    }

    public static void addPendingImage() {
        mPendingImage++;
    }

    public static void remPendingImage() {
        mPendingImage--;
    }

    public static void setPendingImage(int newValue) {
        mPendingImage = newValue;
    }

    public static int getPendingImage() {
        return mPendingImage;
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