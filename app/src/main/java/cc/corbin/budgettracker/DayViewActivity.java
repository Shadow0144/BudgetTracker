package cc.corbin.budgettracker;

import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DayViewActivity extends AppCompatActivity
{
    private final String TAG = "DayViewActivity";

    public final static String DATE_INTENT = "Date";

    private TextView _dateView;
    private ViewPager _pagerView;
    private DayFragmentPagerAdapter _adapter;

    private Button _previousDay;
    private Button _nextDay;

    private Date _currentDate;

    private int _year;
    private int _month;
    private int _day;

    private static String[] _categories;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_view);

        _dateView = findViewById(R.id.dateView);
        _pagerView = findViewById(R.id.itemsPager);

        _previousDay = findViewById(R.id.yesterdayButton);
        _nextDay = findViewById(R.id.tomorrowButton);

        loadCategories();

        _currentDate = new Date();
        _currentDate.setTime(getIntent().getLongExtra(DATE_INTENT, Calendar.getInstance().getTimeInMillis()));
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText("Expenditures for: " + simpleDate.format(_currentDate));

        _year = _currentDate.getYear();
        _month = _currentDate.getMonth();
        _day = _currentDate.getDate();

        _adapter = new DayFragmentPagerAdapter(getSupportFragmentManager(), _currentDate.getMonth(), _currentDate.getYear());
        _pagerView.setAdapter(_adapter);
        _pagerView.setCurrentItem(_day);

        loadMonthData();

        updateDay();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        saveMonthData();
    }

    public void previousDay(View v)
    {
        _day--;

        updateDay();
    }

    public void nextDay(View v)
    {
        _day++;

        updateDay();
    }

    private void updateDay()
    {
        _pagerView.setCurrentItem(_day);

        if (_day == 1)
        {
            _previousDay.setEnabled(false);
            _nextDay.setEnabled(true);
        }
        else if (_day == _adapter.lastDay())
        {
            _previousDay.setEnabled(true);
            _nextDay.setEnabled(false);
        }
        else
        {
            _previousDay.setEnabled(true);
            _nextDay.setEnabled(true);
        }

        _currentDate = new Date(_year, _month, _day);
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");

        _dateView.setText("Expenditures for: " + simpleDate.format(_currentDate));
    }

    public void addItem(View v)
    {
        _adapter.addExpenditure(_pagerView.getCurrentItem());
    }

    private void loadCategories()
    {
        _categories = getResources().getStringArray(R.array.default_categories);
    }

    public static String[] getCategories()
    {
        return _categories;
    }

    private void saveMonthData()
    {
        try
        {
            File records;
            if (isExternalStorageReadable())
            {
                records = new File(getFilesDir() + "/" + _year + "_" + _month + ".xml");
            }
            else
            {
                records = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + _year + "_" + _month + ".xml");
            }

            OutputStream outputStream = new FileOutputStream(records);

            String text;

            text = "<" + "month" + ">";
            outputStream.write(text.getBytes());

            int count = _adapter.getCount();
            for (int i = 0; i < count; i++)
            {
                ArrayList<Expenditure> expenditure = _adapter.getExpenditures(i);
                int exp = expenditure.size();
                text = "<" + "day" + ">";
                outputStream.write(text.getBytes());
                for (int j = 0; j < exp; j++)
                {
                    text = "<" + "item" + ">";
                    outputStream.write(text.getBytes());

                    text = "<" + "cost" + ">";
                    outputStream.write(text.getBytes());

                    text = "" + expenditure.get(j).cost;
                    outputStream.write(text.getBytes());

                    text = "</" + "cost" + ">";
                    outputStream.write(text.getBytes());

                    text = "<" + "category" + ">";
                    outputStream.write(text.getBytes());

                    text = "" + expenditure.get(j).category;
                    outputStream.write(text.getBytes());

                    text = "</" + "category" + ">";
                    outputStream.write(text.getBytes());

                    text = "</" + "item" + ">";
                    outputStream.write(text.getBytes());
                }
                text = "</" + "day" + ">";
                outputStream.write(text.getBytes());
            }

            text = "</" + "month" + ">";
            outputStream.write(text.getBytes());

            outputStream.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    private void loadMonthData()
    {
        try
        {
            XmlPullParser parser = Xml.newPullParser();

            File records;
            if (isExternalStorageReadable())
            {
                records = new File(getFilesDir() + "/" + _year + "_" + _month + ".xml");
            }
            else
            {
                records = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + _year + "_" + _month + ".xml");
            }

            if (records.exists())
            {
                InputStream inputStream = new FileInputStream(records);
                parser.setInput(inputStream, null);

                parser.next();
                parser.require(XmlPullParser.START_TAG, null, "month");

                ArrayList<ArrayList<Expenditure>> dailyExpenditures = new ArrayList<ArrayList<Expenditure>>();
                int count = _adapter.getCount();
                for (int i = 0; i < count; i++)
                {
                    ArrayList<Expenditure> expenditures = new ArrayList<Expenditure>();

                    parser.nextTag();
                    parser.require(XmlPullParser.START_TAG, null, "day");
                    while (parser.nextTag() != XmlPullParser.END_TAG)
                    {
                        // Item
                        parser.require(XmlPullParser.START_TAG, null, "item");
                        parser.nextTag();
                        parser.require(XmlPullParser.START_TAG, null, "cost");
                        parser.next();
                        float cost = Float.parseFloat(parser.getText());
                        parser.nextTag();
                        parser.require(XmlPullParser.END_TAG, null, "cost");
                        parser.nextTag();
                        parser.require(XmlPullParser.START_TAG, null, "category");
                        parser.next();
                        String cat = parser.getText();
                        parser.nextTag();
                        parser.require(XmlPullParser.END_TAG, null, "category");
                        parser.nextTag();
                        parser.require(XmlPullParser.END_TAG, null, "item");

                        expenditures.add(new Expenditure(cost, cat));
                    }
                    parser.require(XmlPullParser.END_TAG, null, "day");

                    dailyExpenditures.add(expenditures);
                }

                inputStream.close();

                _adapter.setExpenditures(dailyExpenditures);
            }
            else { }
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
