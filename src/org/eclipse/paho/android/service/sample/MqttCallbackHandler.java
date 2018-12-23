/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service.sample;

import org.eclipse.paho.android.service.SensorListener;
import org.eclipse.paho.android.service.sample.R;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import android.content.Intent;
import org.eclipse.paho.android.service.sample.Connection.ConnectionStatus;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {

  /** {@link Context} for the application used to format and import external strings**/
  private Context context;
  /** Client handle to reference the connection that this handler is attached to**/
  private String clientHandle;

  /**
   * Creates an <code>MqttCallbackHandler</code> object
   * @param context The application's context
   * @param clientHandle The handle to a {@link Connection} object
   */
  public MqttCallbackHandler(Context context, String clientHandle)
  {
    this.context = context;
    this.clientHandle = clientHandle;
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
   */
  @Override
  public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
    if (cause != null) {
      Connection c = Connections.getInstance(context).getConnection(clientHandle);
      c.addAction("Connection Lost");
      c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

      //format string to use a notification text
      Object[] args = new Object[2];
      args[0] = c.getId();
      args[1] = c.getHostName();

      String message = context.getString(R.string.connection_lost, args);

      //build intent
      Intent intent = new Intent();
      intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
      intent.putExtra("handle", clientHandle);

    }
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
   */
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {

    //Get connection object associated with this object
    Connection c = Connections.getInstance(context).getConnection(clientHandle);

    //create arguments to format message arrived notifcation string
    String[] args = new String[2];
    args[0] = new String(message.getPayload());
    args[1] = topic + ";qos:" + message.getQos() + ";retained:" + message.isRetained();
    String payload = new String(message.getPayload());
    String[]info = payload.split(" ");
    if (info.length >= 3) {
      String from = info[0];
      String to = info[1];
      String content = info[2];
      String myID = clientHandle.split("1883")[1];
      if (to.equals(myID)) {
        switch (content) {
          case "info":
            String trix = SensorListener.getListener().getInfo();
            trix = myID + " " + from +" "+ trix;
            Connections.getInstance(context).getConnection(clientHandle).getClient()
                    .publish(topic, trix.getBytes(), 0, false, null, new ActionListener(context, ActionListener.Action.PUBLISH, clientHandle, args));
            Intent ints = new Intent();
            ints.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
            ints.putExtra("handle", clientHandle);
            String not = from + "查看了你的传感器数据";
            c.addAction(not);
            break;
          case "del":
            c.getClient().disconnect(null, new ActionListener(context, ActionListener.Action.DISCONNECT, clientHandle));
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTING);
            String del = from + "把你断开了连接";
            c.addAction(del);
            break;
          case "edit":
            if (info.length == 4) {
              String name = info[3];
              Connections.getInstance(context).getConnection(clientHandle).getClient()
                      .setClientNickName(name);
              String edit = from + "把你的昵称改为了" + name;
              c.addAction(edit);
            } else {
              String er = "已阻止"+from+"的非法修改";
              c.addAction(er);
            }

            break;
          default:
            String messageString = from + ":" + content;

            //create intent to start activity
            Intent intent = new Intent();
            intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            //format string args
            Object[] notifyArgs = new String[3];
            notifyArgs[0] = c.getId();
            notifyArgs[1] = new String(message.getPayload());
            notifyArgs[2] = topic;

            //update client history
            c.addAction(messageString);
        }

      }
//    //get the string from strings.xml and format
//    String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

    }
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // Do nothing
  }

}
