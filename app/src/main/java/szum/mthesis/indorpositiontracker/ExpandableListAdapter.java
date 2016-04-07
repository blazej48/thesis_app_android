package szum.mthesis.indorpositiontracker;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
 
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private static final String TAG = ExpandableListAdapter.class.getSimpleName();
 
    private HistoryFragment mHistoryFragment;
	private List<RunInfo> runList;
    private MainActivity mActivity;
    private Context mContext;
    // child data in format of header title, child title
    private HashMap<String, List<String>> _listDataChild;
    
      
    public ExpandableListAdapter(MainActivity activity, List<RunInfo> runList, HistoryFragment historyFragment, Context context) {
    	super();
    	mHistoryFragment = historyFragment;
        this.mActivity = activity;
        this.mContext = context;
        this.runList = runList;
	}

	@Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataChild.get(this.runList.get(groupPosition))
                .get(childPosititon);
    }
 
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
 
    @Override
    public View getChildView(final int position, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
 

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.history_expanded_element, null);
        }
 
        TextView expandedView = (TextView) convertView.findViewById(R.id.expanded);

        String text;
        if( runList.get(position).getStepsCount() != 0 ){
            text =  "Id: " + runList.get(position).getId() +
                        "\nTime: " + runList.get(position).getDate() + " m" +
        				"\nLat: " + runList.get(position).getInitLatitude() +
        				"\nLong: " + runList.get(position).getInitLongitude() +
        				"\nSteps count: " + runList.get(position).getStepsCount() +
        				"\nWalk time: " + runList.get(position).getWalkTime()/1000 + " [s]" +
        				"\nOrientation correction: " + runList.get(position).getRotationCorrection() + " *" +
                        String.format("\nWalk distance: %.1f [m]", runList.get(position).getWalkDistance()) +
                        String.format("\nEstimated step length: %.3f [m]", runList.get(position).getWalkDistance()/(double)runList.get(position).getStepsCount()) +
                        String.format("\nEstimated step frequency: %.3f [1/s]", (double)runList.get(position).getStepsCount()/runList.get(position).getWalkTime()*1000) +
                        String.format("\nAvg GPS accuracy: %.2f [m]", runList.get(position).getAvgAccuracy());
        } else{
            Logger.w(TAG, "step count is 0 while displaying route info.");
            text = "step count: 0 !";
        }

        expandedView.setText(text);
        
        Button mapButton = (Button) convertView.findViewById(R.id.showMap);
        Button deleteButton = (Button) convertView.findViewById(R.id.deleteInfo);
        
        mapButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
                mActivity.openMap(runList.get(position));
			}
		});
        

        
        deleteButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {

                Database database = new Database();
                database.deleteData(runList.get(position).getDate(), runList.get(position).getDate());
                database.close();
		    	mHistoryFragment.onResume();
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
        return runList.size();
    }
 
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
 
    @Override
    public View getGroupView(int position, boolean isExpanded,
            View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.history_element, null);
        }
        
        SimpleDateFormat ft = new SimpleDateFormat ("E yyyy.MM.dd 'at' HH:mm:ss", Locale.US);
        
        String text = ft.format(new Date(runList.get(position).getDate()));
 
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