package cc.corbin.budgettracker.workerthread;

import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

public class ExpenditureRunnable implements Runnable
{
    private final String TAG = "ExpenditureRunnable";

    public static ExpenditureViewModel viewModel;

    @Override
    public void run()
    {
        viewModel.checkQueue();
    }
}