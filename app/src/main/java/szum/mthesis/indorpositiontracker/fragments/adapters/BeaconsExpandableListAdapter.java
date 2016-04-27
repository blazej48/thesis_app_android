package szum.mthesis.indorpositiontracker.fragments.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import szum.mthesis.indorpositiontracker.R;
import szum.mthesis.indorpositiontracker.fragments.BeaconsFragment;
import szum.mthesis.indorpositiontracker.orm.Beacon;


public class BeaconsExpandableListAdapter extends BaseExpandableListAdapter {

    private BeaconsFragment mBeaconsFragment;
	private List<Beacon> beacons;

    private HashMap<String, List<String>> _listDataChild;


    public BeaconsExpandableListAdapter(List<Beacon> beacons, BeaconsFragment fragment) {
    	super();
    	mBeaconsFragment = fragment;
    	this.beacons = beacons;
	}

	@Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this.beacons.get(groupPosition))
                .get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(final int position, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        final Beacon beacon =beacons.get(position);
 

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mBeaconsFragment.getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.beacon_expanded_element, null);
        }
 
        TextView expandedView = (TextView) convertView.findViewById(R.id.beacons_expanded);
 
        String  text = "name " + beacon.getName();
        expandedView.setText(text);

        final EditText latText = (EditText) convertView.findViewById(R.id.lat);
        latText.setText(""+beacon.getLat());

        final EditText lngText = (EditText) convertView.findViewById(R.id.lng);
        lngText.setText(""+beacon.getLng());

        Button saveButton = (Button) convertView.findViewById(R.id.beacons_save_info);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                beacon.setLat(Double.parseDouble(latText.getText().toString()));
                beacon.setLng(Double.parseDouble(lngText.getText().toString()));
                beacon.save();
            }
        });

        Button deleteButton = (Button) convertView.findViewById(R.id.beacons_deleteInfo);
        deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                beacon.delete();
		    	mBeaconsFragment.onResume();
		}});
        
        return convertView;
    }
 
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }
 
    @Override
    public Object getGroup(int groupPosition) {
        return 0;
    }
 
    @Override
    public int getGroupCount() {
        return beacons.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int position, boolean isExpanded,
            View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mBeaconsFragment.getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.history_element, null);
        }

        String text =  beacons.get(position).getId() + "   " + beacons.get(position).getName();
 
        TextView runHeader = (TextView) convertView.findViewById(R.id.historyelement);
        runHeader.setTypeface(null, Typeface.BOLD);
        runHeader.setText(text);
 
        return convertView;
    }
 
    @Override
    public boolean hasStableIds() {
        return false;
    }
 
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}