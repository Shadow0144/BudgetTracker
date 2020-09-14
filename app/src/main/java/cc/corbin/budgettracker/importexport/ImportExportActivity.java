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
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.DatePickerFragment;
import cc.corbin.budgettracker.navigation.NavigationDrawerHelper;
import cc.corbin.budgettracker.search.SearchHelper;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Corbin on 2/27/2018.
 */

public class ImportExportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, TabLayout.OnTabSelectedListener
{
    private final String TAG = "ImportExportActivity";

    private DrawerLayout _drawerLayout;

    private final int EXTERNAL_SAVING_CODE = 0;
    private final int IMPORT_FILE_SELECT_CODE = 1;

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
    private boolean _externalStoragePermission = false;

    private Calendar _selectedDate;
    private boolean _yearSelected;
    private boolean _monthSelected;
    private boolean _daySelected;

    private EditText _expFileNameEditText;
    private EditText _budFileNameEditText;
    private ConstraintLayout _fileNameConstraintLayout;

    private String _expFileName;
    private String _budFileName;

    private Button _importButton;
    private Button _exportButton;

    private MutableLiveData<Date> _dateLiveData;
    private MutableLiveData<Boolean> _databaseActionComplete;

    private ExpenditureViewModel _viewModel;

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

        _importButton = findViewById(R.id.importButton);
        _exportButton = findViewById(R.id.exportButton);

        _expFileNameEditText = findViewById(R.id.expFileNameEditText);
        _budFileNameEditText = findViewById(R.id.budFileNameEditText);
        _fileNameConstraintLayout = findViewById(R.id.fileNameConstraintLayout);

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

        updateFileNames();

        _databaseActionComplete = new MutableLiveData<Boolean>();
        final Observer<Boolean> databaseObserver = new Observer<Boolean>()
        {
            @Override
            public void onChanged(@Nullable Boolean aBoolean)
            {
                if (aBoolean) // if - complete
                {
                    _importButton.setEnabled(true);
                    _exportButton.setEnabled(true);
                    makeCompletionToast();
                }
                else { } // else - not complete
            }
        };
        _databaseActionComplete.observe(this, databaseObserver);
        _databaseActionComplete.setValue(false);

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

    private void checkPermissions(Activity activity, int code)
    {
        if (activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            activity.requestPermissions(new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, code);
        }
        else
        {
            _externalStoragePermission = true;
            respondToRequestCode(code);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        // If request is cancelled, the result arrays are empty
        switch (requestCode)
        {
            case EXTERNAL_SAVING_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    _externalStoragePermission = true;
                    respondToRequestCode(requestCode); // Only respond on a success or an infinite loop may occur
                }
                else
                {
                    _externalStoragePermission = false;
                    Toast.makeText(this, "Failed to acquire permissions", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    // Complete the export if the permission was given
    private void respondToRequestCode(int requestCode)
    {
        switch (requestCode)
        {
            case EXTERNAL_SAVING_CODE:
                onExport(null);
                break;
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
                _fileNameConstraintLayout.setVisibility(View.VISIBLE);
                break;
            case YEAR_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.GONE);
                _monthSelectedTextView.setVisibility(View.GONE);
                _dayTextView.setVisibility(View.GONE);
                _daySelectedTextView.setVisibility(View.GONE);
                _fileNameConstraintLayout.setVisibility(View.VISIBLE);
                break;
            case MONTH_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.VISIBLE);
                _monthSelectedTextView.setVisibility(View.VISIBLE);
                _dayTextView.setVisibility(View.GONE);
                _daySelectedTextView.setVisibility(View.GONE);
                _fileNameConstraintLayout.setVisibility(View.VISIBLE);
                break;
            case DAY_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.VISIBLE);
                _monthTextView.setVisibility(View.VISIBLE);
                _monthSelectedTextView.setVisibility(View.VISIBLE);
                _dayTextView.setVisibility(View.VISIBLE);
                _daySelectedTextView.setVisibility(View.VISIBLE);
                _fileNameConstraintLayout.setVisibility(View.GONE);
                break;
            case CUSTOM_TAG:
                _exportingTotalTextView.setVisibility(View.GONE);
                _exportingSubsetTextView.setVisibility(View.VISIBLE);
                _dateSelectLayout.setVisibility(View.GONE);
                _fileNameConstraintLayout.setVisibility(View.VISIBLE);
                break;
        }
        updateFileNames();
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
        updateFileNames();
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

        startActivityForResult(Intent.createChooser(intent, "Select a file"), IMPORT_FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMPORT_FILE_SELECT_CODE && resultCode == RESULT_OK)
        {
            continueImport(data.getData()); //The URI with the location of the file
        }
        else { }
    }

    private void continueImport(Uri selectedFile)
    {
        Log.e(TAG, "Import: " + selectedFile.getPath());
    }

    private String getFolder()
    {
        String folder = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
        folder = folder.substring(0, folder.lastIndexOf("/"));
        folder += "/BudgetTracker/";
        return folder;
    }

    // TODO: Set where to store the files
    public void onExport(View v)
    {
        if (!_externalStoragePermission)
        {
            checkPermissions(this, EXTERNAL_SAVING_CODE);
        }
        else
        {
            String folder = getFolder();

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
                    case CUSTOM_TAG:
                        exportCustom(folder);
                        break;
                }
                _importButton.setEnabled(false);
                //_exportButton.setEnabled(false); // TODO Temporary
            }
        }
    }

    private void updateFileNames()
    {
        String yearString;
        String monthString;
        String dayString;
        switch (_currentExportSetting)
        {
            case TOTAL_TAG:
                Calendar calendar = Calendar.getInstance();
                _expFileName = "ExpenditureDatabase_as_of_" + calendar.get(Calendar.YEAR) + "_" +
                        (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DATE) + ".db";
                _budFileName = "BudgetDatabase_as_of_" + calendar.get(Calendar.YEAR) + "_" +
                        (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DATE) + ".db";
                break;
            case YEAR_TAG:
                yearString = "";
                if (_yearSelected)
                {
                    yearString = "" + _selectedDate.get(Calendar.YEAR);
                }
                else{ }
                _expFileName = "ExpenditureDatabase_" + yearString + ".db";
                _budFileName = "BudgetDatabase_" + yearString + ".db";
                break;
            case MONTH_TAG:
                yearString = "";
                monthString = "";
                if (_yearSelected && _monthSelected)
                {
                    yearString = "" + _selectedDate.get(Calendar.YEAR);
                    monthString = "" + (_selectedDate.get(Calendar.MONTH)+1);
                }
                else{ }
                _expFileName = "ExpenditureDatabase_" + yearString + "_" + monthString + ".db";
                _budFileName = "BudgetDatabase_" + yearString + "_" + monthString + ".db";
                break;
            case DAY_TAG:
                yearString = "";
                monthString = "";
                dayString = "";
                if (_yearSelected && _monthSelected && _daySelected)
                {
                    yearString = "" + _selectedDate.get(Calendar.YEAR);
                    monthString = "" + (_selectedDate.get(Calendar.MONTH)+1);
                    dayString = "" + (_selectedDate.get(Calendar.DATE));
                }
                else{ }
                _expFileName = "ExpenditureDatabase_" + yearString + "_" + monthString + "_" + dayString + ".db";
                _budFileName = "BudgetDatabase_" + yearString + "_" + monthString + "_" + dayString + ".db";
                break;
            case CUSTOM_TAG:
                _expFileName = "ExpenditureDatabase_" + "custom" + ".db";
                _budFileName = "BudgetDatabase_" + "custom" + ".db";
                break;
        }

        _expFileNameEditText.setHint(_expFileName);
        _budFileNameEditText.setHint(_budFileName);
    }

    private String getExpenditureFileName()
    {
        String expFileName = _expFileNameEditText.getText().toString();
        expFileName = (expFileName.length() == 0) ? _expFileName : expFileName;
        return expFileName;
    }

    private String getBudgetFileName()
    {
        String budFileName = _budFileNameEditText.getText().toString();
        budFileName = (budFileName.length() == 0) ? _budFileName : budFileName;
        return budFileName;
    }

    // TODO - Format files to have two digits for months and weeks

    private void exportTotal(String folder)
    {
        String whereQuery = "";
        _viewModel.exportDatabases(_databaseActionComplete, whereQuery, folder,
                getDatabasePath("expenditures").getAbsolutePath(), getExpenditureFileName(),
                getDatabasePath("budgets").getAbsolutePath(), getBudgetFileName(), true);
    }

    private void exportYear(String folder)
    {
        if (_yearSelected)
        {
            int year = _selectedDate.get(Calendar.YEAR);
            String whereQuery = "WHERE (year == " + year + ")";
            _viewModel.exportDatabases(_databaseActionComplete, whereQuery, folder,
                    getDatabasePath("expenditures").getAbsolutePath(), getExpenditureFileName(),
                    getDatabasePath("budgets").getAbsolutePath(), getBudgetFileName(), true);
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
            String whereQuery = "WHERE (year == " + year + ") " +
                    "AND (month == " + month + ")";
            _viewModel.exportDatabases(_databaseActionComplete, whereQuery, folder,
                    getDatabasePath("expenditures").getAbsolutePath(), getExpenditureFileName(),
                    getDatabasePath("budgets").getAbsolutePath(), getBudgetFileName(), true);
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
            String whereQuery = "WHERE (year == " + year + ") " +
                    "AND (month == " + month + ") " +
                    "AND (day == " + day + ")";
            _viewModel.exportDatabases(_databaseActionComplete, whereQuery, folder,
                    getDatabasePath("expenditures").getAbsolutePath(), getExpenditureFileName(),
                    "", "", false);
        }
        else
        {
            Toast.makeText(this,"Select a date", Toast.LENGTH_LONG).show();
        }
    }

    private void exportCustom(String folder)
    {
        // TODO
        // Temporary
        MutableLiveData<Boolean> _completed = new MutableLiveData<Boolean>();
        String fileName = getExpenditureFileName();
        int index = fileName.indexOf('.');
        fileName = fileName.substring(0, ((index == -1) ? (fileName.length()-1) : index));
        Intent intent = new Intent();
        SearchHelper searchHelper = new SearchHelper(intent);
        String whereExpQuery = searchHelper.getQuery();
        // Remove the yearly budget sums, i.e. where month == 0
        String whereBudQuery = "SELECT * FROM budgetentity WHERE (month != 0) ORDER BY year ASC, month ASC, id ASC;";
        Log.e(TAG, whereExpQuery);
        ExportXMLHelper exportXMLHelper = new ExportXMLHelper(this, _viewModel,
                ExportXMLHelper.TimeFrame.years, getFolder(), fileName,
                true, true, whereExpQuery, whereBudQuery,
                _completed);
        boolean exportSuccessful = exportXMLHelper.export();
        if (exportSuccessful)
        {
            Log.e(TAG, "Finished exporting");
            Toast.makeText(this, "Finished exporting", Toast.LENGTH_LONG).show();
        }
        else
        {
            Log.e(TAG, "Failed exporting");
            Toast.makeText(this, "Exporting failed", Toast.LENGTH_LONG).show();
        }
    }

    private void makeCompletionToast()
    {
        Toast.makeText(this, "Database operation complete", Toast.LENGTH_LONG).show();
    }
}
