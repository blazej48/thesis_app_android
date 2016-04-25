package szum.mthesis.indorpositiontracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;

import java.util.List;

import szum.mthesis.indorpositiontracker.entities.GpsLocation;
import szum.mthesis.indorpositiontracker.entities.Path;

public class HistoryFragment extends Fragment{

    Button refreshButton;


        @Override
    public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            View view = getView();
            refreshButton  = (Button) view.findViewById(R.id.refresh);

            final MainActivity activity = (MainActivity)getActivity();

            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                            List<Path> runList = Path.listAll(Path.class);

                            View view = getView();
                            ExpandableListView adapterView = (ExpandableListView) view.findViewById(R.id.historylist);
                            ExpandableListAdapter listAdapter = new ExpandableListAdapter(activity, runList, HistoryFragment.this, getContext());
                            adapterView.setAdapter(listAdapter);
                }
            });
        }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        return inflater.inflate(R.layout.history_fragment, container, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        List<Path> runList = Path.listAll(Path.class);

        View view = getView();
        ExpandableListView adapterView = (ExpandableListView) view.findViewById(R.id.historylist);
        ExpandableListAdapter listAdapter = new ExpandableListAdapter((MainActivity)getActivity(), runList, this, getContext());
        adapterView.setAdapter(listAdapter);
    }
}