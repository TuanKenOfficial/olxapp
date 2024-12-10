package com.example.olx;

import android.annotation.SuppressLint;
import android.widget.Filter;


import com.example.olx.adapter.AdapterOrderUser;
import com.example.olx.model.ModelOrderUser;

import java.util.ArrayList;

public class FilterOrderUser extends Filter {

    private final AdapterOrderUser adapter;
    private final ArrayList<ModelOrderUser> filterList;

    public FilterOrderUser(AdapterOrderUser adapter, ArrayList<ModelOrderUser> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length() > 0){
            //search filed not empty, searching something, perform search

            //change to upper case, to make case insensitive
            constraint = constraint.toString().toUpperCase();
            //store our filtered list
            ArrayList<ModelOrderUser> filteredModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){
                //check, search by trạng thái hoá đơn
                if (filterList.get(i).getOrderStatus().contains(constraint)){
                    //add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }

            results.count = filteredModels.size();
            results.values = filteredModels;

        }
        else {
            //search filed empty, not searching, return original/all/complete list

            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.orderUserArrayList = (ArrayList<ModelOrderUser>) results.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
