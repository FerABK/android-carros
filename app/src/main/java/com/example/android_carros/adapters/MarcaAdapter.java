package com.example.android_carros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android_carros.R;
import com.example.android_carros.models.Marca;

import java.util.List;

public class MarcaAdapter extends ArrayAdapter<Marca> {

    public MarcaAdapter(Context context, List<Marca> marcas) {
        super(context, 0, marcas);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_marca, parent, false);
        }

        Marca marca = getItem(position);

        TextView nomeMarca = convertView.findViewById(R.id.nomeMarca);

        nomeMarca.setText(marca.getNome());

        return convertView;
    }
}
