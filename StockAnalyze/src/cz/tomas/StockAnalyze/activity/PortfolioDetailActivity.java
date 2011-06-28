package cz.tomas.StockAnalyze.activity;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;


public class PortfolioDetailActivity extends ChartActivity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.setContentView(R.layout.portfolio_detail_layout);
		
		MyExpandableListAdapter mAdapter;
		ExpandableListView epView = (ExpandableListView) findViewById(R.id.portfolioDetailList);
		this.chartView = (CompositeChartView) this.findViewById(R.id.portfolioDetailChartView);
		mAdapter = new MyExpandableListAdapter();
		epView.setAdapter(mAdapter);
		super.onCreate(savedInstanceState);
	}

	 /**
     * A simple adapter which maintains an ArrayList of photo resource Ids. Each
     * photo is displayed as an image. This adapter supports clearing the list
     * of photos and adding a new photo.
     * 
     */
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
	    // Sample data set. children[i] contains the children (String[]) for
	    // groups[i].
	    private String[] groups = { "Parent1", "Parent2",
	        "Parent3" };
	    private String[][] children = { { "Child1" },{ "Child2" }, { "Child3" },{ "Child4" }, { "Child5" } };
	
	    public Object getChild(int groupPosition, int childPosition) {
	        return children[groupPosition][childPosition];
	    }
	
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	
	    public int getChildrenCount(int groupPosition) {
	        int i = 0;
	        try {
	        i = children[groupPosition].length;
	
	        } catch (Exception e) {
	        }
	
	        return i;
	    }
	
	    public TextView getGenericView() {
	        // Layout parameters for the ExpandableListView
	        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
	            ViewGroup.LayoutParams.FILL_PARENT, 64);
	
	        TextView textView = new TextView(PortfolioDetailActivity.this);
	        textView.setLayoutParams(lp);
	        // Center the text vertically
	        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	        textView.setTextColor(android.R.color.darker_gray);
	        // Set the text starting position
	        textView.setPadding(36, 0, 0, 0);
	        return textView;
	    }
	
	    public View getChildView(int groupPosition, int childPosition,
	        boolean isLastChild, View convertView, ViewGroup parent) {
	        TextView textView = getGenericView();
	        textView.setText(getChild(groupPosition, childPosition).toString());
	        return textView;
	    }
	
	    public Object getGroup(int groupPosition) {
	        return groups[groupPosition];
	    }
	
	    public int getGroupCount() {
	        return groups.length;
	    }
	
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }
	
	    public View getGroupView(int groupPosition, boolean isExpanded,
	        View convertView, ViewGroup parent) {
	        TextView textView = getGenericView();
	        textView.setText(getGroup(groupPosition).toString());
	        return textView;
	    }
	
	    public boolean isChildSelectable(int groupPosition, int childPosition) {
	        return true;
	    }
	
	    public boolean hasStableIds() {
	        return true;
	    }

    }
}
