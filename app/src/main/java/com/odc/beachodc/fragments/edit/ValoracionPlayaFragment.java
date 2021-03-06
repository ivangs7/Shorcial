package com.odc.beachodc.fragments.edit;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.odc.beachodc.R;
import com.odc.beachodc.activities.EdicionPlaya;
import com.odc.beachodc.db.models.Comentario;
import com.odc.beachodc.utilities.AnimateFirstDisplayListener;
import com.odc.beachodc.utilities.Utilities;
import com.odc.beachodc.webservices.Request;


import java.util.Date;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


/**
 * Created by Paco on 7/01/14.
 * Fragment que se encarga de Loguear al usuario, es el splash screen inicial de login
 */
public class ValoracionPlayaFragment extends Fragment {

        View rootView;
        ImageView v1, v2, v3, v4, v5;
        public EditText comentario;
        Button enviar;
        public int valoracion;
        public String idplaya;
        Activity activity;
        private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
        ProgressDialog pd;

        public ValoracionPlayaFragment() {
            // Se ejecuta antes que el onCreateView
        }

        public void setParams (Activity activity, String idplaya) {
            // Se ejecuta antes que el onCreateView
            this.idplaya = idplaya;
            this.activity = activity;
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
                rootView = inflater.inflate(R.layout.fragment_edit_valoracion, container, false);
            } catch (InflateException e) {}

            // Empezar aqui a trabajar con la UI

            v1 = (ImageView) rootView.findViewById(R.id.v1);
            v2 = (ImageView) rootView.findViewById(R.id.v2);
            v3 = (ImageView) rootView.findViewById(R.id.v3);
            v4 = (ImageView) rootView.findViewById(R.id.v4);
            v5 = (ImageView) rootView.findViewById(R.id.v5);

            initValoracion();

            comentario = (EditText) rootView.findViewById(R.id.comentarioET);

            enviar = (Button) rootView.findViewById(R.id.nuevoComentarioBTN);

            enviar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pd = ProgressDialog.show(getActivity(), getResources().getText(R.string.esperar), getResources().getText(R.string.esperar));
                    pd.setIndeterminate(false);
                    pd.setCancelable(false);
                    enviar.setClickable(false);
                    if (Utilities.haveInternet(getActivity())) {
                        if (validacionValoracion()){
                            Comentario comment = new Comentario(Utilities.getUserIdFacebook(getActivity()), idplaya, comentario.getText().toString(), Utilities.getUserNameFacebook(getActivity()), new Date(), valoracion);
                            Request.valorarPlaya(activity, comment, pd);
                            enviar.setClickable(true);
                        } else {
                            pd.dismiss();
                            enviar.setClickable(true);
                        }
                    } else {
                        pd.dismiss();
                        Crouton.makeText(getActivity(), getString(R.string.no_internet), Style.ALERT).show();
                        enviar.setClickable(true);
                    }
                }
            });

            return rootView;
        }

    public void initValoracion(){

        Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v1, animateFirstListener);
        Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v2, animateFirstListener);
        Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v3, animateFirstListener);
        Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v4, animateFirstListener);
        Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v5, animateFirstListener);

        v1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                valoracion=1;
                setValoracion(true, false, false, false, false);
            }
        });

        v2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                valoracion=2;
                setValoracion(false, true, false, false, false);
            }
        });

        v3.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                valoracion=3;
                setValoracion(false, false, true, false, false);
            }
        });

        v4.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                valoracion=4;
                setValoracion(false, false, false, true, false);
            }
        });

        v5.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                valoracion=5;
                setValoracion(false, false, false, false, true);
            }
        });

    }

    public void setValoracion(boolean uno, boolean dos, boolean tres, boolean cuatro, boolean cinco){
        if (uno){
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v1, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v2, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v3, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v4, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v5, animateFirstListener);
        } else if (dos){
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v1, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v2, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v3, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v4, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v5, animateFirstListener);
        } else if (tres){
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v1, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v2, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v3, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v4, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v5, animateFirstListener);
        } else if (cuatro){
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v1, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v2, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v3, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v4, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_off), v5, animateFirstListener);
        } else if (cinco){
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v1, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v2, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v3, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v4, animateFirstListener);
            Utilities.imageLoader.displayImage(Utilities.getURIDrawable(R.drawable.star_on), v5, animateFirstListener);
        }
    }

    public boolean validacionValoracion(){
        if (valoracion == 0){
            Crouton.makeText(getActivity(), R.string.error_valoracion, Style.ALERT).show();
            return false;
        }
        return true;
    }


}
