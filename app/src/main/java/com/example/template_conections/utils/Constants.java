package com.example.template_conections.utils;

public class Constants {

    // Connection constants
//    public final static String SERVER_USERNAME = "smpBAzb2015";              //DEV y QA
//    public final static String SERVER_PASSWORD = "RecM0b2015";               //DEV y QA
    public final static String SERVER_USERNAME = "SMPGOCHU";                  //PRD
    public final static String SERVER_PASSWORD = "2x3=seis";                  //PRD
    //    public final static String SERVER_IP = "sap-pm-desa.gcba.gob.ar";       //dev nuevo
//    public final static String SERVER_IP = "sap-pm-qa.gcba.gob.ar";         //qa nuevo
    public final static String SERVER_IP = "sap-pm.buenosaires.gob.ar";       //prod
    public final static String SERVER_PORT_HTTPS = "443";                     //dev nuevo
    public final static String HTTP_MODE = "httpMode";
    public final static String MODE_HTTP = "http";
    public final static String MODE_HTTPS = "https";
    public final static String APP_NAME = "dtt.gcba.checklist2";              //DEV - QA -PRD
    public final static String APP_SEC_PROFILE = "admin";
    public final static int NUMBER_OF_THREADS = 5;
    public final static int HTTPS_PORT = 443;
    public final static int TIMEOUT_MS = 70000;

    //Constantes generales
    public final static String UTF8 = "UTF-8";

    // Messaging constants
    public final static String ERROR = "ERROR";
    public final static String INFO = "INFO";
    public final static String WARN = "WARN";
    public final static String MSG_TYPE = "MSG_TYPE";
    public final static String MSG_VALUE = "MSG_VALUE";
    public final static int OK_M = 1;
    public final static int ERROR_M = 2;
    public final static int OK_REGISTER = 2;
    public final static int ERROR_REGISTER = 102;
    public final static int OK_SERVICE_DOCUMENT = 3;
    public final static int ERROR_SERVICE_DOCUMENT = 103;
    public final static int OK_SCHEMA = 4;
    public final static int ERROR_SCHEMA = 104;
    public final static int OK_GET_DATA = 5;
    public final static int ERROR_GET_DATA = 105;
    public final static int OK_CREATE_NOTIF = 6;
    public final static int WARN_CREATE_NOTIF = 16;
    public final static int ERROR_CREATE_NOTIF = 106;
    public final static int OK_DATOS_ADMIN = 7;
    public final static int ERROR_DATOS_ADMIN = 107;
    public final static int OK_READ_DATA = 8;
    public final static int COUNT_CREATE_NOTIF = 12;
    public final static int PROCES_READ_DATA = 13;
    public final static int PROCES_DATOS_ADMIN = 14;
    public final static int ERROR_READ_DATA = 108;
    public final static int OK_IMAGE = 9;
    public final static int WARN_IMAGE = 19;
    public final static int ERROR_IMAGE = 109;
    public final static int OK_LOG = 10;
    public final static int ERROR_LOG = 110;
    public final static int ERROR_CONNECTION_IDLE = 111;
    public final static int ERROR_CONNECTION_IDLE_NOTIF = 115;
    public final static int ERROR_CONNECTION_IDLE_LOG = 113;
    public final static int ERROR_CONNECTION_IDLE_IMAGE = 114;
    public final static int DEFAULT = 11;
    public final static int RESPONSE_NULL = 112;
    public final static int OK_RECONNECT_NOTIF = 116;
    public final static int OK_RECONNECT_IMG = 117;
    public final static int OK_RECONNECT_LOG = 118;
    public final static int SENDING_PHOTO = 119;
    public final static int RESUME_IMG_THREAD = 120;
    public final static int RECEIVING_NOTIF = 121;

    public static final int OK_TEXTO = 180;


    public final static String LOGIN = "LOGIN";
    public final static String READ_DATA = "READ";
    public final static String VISITA = "VISITA";
    public final static String ULTIMAS_VISITAS = "ULTIMAS_VISITAS";
    public final static String ULTIMAS_ACTAS = "ULTIMAS_ACTAS";
    public final static String CHECKLIST = "CHECKLIST";
    public static final String OFICIO = "OFICIO";
    public static final String SUACI = "SUACI";


    public final static String READ_DATA_ORDEN = "ORDEN";
    public final static String POST_AVISO = "POST_AVISO";
    public final static String POST_VISITA = "POST_VISITA";
    public final static String POST_ORDEN = "POST_ORDEN";
    public static final String POST_ACTA = "POST_ACTA";
    public static final String POST_FIN = "POST_FIN";
    public final static String AVISO_PR = "PR";
    public final static String ORDEN = "ORDEN";
    public final static String TEXTO = "TEXTO";
    public final static String TEXTOPR = "TEXTOPR";
    public final static String IMAGEN = "IMAGEN";
    public final static String DATA_ADMIN = "DATA_ADMIN";
    public final static String IMAGE = "IMAGEN";
    public final static String IMAGE_END = "<END>";
    public final static String IT_DATA = "ItDataSet";
    public final static String OT_DATA = "OtDataSet";
    public final static int OK_LOGIN = 12;
    public final static int ERROR_LOGIN = 120;
    public final static int OK_ADMIN_DATA = 13;
    public final static int ERROR_ADMIN_DATA = 130;
    public final static int OK_SEND_VISITA = 14;
    public final static int WARN_SEND_VISITA = 141;
    public final static int ERROR_SEND_VISITA = 140;
    public final static int OK_SEND_FIN_CHECKLIST = 15;
    public final static int ERROR_SEND_FIN_CHECKLIST = 150;
    public final static int WARN_SEND_FIN_CHECKLIST = 151;
    public final static int OK_SEND_ACTA = 16;
    public final static int WARN_SEND_ACTA = 161;
    public final static int ERROR_SEND_ACTA = 160;
    public static final int OK_RECOVERY = 17;
    public static final int ERROR_RECOVERY = 170;
    public static final int ERROR_SEND_TEXTO = 181;

    public final static String MSG_E = "E";
    public final static String MSG_S = "S";

    // RFC Response constants
    public static final String REQUEST_SERVICE_DOCUMENT = "requestServiceDocument";
    public static final String REQUEST_METADATA = "requestMetadata";


    //Shared preferences constants
    public final static String SHARED_PREFS = "dtt.gcba.mobile_ordenes_m002.datosConfig";
    public final static String SERVICE_DOCUMENT = "serviceDocument";
    public final static String SCHEMA = "schema";
    public final static String SPLOGIN = "login";
    public final static String SPRECORDAR = "recordar";
    public final static String SPUSER = "user";
    public final static String SPPASSWORD = "password";
    public final static String SPSAMEUSER = "same_user";
    public final static String SPFIRST_RUN = "first_run";
    public final static String APPCID = "appCID";
    public final static String FIRST_RUN = "firstRun";

    public static final String ACTA_ADMIN = "ACTA_ADMIN";

}
