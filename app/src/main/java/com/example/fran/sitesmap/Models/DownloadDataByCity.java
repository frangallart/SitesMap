package com.example.fran.sitesmap.Models;

/**
 * Created by Fran on 12/03/2015.
 */

import android.os.AsyncTask;
import android.view.View;

import com.example.fran.sitesmap.Controllers.MapsActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe DownloadDataByCity
 * <p/>
 * Classe que descarrega les dades mitjançant un tasca asíncrona
 */
public class DownloadDataByCity extends AsyncTask<String, Void, ArrayList<MapPoint>> {

    private static final String URL_DATA = "http://www.infobosccoma.net/pmdm/pois.php";
    private MapsActivity mapsActivity;

    /**
     * Constructor amb paràmetres
     *
     * @param mapsActivity activitat on es mostra el mapa
     */
    public DownloadDataByCity(MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    /**
     * Mètode que s'executa abans de començar amb la tasca
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mapsActivity.getProgressBar().setVisibility(View.VISIBLE);
    }

    /**
     * Mètode principal de la tasca
     *
     * @param params paràmetres de la tasca
     * @return llista de punts a parti d'un ArrayList<MapPoint>
     */
    @Override
    protected ArrayList<MapPoint> doInBackground(String... params) {
        ArrayList<MapPoint> llistaPunts = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPostReq = new HttpPost(URL_DATA);
        HttpResponse httpResponse = null;

        try {
            // Afegir el paràmetre ciutat
            List<NameValuePair> parametres = new ArrayList<NameValuePair>(1);
            parametres.add(new BasicNameValuePair("city", params[0]));
            httpPostReq.setEntity(new UrlEncodedFormEntity(parametres));
            // Executar la petició http
            httpResponse = httpClient.execute(httpPostReq);
            // Rebre la resposta de la petició
            String responseText = EntityUtils.toString(httpResponse.getEntity());
            llistaPunts = tractarPuntsJSON(responseText);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return llistaPunts;
    }

    /**
     * Mètode que s'executa desprès d'executar la tasca i desa el resultat
     *
     * @param list llista de MapPoint
     */
    @Override
    protected void onPostExecute(ArrayList<MapPoint> list) {
        // Netejem la llista
        mapsActivity.getmMap().clear();
        // Tornem a carregar les dades a la llista
        mapsActivity.setLlistaPosicions(list);
        mapsActivity.refreshData();
        mapsActivity.getProgressBar().setVisibility(View.GONE);
    }

    /**
     * Mètode que ens tracta el fitxer json que conté les dades
     *
     * @param json resposta obtinguda
     * @return llista de punts per inserir al mapa
     */
    private ArrayList<MapPoint> tractarPuntsJSON(String json) {
        Gson converter = new Gson();
        return converter.fromJson(json, new TypeToken<ArrayList<MapPoint>>() {
        }.getType());
    }
}
