package com.example.android.fragmentpractise;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.fragmentpractise.Modules.PlacesAutoCompleteAdapter;
import com.example.android.fragmentpractise.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.fragmentpractise.R.id.dateButton;

public class ReportFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private View view;
    private  Button reportButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view= inflater.inflate(R.layout.fragment_report, container, false);

        AutoCompleteTextView autocompleteViewStart = (AutoCompleteTextView) view.findViewById(R.id.incidentPlace);
        autocompleteViewStart.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));

        autocompleteViewStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String description = (String) parent.getItemAtPosition(position);
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Accident");
        categories.add("Road blocked");
        categories.add("Damage Road");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource( R.layout.autocomplete_list_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        reportButton=(Button) view.findViewById(R.id.reportButton);
        reportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

}
