package com.example.template_conections.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.template_conections.App;
import com.example.template_conections.R;
import com.example.template_conections.utils.Connection;
import com.example.template_conections.utils.Constants;
import com.example.template_conections.utils.DataHandler;
import com.example.template_conections.utils.MessageData;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE = 1;
    private DataHandler dataHandler;
    private Handler.Callback callback;
    private ProgressDialog mProgressDialog;
    private Connection conexionSync;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeConnection();
    }

    private void initializeConnection() {
        {

            if (conexionSync == null || dataHandler == null) {
                conexionSync = ((App) getApplication()).getConnection();
                dataHandler = ((App) getApplication()).getDataHandler();
                callback = new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {

                        MessageData msgData;
                        switch (msg.what) {
                            case Constants.OK_REGISTER: {
                                conexionSync.getServiceDocument();
                                break;
                            }

                            case Constants.ERROR_REGISTER: {
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                Toast.makeText(MainActivity.this, "Error al registrarse", Toast.LENGTH_SHORT).show();
                                break;
                            }

                            case Constants.OK_SERVICE_DOCUMENT: {
                                break;
                            }

                            case Constants.ERROR_SERVICE_DOCUMENT: {
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                Toast.makeText(MainActivity.this, "Error al registrarse", Toast.LENGTH_SHORT).show();
                                break;
                            }


                            case Constants.OK_SCHEMA: {
                                Toast.makeText(MainActivity.this, "Conectado al servicio", Toast.LENGTH_SHORT).show();

                                break;
                            }

                            case Constants.ERROR_SCHEMA: {
                                Toast.makeText(MainActivity.this, "Error conectando al servicio", Toast.LENGTH_SHORT).show();
                                break;
                            }

                            case Constants.OK_READ_DATA: {

                                //ToDo - Hacer read data
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }

                                Toast.makeText(MainActivity.this, "Sincronizado", Toast.LENGTH_SHORT).show();

                                break;
                            }

                            case Constants.ERROR_READ_DATA: {
                                //ToDo - hacer caso de error
                                Toast.makeText(MainActivity.this, "Error al sincronizar", Toast.LENGTH_SHORT).show();
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                break;
                            }

                        }
                        return false;
                    }
                };

                handler = new

                        Handler(callback);
                conexionSync.setHandler(handler);
                dataHandler.setHandler(handler);
            }
            if (conexionSync.getODataSchema() != null) {

            } else {
                SharedPreferences mSharedPrefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
                boolean firstRun = mSharedPrefs.getBoolean(Constants.FIRST_RUN, true);
                if (firstRun) {
                    new Initialization().execute();
                } else {
                    new Registration().execute();
                }
            }

        }
    }

    public class Initialization extends AsyncTask<Void, Void, String> {


        private Connection conexionSync;

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            SharedPreferences mSharedPrefs = getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, 0);
            if (mSharedPrefs.getBoolean(Constants.FIRST_RUN, true)) {
                conexionSync.setODataSchema(null);
            }
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putBoolean(Constants.FIRST_RUN, false);
            editor.commit();

            if (conexionSync.getODataSchema() == null) {
                try {
                    new Registration().execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
                /*mProgressDialog = ProgressDialog.show(MenuPrincipal.this, getResources().getString(R.string.initializeTitle),
                        getResources().getString(R.string.initializeText), true);*/
        }

        @Override
        protected String doInBackground(Void... params) {
            return "";
        }
    }

    public class Registration extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            //if (mProgressDialog != null) {
            super.onPostExecute(result);
            //mProgressDialog.dismiss();
            //}
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (conexionSync.isOnline()) {
            } else {
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            if (conexionSync.isOnline() && conexionSync.getODataSchema() == null) {
                conexionSync.registerApp(Constants.APP_NAME);
            }
            return "";
        }
    }

    public static interface ClickListener {

        public void onClick(View view, int position);

        public void onLongClick(View view, int position);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (mProgressDialog != null) {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                }
                break;
        }
    }

    private void reconnect(int source) {
        conexionSync.recconect(source);
        conexionSync.setODataSchema(null);
        conexionSync.setmServiceDocument(null);
        conexionSync.getServiceDocument();
    }
}
