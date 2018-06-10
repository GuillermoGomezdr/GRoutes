package org.example.willy.GRoutes;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class RouteAdapter extends ArrayAdapter<RouteInfo> {

    public RouteAdapter(Context context, ArrayList<RouteInfo> routeInfos) {
        super(context, 0, routeInfos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        RouteInfo routeInfoActual = getItem(position);

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View nuevaVista = inflater.inflate(R.layout.ruta_item, parent,false);
            convertView = nuevaVista;
        }

        TextView nombreRuta = convertView.findViewById(R.id.nombreRutaTv);
        TextView nombrePersona = convertView.findViewById(R.id.nombrePersonaTv);

        nombreRuta.setText(routeInfoActual.getRouteName());
        nombrePersona.setText(routeInfoActual.getUserName());

        //convertView.setTag(routeInfoActual.getUserID());

        return convertView;
    }
}
