package com.example.android_carros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.android_carros.R;
import com.example.android_carros.models.Anuncio;


import java.util.List;

public class AnuncioAdapter extends ArrayAdapter<Anuncio> {

    public AnuncioAdapter(Context context, List<Anuncio> anuncios) {
        super(context, 0, anuncios);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_anuncio, parent, false);
        }

        Anuncio anuncio = getItem(position);

        TextView nomeAnuncio = convertView.findViewById(R.id.nomeAnuncio);

        nomeAnuncio.setText(anuncio.getDescricao());

        return convertView;
    }
}
