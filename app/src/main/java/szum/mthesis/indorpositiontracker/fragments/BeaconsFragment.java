package szum.mthesis.indorpositiontracker.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.List;

import szum.mthesis.indorpositiontracker.fragments.adapters.BeaconsExpandableListAdapter;
import szum.mthesis.indorpositiontracker.R;
import szum.mthesis.indorpositiontracker.orm.Beacon;

public class BeaconsFragment extends Fragment{

    TextView name;
    TextView lat;
    TextView lng;
    Button add;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        return inflater.inflate(R.layout.beacons_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        name = (TextView)getView().findViewById(R.id.name);
        lat = (TextView)getView().findViewById(R.id.lat);
        lng = (TextView)getView().findViewById(R.id.lng);
        add = (Button)getView().findViewById(R.id.add_beacon);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name_ = name.getText().toString();
                String lat_ = lat.getText().toString();
                String lng_ = lng.getText().toString();

                if(name_.length() == 0){
                    return;
                }
                if(lat_.length() == 0){
                    return;
                }
                if(lng_.length() == 0){
                    return;
                }

                Beacon beacon = new Beacon(name_, Double.parseDouble(lat_), Double.parseDouble(lng_));
                beacon.save();
                onResume();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        
        List<Beacon> beacons = Beacon.listAll(Beacon.class);
        
        View view = getView();
        ExpandableListView adapterView = (ExpandableListView) view.findViewById(R.id.beaconslist);
        BeaconsExpandableListAdapter listAdapter = new BeaconsExpandableListAdapter(beacons, this);
        adapterView.setAdapter(listAdapter);
    }
    
    
}