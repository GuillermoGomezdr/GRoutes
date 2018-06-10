package org.example.willy.GRoutes;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login_ly extends AppCompatActivity implements AsyncResponse {

    EditText Username, Password;
    private DdbbConnection db = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ly);
        Username = (EditText) findViewById(R.id.txt_UserName);
        Password = (EditText) findViewById(R.id.txt_Password);
    }


    public void OnRegisterClick(View view) {
        startActivity(new Intent(this, register_ly.class));
    }

    public void OnLogin(View view) {
        db = new DdbbConnection((AsyncResponse) this);

        db.execute("select", "SELECT ID FROM User WHERE Username='" + Username.getText() + "' and Password='" + Password.getText() + "'");
    }


    @Override
    public void processFinish(JSONArray output) throws JSONException {
        JSONArray resultadoJSON = output;

        if(resultadoJSON.length() > 0){
            JSONObject personaActual = resultadoJSON.getJSONObject(0);

            int idUser = personaActual.getInt("ID");

            Intent pass_ID = new Intent(this, MapsActivity.class);
            pass_ID.putExtra("UserID", idUser);
            startActivity(pass_ID);
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(Login_ly.this).create();
            alertDialog.setMessage("Error al intentar iniciar sesión. \n¡Inténtelo de nuevo!");
            alertDialog.show();
        }
    }
}

