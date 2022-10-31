package com.example.helpinghands;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfileListAdapter extends ArrayAdapter {

    private final Activity context;
    private final Integer[] imageIDarray;
    private final String[] nameArray;
    private final String[] infoArray;

    public ProfileListAdapter(
            Activity context, String[] nameArrayParam, String[] infoArrayParam,
            Integer[] imageIDArrayParam){

        super(context,R.layout.profile_list_row , nameArrayParam);
        this.context=context;
        this.imageIDarray = imageIDArrayParam;
        this.nameArray = nameArrayParam;
        this.infoArray = infoArrayParam;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.profile_list_row, null,true);

        ((TextView) rowView.findViewById(R.id.nameTextViewId)).setText(nameArray[position]);
        ((TextView) rowView.findViewById(R.id.InfoTextViewid)).setText(infoArray[position]);
        ((ImageView) rowView.findViewById(R.id.imageView1ID)).setImageResource(
                imageIDarray[position]);

        return rowView;

    }

}
