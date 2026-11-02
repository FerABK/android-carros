package com.example.android_carros.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android_carros.R;
import com.example.android_carros.models.Cidade;

import java.util.List;

public class CidadeAdapter extends ArrayAdapter<Cidade> {

    public CidadeAdapter(Context context, List<Cidade> cidades) {
        super(context, 0, cidades);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_cidade, parent, false);
        }

        Cidade cidade = getItem(position);

        TextView nomeCidade = convertView.findViewById(R.id.nomeCidade);

        nomeCidade.setText(cidade.getNome());

        return convertView;
    }
}
