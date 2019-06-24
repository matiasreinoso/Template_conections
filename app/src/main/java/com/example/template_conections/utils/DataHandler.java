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

    public void login(String name, String password) {

        if (mConnection.getODataSchema() == null) {
            Message.obtain(mHandler, Constants.ERROR_LOGIN, 0, 0).sendToTarget();
            return;
        }
        checkLogin(name, password, Constants.ERROR_LOGIN);
        String msj = "Logueando";
    }


    public void checkLogin(String name, String pass, int error) {
        if (mConnection.getODataSchema() == null) {
            Message.obtain(mHandler, error, 0, 0).sendToTarget();
            return;
        }

        name = android.net.Uri.encode(name, "UTF-8");
        pass = android.net.Uri.encode(pass, "UTF-8");
        IRequest request;
        request = RequestBuilder.getInstance().buildGETRequest(mConnection, Constants.IT_DATA, 0, "Tipo%20eq%20%27" + Constants.LOGIN
                + "%27%20and%20Usuario%20eq%20%27" + name + "%27%20and%20Num%20eq%20%27" + pass
                + "%27");

        request.setRequestTAG(Constants.LOGIN);
        mConnection.getRequestManager().makeRequest(request);
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

    public void sendActa(VisitaModel visita) {
        enviarActa(visita, Constants.ERROR_SEND_ACTA);
    }

    private void enviarActa(VisitaModel visita, int error) {

        if (mConnection.getODataSchema() == null) {
            Message.obtain(mHandler, error, 0, 0).sendToTarget();
            return;
        }
        final SharedPreferences datosLogin = mContext.getApplicationContext().getSharedPreferences(Constants.SPLOGIN, 0);

        ArrayList<ActasModel> actas = CheckListDB.getInstance(mContext).getActasGuardadas(datosLogin.getString(Constants.SPUSER, ""), visita.getNum());

        for (ActasModel acta : actas) {

            IRequest request;
            Thread thActas = MainActivity.thSendActa;

            request = RequestBuilder.getInstance().buildGETRequest(mConnection, Constants.IT_DATA, 0, "Tipo%20eq%20%27" + android.net.Uri.encode(Constants.POST_ACTA, "UTF-8")
                    + "%27%20and%20Usuario%20eq%20%27" + android.net.Uri.encode(datosLogin.getString(Constants.SPUSER, ""), "UTF-8")
                    + "%27%20and%20Num%20eq%20%27" + android.net.Uri.encode(acta.getNumero(), "UTF-8")
                    + "%27%20and%20Nombre%20eq%20%27" + android.net.Uri.encode(acta.getVisita(), "UTF-8")
                    + "%27%20and%20Direccion%20eq%20%27" + android.net.Uri.encode(acta.getPosicion(), "UTF-8")
                    + "%27%20and%20Motivo%20eq%20%27" + android.net.Uri.encode(acta.getMotivo(), "UTF-8")
                    + "%27%20and%20TipoVisita%20eq%20%27" + android.net.Uri.encode(acta.getTipo(), "UTF-8")
                    + "%27");
            request.setRequestTAG(Constants.POST_ACTA);

            mConnection.getRequestManager().makeRequest(request);
            Connection.addPendingOrder();

            try {
                synchronized (thActas) {
                    thActas.wait(2000); //Espero 2 segundos para que no se pisen los envíos a sap
                }
            } catch (Exception e) {
            }

        }


    }


    public void updateDataAdmin(List<IODataProperty> propertiesData) {
        try {
            DataAdminModel dataAdmin = mapeoDataAdmin(propertiesData);

            checkListDB.updateDataAdmin(dataAdmin);
        } catch (Exception e) {
//            Toast.makeText(mContext, "lalala", Toast.LENGTH_SHORT).show();
            //AppLogger.log("Error actualizando la Orden", e.getMessage());
        }

    }

    private DataAdminModel mapeoDataAdmin(List<IODataProperty> propertiesData) {

        DataAdminModel dataAdmin = new DataAdminModel();

        dataAdmin.setNum(propertiesData.get(4).getValue());
        dataAdmin.setTexto(propertiesData.get(3).getValue());
        dataAdmin.setType(propertiesData.get(5).getValue());

        return dataAdmin;
    }


}


