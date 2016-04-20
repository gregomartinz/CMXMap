package cmx.acuntia.es.cmxmap;

import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {


    private GoogleApiClient client;
    private static TextView test;
    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        test = (TextView) findViewById(R.id.textView);
        Button boton = (Button) findViewById(R.id.buttonJson);

        boton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    descarga();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void descarga() throws IOException {



        // Making HTTP request
        try {
            // defaultHttpClient
            URL url = new URL("http://ip.jsontest.com/");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(1000);
           is = urlConnection.getInputStream();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        test.setText(jObj.toString());
    }

    
}
