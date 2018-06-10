package org.example.willy.GRoutes;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

// Usaremos el extends AsyncTask para realizar tareas mientras la aplicación se ejecuta. Los parámetros que pasamos indican los siguiente:
// 1º Datos que pasaremos al comenzar la tarea.
// 2º Datos que necesitaremos para actualizar la UI.
// 3º Datos que devolveremos al termina la tarea.
public class DdbbConnection extends AsyncTask<String, Void, JSONArray> {

    private String type = "";
    private AsyncResponse delegate = null;


    public DdbbConnection(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        type = params[0];
        String connection_url = "";
        String query = params[1];

        if(type.equals("select")){
            connection_url = "http://ggdrroutesmap.dx.am/select2.php";
        }else if (type.equals("selectJustOneValue")) {
            connection_url = "http://ggdrroutesmap.dx.am/selectJustOneValue.php";
            //connection_url = "http://ggdrroutesmap.dx.am/select2.php";
        }else if(type.equals("insert")){
            connection_url = "http://ggdrroutesmap.dx.am/insert.php";
        }

        try {
            URL url = new URL(connection_url);
            HttpURLConnection hurlc = (HttpURLConnection)url.openConnection();
            hurlc.setRequestMethod("POST");
            hurlc.setDoInput(true);
            OutputStream os = hurlc.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String post_data = URLEncoder.encode("query","UTF-8")+"="+URLEncoder.encode(query,"UTF-8");
            bw.write(post_data);
            bw.flush();

            bw.close();
            os.close();

            InputStream is = hurlc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "iso-8859-1"));
            String result = "";
            String line = "";
            while((line = br.readLine()) != null){
                result += line;
            }
            br.close();
            is.close();
            hurlc.disconnect();
            JSONArray resultadoJSON = new JSONArray(result);
            return resultadoJSON;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        if(type.equals("select") || type.equals("selectJustOneValue")) {
            try {
                delegate.processFinish(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
