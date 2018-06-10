package org.example.willy.GRoutes;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

// androidhive.info
// hermosaprogramacion.com
// sgoliver.net

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        AsyncResponse{

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private boolean centerCamera = false;

    private int UserID;
    private DdbbConnection db = null;

    LatLng positionUser;
    ArrayList<LatLng> MarkerPoints;
    ArrayList<String> wpName;
    ArrayList<String> wpDescription;
    ArrayList<String> wpCategory;


    //
    Dialog dialog;
    ImageView closePopup;
    Spinner spCategory;
    //
    ListView listView;

    public static final int REQUEST_LOCATION_CODE = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Obtenemos y guardamos el ID del usuario conectado.
        Bundle bundle = getIntent().getExtras();
        UserID = bundle.getInt("UserID");

        // Se comprueba que se tengan los permisos necesarios.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkLocationPermission();
        }

        // Obtenemos SupportMapFragment y nos notifica cuando está listo para usarlo.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Inicializamos el array de marcas.
        MarkerPoints = new ArrayList<>();
        wpName = new ArrayList<>();
        wpDescription = new ArrayList<>();
        wpCategory = new ArrayList<>();

        //Para los popups..
        dialog = new Dialog(this);


    }




    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Desactivamos algunos de los botones por defecto de Google Maps que no nos interesan.
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);


        // Necesitaremos permisos de localización:

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


        // Aplicamos el estilo visual del mapa.
        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json);
        googleMap.setMapStyle(style);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){

            @Override
            public void onMapClick(LatLng latLng) {
                if(MarkerPoints.size() == 0){
                    MarkerPoints.add(positionUser);
                }

                MarkerPoints.add(latLng);
                wpCategory.add("");
                wpDescription.add("");
                wpName.add("");

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                //smarkerOptions.title("posPuntoInteres");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.addMarker(markerOptions);


                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker m) {
                        int idMarker = -1;

                        LatLng mLatLng = new LatLng(m.getPosition().latitude, m.getPosition().longitude);
                        for(int i = 1; i < MarkerPoints.size();i++){
                            if(MarkerPoints.get(i).equals(mLatLng)){
                                idMarker = i - 1;
                            }
                        }

                        showPopup(idMarker);
                        return false;
                    }
                });



                //Toast.makeText(MapsActivity.this, "onMapClick:\n" + latLng.latitude + " : " + latLng.longitude, Toast.LENGTH_LONG).show();


                if (MarkerPoints.size() >= 2) {
                    // Establecemos un punto de origen y otro de destino.
                    LatLng origin = MarkerPoints.get(MarkerPoints.size()-2);
                    LatLng dest = MarkerPoints.get(MarkerPoints.size()-1);

                    // Necesitaremos una URL para utilizar la API de Google Directions.
                    String url = getUrl(origin, dest);
                    RecoverUrl RecoverUrl = new RecoverUrl();

                    // Descargamos los datos de la API de Google Directions.
                    RecoverUrl.execute(url);
                }

            }
        });

    }


    public void showPopup(final int id){
        dialog.setContentView(R.layout.popup);
        closePopup = dialog.findViewById(R.id.closePopup);
        spCategory = dialog.findViewById(R.id.spCategory);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,
                android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.category)){
            public View getView(int position, View convertView, ViewGroup parent) {
                // Para que el elemento seleccionado tenga un color específico...
                TextView category = (TextView) super.getView(position, convertView, parent);
                category.setTextColor(Color.WHITE);
                return category;
            }

        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);


        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        Button saveWP = dialog.findViewById(R.id.btn_saveWP);
        final TextView[] descriptionWP = {dialog.findViewById(R.id.txt_Descripcion)};
        final TextView[] nameWP = {dialog.findViewById(R.id.txt_nameWP)};


        if (!wpName.get(id).equals("")){
            nameWP[0].setText(wpName.get(id));
        }

        if (!wpDescription.get(id).equals("")){
            descriptionWP[0].setText(wpDescription.get(id));
        }

        if (!wpCategory.get(id).equals("")){
            for (int i=0;i<spCategory.getCount();i++){
                if (spCategory.getItemAtPosition(i).equals(wpCategory.get(id))){
                    spCategory.setSelection(i);
                }
            }
        }

        saveWP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameWP[0] = dialog.findViewById(R.id.txt_nameWP);
                String nameText = nameWP[0].getText().toString();
                wpName.set(id, nameText);

                descriptionWP[0] = dialog.findViewById(R.id.txt_Descripcion);
                String descriptionText = descriptionWP[0].getText().toString();
                wpDescription.set(id, descriptionText);

                String categoryText = spCategory.getSelectedItem().toString();
                wpCategory.set(id, categoryText);

                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void showNonEditablePopup(final int id){
        dialog.setContentView(R.layout.non_editable_popup);
        closePopup = dialog.findViewById(R.id.closePopup);
        spCategory = dialog.findViewById(R.id.spCategory);

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        TextView nameWP = dialog.findViewById(R.id.txt_nonEditableNameWp);
        TextView descriptionWP = dialog.findViewById(R.id.txt_nonEditableDescripcion);
        TextView categoryWP = dialog.findViewById(R.id.txt_nonEditableCategory);


        if (!wpName.get(id).equals("")){
            nameWP.setText(wpName.get(id));
        } else {
            nameWP.setText("Sin Nombre");
        }

        if (!wpDescription.get(id).equals("")){
            descriptionWP.setText(wpDescription.get(id));
        } else {
            descriptionWP.setText("Sin Descripción.");
        }

        if (!wpCategory.get(id).equals("")){
            categoryWP.setText(wpCategory.get(id));
        } else {
            categoryWP.setText("Sin categoría");
        }

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void setCamera(View v){
        centerCamera = !centerCamera;

        ImageButton btn = findViewById(R.id.btn_setCamera);

        if(centerCamera){
            Drawable background = getResources().getDrawable(R.drawable.darkblue_button2);
            btn.setBackground(background);
        } else {
            Drawable background = getResources().getDrawable(R.drawable.blue_button);
            btn.setBackground(background);
        }


    }

    public void cleanRoute(View v){
        MarkerPoints.clear();
        wpCategory.clear();
        wpDescription.clear();
        wpName.clear();
        mMap.clear();


        positionUser = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
    }

    public void saveRoute(View v){

        // Si la ruta no está vacía (por defecto tiene nuestra posición inicial, así que tiene que ser > 1)..
        if (MarkerPoints.size() > 1){

            dialog.setContentView(R.layout.save_name_popup);
            final TextView nameRoute = dialog.findViewById(R.id.txt_nameRoute);
            Button saveRoute = dialog.findViewById(R.id.btn_saveRouteName);

            saveRoute.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveNamedRoute(nameRoute.getText().toString());
                    dialog.dismiss();
                }
            });

            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();

        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setMessage("No tienes ruta que guardar. ¡Asegurate de haber creado algún punto antes de guardar!");
            alertDialog.show();
            //Toast.makeText(MapsActivity.this, "RouteInfo NO guardada.", Toast.LENGTH_LONG).show();
        }

    }

    public void saveNamedRoute(String name){
        db = new DdbbConnection( this);
        db.execute("insert", "INSERT INTO `Route` (`Name`, `ID_User`) VALUES ('" + name + "', '" + UserID + "')");

        db = new DdbbConnection(new AsyncResponse() {
            @Override
            public void processFinish(JSONArray output) throws JSONException {
                JSONArray resultadoJSON = output;

                if(resultadoJSON.length() > 0){
                    JSONObject personaActual = resultadoJSON.getJSONObject(0);

                    int idRoute = personaActual.getInt("ID");


                    for(int i = 1; i < MarkerPoints.size(); i++){
                        db = new DdbbConnection( this);
                        //INSERT INTO `Waypoint` (`Name`, `Type`, `Description`, `Lat`, `Long`, `OrderWP`, `ID_Route`) VALUES ('PruebaWP', 'CatPrueba', 'Esto es una prueba', '40.44922523281772', '-3.6073223501443863', '1', '11')
                        db.execute("insert", "INSERT INTO `Waypoint` (`Name`, `Type`, `Description`, `Lat`, `Long`, `OrderWP`, `ID_Route`) " +
                                "VALUES ('" + wpName.get(i-1) + "', '" + wpCategory.get(i-1) + "', '" + wpDescription.get(i-1) + "', '" + MarkerPoints.get(i).latitude + "','" + MarkerPoints.get(i).longitude + "', '" + i + "', '" + idRoute + "')");

                    }
                }
            }

        });
        db.execute("select", "SELECT ID FROM Route ORDER BY ID DESC LIMIT 1");
    }

    public void loadRoute(View v){
        MarkerPoints.clear();
        wpCategory.clear();
        wpDescription.clear();
        wpName.clear();
        mMap.clear();

        MarkerPoints.add(positionUser);

        final String[] queryToSend = {"SELECT r.ID, r.Name, u.Username FROM Route As r INNER JOIN User As u ON r.ID_User = u.ID WHERE 1 = 1 "};

        dialog.setContentView(R.layout.loadroutes_ly);

        closePopup = dialog.findViewById(R.id.closePopup);
        listView = dialog.findViewById(R.id.routesList);

        ImageView searchAgain = dialog.findViewById(R.id.searchRoutes);
        final TextView nameFilter = dialog.findViewById(R.id.txt_nameFilter);
        final RadioButton justMine = dialog.findViewById(R.id.rb_justMineSelected);

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        searchAgain.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                queryToSend[0] = "SELECT r.ID, r.Name, u.Username FROM Route As r INNER JOIN User As u ON r.ID_User = u.ID WHERE 1 = 1 ";
                queryToSend[0] += "and r.Name LIKE '%" + nameFilter.getText() + "%' ";
                if(justMine.isChecked()){
                    queryToSend[0] += "and r.ID_user = " + UserID;
                }

                loadListOfRoutes(queryToSend[0]);
            }
        });


        dialog.show();


        loadListOfRoutes(queryToSend[0]);
    }

    public void loadListOfRoutes(String query){
        db = new DdbbConnection(new AsyncResponse() {
            @Override
            public void processFinish(JSONArray output) throws JSONException {
                JSONArray resultadoJSON = output;

                final ArrayList<RouteInfo> routeInfo = new ArrayList<>();
                final RouteAdapter routeAdapter;

                for(int i=0;i<resultadoJSON.length();i++)
                {
                    JSONObject object= resultadoJSON.getJSONObject(i);
                    routeInfo.add(new RouteInfo(object.getString("Name"), object.getString("Username"), object.getString("ID")));
                }

                routeAdapter = new RouteAdapter(MapsActivity.this, routeInfo);

                if (listView != null) {
                    listView.setAdapter(routeAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            //String tag = (String) view.getTag();
                            db = new DdbbConnection(new AsyncResponse() {
                                @Override
                                public void processFinish(JSONArray output) throws JSONException {
                                    JSONArray resultadoJSON = output;

                                    if(resultadoJSON.length() > 0){
                                        for(int i=0;i<resultadoJSON.length();i++) {
                                            JSONObject currentWP = resultadoJSON.getJSONObject(i);

                                            double Lat = currentWP.getDouble("Lat");
                                            double Long = currentWP.getDouble("Long");

                                            LatLng coordinates = new LatLng(Lat, Long);

                                            MarkerPoints.add(coordinates);
                                            wpCategory.add(currentWP.getString("Type"));
                                            wpDescription.add(currentWP.getString("Description"));
                                            wpName.add(currentWP.getString("Name"));

                                            MarkerOptions markerOptions = new MarkerOptions();
                                            markerOptions.position(coordinates);
                                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                                            mMap.addMarker(markerOptions);

                                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                @Override
                                                public boolean onMarkerClick(Marker m) {
                                                    int idMarker = -1;

                                                    LatLng mLatLng = new LatLng(m.getPosition().latitude, m.getPosition().longitude);
                                                    for(int i = 1; i < MarkerPoints.size();i++){
                                                        if(MarkerPoints.get(i).equals(mLatLng)){
                                                            idMarker = i - 1;
                                                        }
                                                    }

                                                    showNonEditablePopup(idMarker);
                                                    return false;
                                                }
                                            });

                                            if (MarkerPoints.size() >= 2) {
                                                // Establecemos un punto de origen y otro de destino.
                                                LatLng origin = MarkerPoints.get(MarkerPoints.size()-2);
                                                LatLng dest = MarkerPoints.get(MarkerPoints.size()-1);

                                                // Necesitaremos una URL para utilizar la API de Google Directions.
                                                String url = getUrl(origin, dest);
                                                RecoverUrl RecoverUrl = new RecoverUrl();

                                                // Descargamos los datos de la API de Google Directions.
                                                RecoverUrl.execute(url);
                                            }

                                        }



                                        dialog.dismiss();
                                    } else {
                                        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
                                        alertDialog.setMessage("Esto es... inesperado. Resulta que la ruta está vacía. Pruebe con otra.");
                                        alertDialog.show();
                                        //Toast.makeText(MapsActivity.this, "ERROR:RouteInfo vacía.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            db.execute("select", "SELECT * FROM `Waypoint` WHERE ID_Route =" + routeInfo.get(i).getUserID());
                        }
                    });
                }
            }

        });
        db.execute("select", query);
    }


    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        if(lastLocation == null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

        lastLocation = location;

        // Necesitaremos la latitud y longitud. (1º siempre va la latitud, 2º la longitud)
        positionUser = new LatLng(location.getLatitude(), location.getLongitude());


        if(centerCamera) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(positionUser));
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();

        locationRequest.setInterval(1000);          //La cantidad introducida es en milisegundos.
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));

    }



    public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origen de la ruta.
        String txt_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destino de la ruta.
        String txt_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Construimos los parámetros para el web service.
        String parameters = txt_origin + "&" + txt_dest + "&sensor=false";
        // Añadimos el modo de ruta (calculada como si fuese a pie, no en coche que es la que se asigna por defecto)
        parameters += "&mode=walking";
        // Construimos la url del web service
        String url = "https://maps.googleapis.com/maps/api/directions/json?" + parameters;

        return url;
    }

    @Override
    public void processFinish(JSONArray output) throws JSONException {

    }


    private class RecoverUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // Almacenaremos los datos del web service en el string data.
            String data = "";
            try {
                data = createUrlConection(url[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask pt = new ParserTask();
            // Analizamos los datos.
            pt.execute(result);

        }
    }

    private String createUrlConection(String strUrl) throws IOException {
        String data = "";
        InputStream is = null;
        HttpURLConnection hurlc = null;
        try {
            URL url = new URL(strUrl);

            //Creamos una conexión http para comunicarnos con la url
            hurlc = (HttpURLConnection) url.openConnection();

            // Conectamos con la url
            hurlc.connect();

            // Preparamos para la lectura usando Buffers.
            is = hurlc.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();

            // Leeremos cada linea y la almacenaremos siempre que obtengamos contenido al leer.
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            // Cerramos el Buffer.
            br.close();

        } catch (Exception e) {
            // ERROR
        } finally {
            // Nos aseguramos de cerrar la conexión del inputStram y de desconectarnos con la url.
            is.close();
            hurlc.disconnect();
        }
        return data;
    }


    // IMPORTANTE: Tan la siguiente clase, como DataParser no han sido elaboradas por mí. Para más información, en la documentación.

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.rgb(62,124,163));

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            if(lineOptions != null) {
                mMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
    }
}
