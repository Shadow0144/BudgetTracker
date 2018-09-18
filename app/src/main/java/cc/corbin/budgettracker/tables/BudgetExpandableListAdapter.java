package cc.corbin.budgettracker.tables;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;

public class BudgetExpandableListAdapter extends BaseExpandableListAdapter
{
    private final String TAG = "BudgetExpandableListAdapter";

    private Context _context;
    private List<BudgetEntity> _budgetEntities;
    private List<BudgetEntity> _budgets;
    private Map<String, List<BudgetEntity>> _adjustments;

    public BudgetExpandableListAdapter(Context context)
    {
        _context = context;
        _budgets = new ArrayList<BudgetEntity>();
        _adjustments = new HashMap<String, List<BudgetEntity>>();
    }

    public void setBudgets(List<BudgetEntity> budgetEntities)
    {
        _budgetEntities = budgetEntities;
        String[] categories = Categories.getCategories();
        int catCount = categories.length;

        int i = 0;
        for (; i < catCount; i++)
        {
            _budgets.add(_budgetEntities.get(i));
        }

        int len = _budgetEntities.size();
        for (int j = 0; j < catCount; j++)
        {
            List<BudgetEntity> adjs = new ArrayList<BudgetEntity>();
            while (i < len && _budgetEntities.get(i).getCategoryName().equals(categories[j]))
            {
                adjs.add(_budgetEntities.get(i));
                i++;
            }
            _adjustments.put(categories[j], adjs);
        }

        notifyDataSetInvalidated();
    }

    @Override
    public int getGroupCount()
    {
        return _budgets.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return _adjustments.get(Categories.getCategories()[groupPosition]).size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return _budgets.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return _adjustments.get(Categories.getCategories()[groupPosition]).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        convertView = new BudgetTableCell(_context, ((BudgetEntity) getGroup(groupPosition)));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        convertView = new AdjustmentTableCell(_context, ((BudgetEntity) getChild(groupPosition, childPosition)));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return true;
    }
}
