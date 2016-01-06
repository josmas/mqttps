package ie.mu.jos.mqttps;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * A class to create and handle MQTT connections
 */
public class Connection {

    private final Context context;
    public static final String TAG = "Connection";
    private MqttAndroidClient client;

    public Connection(Context context){
        this.context = context;
    }

    void connectMqtt() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);

            String clientId = MqttClient.generateClientId();
            Log.i(TAG, clientId);

            //TODO (jos) MQTT broker is hardcoded.
            client = new MqttAndroidClient(this.context, "tcp://test.mosquitto.org:1883", clientId);

            if (!client.isConnected()){
                client.setCallback(new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable cause) {
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        System.out.println(topic + ": " + new String(message.getPayload(), "UTF-8"));
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                    }
                });

                client.connect(options, this, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        subscribe("jos_test", 1);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        logFailure("connecting", asyncActionToken, exception);
                    }
                });

            }

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void logFailure(String action, IMqttToken asyncActionToken, Throwable exception) {
        Toast.makeText(this.context, "Problem " + action + " to " + asyncActionToken, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Issues " + action + " to " + asyncActionToken);
        Log.e(TAG, "Issues " + action + " to " + exception.getMessage());
    }

    void subscribe(String jos_test, int i) {
        try {
            client.subscribe("jos_test", 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "ALL IS GOOD subscribing to " + asyncActionToken);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logFailure("subscribing", asyncActionToken, exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
