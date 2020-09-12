package cc.corbin.budgettracker.auxilliary;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cc.corbin.budgettracker.R;

// Parent class for an Activity that implements a page swiping system, e.g. DayView, MonthView, YearView, etc.
// The content view is set here as well
public abstract class PagingActivity extends NavigationActivity
{
    private final String TAG = "PagingActivity";

    protected RecyclerView _recyclerView;
    protected LinearLayoutManager _layoutManager;
    protected LinearLayout _dateLayout;

    protected Button _leftCurrentButton;
    protected Button _rightCurrentButton;

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

        _dateLayout = findViewById(R.id.dateLayout);
        _leftCurrentButton = findViewById(R.id.jumpToCurrentLeftButton);
        _rightCurrentButton = findViewById(R.id.jumpToCurrentRightButton);
    }

    public void previousView(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()-1);
    }

    public void nextView(View v)
    {
        _recyclerView.smoothScrollToPosition(_layoutManager.findFirstVisibleItemPosition()+1);
    }

    // Jumps to the current view
    public abstract void currentView(View v);

    public void setHeaderColor(int color)
    {
        _dateLayout.setBackgroundColor(color);
    }

    public void showLeftCurrentButton()
    {
        _leftCurrentButton.setVisibility(View.VISIBLE);
    }

    public void showRightCurrentButton()
    {
        _rightCurrentButton.setVisibility(View.VISIBLE);
    }

    public void hideLeftCurrentButton()
    {
        _leftCurrentButton.setVisibility(View.INVISIBLE);
    }

    public void hideRightCurrentButton()
    {
        _rightCurrentButton.setVisibility(View.INVISIBLE);
    }
}
