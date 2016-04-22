package cmx.acuntia.es.cmxmap;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {


    private static TextView text;
    private static TextView pos;
    static ImageView img;
    static Button boton;

    static InputStream is = null;
    static JSONObject jObj = null;
    static JSONObject positionObj = null;
    static JSONArray jarray = null;
    String ubicacion = "";
    static String json = "";
    String imgMap = "";
    Integer imgx = 0;
    Integer imgy = 0;
    private Paint drawPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        boton = (Button) findViewById(R.id.buttonJson);
        text = (TextView) findViewById(R.id.textView);
        pos = (TextView) findViewById(R.id.textView2);
        img = (ImageView) findViewById(R.id.imageView);
        try {
            descarga();
            downloadMap();
            getZone();
        } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        assert boton != null;
        boton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    descarga();
                    downloadMap();
                    getZone();
                } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private void descarga() throws IOException, JSONException, InterruptedException, ExecutionException {

        String mac = getWifiMacAddress();
        jObj = new DownloadTask().execute(mac).get();

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

    private void downloadMap() throws IOException, JSONException, ExecutionException, InterruptedException {
        getJSONData();
        String dir = "http://192.168.104.24/api/config/v1/maps/imagesource/" + imgMap;

        Bitmap bitmap = new ImageTask().execute(dir).get();
        img.setImageBitmap(bitmap);

        imgx = img.getWidth();
        imgy = img.getHeight();
    }

    public void getZone() throws JSONException, InterruptedException {

        getJSONData();
        JSONObject mapInfo = jObj.getJSONObject("mapInfo");
        JSONObject floorDimension = mapInfo.getJSONObject("floorDimension");
        Integer mapx = floorDimension.getInt("width");
        Integer mapy = floorDimension.getInt("length");

        imgx = img.getWidth();
        imgy = img.getHeight();

        Integer posx = positionObj.getInt("x");
        Integer posy = positionObj.getInt("y");

        if(imgx == 0 && imgy == 0){
            imgx = 1;
            imgy = 1;
        }

        Double propx = (double) (imgx / mapx);
        Double propy = (double) (imgy / mapy);

        Double x = posx*propx;
        Double y = posy*propy;

//        ImageView imageView=(ImageView) findViewById(R.id.imageView2);
//        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setColor(Color.BLACK);
//        canvas.drawCircle( 10, 10,  10, paint);
//        img.draw(canvas);
//        imageView.setImageBitmap(bitmap);

        if(posy<60 && posx<105 || 45<posy && posy<55 && posx<140){
            ubicacion = "FORMACION";
        }if (posy>60 && posx<135){
            ubicacion = "PYCLUCAS";
        }if (posy<45 && posx>105 && posx<140 || posx>139 && posx<172 && posy<55 || posx>172 && posx<200 && posy<61 || posx>200 && posx<240 && posy<75){
            ubicacion = "SOPORTE";
        }if (posx>139 && posx<172 && posy>84 || posx>172 && posx<200 && posy>79 || posx>200 && posx<240 && posy>75){
            ubicacion = "PYCALBERTO";
        }if (posy>55 && posy<85 && posx>113 && posx<160){
            ubicacion = "HALL";
        }
        text.setText(ubicacion);
        pos.setText("x: " + x + "y: " + y);
    }

    private void getJSONData() throws JSONException {
        positionObj = jObj.getJSONObject("mapCoordinate");
        JSONObject aux = jObj.getJSONObject("mapInfo");
        JSONObject aux2 = aux.getJSONObject("image");
        imgMap = aux2.getString("imageName");
    }
}
