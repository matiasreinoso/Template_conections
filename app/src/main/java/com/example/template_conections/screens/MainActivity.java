package com.example.template_conections.screens;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.template_conections.App;
import com.example.template_conections.R;
import com.example.template_conections.utils.Connection;
import com.example.template_conections.utils.Constants;
import com.example.template_conections.utils.DataHandler;
import com.example.template_conections.utils.MessageData;

import java.util.ArrayList;
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

                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }

                                Toast.makeText(MainActivity.this, "Sincronizado", Toast.LENGTH_SHORT).show();

                                transaction = getSupportFragmentManager().beginTransaction();
                                transaction.detach(visitas);
                                transaction.attach(visitas);

//                                transaction.commit();
                                transaction.commitAllowingStateLoss();

                                homeFragment.reloadPage();
                                iniciarContador();

                                break;
                            }

                            case Constants.ERROR_READ_DATA: {
                                Toast.makeText(MainActivity.this, "Error al sincronizar", Toast.LENGTH_SHORT).show();
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                homeFragment.reloadPage();
                                iniciarContador();

                                break;
                            }


                            case Constants.OK_TEXTO:

                                if (pendingText > 0) {
                                    sendText++;
                                }

                                if (pendingText == 0 | pendingText == sendText) {

                                    sendText = 0;
                                    if (thSendNota != null) {
                                        synchronized (thSendNota) {
                                            if (thSendNota.getState() == Thread.State.WAITING)
                                                thSendNota.notifyAll();
                                        }
                                    }

//                                pendingNotif = checkListDB.getPendingVisitasFinByVisita(visitasNum).size();
                                    thSendFin = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
//                                                final int sent = dataHandler.sendAllPendingVisitasFinData();
                                                final int sent = dataHandler.sendAllPendingVisitasFinDataByVisitas(visitasNum);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (sent > 0) {
                                                            //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                                            //inform(Constants.INFO, messageString);
                                                        } else if (sent < 0) {
                                                            //                                                            inform(Constants.ERROR,
                                                            //                                                                    "Error al enviar datos a SAP", true);
                                                            reconnect(Constants.SOURCE_DEFAULT);
                                                        } else if (sent == 0) {
                                                            if (mProgressDialog != null) {
                                                                if (mProgressDialog.isShowing()) {
                                                                    mProgressDialog.dismiss();
                                                                }
                                                            }
//                                                            Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                    }
                                                });
                                            }
                                        }
                                    });
                                    thSendFin.start();

                                }

                                break;
                            case Constants.OK_SEND_VISITA:

                                msgData = (MessageData) msg.obj;

                                String value = msgData.getValue("value");

                                reenvioChecklist = 0;
                                checklistSentOk++;
                                if (thSendVisita != null) {
                                    synchronized (thSendVisita) {
                                        if (thSendVisita.getState() == Thread.State.WAITING)
                                            thSendVisita.notifyAll();
                                    }
                                }
                                if (Connection.getPendingNotif() == 0) {

                                }

                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        if (checklistSentOk <= pendingChecklist && value.equals(Constants.MSG_S)) {
                                            mProgressDialog.setTitle("Enviando visita - Respuestas");
                                            if (pendingChecklist != 0) {
                                                mProgressDialog.setMessage("Enviado respuestas: " + checklistSentOk + " de " + pendingChecklist);
                                            }
                                        }
                                    }
                                }

                                // esto se hace para el caso que no se pudo realizar la fizcalización
                                if (value.equals(Constants.MSG_E)) {
                                    checklistSentOk = pendingChecklist;
//                                if (mProgressDialog != null) {
//                                    if (mProgressDialog.isShowing()) {
//                                        mProgressDialog.setMessage("Procesando visita");
//                                        Toast.makeText(MainActivity.this, "Enviado no fizcalización", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
                                }

                                if (pendingChecklist == checklistSentError + checklistSentOk || pendingChecklist == 0) {

                                    //Si no tiene nada que reenviar no muestra el mensaje
                                    if (value.equals(Constants.MSG_S)) {
                                        if (checklistSentOk <= pendingChecklist && pendingChecklist > 0) {
                                            Toast.makeText(getApplicationContext(), "Respuestas enviadas:  " + checklistSentOk
                                                            + " de " + pendingChecklist
                                                    , Toast.LENGTH_SHORT).show();
                                        }
                                    }


                                    actasSentOk = 0;
                                    actasSentError = 0;
                                    pendingActas = 0;
                                    try {

//                                    pendingActas = checkListDB.getInstance(getApplicationContext()).getPendingActas().size();
                                        pendingActas = checkListDB.getPendingActasByVisita(visitasNum).size();
                                    } catch (Exception e) {
                                    }
                                    if (pendingActas > 0) {

                                        mProgressDialog.setMessage("Enviando actas pendientes..");
                                        mProgressDialog.setTitle("Enviando Visita - Actas");

                                        // si la primera vez tengo actas, inicio el hilo de sendActas
                                        thSendActa = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
//                                                final int sent = dataHandler.sendAllPendingActasData();
                                                    final int sent = dataHandler.sendAllPendingActasDataByVisita(visitasNum);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (sent > 0) {
                                                                //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                                                //inform(Constants.INFO, messageString);
                                                            } else if (sent < 0) {
                                                                //                                                            inform(Constants.ERROR,
                                                                //                                                                    "Error al enviar datos a SAP", true);
                                                                reconnect(Constants.SOURCE_DEFAULT);
                                                            } else if (sent == 0) {
                                                                if (mProgressDialog != null) {
                                                                    if (mProgressDialog.isShowing()) {
                                                                        mProgressDialog.dismiss();
                                                                    }
                                                                }
//                                                            Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        thSendActa.start();
                                    } else {

                                        //como no tengo actas por enviar, envío el post final de la visita
                                        //iniciando el hilo del postFin
                                        mProgressDialog.setMessage("Enviando visita tratada..");
                                        mProgressDialog.setTitle("Enviando visita");


                                        notifSentOk = 0;
                                        notifSentError = 0;
                                        pendingNotif = 0;
                                        try {

//                                        pendingNotif = checkListDB.getInstance(getApplicationContext()).getPendingVisitasFin().size();
                                            pendingNotif = checkListDB.getPendingVisitasFinByVisita(visitasNum).size();
                                            pendingText = checkListDB.getPendingTextosFin(visitasNum).size();

                                        } catch (Exception e) {
                                        }

                                        if (pendingText > 0) {

                                            thSendNota = new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        final int sent = dataHandler.sendAllPendingTextByVisitas(visitasNum);
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if (sent > 0) {
                                                                    //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                                                    //inform(Constants.INFO, messageString);
                                                                } else if (sent < 0) {
                                                                    //                                                            inform(Constants.ERROR,
                                                                    //                                                                    "Error al enviar datos a SAP", true);
                                                                    reconnect(Constants.SOURCE_DEFAULT);
                                                                } else if (sent == 0) {
                                                                    if (mProgressDialog != null) {
                                                                        if (mProgressDialog.isShowing()) {
                                                                            mProgressDialog.dismiss();
                                                                        }
                                                                    }
//                                                            Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();

                                                                }
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {

                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                            thSendNota.start();
                                        } else {
                                            Message.obtain(handler, Constants.OK_TEXTO, 0, 0).sendToTarget();
                                        }


                                    }

                                }
                                break;


                            case Constants.WARN_SEND_VISITA:
                                if (Connection.getPendingNotif() == 0) {
//                                    if (mProgressDialog != null) {
//                                        if (mProgressDialog.isShowing()) {
//                                            mProgressDialog.setMessage("Enviando actas pendientes..");
//                                            mProgressDialog.setTitle("Enviando actas");
//                                        }
//    //                                }
//                                        actasSentOk = 0;
//                                        actasSentError = 0;
//                                        pendingActas = 0;
//                                        try {
//
//                                            pendingActas = checkListDB.getInstance(getApplicationContext()).getPendingActas().size();
//                                        } catch (Exception e) {
//                                        }
//                                        thSendActa = new Thread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                try {
//                                                    final int sent = dataHandler.sendAllPendingActasData();
//                                                    runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            if (sent > 0) {
//                                                                //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
//                                                                //inform(Constants.INFO, messageString);
//                                                            } else if (sent < 0) {
//    //                                                            inform(Constants.ERROR,
//    //                                                                    "Error al enviar datos a SAP", true);
//                                                                reconnect(Constants.SOURCE_DEFAULT);
//                                                            } else if (sent == 0) {
//                                                                if (mProgressDialog != null) {
//                                                                    if (mProgressDialog.isShowing()) {
//                                                                        mProgressDialog.dismiss();
//                                                                    }
//                                                                }
//                                                                Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();
//
//                                                            }
//                                                        }
//                                                    });
//                                                } catch (Exception e) {
//                                                    runOnUiThread(new Runnable() {
//                                                        @Override
//                                                        public void run() {
//
//                                                        }
//                                                    });
//                                                }
//                                            }
//                                        });
//                                        thSendActa.start();
//                                    }

                                    Fragment visitas = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                                    transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.detach(visitas);
                                    transaction.attach(visitas);
//                                transaction.commit()
                                    transaction.commitAllowingStateLoss();

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
//                                        mProgressDialog.dismiss();
                                        }
                                    }
                                }
                                break;
                            //
                            case Constants.ERROR_SEND_VISITA:
                                //loadingSendOrders.dismiss();

                                msgData = (MessageData) msg.obj;
                                String error = msgData.getValue("error");
                                //BOI GSEQUEIRA 28-8-2018 mato al hilo, si no lo mato hace multiples con otros hilos
                                thSendVisita.currentThread().interrupted();
                                thSendVisita = null;

                                //EOI GSEQUEIRA 28-2-2018

                                if (checklistSentError == 0) {

//                                Toast.makeText(MainActivity.this, "Error al enviar las fotos", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                                    homeFragment.reloadPage();
                                    iniciarContador();
                                }

//                            if (reenvioChecklist > Constants.MAX_INTENT_SEND_PHOTOS) {
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
//                            }
                                checklistSentError++;
//                            Toast.makeText(getApplicationContext(), "Error al enviar respuestas", Toast.LENGTH_SHORT).show();


//                            if (mProgressDialog != null) {
//                                if (mProgressDialog.isShowing()) {
//                                    //                                    mProgressDialog.setMessage(getResources().getString(R.string.errSMPCreate) + " " + msgData.getValue("local"));
//                                }
//                            }
                                if (thSendVisita != null) {
                                    synchronized (thSendVisita) {
                                        if (thSendVisita.getState() == Thread.State.WAITING)
                                            thSendVisita.notifyAll();
                                    }
                                }
//                            thSendVisita.interrupt();
                                //                            inform(Constants.INFO, getResources().getString(R.string.errSMPCreate),false);
//                            if (Connection.getPendingNotif() == 0) {
//                                //                                inform(Constants.ERROR, getResources().getString(R.string.errSMPCreateGroup),false);
//                            }
                                if (pendingChecklist == checklistSentError + checklistSentOk) {
                                    //                                String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  pendingNotif;
                                    //inform(Constants.INFO, messageString,true);
                                    /*if(loadingSendOrders != null)
                                        if(loadingSendOrders.isShowing())
                                            loadingSendOrders.dismiss();*/
                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                    //                                refreshOrderList();
                                }
                                break;

                            case Constants.OK_SEND_ACTA:

                                msgData = (MessageData) msg.obj;

                                actasSentOk++;
                                if (thSendActa != null) {
                                    synchronized (thSendActa) {
                                        if (thSendActa.getState() == Thread.State.WAITING)
                                            thSendActa.notifyAll();
                                    }
                                }
                                if (Connection.getPendingNotif() == 0) {

                                }
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.setTitle("Enviando visita - Actas");
                                        if (pendingActas != 0) {
                                            mProgressDialog.setMessage("Enviado actas: " + actasSentOk + " de " + pendingActas);
                                        }
                                    }
                                }
//                            if (pendingActas == actasSentError + actasSentOk || pendingActas == 0) {
                                if (pendingActas == actasSentOk || pendingActas == 0) {


                                    if (pendingActas != 0) {
                                        Toast.makeText(getApplicationContext(), "Actas enviadas:  " + actasSentOk
                                                        + " de " + pendingActas
                                                , Toast.LENGTH_SHORT).show();
                                    }

                                    mProgressDialog.setMessage("Enviando visitas tratadas..");
                                    mProgressDialog.setTitle("Enviando visita");

                                    //                                }
                                    actasSentOk = 0;
                                    notifSentOk = 0;
                                    notifSentError = 0;
                                    pendingNotif = 0;
                                    try {

                                        pendingNotif = checkListDB.getPendingVisitasFinByVisita(visitasNum).size();
                                        pendingText = checkListDB.getPendingTextosFin(visitasNum).size();

                                    } catch (Exception e) {
                                    }

                                    if (pendingText > 0) {

                                        thSendNota = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final int sent = dataHandler.sendAllPendingTextByVisitas(visitasNum);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (sent > 0) {
                                                                //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                                                //inform(Constants.INFO, messageString);
                                                            } else if (sent < 0) {
                                                                //                                                            inform(Constants.ERROR,
                                                                //                                                                    "Error al enviar datos a SAP", true);
                                                                reconnect(Constants.SOURCE_DEFAULT);
                                                            } else if (sent == 0) {
                                                                if (mProgressDialog != null) {
                                                                    if (mProgressDialog.isShowing()) {
                                                                        mProgressDialog.dismiss();
                                                                    }
                                                                }
//                                                            Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        thSendNota.start();
                                    } else {
                                        Message.obtain(handler, Constants.OK_TEXTO, 0, 0).sendToTarget();
                                    }


//                                pendingNotif = 0;
//                                try {
////                                    pendingNotif = checkListDB.getInstance(getApplicationContext()).getPendingVisitasFin().size();
//                                    pendingNotif = checkListDB.getPendingVisitasFinByVisita(visitasNum).size();
//                                } catch (Exception e) {
//                                }
//                                thSendFin = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
////                                            final int sent = dataHandler.sendAllPendingVisitasFinData();
//                                            final int sent = dataHandler.sendAllPendingVisitasFinDataByVisitas(visitasNum);
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    if (sent > 0) {
//                                                        //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
//                                                        //inform(Constants.INFO, messageString);
//                                                    } else if (sent < 0) {
//                                                        //                                                            inform(Constants.ERROR,
//                                                        //                                                                    "Error al enviar datos a SAP", true);
//                                                        reconnect(Constants.SOURCE_DEFAULT);
//                                                    } else if (sent == 0) {
//                                                        if (mProgressDialog != null) {
//                                                            if (mProgressDialog.isShowing()) {
//                                                                mProgressDialog.dismiss();
//                                                            }
//                                                        }
//                                                        Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();
//
//                                                    }
//                                                }
//                                            });
//                                        } catch (Exception e) {
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//
//                                                }
//                                            });
//                                        }
//                                    }
//                                });
//                                thSendFin.start();
                                }
                                break;

                            case Constants.WARN_SEND_FIN_CHECKLIST:

                                if (Connection.getPendingNotif() == 0) {
                                    //                                if (mProgressDialog != null) {
                                    //                                    if (mProgressDialog.isShowing()) {
                                    //                                        mProgressDialog.setMessage("Enviando visitas pendientes..");
                                    //                                        mProgressDialog.setTitle("Enviando visita");
                                    //                                    }
                                    ////                                }

                                    //                                if (mProgressDialog != null) {
//                                if (mProgressDialog.isShowing()) {
//                                    mProgressDialog.setMessage("Buscando visitas pendientes");
//                                    mProgressDialog.setTitle("Actualizando visitas");
//                                }
//                                //                                }
//                                readVisitas();

                                    //
                                    //                                    pendingChecklist = 0;
                                    //                                    checklistSentOk = 0;
                                    //                                    orderSentError = 0; //Inicializo contadores de envios
                                    //                                    try {
                                    //
                                    //                                        pendingChecklist = checkListDB.getInstance(getApplicationContext()).getPendingVisitasFin().size();
                                    //                                    } catch (Exception e) {
                                    //                                    }
                                    //                                    thSendFin = new Thread(new Runnable() {
                                    //                                        @Override
                                    //                                        public void run() {
                                    //                                            try {
                                    //                                                final int sent = dataHandler.sendAllPendingVisitaFin();
                                    //                                                runOnUiThread(new Runnable() {
                                    //                                                    @Override
                                    //                                                    public void run() {
                                    //                                                        if (sent > 0) {
                                    //                                                            //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                    //                                                            //inform(Constants.INFO, messageString);
                                    //                                                        } else if (sent < 0) {
                                    ////                                                            inform(Constants.ERROR,
                                    ////                                                                    "Error al enviar datos a SAP", true);
                                    //                                                            reconnect(Constants.SOURCE_DEFAULT);
                                    //                                                        } else if (sent == 0) {
                                    //                                                            if (mProgressDialog != null) {
                                    //                                                                if (mProgressDialog.isShowing()) {
                                    //                                                                    mProgressDialog.dismiss();
                                    //                                                                }
                                    //                                                            }
                                    //                                                            Toast.makeText(MainActivity.this, "No hay encuestas pendientes", Toast.LENGTH_SHORT).show();
                                    //
                                    //                                                        }
                                    //                                                    }
                                    //                                                });
                                    //                                            } catch (Exception e) {
                                    //                                                runOnUiThread(new Runnable() {
                                    //                                                    @Override
                                    //                                                    public void run() {
                                    //
                                    //                                                    }
                                    //                                                });
                                    //                                            }
                                    //                                        }
                                    //                                    });
                                    //                                    thSendFin.start();
                                    //                                }
                                    //
                                    //
                                    //                                if (mProgressDialog != null) {
                                    //                                    if (mProgressDialog.isShowing()) {
                                    //                                        mProgressDialog.dismiss();
                                    //                                    }
                                    //                                }

                                    Fragment visitas = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                                    transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.detach(visitas);
                                    transaction.attach(visitas);
//                                transaction.commit();
                                    transaction.commitAllowingStateLoss();

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }

                                }
                                break;
                            case Constants.WARN_SEND_ACTA:
                                if (Connection.getPendingNotif() == 0) {
                                    //                                if (mProgressDialog != null) {
//                                if (mProgressDialog.isShowing()) {
//                                    mProgressDialog.setMessage("Enviando visitas tratadas..");
//                                    mProgressDialog.setTitle("Enviando visita");
//                                }
//                                //                                }
//                                notifSentOk = 0;
//                                notifSentError = 0;
//                                pendingNotif = 0;
//                                try {
//
//                                    pendingNotif = checkListDB.getInstance(getApplicationContext()).getPendingVisitasFin().size();
//                                } catch (Exception e) {
//                                }
//                                thSendFin = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            final int sent = dataHandler.sendAllPendingVisitasFinData();
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    if (sent > 0) {
//                                                        //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
//                                                        //inform(Constants.INFO, messageString);
//                                                    } else if (sent < 0) {
//                                                        //                                                            inform(Constants.ERROR,
//                                                        //                                                                    "Error al enviar datos a SAP", true);
//                                                        reconnect(Constants.SOURCE_DEFAULT);
//                                                    } else if (sent == 0) {
//                                                        if (mProgressDialog != null) {
//                                                            if (mProgressDialog.isShowing()) {
//                                                                mProgressDialog.dismiss();
//                                                            }
//                                                        }
//                                                        Toast.makeText(MainActivity.this, "No hay actas pendientes", Toast.LENGTH_SHORT).show();
//
//                                                    }
//                                                }
//                                            });
//                                        } catch (Exception e) {
//                                            runOnUiThread(new Runnable() {
//                                                @Override
//                                                public void run() {
//
//                                                }
//                                            });
//                                        }
//                                    }
//                                });
//                                thSendFin.start();
                                    Fragment visitas = getSupportFragmentManager().findFragmentById(R.id.home_fragment);
                                    transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.detach(visitas);
                                    transaction.attach(visitas);
//                                transaction.commit();
                                    transaction.commitAllowingStateLoss();

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }

                                }
                                break;

                            case Constants.ERROR_SEND_ACTA:
                                //loadingSendOrders.dismiss();
                                actasSentError++;

                                thSendActa.currentThread().interrupted();
                                thSendActa = null;

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_conexion), Toast.LENGTH_SHORT).show();

                                msgData = (MessageData) msg.obj;
                                //notifSentError ++;
                                //                            inform(Constants.ERROR, getResources().getString(R.string.errSMPCreate) + " " + msgData.getValue("local"), false);
                                //                            inform(Constants.INFO, getResources().getString(R.string.errSMPCreate) + " " + msgData.getValue("local") + ". (" + (notifSentOk + notifSentError) + "/" + pendingNotif + ")",false);
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        //                                    mProgressDialog.setMessage(getResources().getString(R.string.errSMPCreate) + " " + msgData.getValue("local"));
                                    }
                                }
                                if (thSendActa != null) {
                                    synchronized (thSendActa) {
                                        if (thSendActa.getState() == Thread.State.WAITING)
                                            thSendActa.notifyAll();
                                    }
                                }
                                //                            inform(Constants.INFO, getResources().getString(R.string.errSMPCreate),false);
                                if (Connection.getPendingNotif() == 0) {
                                    //                                inform(Constants.ERROR, getResources().getString(R.string.errSMPCreateGroup),false);
                                }
                                if (pendingActas == actasSentError + actasSentOk) {
                                    //                                String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  pendingNotif;
                                    //inform(Constants.INFO, messageString,true);
                                    /*if(loadingSendOrders != null)
                                        if(loadingSendOrders.isShowing())
                                            loadingSendOrders.dismiss();*/
                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                    //                                refreshOrderList();
                                }
                                break;
                            case Constants.ERROR_SEND_TEXTO:
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_texto), Toast.LENGTH_SHORT).show();
                                break;
                            case Constants.ERROR_SEND_FIN_CHECKLIST:

                                notifSentError++;

                                msgData = (MessageData) msg.obj;
//                            String errorFin = msgData.getValue("error");

                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_servidor), Toast.LENGTH_SHORT).show();

                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                    }

                                }
                                if (thSendFin != null) {
                                    synchronized (thSendFin) {
                                        if (thSendFin.getState() == Thread.State.WAITING)
                                            thSendFin.notifyAll();
                                    }
                                }
                                if (Connection.getPendingNotif() == 0) {
                                }
                                if (pendingNotif == notifSentError + notifSentOk) {

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                }
                                homeFragment.reloadPage();
                                iniciarContador();

                                break;

                            case Constants.OK_SEND_FIN_CHECKLIST:

                                msgData = (MessageData) msg.obj;

                                notifSentOk++;
                                if (thSendFin != null) {
                                    synchronized (thSendFin) {
                                        if (thSendFin.getState() == Thread.State.WAITING)
                                            thSendFin.notifyAll();
                                    }
                                }
                                if (Connection.getPendingNotif() == 0) {

                                }


//                            if (pendingNotif == notifSentError + notifSentOk || pendingNotif == 0) {
                                if (notifSentError == 0) {

                                    visitasActuales++;


                                    if (visitasActuales <= visitasTotal) {
                                        Toast.makeText(getApplicationContext(), "Visitas enviadas:  " + visitasActuales
                                                        + " de " + visitasTotal
                                                , Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Visitas enviadas:  " + visitasTotal
                                                        + " de " + visitasTotal
                                                , Toast.LENGTH_SHORT).show();
                                    }

                                    //Busca nuevas visitas para enviar
//                                checkListDB.deleteNoPendientesyCompletas(visitasNum);
                                    checkListDB.deleteVisitaList(visitasNum);

                                    visitasNum = new ArrayList<>();
                                    visitasNum = getOnlyVisita(Constants.NUMBER_OF_VIEW);

                                    if (visitasNum.size() > 0) {

                                        if (mProgressDialog != null) {
                                            if (mProgressDialog.isShowing()) {
                                                mProgressDialog.dismiss();
                                            }
                                        }

                                        mProgressDialog = ProgressDialog.show(MainActivity.this, getResources().getString(R.string.loading_send_imagen),
                                                getResources().getString(R.string.loading_waiting), true);
                                        //                        Toast.makeText(MainActivity.this, "Sincronizando", Toast.LENGTH_LONG).show();

                                        reenvioImages = 0;
                                        pendingImages = 0;
                                        imagesSentOk = 0;
                                        imagesSentError = 0;

//                        pendingImages = checkListDB.getPendingImages();

                                        for (VisitaModel v : visitasNum) {
                                            int i = checkListDB.getPendingImagesByVisita(v.getNum());
                                            pendingImages = pendingImages + i;
                                        }

                                        thImage = new Thread(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        dataHandler.restarPos();
                                                        dataHandler.sendAllImageDataByVisita(visitasNum);
                                                    }
                                                });
                                        //envía las imagenes y las visitas finalizadassn
                                        thImage.start();

                                    } else {

//                                    Toast.makeText(getApplicationContext(), "Visitas enviadas:  " + visitasActuales
//                                                    + " de " + visitasTotal
//                                            , Toast.LENGTH_SHORT).show();

//                                    Toast.makeText(getApplicationContext(), "Visita enviada", Toast.LENGTH_SHORT).show();
                                        mProgressDialog.setMessage("Buscando visitas pendientes");
                                        mProgressDialog.setTitle("Actualizando visitas");

                                        readVisitasByVisita(visitasNum);
                                    }

                                }

                                break;


                            case Constants.RESUME_IMG_THREAD: {
                                synchronized (thImage) {
                                    thImage.notifyAll();
                                }
                                break;
                            }

                            case Constants.OK_IMAGE: {
//                            Toast.makeText(MainActivity.this, "Imágenes enviadas exitosamente", Toast.LENGTH_SHORT).show();
                                imagesSentOk++;
                                reenvioImages = 0;
                                msgData = (MessageData) msg.obj;
                                String num = msgData.getValue("num");
                                String img = msgData.getValue("img");
                                //                            AppLogger.log("Orden " + num + ",imagen " + img + " envio exitoso" );
//                            mProgressDialog.setMessage("Visita " + num + ",imagen " + img + " envio exitoso");

                                break;
                            }

                            case Constants.WARN_IMAGE:

                                msgData = (MessageData) msg.obj;
                                String total = msgData.getValue("total");

                                if (mProgressDialog != null) {
//                                if (mProgressDialog.isShowing()) {
//                                    mProgressDialog.dismiss();
//                                }
                                    //                                imagesSentError = 0;
                                    //                                pendingImages = 0;


                                    //TODO mejora esto, pq entra cuando quiere

//                                if (imagesSentError == 0 || pendingImages == 0) {
                                    if (imagesSentError == 0 && reenvioImages == 0) {
                                        //checkEnvioTotal = false;
                                        reenvioChecklist = 0;
                                        pendingChecklist = 0;
                                        checklistSentOk = 0;
                                        checklistSentError = 0; //Inicializo contadores de envios

                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.setMessage("Enviando respuestas completadas...");
                                            mProgressDialog.setTitle("Enviando visita - Respuestas");
                                        } else {
                                            mProgressDialog = ProgressDialog.show(MainActivity.this, "Enviando visita - Respuestas",
                                                    "Enviando respuestas completadas...", true);
                                        }
                                        //                                    inform(Constants.INFO, "Enviando visitas pendientes..", true);
                                        try {

//                                        pendingChecklist = checkListDB.getInstance(getApplicationContext()).getPendingChecklist().size();
                                            pendingChecklist = checkListDB.getPendingChecklistByVisita(visitasNum).size();

                                        } catch (Exception e) {
                                        }
                                        thSendVisita = new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final int sent = dataHandler.sendAllPendingVisitasDataByVisita(visitasNum);
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            if (sent > 0) {
                                                            } else if (sent < 0) {
                                                                //                                                            inform(Constants.ERROR,
                                                                //                                                                    "Error al enviar datos a SAP", true);
                                                                reconnect(Constants.SOURCE_DEFAULT);
                                                            } else if (sent == 0) {
                                                                if (mProgressDialog != null) {
                                                                    if (mProgressDialog.isShowing()) {
                                                                        mProgressDialog.dismiss();
                                                                    }
                                                                }
//                                                            Toast.makeText(MainActivity.this, "No hay visitas pendientes de envio", Toast.LENGTH_SHORT).show();

                                                            }
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                        }
                                                    });
                                                }
                                            }
                                        });
                                        pendingImages = 0;
                                        imagesSentOk = 0;
                                        imagesSentError = 0;
                                        thSendVisita.start();
                                    }


// if (imagesSentError > 0) {
//                                    Toast.makeText(MainActivity.this, "Error al envíar las fotos", Toast.LENGTH_SHORT).show();
//                                }
//                                pendingImages = 0;
//                                imagesSentOk = 0;
//                                imagesSentError = 0;
                                }
                                break;

                            case Constants.ERROR_IMAGE: {


                                msgData = (MessageData) msg.obj;
                                String errorImagen = msgData.getValue("error");

                                if (thImage != null) {
                                    synchronized (thImage) {
                                        if (thImage.getState() == Thread.State.WAITING)
                                            thImage.notifyAll();
                                    }
                                }

//                            thImage.interrupt();

                                if (reenvioImages > Constants.MAX_INTENT_SEND_PHOTOS) {
                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                }

                                if (imagesSentError == 1) {
//                                Toast.makeText(MainActivity.this, "Error al enviar las fotos", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(MainActivity.this, errorImagen, Toast.LENGTH_SHORT).show();
                                    homeFragment.reloadPage();
                                    iniciarContador();
                                }
                                dataHandler.restarPos();
                                imagesSentError++;

                                //if (Connection.getPendingImage() == 0) {
                                //                            AppLogger.log(getResources().getString(R.string.errSMPImage));
                                //                            mProgressDialog.setMessage(getResources().getString(R.string.errSMPImage));
                            }

                            case Constants.SENDING_PHOTO:
                                msgData = (MessageData) msg.obj;
                                if (msgData != null) {
                                    mProgressDialog.setTitle(R.string.loading_send_imagen);
                                    mProgressDialog.setMessage(msgData.getValue());
                                } else {

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.dismiss();
                                        }
                                    }
                                }


                                break;
                            //
                            //                        case Constants.OK_RECONNECT_NOTIF: {
                            //                            /*synchronized (thSendAviso){ thSendAviso.notifyAll(); }
                            //                            break;*/
                            ////                            inform(Constants.INFO,
                            ////                                    "Reanudando envío de visitas...",true);
                            //                            thSendAviso.interrupt();
                            //                            thSendAviso = new Thread(
                            //                                    new Runnable() {
                            //                                        @Override
                            //                                        public void run() {
                            //                                            setThSend();
                            //                                        }
                            //                                    });
                            //                            thSendAviso.start();
                            //                            break;
                            //                        }
                            //
                            case Constants.OK_RECONNECT_IMG: {
                                //                            inform(Constants.INFO,
                                //                                    "Reanudando envío de imágenes de visitas...",true);

                                if (reenvioImages < Constants.MAX_INTENT_SEND_PHOTOS) {

                                    reenvioImages++;

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.setTitle("Enviando visita");
                                            mProgressDialog.setMessage("Reestableciendo conexión");
                                        }
                                    }

                                    if (reenvioImages == Constants.MAX_INTENT_SEND_PHOTOS) {

                                        visitasNum = getOnlyVisita(Constants.NUMBER_OF_VIEW);

                                        for (VisitaModel v : visitasNum) {
                                            checkListDB.resetImages(v.getNum());
                                        }
                                    }

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {


//                                        if (mProgressDialog != null) {
//                                            if (mProgressDialog.isShowing()) {
//                                                mProgressDialog.dismiss();
//                                            }
//                                        }

                                            pendingImages = 0;
                                            imagesSentOk = 0;
                                            imagesSentError = 0;
                                            dataHandler.restarPos();

//                                        pendingImages = checkListDB.getPendingImages();
                                            thImage.currentThread().interrupted();
                                            thImage = null;

                                            visitasNum = getOnlyVisita(Constants.NUMBER_OF_VIEW);

                                            for (VisitaModel v : visitasNum) {
                                                int i = checkListDB.getPendingImagesByVisita(v.getNum());
                                                pendingImages = pendingImages + i;
                                            }


                                            thImage = new Thread(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            dataHandler.restarPos();
//                                                        dataHandler.sendAllImageData();
                                                            dataHandler.sendAllImageDataByVisita(visitasNum);
                                                        }
                                                    });

                                            thImage.start();
                                        }
                                    }, 4000);
//                                Toast.makeText(MainActivity.this, "Se ha intentado el envío: " + String.valueOf(reenvioImages), Toast.LENGTH_SHORT).show();


//                                    pendingImages = 0;
//                                    imagesSentOk = 0;
//                                    imagesSentError = 0;
//                                    dataHandler.restarPos();
//
//
//                                    pendingImages = checkListDB.getPendingImages();
//                                    thImage.interrupt();
//
//
//                                    thImage = new Thread(
//                                            new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    dataHandler.sendAllImageData();
//                                                }
//                                            });
//
//                                    thImage.start();


                                } else {

                                    if (reenvioImages == Constants.MAX_INTENT_SEND_PHOTOS) {
                                        MessageData messageData = new MessageData(Constants.IMAGE);
                                        messageData.addValue("error", getResources().getString(R.string.error_conexion));
                                        Message.obtain(handler, Constants.ERROR_IMAGE, 0, 0, messageData).sendToTarget();
                                        reenvioImages++;
                                        imagesSentError++;
                                    }
                                }
                                break;
                            }

                            case Constants.OK_RECONNECT_NOTIF: {

                                if (reenvioChecklist < Constants.MAX_INTENT_SEND_PHOTOS) {

                                    reenvioChecklist++;

                                    if (mProgressDialog != null) {
                                        if (mProgressDialog.isShowing()) {
                                            mProgressDialog.setTitle("Enviando visita");
                                            mProgressDialog.setMessage("Reestableciendo conexión");
                                        }
                                    }


                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {

                                            if (reenvioChecklist < Constants.MAX_INTENT_SEND_PHOTOS
//                                                && !(thSendVisita.getState() == Thread.State.RUNNABLE)
                                            ) {

                                                if (mProgressDialog != null) {
                                                    if (mProgressDialog.isShowing()) {
                                                        mProgressDialog.dismiss();
                                                    }
                                                }


                                                thSendVisita.currentThread().interrupted();
                                                thSendVisita = null;

                                                pendingChecklist = 0;
                                                checklistSentOk = 0;
                                                checklistSentError = 0; //Inicializo contadores de envios
                                                mProgressDialog = ProgressDialog.show(MainActivity.this, "Enviando visita - Respuestas",
                                                        "Enviando respuestas completadas...", true);
                                                //                                    inform(Constants.INFO, "Enviando visitas pendientes..", true);
                                                try {

//                                        pendingChecklist = checkListDB.getInstance(getApplicationContext()).getPendingChecklist().size();
                                                    pendingChecklist = checkListDB.getPendingChecklistByVisita(visitasNum).size();


                                                } catch (Exception e) {
                                                }
                                                thSendVisita = new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
//                                                final int sent = dataHandler.sendAllPendingVisitasData();
                                                            final int sent = dataHandler.sendAllPendingVisitasDataByVisita(visitasNum);
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (sent > 0) {
                                                                        //String messageString = getResources().getString(R.string.startCreateNotif) + " " + notifSentOk + " de " +  String.valueOf(sent);
                                                                        //inform(Constants.INFO, messageString);
                                                                    } else if (sent < 0) {
                                                                        //                                                            inform(Constants.ERROR,
                                                                        //                                                                    "Error al enviar datos a SAP", true);
                                                                        reconnect(Constants.SOURCE_DEFAULT);
                                                                    } else if (sent == 0) {
                                                                        if (mProgressDialog != null) {
                                                                            if (mProgressDialog.isShowing()) {
                                                                                mProgressDialog.dismiss();
                                                                            }
                                                                        }
//                                                                Toast.makeText(MainActivity.this, "No hay visitas pendientes de envio", Toast.LENGTH_SHORT).show();

                                                                    }
                                                                }
                                                            });
                                                        } catch (Exception e) {
                                                            runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                                pendingImages = 0;
                                                imagesSentOk = 0;
                                                imagesSentError = 0;
                                                thSendVisita.start();
                                            }
                                        }
                                    }, 6000);
                                } else {
                                    if (reenvioChecklist == Constants.MAX_INTENT_SEND_PHOTOS) {
                                        reenvioChecklist++;
                                        MessageData messageData = new MessageData(Constants.POST_VISITA);
                                        messageData.addValue("error", getResources().getString(R.string.error_conexion));
                                        Message.obtain(handler, Constants.ERROR_SEND_VISITA, 0, 0, messageData).sendToTarget();
                                    }
                                    if (reenvioChecklist > Constants.MAX_INTENT_SEND_PHOTOS + 1) {
                                        reenvioChecklist = 0;
                                        if (mProgressDialog != null) {
                                            if (mProgressDialog.isShowing()) {
                                                mProgressDialog.dismiss();
                                            }
                                        }
                                    }
                                }
                                break;
                            }

                            case Constants.OK_ADMIN_DATA:
                                Toast.makeText(MainActivity.this, "Datos administrativos sincronizados exitosamente"
                                        , Toast.LENGTH_SHORT).show();
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                break;

                            case Constants.ERROR_ADMIN_DATA:
                                Toast.makeText(MainActivity.this, "No se pudo sincronizar datos administrativos"
                                        , Toast.LENGTH_SHORT).show();
                                if (mProgressDialog != null) {
                                    if (mProgressDialog.isShowing()) {
                                        mProgressDialog.dismiss();
                                    }
                                }
                                break;
                        }
                        return false;
                    }
                }

                ;
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

                /*if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                }*/
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
                //tvEstado.setText(getResources().getString(R.string.initStat));
            } else {
                    /*Toast.makeText(getContext(),
                            getResources().getString(R.string.errConnect),
                            Toast.LENGTH_SHORT).show();*/
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
                homeFragment.reloadPage();
                iniciarContador();

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
