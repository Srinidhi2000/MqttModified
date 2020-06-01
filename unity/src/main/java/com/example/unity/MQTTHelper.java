package com.example.unity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MQTTHelper extends Service {
    public MqttAndroidClient mqttAndroidClient;
    public String IP="192.168.43.236:1883";
    String ServerUri = "tcp://"+IP;
    final String clientId = "ExampleAndroidClient";
    String subscriptionTopic;
    String statusTopic;
    String changeSwitch="OFF";
    Context mcontext;
    ArrayList<String> switchList=new ArrayList<>();
    private static final String TAG = "MyPlugIn";

    // default Constructor
    public MQTTHelper(Context context) {
        this.mcontext= context;
        mqttAndroidClient = new MqttAndroidClient(context, ServerUri, clientId);

        //executed whenever a new value arrives for the subscribed topics
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w(TAG+"mqtt", serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(mcontext, "Connection_lost", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w(TAG+"mqtt", message.toString());

            if(topic.equals(statusTopic))
            { String s=topic;
             s=s.replace("stat/sw","");
             s=s.replace("/POWER","");
                if(message.toString().equals("ON")){
                switchList.set(Integer.parseInt(s),"1");}
                else
                {
                    switchList.set(Integer.parseInt(s),"0");}
            }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.w(TAG+"mqtt", token.toString());
            }
        });
        connectToUri();
    }
    public MQTTHelper(Context context,String ip) {
        this.mcontext= context;
        IP=ip;
        ServerUri="tcp://"+ip;
        mqttAndroidClient = new MqttAndroidClient(context, ServerUri, clientId);
        Log.d(TAG, "MQTTHelper: IPSet");
        //executed whenever a new value arrives for the subscribed topics
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.w(TAG+"mqtt", serverURI);
            }

            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(mcontext, "Connection_lost", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.w(TAG+"mqtt", message.toString());
                if(topic.equals(statusTopic))
                { String s=topic;
                    s=s.replace("stat/sw","");
                    s=s.replace("/POWER","");
                    if(message.toString().equals("ON")){
                        switchList.set(Integer.parseInt(s),"1");}
                    else
                    {
                        switchList.set(Integer.parseInt(s),"0");}
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.w(TAG+"mqtt", token.toString());
            }
        });
        connectToUri();
    }
    private void connectToUri(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                   if(!switchList.isEmpty())
                   {
                       for(int i=0;i<switchList.size();i++)
                       {
                           setSubscriptionTopic(Integer.toString(i));
                           subscribeToTopic(0);
                       }
                   }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG+"mqtt", "Failed to connect to: " + ServerUri + exception.toString());
                    Toast.makeText(mcontext, "Failed to connect to"+ServerUri, Toast.LENGTH_SHORT).show();
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    //Subscribes for power on and off
    public void subscribeToTopic(final int type) {
         try {
           mqttAndroidClient.subscribe(subscriptionTopic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG+"mqtt","Subscribed!");
                    if(type==1){switchList.add("0");}
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG+"mqtt", "Subscribed fail!");
                }
            });

        }
          catch (MqttException ex) {
            System.err.println("Exception subscribing");
              Log.d(TAG, "subscribeToTopic: someproblem in subscribe");
            ex.printStackTrace();
        }
    }
    //sets subscription topic
    public void setSubscriptionTopic(String switchnum) {
        subscriptionTopic="cmnd/sw"+switchnum+"/POWER1";
    }

    //returns an arraylist of status for all the switches
    public ArrayList<String> getStatus()
    { if(!switchList.isEmpty())
    {for(int i=0;i<switchList.size();i++)
    {   setStatusTopic(Integer.toString(i));
        try {
            mqttAndroidClient.subscribe(statusTopic, 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG+"mqtt","Subscribed!");

                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG+"mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception subscribing");
            ex.printStackTrace();
        }

    }}
        return switchList;
    }

    //sets the subscription topic for getting the status
    private String setStatusTopic(String num){

        statusTopic="stat/sw"+num+"/POWER";
        return statusTopic;
    }

    //unsubscribe to a particular topic
    public void unSubscribeToTopic(){
        for(int i=0;i<switchList.size();i++){
            try {
                IMqttToken unsubToken = mqttAndroidClient.unsubscribe(setStatusTopic(Integer.toString(i)));
                unsubToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // The subscription could successfully be removed from the client
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        // some error occurred, this is very unlikely as even if the client
                        // did not had a subscription to the topic the unsubscribe action
                        // will be successfully
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }}
    }

    //Publish a message to  subscriptiontopic
    public void toPublish(String switchNum)
    {
        Log.w("Mqtt", "publishing");
        if(switchList.get(Integer.parseInt(switchNum)).equals("1"))
        {changeSwitch="OFF";
        switchList.set(Integer.parseInt(switchNum),"0"); }
        else
        {changeSwitch="ON";
            switchList.set(Integer.parseInt(switchNum),"1");}
        try {
            mqttAndroidClient.publish(subscriptionTopic, changeSwitch.getBytes(),0,true);
        } catch ( MqttException e) {
            e.printStackTrace();
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}