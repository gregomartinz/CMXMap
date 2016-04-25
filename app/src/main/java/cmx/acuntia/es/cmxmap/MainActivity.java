package cmx.acuntia.es.cmxmap;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static TextView text;
    private static TextView pos;
    static ImageView img;
    static Button boton;

    static JSONObject jObj = null;
    static JSONObject positionObj = null;
    String ubicacion = "";
    String imgMap = "";
    Double imgx = 0.0;
    Double imgy = 0.0;
    Double x = 0.0;
    Double y = 0.0;
    final Handler h = new Handler();
    final int delay = 5000; //milliseconds
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;

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


        mScaleDetector = new ScaleGestureDetector(getBaseContext(), new ScaleListener());
        h.postDelayed(new Runnable(){
            public void run(){
                try {
                    descarga();
                    downloadMap();
                    getZone();
                } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                h.postDelayed(this, delay);
            }
        }, delay);

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
        try {
            descarga();
            downloadMap();
            getZone();
        } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
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

        Paint currentPaint;
        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setColor(0xFF000000);  // alpha.r.g.b
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(20);

        Bitmap bitmap = new ImageTask().execute(dir).get();
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);
       ///////////////
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            x = x+208;
        }
        tempCanvas.drawCircle(Float.valueOf(String.valueOf(x)),Float.valueOf(String.valueOf(y)),10,currentPaint);
        img.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

        imgx = (double) img.getWidth();
        imgy = (double) img.getHeight();
    }

    public void getZone() throws JSONException, InterruptedException {

        getJSONData();
        JSONObject mapInfo = jObj.getJSONObject("mapInfo");
        JSONObject floorDimension = mapInfo.getJSONObject("floorDimension");
        Integer mapx = floorDimension.getInt("width");
        Integer mapy = floorDimension.getInt("length");


        imgx = (double) img.getWidth();
        imgy = (double) img.getHeight();

        Double posx = positionObj.getDouble("x");
        Double posy = positionObj.getDouble("y");

        if(imgx == 0 && imgy == 0){
            imgx = 1.0;
            imgy = 1.0;
        }

        Double propx = imgx / mapx;
        Double propy = imgy / mapy;

        x = posx*propx;
        y = posy*propy;

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
        pos.setText("x: " + posx.intValue() + "y: " + posy.intValue());
    }

    private void getJSONData() throws JSONException {
        positionObj = jObj.getJSONObject("mapCoordinate");
        JSONObject aux = jObj.getJSONObject("mapInfo");
        JSONObject aux2 = aux.getJSONObject("image");
        imgMap = aux2.getString("imageName");
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(ev);
        return true;
    }
    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

            return true;
        }
    }

    @Override
    public void onRefresh() {
//        try {
//            descarga();
//            downloadMap();
//            getZone();
//        } catch (IOException | JSONException | InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
    }
}
