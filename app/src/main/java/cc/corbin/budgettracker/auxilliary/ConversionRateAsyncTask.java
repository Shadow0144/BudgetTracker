package cc.corbin.budgettracker.auxilliary;

import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class ConversionRateAsyncTask extends AsyncTask<String, Void, String>
{
    private final String TAG = "ConversionRateAsyncTask";

    private MutableLiveData<String> _conversionRateMLD;

    // This is a constructor that allows you to pass in the JSON body
    public ConversionRateAsyncTask(MutableLiveData<String> conversionRateMLD)
    {
        _conversionRateMLD = conversionRateMLD;
    }

    // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
    @Override
    protected String doInBackground(String... URLStrings)
    {
        String result = "";

        try
        {
            // This is getting the url from the string we passed in
            URL conversionURL = new URL(URLStrings[0]);
            URLConnection urlConnection = conversionURL.openConnection();
            urlConnection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            result = in.readLine();
            in.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result)
    {
        _conversionRateMLD.setValue(result);
    }
}
