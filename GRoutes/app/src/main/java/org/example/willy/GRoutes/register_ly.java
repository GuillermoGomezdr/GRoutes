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

public class register_ly extends AppCompatActivity  implements AsyncResponse {

    EditText Username, Password, Password2;
    private DdbbConnection db = null;
    private String resultado;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_ly);
        Username = (EditText)findViewById(R.id.txt_UserName);
        Password = (EditText)findViewById(R.id.txt_Password);
        Password2 = (EditText)findViewById(R.id.txt_RepeatPassword);
    }

    public void OnRegister(View view){
        if (Username.getText().toString().equals("")){
            AlertDialog alertDialog = new AlertDialog.Builder(register_ly.this).create();
            alertDialog.setMessage("¡No has introducido un nombre de usuario!, se necesita uno para que el registro sea válido.");
            alertDialog.show();
        }
        else if (Password.getText().toString().equals("") || Password2.getText().toString().equals("")){
            AlertDialog alertDialog = new AlertDialog.Builder(register_ly.this).create();
            alertDialog.setMessage("¡No has introducido la contraseña en alguno de los campos!, Se necesita una para que el registro sea válido.");
            alertDialog.show();
        }
        else if(!Password.getText().toString().equals(Password2.getText().toString())){
            AlertDialog alertDialog = new AlertDialog.Builder(register_ly.this).create();
            alertDialog.setMessage("Has repetido incorrectamente la contraseña. ¡Asegurate de haberla escrito bien!");
            alertDialog.show();
        }
        else {
            db = new DdbbConnection(this);
            state = 1;

            db.execute("select", "SELECT Username FROM User WHERE Username='" + Username.getText() + "'");

        }



    }

    @Override
    public void processFinish(org.json.JSONArray output) throws JSONException {
        JSONArray resultadoJSON = output;

        if(state == 1){
            if(resultadoJSON.length() > 0){
                AlertDialog alertDialog = new AlertDialog.Builder(register_ly.this).create();
                alertDialog.setMessage("Ya existe un usuario con dicho nombre. \n¡Pruebe uno distinto!");
                alertDialog.show();

            } else {
                db = new DdbbConnection( this);
                db.execute("insert", "INSERT INTO User (Username, Password) VALUES ('" + Username.getText() + "', '" + Password.getText() + "');");

                db = new DdbbConnection( this);
                state = 2;

                db.execute("select", "SELECT ID FROM User ORDER BY ID DESC LIMIT 1");

            }
        } else if(state == 2){
            JSONObject personaActual = resultadoJSON.getJSONObject(0);

            int idUser = personaActual.getInt("ID");

            Intent pass_ID = new Intent(this, MapsActivity.class);
            pass_ID.putExtra("UserID", idUser);
            startActivity(pass_ID);
        }
    }

}
