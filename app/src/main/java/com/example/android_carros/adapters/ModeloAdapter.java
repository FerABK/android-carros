package com.example.android_carros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android_carros.R;
import com.example.android_carros.models.Modelo;

import java.util.List;

public class ModeloAdapter extends ArrayAdapter<Modelo> {

    public ModeloAdapter(Context context, List<Modelo> modelos) {
        super(context, 0, modelos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_modelo, parent, false);
        }

        Modelo modelo = getItem(position);

        TextView nomeModelo = convertView.findViewById(R.id.nomeModelo);

        nomeModelo.setText(modelo.getNome());

        return convertView;
    }
}
