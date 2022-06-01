package com.example.unity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

public class MyPlugIn {
    private static final MyPlugIn ourInstance = new MyPlugIn();
    private static final String TAG = "MyPlugIn";
    MQTTHelper mqttHelper;
    Context context;
    ArrayList<String> switchList=new ArrayList<>();

    public static MyPlugIn getInstance() {
        return ourInstance;
    }

    private MyPlugIn() {
        Log.d(TAG, "MyPlugIn: Created");
    }

    public void setContext (Context unityContext){
        Log.d(TAG, "setContext: set");
        context=unityContext;
        mqttHelper =new MQTTHelper(context);
    }

    public void publish(String switchNum){
        mqttHelper.setSubscriptionTopic(switchNum);
        mqttHelper.toPublish(switchNum);
    }

    public void subscribeNewSwitch(){
      mqttHelper.setSubscriptionTopic(Integer.toString(mqttHelper.switchList.size()));
        Toast.makeText(context,"topic="+Integer.toString(mqttHelper.switchList.size()),Toast.LENGTH_LONG).show();
      mqttHelper.subscribeToTopic(1);
    }
    public int getLength(){
        switchList= mqttHelper.getStatus();
        if(!mqttHelper.switchList.isEmpty())
        {mqttHelper.unSubscribeToTopic();}
        return switchList.size();
//return 2;
    }
    public String getStateInit(String index){
        return switchList.get(Integer.parseInt(index));
//    if(index.equals("0")){
//        return ("sp");
//    }else{
//        return ("sss");
//    }
    }
    public void GetIPAddress(String IP){
        Log.d(TAG, "GetIPAdreess called");
        mqttHelper =new MQTTHelper(context,IP);
    }
}
