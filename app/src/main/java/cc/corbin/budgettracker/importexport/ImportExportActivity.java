package cc.corbin.budgettracker.importexport;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.DatePickerFragment;
import cc.corbin.budgettracker.auxilliary.NavigationDrawerHelper;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.write.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Corbin on 2/27/2018.
 */

public class ImportExportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener
{
    private final String TAG = "ImportExportActivity";

    private DrawerLayout _drawerLayout;

    private final int CHECK_CODE = 0; // Just check and request and return
    private final int EXPORT_ALL_CODE = 1;
    private final int IMPORT_CODE = 2;

    private TabLayout _tabLayout;
    private LinearLayout _importLayout;
    private LinearLayout _exportLayout;

    private TextView _exportToLocalStorageTextView;
    private TextView _exportToSDCardTextView;
    private Switch _exportLocationSwitch;

    private RadioButton _exportTotalRadioButton;
    private final int TOTAL_TAG = 1;
    private RadioButton _exportYearRadioButton;
    private final int YEAR_TAG = 2;
    private RadioButton _exportMonthRadioButton;
    private final int MONTH_TAG = 3;
    private RadioButton _exportDayRadioButton;
    private final int DAY_TAG = 4;
    private RadioButton _exportCustomRadioButton;
    private final int CUSTOM_TAG = 5;
    private int _currentExportSetting;

    private TextView _exportingTotalTextView;
    private TextView _exportingSubsetTextView;
    private LinearLayout _dateSelectLayout;
    // private TextView _yearTextView; // Not used
    private TextView _yearSelectedTextView;
    private TextView _monthTextView;
    private TextView _monthSelectedTextView;
    private TextView _dayTextView;
    private TextView _daySelectedTextView;

    private boolean _sdStorage = false;
    private boolean _publicStorage = false;

    private Calendar _selectedDate;
    private boolean _yearSelected;
    private boolean _monthSelected;
    private boolean _daySelected;

    private MutableLiveData<Date> _dateLiveData;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expenditures;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        // Setup the navigation toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        _tabLayout = findViewById(R.id.importExportTabLayout);
        _tabLayout.addOnTabSelectedListener(this);
        _importLayout = findViewById(R.id.importLinearLayout);
        _exportLayout = findViewById(R.id.exportLinearLayout);
        switch (_tabLayout.getSelectedTabPosition())
        {
            case 0:
                _importLayout.setVisibility(View.VISIBLE);
                _exportLayout.setVisibility(View.GONE);
                break;
            case 1:
                _importLayout.setVisibility(View.GONE);
                _exportLayout.setVisibility(View.VISIBLE);
                break;
        }

        _exportToLocalStorageTextView = findViewById(R.id.exportToLocalTextView);
        _exportToSDCardTextView = findViewById(R.id.exportToSDTextView);
        _exportLocationSwitch = findViewById(R.id.exportLocationSwitch);
        _exportLocationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                storageCheckChanged(isChecked);
            }
        });
        storageCheckChanged(_exportLocationSwitch.isChecked());

        _exportTotalRadioButton = findViewById(R.id.exportTotalRadioButton);
        _exportTotalRadioButton.setId(TOTAL_TAG);
        _exportYearRadioButton = findViewById(R.id.exportYearRadioButton);
        _exportYearRadioButton.setId(YEAR_TAG);
        _exportMonthRadioButton = findViewById(R.id.exportMonthRadioButton);
        _exportMonthRadioButton.setId(MONTH_TAG);
        _exportDayRadioButton = findViewById(R.id.exportDayRadioButton);
        _exportDayRadioButton.setId(DAY_TAG);
        _exportCustomRadioButton = findViewById(R.id.exportCustomRadioButton);
        _exportCustomRadioButton.setId(CUSTOM_TAG);

        _exportingTotalTextView = findViewById(R.id.exportTotalTextView);
        _exportingSubsetTextView = findViewById(R.id.exportSubsetTextView);
        _dateSelectLayout = findViewById(R.id.dateSelectLayout);
        _yearSelectedTextView = findViewById(R.id.yearSelectedTextView);
        _monthTextView = findViewById(R.id.monthTextView);
        _monthSelectedTextView = findViewById(R.id.monthSelectedTextView);
        _dayTextView = findViewById(R.id.dayTextView);
        _daySelectedTextView = findViewById(R.id.daySelectedTextView);

        View.OnClickListener radioClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                exportRadioButtonClicked(v);
            }
        };
        _exportTotalRadioButton.setOnClickListener(radioClickListener);
        _exportYearRadioButton.setOnClickListener(radioClickListener);
        _exportMonthRadioButton.setOnClickListener(radioClickListener);
        _exportDayRadioButton.setOnClickListener(radioClickListener);
        _exportCustomRadioButton.setOnClickListener(radioClickListener);
        _exportTotalRadioButton.setChecked(true);
        _currentExportSetting = TOTAL_TAG;

        _yearSelected = false;
        _monthSelected = false;
        _daySelected = false;

        _dateLiveData = new MutableLiveData<Date>();
        final Observer<Date> dateObserver = new Observer<Date>()
        {
            @Override
            public void onChanged(@Nullable Date date)
            {
                dateReceived(date);
            }
        };
        _dateLiveData.observe(this, dateObserver);

        _viewModel = ExpenditureViewModel.getInstance();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab)
    {

    }

    @Override
    public void onTabSelected(TabLayout.Tab tab)
    {
        switch (_tabLayout.getSelectedTabPosition())
        {
            case 0:
                _importLayout.setVisibility(View.VISIBLE);
                _exportLayout.setVisibility(View.GONE);
                break;
            case 1:
                _importLayout.setVisibility(View.GONE);
                _exportLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab)
    {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Intent intent = NavigationDrawerHelper.handleNavigation(item);

        boolean handled = (intent != null);
        if (handled)
        {
            startActivity(intent);
            _drawerLayout.closeDrawer(GravityCompat.START);
        }
        else { }

        return handled;
    }

    public void checkPermissions(Activity activity)
    {
        checkPermissions(activity, CHECK_CODE);
    }

    private void checkPermissions(Activity activity, int code)
    {
        if (activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            activity.requestPermissions(new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, code);
        }
        else
        {
            _publicStorage = true;
            respondToRequestCode(code);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        // If request is cancelled, the result arrays are empty
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            _publicStorage = true;
            respondToRequestCode(requestCode);
        }
        else
        {
            _publicStorage = false;
            Toast.makeText(this, "Failed to acquire permissions", Toast.LENGTH_SHORT).show();
            _expenditures = null;
        }
    }

    private void respondToRequestCode(int requestCode)
    {
        switch (requestCode)
        {
            case EXPORT_ALL_CODE:
                onExport(null);
                break;
            //case EXPORT_MONTH_CODE:
            //    exportMonth(0, 0); // TODO
            //    break;
        }
    }

    private void storageCheckChanged(boolean isChecked)
    {
        _sdStorage = isChecked;
        if (_sdStorage)
        {
            _exportToLocalStorageTextView.setTypeface(null, Typeface.NORMAL);
            _exportToSDCardTextView.setTypeface(null, Typeface.BOLD);
        }
        else
        {
            _exportToLocalStorageTextView.setTypeface(null, Typeface.BOLD);
            _exportToSDCardTextView.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void exportRadioButtonClicked(View v)
    {
        _currentExportSetting = v.getId();
        switch (_currentExportSetting)
        {
            case TOTAL_TAG:
                _exportingTotalTextView.setVisibility(View.VISIBLE);
                _exportingSubsetTextView.setVisibility(View.GONE);
                _dateSelectLayout.setVisibility(View.GONE);
                break;
            case YEAR_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.GONE);
                _monthSelectedTextView.setVisibility(View.GONE);
                _dayTextView.setVisibility(View.GONE);
                _daySelectedTextView.setVisibility(View.GONE);
                break;
            case MONTH_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.VISIBLE);
                _monthSelectedTextView.setVisibility(View.VISIBLE);
                _dayTextView.setVisibility(View.GONE);
                _daySelectedTextView.setVisibility(View.GONE);
                break;
            case DAY_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.VISIBLE);
                _monthSelectedTextView.setVisibility(View.VISIBLE);
                _dayTextView.setVisibility(View.VISIBLE);
                _daySelectedTextView.setVisibility(View.VISIBLE);
                break;
            case CUSTOM_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.GONE);
                break;
        }
    }

    public void selectDate(View v)
    {
        DialogFragment fragment = new DatePickerFragment();
        ((DatePickerFragment) fragment).setLiveData(_dateLiveData);
        switch (_currentExportSetting)
        {
            case YEAR_TAG:
                // TODO
                break;
            case MONTH_TAG:
                // TODO
                break;
            case DAY_TAG:
                // TODO
                break;
        }
        fragment.show(this.getSupportFragmentManager(), "datePicker");
    }

    private void dateReceived(Date date)
    {
        _selectedDate = Calendar.getInstance();
        _selectedDate.setTime(date);
        switch (_currentExportSetting)
        {
            case YEAR_TAG:
                _yearSelected = true;
                _yearSelectedTextView.setText(""+_selectedDate.get(Calendar.YEAR));
                break;
            case MONTH_TAG:
                _yearSelected = true;
                _monthSelected = true;
                _yearSelectedTextView.setText(""+_selectedDate.get(Calendar.YEAR));
                _monthSelectedTextView.setText(""+(_selectedDate.get(Calendar.MONTH)+1));
                break;
            case DAY_TAG:
                _yearSelected = true;
                _monthSelected = true;
                _daySelected = true;
                _yearSelectedTextView.setText(""+_selectedDate.get(Calendar.YEAR));
                _monthSelectedTextView.setText(""+(_selectedDate.get(Calendar.MONTH)+1));
                _daySelectedTextView.setText(""+_selectedDate.get(Calendar.DATE));
                break;
        }
    }

    public void onImport(View v)
    {
        Intent intent = new Intent()
                .setType("*/*")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), IMPORT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_CODE && resultCode == RESULT_OK)
        {
            continueImport(data.getData()); //The URI with the location of the file
        }
        else { }
    }

    private void continueImport(Uri selectedFile)
    {
        Log.e(TAG, "Import: " + selectedFile.getPath());
    }

    // TODO: Set where to store the files
    public void onExport(View v)
    {
        if (!_publicStorage)
        {
            checkPermissions(this, EXPORT_ALL_CODE);
        }
        else
        {
            String folder = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
            folder = folder.substring(0, folder.lastIndexOf("/"));
            folder += "/BudgetTracker/";

            File dstFolder = new File(folder);

            if (!dstFolder.exists())
            {
                if (!dstFolder.mkdir())
                {
                    Log.e(TAG, "Failed to make directory");
                }
                else { }
            }
            else { }

            if (!dstFolder.exists())
            {
                Log.e(TAG, "Destination folder does not exist");
            }
            else
            {
                switch (_currentExportSetting)
                {
                    case TOTAL_TAG:
                        exportTotal(folder);
                        break;
                    case YEAR_TAG:
                        exportYear(folder);
                        break;
                    case MONTH_TAG:
                        exportMonth(folder);
                        break;
                    case DAY_TAG:
                        exportDay(folder);
                        break;
                }
            }
        }
    }

    // TODO - Format files to have two digits for months and days

    private void exportTotal(String folder)
    {
        Calendar calendar = Calendar.getInstance();
        String expFileName = "ExpenditureDatabase_as_of_" + calendar.get(Calendar.YEAR) + "_" +
                (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DATE) + ".db";
        String budFileName = "BudgetDatabase_as_of_" + calendar.get(Calendar.YEAR) + "_" +
                (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DATE) + ".db";

        ExpenditureDatabase.createDatabaseFile(
                getDatabasePath("expenditures").getAbsolutePath(),
                folder, expFileName, "");
        BudgetDatabase.createDatabaseFile(
                getDatabasePath("budgets").getAbsolutePath(),
                folder, budFileName, "");
    }

    private void exportYear(String folder)
    {
        if (_yearSelected)
        {
            int year = _selectedDate.get(Calendar.YEAR);
            String expFileName = "ExpenditureDatabase_" + year + ".db";
            String budFileName = "BudgetDatabase_" + year + ".db";
            String whereQuery = "WHERE (year == " + year + ")";
            ExpenditureDatabase.createDatabaseFile(
                    getDatabasePath("expenditures").getAbsolutePath(),
                    folder, expFileName, whereQuery);
            BudgetDatabase.createDatabaseFile(
                    getDatabasePath("budgets").getAbsolutePath(),
                    folder, budFileName, whereQuery);
        }
        else
        {
            Toast.makeText(this,"Select a date", Toast.LENGTH_LONG).show();
        }
    }

    private void exportMonth(String folder)
    {
        if (_yearSelected && _monthSelected)
        {
            int year = _selectedDate.get(Calendar.YEAR);
            int month = _selectedDate.get(Calendar.MONTH)+1;
            String expFileName = "ExpenditureDatabase_" + year +
                    "_" + month + ".db";
            String budFileName = "BudgetDatabase_" + year +
                    "_" + month + ".db";
            String whereQuery = "WHERE (year == " + year + ") " +
                    "AND (month == " + month + ")";
            ExpenditureDatabase.createDatabaseFile(
                    getDatabasePath("expenditures").getAbsolutePath(),
                    folder, expFileName, whereQuery);
            BudgetDatabase.createDatabaseFile(
                    getDatabasePath("budgets").getAbsolutePath(),
                    folder, budFileName, whereQuery);
        }
        else
        {
            Toast.makeText(this,"Select a date", Toast.LENGTH_LONG).show();
        }
    }

    private void exportDay(String folder)
    {
        if (_yearSelected && _monthSelected && _daySelected)
        {
            int year = _selectedDate.get(Calendar.YEAR);
            int month = _selectedDate.get(Calendar.MONTH)+1;
            int day = _selectedDate.get(Calendar.DATE);
            String expFileName = "ExpenditureDatabase_" + year +
                    "_" + month +
                    "_" + day + ".db";
            String whereQuery = "WHERE (year == " + year + ") " +
                    "AND (month == " + month + ") " +
                    "AND (day == " + day + ")";
            ExpenditureDatabase.createDatabaseFile(
                    getDatabasePath("expenditures").getAbsolutePath(),
                    folder, expFileName, whereQuery);
            // No budget table to export
        }
        else
        {
            Toast.makeText(this,"Select a date", Toast.LENGTH_LONG).show();
        }
    }
}
