package com.example.olx;

import android.widget.Filter;

import com.example.olx.adapter.AdapterAddProduct;
import com.example.olx.model.ModelAddProduct;

import java.util.ArrayList;

public class FilterAddProducts extends Filter {

    //arraylist in which we want to search
    AdapterAddProduct adapterAd;
    ArrayList<ModelAddProduct> filterList;
    //adapter


    public FilterAddProducts(AdapterAddProduct adapterAd, ArrayList<ModelAddProduct> filterList) {
        this.adapterAd = adapterAd;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        //value should not be null and empty
        if (constraint != null && constraint.length() > 0){
            //change to upper case, or lower case to avoid case sensitivity
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelAddProduct> filteredModels = new ArrayList<>();

            for (int i=0; i<filterList.size(); i++){
                //validate
                if (filterList.get(i).getCategory().toUpperCase().contains(constraint) ||
                        filterList.get(i).getBrand().toUpperCase().contains(constraint) ||
                        filterList.get(i).getAddress().toUpperCase().contains(constraint)||
                        filterList.get(i).getTitle().toUpperCase().contains(constraint)){
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
        adapterAd.adArrayList = (ArrayList<ModelAddProduct>) results.values;
        //notify change
        adapterAd.notifyDataSetChanged();

    }
}
