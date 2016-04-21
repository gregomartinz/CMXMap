package cmx.acuntia.es.cmxmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private static TextView text;
    static ListView lv;
    static ImageView img;

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONArray jarray = null;
    static String json = "";
    String imgMap = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button boton = (Button) findViewById(R.id.buttonJson);
        text = (TextView) findViewById(R.id.textView);
        img = (ImageView) findViewById(R.id.imageView);


        assert boton != null;
        boton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    descarga();
                    downloadMap();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void descarga() throws IOException, JSONException {
        // Making HTTP GET
        try {
            String mac = getWifiMacAddress();
            mac = mac.toLowerCase();
            mac = URLEncoder.encode(mac, "UTF-8");
            String dir = "http://192.168.104.24/api/location/v2/clients?macAddress=" + mac;
            URL url = new URL(dir);
            Log.d("la url es", url.toString());
            URLConnection urlConnection = url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("authorization", "Basic YWRtaW46QWN1bnQxYQ==");
            urlConnection.setRequestProperty("cache-control", "no-cache");
            urlConnection.setConnectTimeout(1000);
           is = urlConnection.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
        //Read the content of the GET method
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            json = sb.toString();
            Log.d("Lo que se baja", json);
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        //Try parse the string to a JSON object
        try {
          jarray = new JSONArray(json);
        } catch (JSONException e) {
           Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        jObj = jarray.getJSONObject(0);
        JSONObject positionObj = jObj.getJSONObject("mapCoordinate");
        JSONObject aux = jObj.getJSONObject("mapInfo");
        JSONObject aux2 = aux.getJSONObject("image");
        imgMap = aux2.getString("imageName");
        text.setText(positionObj.toString());
    }

    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)){
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac==null){
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length()>0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    private void downloadMap() throws IOException {

        String dir = "http://192.168.104.24/api/config/v1/maps/imagesource/" + imgMap;
        URL url = new URL(dir);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setRequestProperty("authorization", "Basic YWRtaW46QWN1bnQxYQ==");
        urlConnection.setRequestProperty("cache-control", "no-cache");
        urlConnection.setConnectTimeout(1000);
        is = urlConnection.getInputStream();

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        img.setImageBitmap(bitmap);

        Log.d("EL mapa está en :" , String.valueOf(img.getX()));


    }
}
