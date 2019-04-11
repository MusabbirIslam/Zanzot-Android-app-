package com.example.android.fragmentpractise;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.fragmentpractise.Modules.PlacesAutoCompleteAdapter;

import java.util.Calendar;

public class PredictiomFragment extends Fragment{

    //private int year, month, day;
    private  Button dateButton,timeButton,predictButton;
    private Boolean progressBarFirst;
    private ProgressDialog progressDialog;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view= inflater.inflate(R.layout.fragment_predictiom, container, false);

        AutoCompleteTextView autocompleteViewStart = (AutoCompleteTextView) view.findViewById(R.id.startingPointTextView);
        autocompleteViewStart.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));

        autocompleteViewStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String description = (String) parent.getItemAtPosition(position);
            }
        });

        AutoCompleteTextView autocompleteViewDest = (AutoCompleteTextView) view.findViewById(R.id.destinationPointTextView);
        autocompleteViewDest.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));

        autocompleteViewDest.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String description = (String) parent.getItemAtPosition(position);
            }
        });



        dateButton=(Button) view.findViewById(R.id.dateButton);
        dateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SelectDateFragment.setDateButton(dateButton);
                DialogFragment newFragment = new SelectDateFragment();
                newFragment.show(getFragmentManager(), "Select date");

            }
        });

        timeButton=(Button) view.findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TimePickerFragment.setTimeButton(timeButton);
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "Select Time");

            }
        });

        predictButton=(Button) view.findViewById(R.id.predictButton);
        predictButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                prediction();
            }
        });

        progressBarFirst=true;
        // Inflate the layout for this fragment
        return view;
    }

    public static class SelectDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        static Button b;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            int yy = calendar.get(Calendar.YEAR);
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, yy, mm, dd);
        }

        public static void setDateButton(Button but)
        {
            b=but;
        }
        public void onDateSet(DatePicker view, int yy, int mm, int dd) {
            populateSetDate(yy, mm+1, dd);
        }
        public void populateSetDate(int year, int month, int day) {
           b.setText(month+"/"+day+"/"+year);
        }

    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

        static Button b;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            //Use the current time as the default values for the time picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create and return a new instance of TimePickerDialog
            return new TimePickerDialog(getActivity(),this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public static void setTimeButton(Button but)
        {
            b=but;
        }

        //onTimeSet() callback method
        public void onTimeSet(TimePicker view, int hourOfDay, int minute){
            b.setText(hourOfDay+":"+minute);
        }
    }



    public void prediction()
    {
        EditText startE=(EditText) view.findViewById(R.id.startingPointTextView);
        EditText destE=(EditText) view.findViewById(R.id.destinationPointTextView);
        String start=startE.getText().toString();
        String dest=destE.getText().toString();
        if(start.isEmpty()|| dest.isEmpty() || dateButton.getText()=="Pick a date" || timeButton.getText()=="Pick a time")
        {
            Toast.makeText(getActivity(), "Please fill up all field", Toast.LENGTH_SHORT).show();
        }
        else
        {
            progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                    "Predicting traffic", true);

            //demo progressbar show
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                }
            }, 1000);

            //progress bar dismiss after finding result

            progressDialog.dismiss();
        }


    }

}
