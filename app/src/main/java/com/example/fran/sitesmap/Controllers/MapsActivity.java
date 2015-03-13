package com.example.fran.sitesmap.Controllers;
/**
 * Created by Fran on 12/03/2015.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.fran.sitesmap.Models.DownloadDataByCity;
import com.example.fran.sitesmap.Models.MapPoint;
import com.example.fran.sitesmap.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Classe MapsActivity
 * <p/>
 * Classe principal de la aplicació
 */
public class MapsActivity extends FragmentActivity implements OnClickListener, OnItemClickListener, OnMapLoadedCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LatLngBounds centerBounds;

    private LinearLayout lSuperior;
    private EditText txtCercar;
    private ImageButton btnCercar;
    private ImageButton btnNoCercar;
    private ListView navList;
    private ProgressBar progressBar;

    private ArrayList<MapPoint> llistaPosicions;
    private DownloadDataByCity descarregarDades;
    private boolean cercadorObert;

    /**
     * Mètode que s'executa en crear l'activitat
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpGUI();

        // Si hi ha conexiò a internet
        if (isOnline()) {
            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey("llistaPosicionsMapa")) {
                    llistaPosicions = savedInstanceState.getParcelableArrayList("llistaPosicionsMapa");
                }
            }

            setUpMapIfNeeded();
            refreshData();

        } else {
            showAlertDialogInternet(MapsActivity.this, "Servei de connexió",
                    "El teu dispositiu no té connexió a Internet.", false);
        }
    }

    /**
     * Mètode que mostra un missatge d'alerta si no hi ha connexió a internet
     *
     * @param context
     * @param title
     * @param message
     * @param status
     */
    public void showAlertDialogInternet(Context context, String title, String message, Boolean status) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton(-1, "Obre les opcions d'internet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
            }
        });

        alertDialog.setButton(-2, "Cancel·lar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.closeOptionsMenu();
            }
        });

        alertDialog.show();
    }

    /**
     * Mètode que controla si el dispositiu té connexiò a la xarxa
     *
     * @return true en cas afirmatiu, false en cas contrari
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Mètode que recupera els controls de la GUI
     */
    private void setUpGUI() {
        cercadorObert = false;
        lSuperior = (LinearLayout) findViewById(R.id.linearSuperior);
        navList = (ListView) findViewById(R.id.left_drawer);
        btnNoCercar = (ImageButton) findViewById(R.id.btnNoCercar);
        btnNoCercar.setOnClickListener(this);

        // Carregar un vector amb els noms de les opcions
        final String[] names = getResources().getStringArray(
                R.array.nav_options);

        // Primer assignem l'array amb un adaptador per la llista
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        navList.setAdapter(adapter);
        navList.setOnItemClickListener(this);

        txtCercar = (EditText) findViewById(R.id.txtCercar);
        btnCercar = (ImageButton) findViewById(R.id.btnCercar);
        btnCercar.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    /**
     * Mètode que controla l'event onClick dels elements
     *
     * @param v view clicada
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCercar:
                mostrarCercador(true);

                // Si hi ha conexiò a internet
                if (isOnline()) {
                    // Cercar si la barra cercador està desplegada
                    if (cercadorObert) {
                        try {
                            descarregarDades = new DownloadDataByCity(MapsActivity.this);
                            descarregarDades.execute(txtCercar.getText().toString());
                            //Linies per amagar el teclat virtual(Hide keyboard)
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(txtCercar.getWindowToken(), 0);
                        } catch (IllegalStateException ex) {
                            ex.printStackTrace();
                        }
                    }
                } else {
                    showAlertDialogInternet(MapsActivity.this, "Servei de connexió",
                            "El teu dispositiu no té connexió a Internet.", false);
                }

                cercadorObert = true;
                break;
            case R.id.btnNoCercar:
                mostrarCercador(false);
                cercadorObert = false;
                break;
        }
    }

    /**
     * Mètode que permet mostrar la barra de cerca
     *
     * @param mostrar true per mostrar el cercador, false en cas contrari
     */
    public void mostrarCercador(boolean mostrar) {
        // Mostrar
        if (mostrar) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lSuperior.getLayoutParams();
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;
            lSuperior.setLayoutParams(params);
            lSuperior.setBackgroundColor(Color.WHITE);
            lSuperior.setAlpha(0.8f);
            txtCercar.setVisibility(View.VISIBLE);
            btnNoCercar.setVisibility(View.VISIBLE);
        } else {
            // Amagar
            txtCercar.setVisibility(View.INVISIBLE);
            txtCercar.setText("");
            btnNoCercar.setVisibility(View.INVISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) lSuperior.getLayoutParams();
            params.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            lSuperior.setLayoutParams(params);
            lSuperior.setBackgroundColor(Color.TRANSPARENT);
            lSuperior.setAlpha(1);
        }
    }

    /**
     * Mètode que controla l'event onItemClick dels elements
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.left_drawer:
                switch (position) {
                    case 0:
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1:
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                    case 2:
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case 3:
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                }
                break;
        }
    }

    /**
     * Torna a executar la consulta i a enllaçar les dades obtingudes
     */
    public void refreshData() {
        if (llistaPosicions == null) {
            llistaPosicions = new ArrayList<MapPoint>();
        } else {
            // Si no s'han trobat coincidències
            if (llistaPosicions.isEmpty()) {
                Toast toast = Toast.makeText(getApplicationContext(), "No hi ha coincidències", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0, 155);
                toast.show();
            } else {
                // Agafar els punts per centrar i fer el zoom al mapa
                LatLngBounds.Builder bounds = new LatLngBounds.Builder();

                // Recorrer la llista de punts i afegir-los al mapa
                for (MapPoint p : llistaPosicions) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(p.getLatitude(), p.getLongitude()
                    )).title(p.getName()));
                    bounds.include(new LatLng(p.getLatitude(), p.getLongitude()));
                }
                centerBounds = bounds.build();
                // Un cop carregat el mapa centrem la càmera segons els punts
                mMap.setOnMapLoadedCallback(this);
            }
        }
    }

    /**
     * Mètode que s'executa quan el mapa s'ha carregat del tot
     */
    @Override
    public void onMapLoaded() {
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centerBounds, 200));
    }

    /**
     * Mètode que controla la tornada a l'aplicació
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // Per configurar propietats del mapa
        //mMap.getUiSettings().setScrollGesturesEnabled(true);
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Marca de la localització
        /*mMap.setMyLocationEnabled(true);

        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.addMarker(new MarkerOptions().position(INS_BOSC_DE_LA_COMA) // la posició
                        .title(INS_BOSC_DE_LA_COMA_STR) // el títol
                        .snippet("Estudis: ESO, Batxillerat, Cicles Formatius i CAS") // Petita descripció de la marca
                                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Per donar un color a la marca
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
        );*/

        /*PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(42.1737, 2.46631))
                        //Nord del punt anterior, però a la mateiza longitud
                .add(new LatLng(42.1747, 2.46631))
                        //Mateixa longitud, però amb uns kms a l'oest
                .add(new LatLng(42.1737, 2.47731))
                        //Mateixa longitud, però uns kms al sud
                .add(new LatLng(42.16727, 2.46631))
                        // Tancar el polígon
                .add(new LatLng(42.1737, 2.46631));

        // Assignar un color
        rectOptions.color(Color.RED);
        // Afegir al nou plígon basat en línies
        Polyline polyLine = mMap.addPolyline(rectOptions);*/
    }

    /**
     * Mètode que guarda de les dades
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("llistaPosicionsMapa", llistaPosicions);
        super.onSaveInstanceState(outState);
    }

    /**
     * Obté l'objecte GoogleMap
     *
     * @return l'objecte GoogleMap
     */
    public GoogleMap getmMap() {
        return mMap;
    }

    /**
     * Assigna un objecte GoogleMap
     *
     * @param mMap l'objecte GoogleMap a assignar
     */
    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    /**
     * Obté un ArrayList<> de MapPoint
     *
     * @return la llista de MapPoint
     */
    public ArrayList<MapPoint> getLllistaPosicions() {
        return llistaPosicions;
    }

    /**
     * Assigna un objecte ArrayList<MapPoint>
     *
     * @param llistaPosicions l'objecte ArrayList<MapPoint> a assignar
     */
    public void setLlistaPosicions(ArrayList<MapPoint> llistaPosicions) {
        this.llistaPosicions = llistaPosicions;
    }

    /**
     * Obté un objecte ProgressBar
     *
     * @return l'objecte ProgressBar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Assigna un objecte ProgressBar
     *
     * @param progressBar l'objecte ProgressBar
     */
    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }
}
