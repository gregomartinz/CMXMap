package cmx.acuntia.es.cmxmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private static final int RED = -65536 ;
    private static TextView text;
    static ImageView img;
    static ImageView punto;
    static Button boton;

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONObject positionObj = null;
    static JSONArray jarray = null;
    String ubicacion = "";
    static String json = "";
    String imgMap = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boton = (Button) findViewById(R.id.buttonJson);
        text = (TextView) findViewById(R.id.textView);
        img = (ImageView) findViewById(R.id.imageView);


        assert boton != null;
        boton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    descarga();
                    downloadMap();
                } catch (IOException | JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void descarga() throws IOException, JSONException, InterruptedException {
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
        positionObj = jObj.getJSONObject("mapCoordinate");
        JSONObject aux = jObj.getJSONObject("mapInfo");
        JSONObject aux2 = aux.getJSONObject("image");
        imgMap = aux2.getString("imageName");
        getZone();
        //text.setText(positionObj.toString());
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

    private void downloadMap() throws IOException, JSONException {
        getJSONData();
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

        Log.d("EL mapa está en :" , String.valueOf(img.getX()) + " " + String.valueOf(img.getY()));
        Log.d("EL boton está en :" , String.valueOf(boton.getX()));
    }

    public void getZone() throws JSONException {

        getJSONData();
        Integer x = positionObj.getInt("x");
        Integer y = positionObj.getInt("y");

        if(y<60 && x<105 || 45<y && y<55 && x<140){
            ubicacion = "FORMACION";
        }if (y>60 && x<135){
            ubicacion = "PYCLUCAS";
        }if (y<45 && x>105 && x<140 || x>139 && x<172 && y<55 || x>172 && x<200 && y<61 || x>200 && x<240 && y<75){
            ubicacion = "SOPORTE";
        }if (x>139 && x<172 && y>84 || x>172 && x<200 && y>79 || x>200 && x<240 && y>75){
            ubicacion = "PYCALBERTO";
        }if (y>55 && y<85 && x>113 && x<160){
            ubicacion = "HALL";
        }
        text.setText(ubicacion);
        Log.d("El punto en: ", String.valueOf(x));


    }

//    @Override
//    protected void onDraw(Canvas canvas)
//    {
//        super.onDraw(canvas);
//        canvas = new Canvas();
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(RED);
//        canvas.drawCircle(100,50,20,paint);
//        //canvas.drawPoint(700,y,paint);
//    }

    private void getJSONData() throws JSONException {
        positionObj = jObj.getJSONObject("mapCoordinate");
        JSONObject aux = jObj.getJSONObject("mapInfo");
        JSONObject aux2 = aux.getJSONObject("image");
        imgMap = aux2.getString("imageName");
    }
}
