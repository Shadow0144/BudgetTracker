package cc.corbin.budgettracker.auxilliary;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.day.DayRecyclerAdapter;
import cc.corbin.budgettracker.day.DayView;
import cc.corbin.budgettracker.edit.ExpenditureEditActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;

public abstract class PagingActivity extends NavigationActivity
{
    private final String TAG = "PagingActivity";

    protected RecyclerView _recyclerView;
    protected LinearLayoutManager _layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_paging_view);
        super.onCreate(savedInstanceState);
    }

    protected void setupAdapterView(RecyclerView.Adapter recyclerAdapter)
    {
        _recyclerView = findViewById(R.id.itemsPager);
        _recyclerView.setHasFixedSize(false); // TODO

        _layoutManager = new LinearLayoutManager(this);
        _layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        _layoutManager.setStackFromEnd(true); // TODO
        _recyclerView.setLayoutManager(_layoutManager);

        _recyclerView.setAdapter(recyclerAdapter);
        final SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(_recyclerView);
    }

    public void previousView(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()-1);
    }

    public void nextView(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()+1);
    }
}
