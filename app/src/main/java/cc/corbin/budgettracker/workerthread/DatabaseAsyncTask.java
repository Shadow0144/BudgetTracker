package cc.corbin.budgettracker.workerthread;

import android.os.AsyncTask;

public class DatabaseAsyncTask extends AsyncTask<Void, Void, Void>
{
    private ExpenditureViewModel _model;
    private DatabaseThread _thread;

    public DatabaseAsyncTask(ExpenditureViewModel model, DatabaseThread thread)
    {
        _model = model;
        _thread = thread;
    }

    @Override
    protected Void doInBackground(Void... voids)
    {
        if (_thread != null)
        {
            _thread.run();
        }
        else { }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        //_model.checkQueue();
    }
}
