package com.example.olx;

import android.widget.Filter;


import com.example.olx.adapter.AdapterChats;
import com.example.olx.model.ModelChats;

import java.util.ArrayList;

public class FilterChats extends Filter {

    private AdapterChats adapterChats;

    private ArrayList<ModelChats> filterList;

    public FilterChats(AdapterChats adapterChats, ArrayList<ModelChats> filterList) {
        this.adapterChats = adapterChats;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (constraint != null && constraint.length() > 0){
            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelChats> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getName().toUpperCase().contains(constraint) ||
                        filterList.get(i).getMessage().toUpperCase().contains(constraint)){
                    //add to filtered list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;
        }
        else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results; //don't miss it
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        //apply filter changes
        adapterChats.chatsArrayList = (ArrayList<ModelChats>) results.values;
        //notify change
        adapterChats.notifyDataSetChanged();
    }
}
