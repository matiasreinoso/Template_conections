package com.example.template_conections.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;

import com.sap.mobile.lib.parser.IODataProperty;
import com.sap.mobile.lib.request.IRequest;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//se controla el envio de datos desde la aplicación
public class DataHandler {

    private Connection mConnection;
    private Context mContext;
    private Handler mHandler;


    public DataHandler(Context newContext, Connection newConnection) {
        mContext = newContext;
        mConnection = newConnection;
    }

    public void setHandler(Handler newHandler) {
        mHandler = newHandler;
    }

    public Handler getHandler() {
        return mHandler;
    }



    public String addField(String key, String value, String output) {
        return output + "<" + key.toUpperCase() + ">" + value + "</" + key.toUpperCase() + ">";
    }

    public String setValoresEspeciales(String value) {
        value = value.replace("'", "%27");
        value = value.replace("ñ", "%C3%B1");
        value = value.replace("Ñ", "%C3%91");
        value = value.replace("Á", "%C3%81");
        value = value.replace("É", "%C3%89");
        value = value.replace("Í", "%C3%8D");
        value = value.replace("Ó", "%C3%93");
        value = value.replace("Ú", "%C3%9A");
        value = value.replace("Ü", "%C3%9C");
        value = value.replace("á", "%C3%A1");
        value = value.replace("é", "%C3%A9");
        value = value.replace("í", "%C3%AD");
        value = value.replace("ó", "%C3%B3");
        value = value.replace("ú", "%C3%BA");
        value = value.replace("ü", "%C3%BC");

        return value;
    }



}


