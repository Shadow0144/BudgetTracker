package cc.corbin.budgettracker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.DateCell;
import jxl.LabelCell;
import jxl.Range;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.Font;
import jxl.format.ScriptStyle;
import jxl.format.UnderlineStyle;
import jxl.write.Colour;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Corbin on 2/27/2018.
 */

public class ExcelExporter
{
    private static final String TAG = "ExcelExporter";

    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    private static boolean _externalStorage = false;

    public static void checkPermissions(Activity activity)
    {
        if (activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            activity.requestPermissions(new String[] {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
        }
        else
        {
            _externalStorage = true;
        }
    }

    public static void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case WRITE_EXTERNAL_STORAGE_CODE:
            {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    _externalStorage = true;
                }
                else
                {
                    _externalStorage = false;
                }
                break;
            }
        }
    }

    public static void exportMonth(Context context, int month, int year, List<ExpenditureEntity> expenditures)
    {
        if (_externalStorage)
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
            calendar.set(Calendar.MONTH, month);
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
                int expCount = expenditures.size();
                int currExp = 0;
                int note = 1;
                ArrayList<String> notes = new ArrayList<String>();
                for (int i = 0; i < count; i++)
                {
                    date.set(year, month - 1, i, 0, 0, 0);
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
                            ExpenditureEntity exp = expenditures.get(currExp);
                            expDate.setTimeInMillis(exp.getDate());
                            expDate.set(Calendar.MILLISECOND, 0);
                            if (date.compareTo(expDate) == 0)
                            {
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
                                sheet.addCell(new Label(1, r, "" + exp.getExpenseType(), format));
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

                Toast.makeText(context, "Successfully exported month", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Toast.makeText(context, "Failed to export", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(context, "Requires permission to save externally", Toast.LENGTH_SHORT).show();
        }
    }
}
