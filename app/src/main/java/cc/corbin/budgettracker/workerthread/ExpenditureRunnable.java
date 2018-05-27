package cc.corbin.budgettracker.workerthread;

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