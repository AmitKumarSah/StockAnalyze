package cz.tomas.StockAnalyze.Portfolio;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;

public class PortfolioDetailListAdapter extends BaseExpandableListAdapter {

	private static final int CHILD_COUNT = 1;
	
	private List<PortfolioItem> portfolioGroups;
		
	private LayoutInflater inflater;
	
	public PortfolioDetailListAdapter(Context context, StockItem stockItem) {
		if (stockItem == null)
			throw new NullPointerException("stock item must be defined!");
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Portfolio portfolio = (Portfolio) context.getApplicationContext().getSystemService(Application.PORTFOLIO_SERVICE);
		
		this.portfolioGroups = portfolio.getPortfolioItems(stockItem.getId());
	}
		
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (this.portfolioGroups == null) {
			return null;
		}
		return this.portfolioGroups.get(groupPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		if (this.portfolioGroups == null) {
			return 0;
		}
		PortfolioItem portfolioItem = this.portfolioGroups.get(groupPosition);
		return portfolioItem.getId();
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.item_portfolio_detail_child, null);
		}
		PortfolioChildViewHolder holder;
		if (convertView.getTag() == null) {
			holder = new PortfolioChildViewHolder();
			holder.txtBalance = (TextView) convertView.findViewById(R.id.portfolioGroupBalance);
			holder.txtBuyPrice = (TextView) convertView.findViewById(R.id.portfolioGroupBuyPrice);
			holder.txtSellPrice = (TextView) convertView.findViewById(R.id.portfolioGroupSellPrice);
			convertView.setTag(holder);
		} else {
			holder = (PortfolioChildViewHolder) convertView.getTag();
		}
		
		PortfolioItem item = this.portfolioGroups.get(groupPosition);
		holder.txtBalance.setText("N/A");
		holder.txtBuyPrice.setText(String.valueOf(item.getBuyPrice()));
		holder.txtSellPrice.setText(String.valueOf(item.getSellPrice()));
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return CHILD_COUNT;
	}

	@Override
	public Object getGroup(int groupPosition) {
		if (this.portfolioGroups == null) {
			return null;
		}
		return this.portfolioGroups.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		if (this.portfolioGroups == null) {
			return 0;
		}
		return this.portfolioGroups.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		if (this.portfolioGroups == null) {
			return -1;
		}
		PortfolioItem group = this.portfolioGroups.get(groupPosition);
		return group.getId();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		
		if (convertView == null) {
			convertView = this.inflater.inflate(R.layout.item_portfolio_detail_group, null);
		}
		
		TextView countView = (TextView) convertView.findViewById(R.id.portfolioGroupCount);
		TextView dateView = (TextView) convertView.findViewById(R.id.portfolioGroupDate);
		TextView valueView = (TextView) convertView.findViewById(R.id.portfolioGroupValue);
		
		PortfolioItem group = this.portfolioGroups.get(groupPosition);
		if (group != null) {
			countView.setText(String.valueOf(group.getBoughtStockCount() + group.getSoldStockCount()));
			dateView.setText(FormattingUtils.formatStockShortDate(group.getBuyDate()));
			valueView.setText("0");
		}
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	private static class PortfolioChildViewHolder {
        TextView txtSellPrice;
        TextView txtBuyPrice;
        TextView txtBalance;
        
	}
}
