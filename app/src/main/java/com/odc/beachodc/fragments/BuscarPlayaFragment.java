package com.odc.beachodc.fragments;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.odc.beachodc.R;
import com.odc.beachodc.activities.BuscarPlaya;
import com.odc.beachodc.activities.ResultadoBusquedaPlaya;
import com.odc.beachodc.interfaces.IStandardTaskListener;
import com.odc.beachodc.utilities.placeAutocomplete.DetailsPlaceOne;
import com.odc.beachodc.utilities.placeAutocomplete.FillPlace;

import java.util.List;
import java.util.Locale;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


/**
 * Created by Paco on 7/01/14.
 * Fragment que se encarga de Loguear al usuario, es el splash screen inicial de login
 */
public class BuscarPlayaFragment extends Fragment {

        View rootView;
        RadioGroup busqueda;
        EditText nombrePlaya;
        AutoCompleteTextView direccion;
        private DetailsPlaceOne geoLugar;
        private FillPlace buscarLugar;
        private Thread thread;
        private ProgressDialog pd;
        GoogleMap mapa;
        Location myLocation;
        LatLng porCercania;
        Button buscarBTN;
        RelativeLayout groupAddress;

        public BuscarPlayaFragment() {
            // Se ejecuta antes que el onCreateView

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            // Para evitar problemas al inflar Layout con Map Fragments... y reutilizar mejor los fragments
            if (rootView != null) {
                ViewGroup parent = (ViewGroup) rootView.getParent();
                if (parent != null)
                    parent.removeView(rootView);
            }
            try {
                rootView = inflater.inflate(R.layout.fragment_buscar_playas, container, false);
            } catch (InflateException e) {}

            // Empezar aqui a trabajar con la UI

            busqueda = (RadioGroup) rootView.findViewById(R.id.searchRG);
            nombrePlaya = (EditText) rootView.findViewById(R.id.nombreET);

            groupAddress = (RelativeLayout) rootView.findViewById(R.id.groupSearchByAddress);

            try {
                MapsInitializer.initialize(getActivity());
                initMapa();
            } catch (GooglePlayServicesNotAvailableException e) {}

            direccion = (AutoCompleteTextView) rootView.findViewById(R.id.addressET);

            porCercania = null;

            initAutocompletadoDireccion();

            buscarBTN = (Button) rootView.findViewById(R.id.buscarBTN);

            buscarBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (validarBusqueda()){
                        // TODO: Enviar datos al servidor y esperar respuesta, para mostrarlos en una lista
                        // TODO: Enviar nombrePlaya o porCercania + direccion.getText (esto ultimo para mostrarlo en la nueva activity)
                        if (busqueda.getCheckedRadioButtonId() == R.id.searchByName){
                            // TODO: Buscar por nombrePlaya
                            Intent intentS = new Intent(getActivity(), ResultadoBusquedaPlaya.class);
                            intentS.putExtra("search", nombrePlaya.getText().toString());
                            startActivity(intentS);
                        } else if (busqueda.getCheckedRadioButtonId() == R.id.searchByAddress){
                            // TODO: Buscar porCercania
                            Intent intentS = new Intent(getActivity(), ResultadoBusquedaPlaya.class);
                            intentS.putExtra("search", direccion.getText().toString());
                            intentS.putExtra("isSearch", true);
                            intentS.putExtra("latitud", porCercania.latitude);
                            intentS.putExtra("longitud", porCercania.longitude);
                            startActivity(intentS);
                        } else {
                            Crouton.makeText(getActivity(), R.string.error_unknown, Style.ALERT).show();
                        }
                    }
                }
            });

            busqueda.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.searchByName) {
                        ocultarBusquedaNombre(false);
                    } else if (checkedId == R.id.searchByAddress){
                        ocultarBusquedaNombre(true);
                    } else {
                        ocultarBusquedaNombre(false);
                    }
                }
            });

            return rootView;

            }

    private void initMapa(){

        mapa = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mapa==null)
            return;
        mapa.setMyLocationEnabled(true);
        mapa.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                myLocation=arg0;
            }
        });

        mapa.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (myLocation != null){
                    try {
                        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        try {
                            imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                        } catch (Exception e){}
                        Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                        Double lat = myLocation.getLatitude();
                        Double lng = myLocation.getLongitude();
                        List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);
                        String add = "";
                        if (addresses.size() > 0){
                            for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
                                add += addresses.get(0).getAddressLine(i) + " ";
                        }

                        mapa.clear();

                        direccion.setText(add);

                        porCercania = new LatLng(lat, lng);

                        mapa.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title(add)
                                .draggable(true)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    } catch (Exception e) {}
                }
                return false;
            }
        });

        mapa.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            public void onMapLongClick(LatLng point) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e){}
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    List<Address> addresses = geoCoder.getFromLocation(point.latitude, point.longitude, 1);
                    String add = "";
                    if (addresses.size() > 0){
                        for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
                            add += addresses.get(0).getAddressLine(i) + " ";
                    }
                    direccion.setText(add);

                    porCercania = new LatLng(point.latitude, point.longitude);

                    mapa.clear();

                    // Insertamos el Marcador
                    mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(point.latitude, point.longitude))
                            .title(add)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                } catch (Exception e) {
                }

            }
        });

        mapa.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
                marker.setTitle("");
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPosition = marker.getPosition();
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    List<Address> addresses = geoCoder.getFromLocation(newPosition.latitude, newPosition.longitude, 1);
                    String add = "";
                    if (addresses.size() > 0){
                        for (int i=0; i<addresses.get(0).getMaxAddressLineIndex();i++)
                            add += addresses.get(0).getAddressLine(i) + " ";
                    }

                    mapa.clear();

                    mapa.addMarker(new MarkerOptions()
                            .position(new LatLng(newPosition.latitude, newPosition.longitude))
                            .title(add)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                    direccion.setText(add);

                    porCercania = new LatLng(newPosition.latitude, newPosition.longitude);

                } catch (Exception e) {
                }
            }

            @Override
            public void onMarkerDrag(Marker marker) {}
        });
    }

    public void initAutocompletadoDireccion(){
        final ArrayAdapter<String> adapterFrom = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        adapterFrom.setNotifyOnChange(true);
        direccion.setAdapter(adapterFrom);

        direccion.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                geoLugar = new DetailsPlaceOne();
                geoLugar.setContext(arg1.getContext());
                thread=  new Thread(){
                    @Override
                    public void run(){
                        try {
                            synchronized(this){
                                wait(700);
                            }
                        }
                        catch(InterruptedException ex){
                        }
                        pd.dismiss();

                        if ((buscarLugar.referencesPlace.isEmpty()) || (buscarLugar.referencesPlace.get(direccion.getText().toString()) == null)){
                            showToastError();
                        } else {
                            geoLugar.setListener(new PlaceToPointMap_TaskListener(direccion.getText().toString()));
                            geoLugar.execute(buscarLugar.referencesPlace.get(direccion.getText().toString()));
                        }
                    }
                };
                pd = ProgressDialog.show(getActivity(), getResources().getText(R.string.procesando), getResources().getText(R.string.esperar));
                pd.setIndeterminate(false);
                pd.setCancelable(true);
                thread.start();
            }
        });

        // Monitorizamos el evento de cambio en el campo a autocompletar para buscar las propuestas de autocompletado
        direccion.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Calculamos el autocompletado
                if (count%3 == 1){
                    adapterFrom.clear();
                    // Ejecutamos en segundo plano la busqueda de propuestas de autocompletado
                    buscarLugar = new FillPlace(adapterFrom, direccion, getActivity());
                    buscarLugar.execute(direccion.getText().toString());
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void afterTextChanged(Editable s) {}
        });
    }

    private class PlaceToPointMap_TaskListener implements IStandardTaskListener {

        private String markerStr;

        public PlaceToPointMap_TaskListener(String markerStr) {
            this.markerStr =  markerStr;
        }

        @Override
        public void taskComplete(Object result) {
            if ((Boolean)result){
                Double lat, lng;
                // Esconder teclado para que se vea la animación del mapa
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                try {
                    imm.hideSoftInputFromWindow(getActivity().getWindow().getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e){}
                // Recogemos la posicion que hemos pedido al Web Service de Google Place
                lat = geoLugar.coordinates.get("lat");
                lng = geoLugar.coordinates.get("lng");
                if ((lat == null) || (lng == null))
                    return;
                else{
                    porCercania = new LatLng(lat, lng);
                }
                LatLng go = new LatLng(lat, lng);

                direccion.setText(markerStr);

                // Creamos la animación de movimiento hacia el lugar que hemos introducido
                CameraPosition camPos = new CameraPosition.Builder()
                        .target(go)
                        .zoom(18)         //Establecemos el zoom en 17
                        .tilt(45)         //Bajamos el punto de vista de la cámara 70 grados
                        .build();

                // Lanzamos el movimiento
                CameraUpdate camUpd = CameraUpdateFactory.newCameraPosition(camPos);
                mapa.animateCamera(camUpd);

                mapa.clear();

                mapa.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lng))
                        .title(markerStr)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        }
    }

    public void showToastError(){
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), R.string.error_problemautocompletado, Toast.LENGTH_LONG).show();
            }
        });
    }

    public boolean validarBusqueda(){
        if (busqueda.getCheckedRadioButtonId() == R.id.searchByName){
            if (!nombrePlaya.getText().toString().equals("")) {
                return true;
            } else {
                Crouton.makeText(getActivity(), R.string.error_search_name, Style.ALERT).show();
            }
        } else if (busqueda.getCheckedRadioButtonId() == R.id.searchByAddress){
            if (porCercania != null){
                return true;
            } else {
                Crouton.makeText(getActivity(), R.string.error_search_address, Style.ALERT).show();
            }
        }
        return false;
    }

    public void ocultarBusquedaNombre (boolean ocultar){
        try {
            if (ocultar) {
                nombrePlaya.setVisibility(View.GONE);
                groupAddress.setVisibility(View.VISIBLE);
            } else {
                groupAddress.setVisibility(View.GONE);
                nombrePlaya.setVisibility(View.VISIBLE);
            }
        } catch (Exception e){}
    }
}