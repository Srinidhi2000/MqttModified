package com.example.unity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MyPlugIn {
    private static final MyPlugIn ourInstance = new MyPlugIn();
    private static final String TAG = "MyPlugIn";
    MQTTHelper mqttHelper;
    Context context;

    public static MyPlugIn getInstance() {
        return ourInstance;
    }

    private MyPlugIn() {
        Log.d(TAG, "MyPlugIn: Created");
    }

    public void setContext (Context unityContext){
        Log.d(TAG, "setContext: set");
        context=unityContext;
        mqttHelper=new MQTTHelper(context);

    }

    private void publish(String switchNum){
        mqttHelper.setSubscriptionTopic(switchNum);
            mqttHelper.toPublish(switchNum);
    }

    private void subscribeNewSwitch(){
      mqttHelper.setSubscriptionTopic(Integer.toString(mqttHelper.switchList.size()));
        mqttHelper.subscribeToTopic();
    }

    public ArrayList<String> getStateInit(){
    ArrayList<String> switchList= mqttHelper.getStatus();
    return switchList;
    }
    public void unsubscribe(){
        mqttHelper.unSubscribeToTopic();
    }
}
