package ie.mu.jos.mqttps

import android.content.Context
import android.util.Log
import android.widget.Toast

import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage

/**
 * A class to create and handle MQTT connections
 */
class Connection(private val context: Context) {
    private var client: MqttAndroidClient? = null

    internal fun connectMqtt() {
        try {
            val options = MqttConnectOptions()
            options.mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1

            val clientId = MqttClient.generateClientId()
            Log.i(TAG, clientId)

            //TODO (jos) MQTT broker is hardcoded.
            client = MqttAndroidClient(this.context, "tcp://test.mosquitto.org:1883", clientId)

            if (!client!!.isConnected) {
                client!!.setCallback(object : MqttCallback {

                    override fun connectionLost(cause: Throwable) {
                        logFailure("Lost Connection", exception = cause)
                        //TODO (jos) monitor this behaviour - should I connect again?
                    }

                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        println(topic + ": " + String(message.payload, "UTF-8"))
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {
                    }
                })

                client!!.connect(options, this, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        subscribe("jos_test", 1)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        logFailure("connecting", asyncActionToken, exception)
                    }
                })

            }

        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    private fun logFailure(action: String, asyncActionToken: IMqttToken? = null, exception: Throwable) {
        Toast.makeText(this.context, "Problem: $action to $asyncActionToken", Toast.LENGTH_LONG).show()
        Log.e(TAG, "Issues $action to $asyncActionToken")
        Toast.makeText(this.context, "Problem: " + action, Toast.LENGTH_LONG).show()
        Log.e(TAG, "Issues " + action + " to " + exception.message)
    }

    internal fun subscribe(topic: String, qosLevel: Int) {
        try {
            client!!.subscribe(topic, qosLevel, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    Log.i(TAG, "ALL IS GOOD subscribing to " + asyncActionToken)
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    logFailure("subscribing", asyncActionToken, exception)
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

    }

    companion object {
        val TAG = "Connection"
    }
}
