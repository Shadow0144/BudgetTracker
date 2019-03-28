package cc.corbin.budgettracker.importexport;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cc.corbin.budgettracker.R;
import cc.corbin.budgettracker.auxilliary.DatePickerFragment;
import cc.corbin.budgettracker.auxilliary.MonthPickerFragment;
import cc.corbin.budgettracker.budgetdatabase.BudgetDatabase;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.custom.CreateCustomViewActivity;
import cc.corbin.budgettracker.day.DayViewActivity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureDatabase;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.month.MonthViewActivity;
import cc.corbin.budgettracker.search.CreateSearchActivity;
import cc.corbin.budgettracker.settings.SettingsActivity;
import cc.corbin.budgettracker.total.TotalViewActivity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.year.YearViewActivity;
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

public class ImportExportActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private final String TAG = "ImportExportActivity";

    private DrawerLayout _drawerLayout;

    private final int CHECK_CODE = 0; // Just check and request and return
    private final int EXPORT_CODE = 1;
    private final int IMPORT_CODE = 2;

    private boolean _publicStorage = false;

    private ExpenditureViewModel _viewModel;
    private MutableLiveData<List<ExpenditureEntity>> _expenditures;
    private MutableLiveData<List<BudgetEntity>> _budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_export);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        _drawerLayout = findViewById(R.id.rootLayout);
        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);

        Calendar calendar = Calendar.getInstance();
        _viewModel = ViewModelProviders.of(this).get(ExpenditureViewModel.class);
        _viewModel.setDatabases(ExpenditureDatabase.getExpenditureDatabase(this), BudgetDatabase.getBudgetDatabase(this));
        _viewModel.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE));
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
        Intent intent;
        boolean handled = false;
        Calendar calendar = Calendar.getInstance();
        switch (item.getItemId())
        {
            case R.id.searchMenuItem:
                intent = new Intent(getApplicationContext(), CreateSearchActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.dayMenuItem:
                intent = new Intent(getApplicationContext(), DayViewActivity.class);
                Calendar date = Calendar.getInstance();
                date.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DATE));
                intent.putExtra(DayViewActivity.DATE_INTENT, date.getTimeInMillis());
                startActivity(intent);
                handled = true;
                break;
            case R.id.monthMenuItem:
                intent = new Intent(getApplicationContext(), MonthViewActivity.class);
                intent.putExtra(MonthViewActivity.YEAR_INTENT, calendar.get(Calendar.YEAR));
                intent.putExtra(MonthViewActivity.MONTH_INTENT, calendar.get(Calendar.MONTH)+1);
                startActivity(intent);
                handled = true;
                break;
            case R.id.yearMenuItem:
                intent = new Intent(getApplicationContext(), YearViewActivity.class);
                intent.putExtra(YearViewActivity.YEAR_INTENT, calendar.get(Calendar.YEAR));
                startActivity(intent);
                handled = true;
                break;
            case R.id.totalMenuItem:
                intent = new Intent(getApplicationContext(), TotalViewActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.customMenuItem:
                intent = new Intent(getApplicationContext(), CreateCustomViewActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.settingsMenuItem:
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                handled = true;
                break;
            case R.id.importExportMenuItem:
                intent = new Intent(getApplicationContext(), ImportExportActivity.class);
                startActivity(intent);
                handled = true;
                break;
        }

        if (handled)
        {
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
            case EXPORT_CODE:
                onExport(null);
                break;
            //case EXPORT_MONTH_CODE:
            //    exportMonth(0, 0); // TODO
            //    break;
        }
    }

    public void exportMonth(int month, int year)
    {
        //_month = month;
        //_year = year;
        //_expenditures = expenditures;

        if (!_publicStorage)
        {
            //checkPermissions(this, EXPORT_MONTH_CODE);
        }
        else
        {
            File sd = Environment.getExternalStorageDirectory();

            String csvFile;
            if (month < 10)
            {
                csvFile = "0" + month + "_" + year + "_" + "expenditures.xls";
            }
            else
            {
                csvFile = month + "_" + year + "_" + "expenditures.xls";
            }

            DateFormatSymbols dfs = new DateFormatSymbols();
            String monthName = dfs.getMonths()[month - 1];

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month-1);
            int count = calendar.getActualMaximum(Calendar.DATE);

            File directory = new File(sd.getAbsolutePath());
            if (!directory.isDirectory())
            {
                directory.mkdirs();
            }
            else { }

            try
            {
                File file = new File(directory, csvFile);
                WorkbookSettings wbSettings = new WorkbookSettings();
                wbSettings.setLocale(new Locale("en", "EN"));
                WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
                WritableSheet sheet = workbook.createSheet(monthName, 0);

                WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 10);
                WritableFont mainHeaderFont = new WritableFont(WritableFont.ARIAL, 10);
                WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10);
                WritableFont defaultFont = new WritableFont(WritableFont.ARIAL, 10);
                WritableFont superScriptFont = new WritableFont(WritableFont.ARIAL, 10);

                titleFont.setItalic(true);
                titleFont.setColour(Colour.WHITE);
                WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
                titleFormat.setBackground(Colour.BLACK);

                mainHeaderFont.setBoldStyle(WritableFont.BOLD);
                mainHeaderFont.setUnderlineStyle(UnderlineStyle.SINGLE);
                WritableCellFormat mainHeaderFormat = new WritableCellFormat(mainHeaderFont);
                mainHeaderFormat.setBackground(Colour.GRAY_80);

                headerFont.setBoldStyle(WritableFont.BOLD);
                WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
                mainHeaderFormat.setBackground(Colour.GRAY_50);

                WritableCellFormat defaultFormat = new WritableCellFormat(defaultFont);
                WritableCellFormat defaultFormatAlt = new WritableCellFormat(defaultFont);
                defaultFormatAlt.setBackground(Colour.GRAY_25);

                superScriptFont.setScriptStyle(ScriptStyle.SUPERSCRIPT);
                WritableCellFormat superScriptFormat = new WritableCellFormat(superScriptFont);
                WritableCellFormat superScriptFormatAlt = new WritableCellFormat(superScriptFont);
                superScriptFormatAlt.setBackground(Colour.GRAY_25);

                int r = 0;

                // Write the month header
                sheet.addCell(new Label(0, r, monthName, titleFormat));
                sheet.addCell(new Label(1, r, "", titleFormat));
                sheet.addCell(new Label(2, r, "", titleFormat));
                r++;

                Calendar date = Calendar.getInstance();
                Calendar expDate = Calendar.getInstance();
                SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy");
                int expCount = _expenditures.getValue().size();
                int currExp = 0;
                int note = 1;
                ArrayList<String> notes = new ArrayList<String>();
                for (int i = 0; i < count; i++)
                {
                    date.set(year, month - 1, i+1, 0, 0, 0);
                    date.set(Calendar.MILLISECOND, 0);

                    // Print the day, if expenditures are available for that day, print them
                    String dateString = simpleDate.format(date.getTime());
                    sheet.addCell(new Label(0, r, dateString, mainHeaderFormat));
                    sheet.addCell(new Label(1, r, "", mainHeaderFormat));
                    sheet.addCell(new Label(2, r, "", mainHeaderFormat));
                    r++;
                    sheet.addCell(new Label(0, r, "Cost", headerFormat));
                    sheet.addCell(new Label(1, r, "Category", headerFormat));
                    sheet.addCell(new Label(2, r, "*", headerFormat));
                    r++;

                    boolean alt = false;
                    do
                    {
                        if (currExp < expCount)
                        {
                            ExpenditureEntity exp = _expenditures.getValue().get(currExp);
                            expDate.set(exp.getYear(), exp.getMonth(), exp.getDay());
                            expDate.set(Calendar.MILLISECOND, 0);
                            if (date.compareTo(expDate) == 0)
                            {
                                Log.e(TAG, ""+expDate.getTimeInMillis());
                                WritableCellFormat format, superFormat;
                                if (alt)
                                {
                                    format = defaultFormat;
                                    superFormat = superScriptFormat;
                                }
                                else
                                {
                                    format = defaultFormatAlt;
                                    superFormat = superScriptFormatAlt;
                                }
                                alt = !alt;
                                sheet.addCell(new Label(0, r, "" + exp.getAmount(), format));
                                sheet.addCell(new Label(1, r, "" + exp.getCategoryName(), format));
                                if (exp.getNote().length() > 0)
                                {
                                    sheet.addCell(new Label(2, r, "" + note, superFormat));
                                    notes.add(exp.getNote());
                                    note++;
                                }
                                else
                                {
                                    sheet.addCell(new Label(2, r, "", superFormat));
                                }
                                r++;
                                currExp++;
                            }
                            else { }
                        }
                        else { }
                    }
                    while ((date.compareTo(expDate) == 0) && (currExp < expCount));
                } // End of main table

                int noteCount = notes.size();
                if (noteCount > 0)
                {
                    r++;

                    // Print the table headers
                    sheet.addCell(new Label(0, r, "Notes", mainHeaderFormat));
                    sheet.addCell(new Label(1, r, "", mainHeaderFormat));
                    r++;
                    sheet.addCell(new Label(0, r, "*", headerFormat));
                    sheet.addCell(new Label(1, r, "Note", headerFormat));
                    r++;

                    boolean alt = false;
                    WritableCellFormat format, superFormat;
                    for (int i = 0; i < noteCount; i++)
                    {
                        if (alt)
                        {
                            format = defaultFormat;
                            superFormat = superScriptFormat;
                        }
                        else
                        {
                            format = defaultFormatAlt;
                            superFormat = superScriptFormatAlt;
                        }
                        alt = !alt;

                        sheet.addCell(new Label(0, r, "" + (i+1), superFormat));
                        sheet.addCell(new Label(1, r, notes.get(i), format));
                        r++;
                    }
                }
                else { }

                workbook.write();
                workbook.close();

                Toast.makeText(this, "Successfully exported month", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Failed to export", Toast.LENGTH_SHORT).show();
            }

            _expenditures = null;
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

    public void onExport(View v)
    {
        if (!_publicStorage)
        {
            checkPermissions(this, EXPORT_CODE);
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
                Calendar calendar = Calendar.getInstance();
                String expFileName = "ExpenditureDatabase-" + calendar.get(Calendar.YEAR) + "-" +
                        (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + ".db";
                String budFileName = "BudgetDatabase-" + calendar.get(Calendar.YEAR) + "-" +
                        (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + ".db";

                exportAll(folder, expFileName, budFileName);
            }
        }
    }

    public void exportAll(String folder, String expFileName, String budFileName)
    {
        String expPath = getDatabasePath("expenditures").getAbsolutePath();
        String budPath = getDatabasePath("budgets").getAbsolutePath();

        File srcExp = new File(expPath);
        File srcBud = new File(budPath);
        File dstExp = new File(folder, expFileName);
        File dstBud = new File(folder, budFileName);

        boolean exportSucceeded = true;

        // Export the expenditures
        try (InputStream in = new FileInputStream(srcExp))
        {
            try (OutputStream out = new FileOutputStream(dstExp))
            {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            exportSucceeded = false;
        }

        // Export the budgets
        try (InputStream in = new FileInputStream(srcBud))
        {
            try (OutputStream out = new FileOutputStream(dstBud))
            {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            exportSucceeded = false;
        }

        // Report the success or failure
        if (exportSucceeded)
        {
            Toast.makeText(this, "Successfully exported to BudgetTracker/", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(this, "Export failed", Toast.LENGTH_LONG).show();
        }
    }

    public void test(View v)
    {
        MutableLiveData<Date> _monthDateLive = new MutableLiveData<Date>();

        final Observer<Date> monthDateObserver = new Observer<Date>()
        {
            @Override
            public void onChanged(@Nullable Date date)
            {
                Log.e(TAG, date.toString());
            }
        };
        _monthDateLive.observe(this, monthDateObserver);

        DialogFragment fragment = new MonthPickerFragment();
        ((MonthPickerFragment) fragment).setLiveData(_monthDateLive);
        fragment.show(getSupportFragmentManager(), "monthPicker");
    }
}
