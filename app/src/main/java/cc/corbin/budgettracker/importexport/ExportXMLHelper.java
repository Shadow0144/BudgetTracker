package cc.corbin.budgettracker.importexport;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask;
import cc.corbin.budgettracker.budgetdatabase.BudgetEntity;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;
import cc.corbin.budgettracker.auxilliary.SummationAsyncTask.SummationResult;

public class ExportXMLHelper
{
    private final String TAG = "ExportXMLHelper";

    public enum TimeFrame
    {
        day,
        days,
        weeks,
        months,
        years
    }

    private TimeFrame _timeFrame;

    private Context _context;

    private ExpenditureViewModel _viewModel;

    private String _folder;
    private String _fileName;

    private String _expQuery;
    private String _budQuery;

    private boolean _expLoaded;
    private boolean _budLoaded;

    private MutableLiveData<List<ExpenditureEntity>> _expenditureEntities;
    private List<ExpenditureEntity> _expenditures;
    private MutableLiveData<List<BudgetEntity>> _budgetEntities;
    private List<BudgetEntity> _budgets;

    private MutableLiveData<SummationResult[]> _timeSummationResults;
    private MutableLiveData<SummationResult[]> _timeAmounts;
    private MutableLiveData<SummationResult[]> _categoryAmounts;

    private Document _document;

    private final int TIME_ROW_START = 5; // B5
    private final int CAT_ROW_START_OFFSET = 5;
    private int _timeRows;

    private AssetManager _assetManager;
    private File _zipFile;
    private File _contentFile;

    private boolean _createExpenditureSheet;
    private boolean _createBudgetSheet;

    private MutableLiveData<Boolean> _complete;

    public ExportXMLHelper(Context context, ExpenditureViewModel viewModel, TimeFrame timeFrame,
                           String folder, String fileName, boolean exportExps, boolean exportBuds,
                           String expQuery, String budQuery, MutableLiveData<Boolean> complete)
    {
        _context = context;
        _viewModel = viewModel;
        _timeFrame = timeFrame;
        _folder = folder;
        _fileName = fileName;
        _createExpenditureSheet = exportExps;
        _createBudgetSheet = exportBuds;
        _expQuery = expQuery;
        _budQuery = budQuery;
        _expLoaded = false;
        _budLoaded = false;
        _complete = complete;
        _complete.postValue(false);
    }

    public boolean export()
    {
        boolean succeeded = true;

        _expenditureEntities = new MutableLiveData<List<ExpenditureEntity>>();
        final Observer<List<ExpenditureEntity>> expEntitiesObserver = new Observer<List<ExpenditureEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<ExpenditureEntity> expenditureEntities)
            {
                // Null check in the method
                onLoadExpenses(expenditureEntities);
            }
        };
        _expenditureEntities.observe((FragmentActivity) _context, expEntitiesObserver);

        _budgetEntities = new MutableLiveData<List<BudgetEntity>>();
        final Observer<List<BudgetEntity>> budEntitiesObserver = new Observer<List<BudgetEntity>>()
        {
            @Override
            public void onChanged(@Nullable List<BudgetEntity> budgetEntities)
            {
                // Null check in the method
                onLoadBudgets(budgetEntities);
            }
        };
        _budgetEntities.observe((FragmentActivity) _context, budEntitiesObserver);

        _viewModel.customExpenditureQuery(_expenditureEntities, _expQuery);
        _viewModel.customBudgetQuery(_budgetEntities, _budQuery);

        return succeeded;
    }

    private void onLoadExpenses(List<ExpenditureEntity> expenditureEntities)
    {
        _expenditures = expenditureEntities;

        _timeSummationResults = new MutableLiveData<SummationAsyncTask.SummationResult[]>();
        final Observer<SummationAsyncTask.SummationResult[]> timeEntitiesObserver =
                new Observer<SummationAsyncTask.SummationResult[]>()
                {
                    @Override
                    public void onChanged(@Nullable SummationAsyncTask.SummationResult[] timeEntities)
                    {
                        // Null check in the method
                        onLoadTimeSummaries(timeEntities);
                    }
                };
        _timeAmounts = new MutableLiveData<SummationResult[]>();
        _categoryAmounts = new MutableLiveData<SummationResult[]>();
        SummationAsyncTask yearlyAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.yearly, _timeSummationResults);
        SummationAsyncTask categoricalAsyncTask = new SummationAsyncTask(SummationAsyncTask.SummationType.categorically, _categoryAmounts);
        yearlyAsyncTask.execute(expenditureEntities);
        categoricalAsyncTask.execute(expenditureEntities);
    }

    private void onLoadTimeSummaries(SummationAsyncTask.SummationResult[] timeEntities)
    {
        _expLoaded = true;
        if (_expLoaded && _budLoaded)
        {
            exportThreaded();
        }
        else { }
    }

    private void onLoadBudgets(List<BudgetEntity> budgetEntities)
    {
        _budgets = budgetEntities;
        _budLoaded = true;

        if (_expLoaded && _budLoaded)
        {
            exportThreaded();
        }
        else { }
    }

    private boolean exportThreaded()
    {
        boolean succeeded = false;

        Log.e(TAG, "Queries complete");

        try
        {
            copyAssets();
            openFile();
            setupSheetName();
            setupSummaryHeaders();
            setupTimeSummaryTable();
            setupCategorySummaryTable();
            if (_createExpenditureSheet)
            {
                addExpendituresSheet();
            }
            else { }
            if (_createBudgetSheet)
            {
                addBudgetsSheet();
            }
            else { }
            closeFile();
            createODS();

            succeeded = true;
        }
        catch (DOMException domE)
        {
            Log.e(TAG, ""+domE.code);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }

        _complete.postValue(true);

        Log.e(TAG, "Export complete");

        return succeeded;
    }

    private void copyAssets()
    {
        _assetManager = _context.getAssets();

        String zipFileName = "budget_tracker_template.zip";
        String contentFileName = "content.xml";

        _zipFile = copyFile(zipFileName);
        _contentFile = copyFile(contentFileName);

    }

    private File copyFile(String filename)
    {
        InputStream in = null;
        OutputStream out = null;
        File outFile = null;

        try
        {
            in = _assetManager.open(filename);
            outFile = new File(_folder, filename);
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, read);
            }
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e) { }
            }
            else { }
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e) { }
            }
            else { }
        }

        return outFile;
    }

    private void openFile()
    {
        try
        {
            // Open the file
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            _document = docBuilder.parse(_contentFile);

        }
        catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void setupSheetName()
    {
        // Update table:name attribute
        Element firstSheet = _document.getElementById("summary_sheet");
        firstSheet.setAttribute("table:name", "Test");
    }

    private void setupSummaryHeaders()
    {
        Node title = _document.getElementById("summary_title_text");
        title.setTextContent("Summary Tables");

        Node timeSummaryHeader = _document.getElementById("time_summary_header_text");
        timeSummaryHeader.setTextContent("Expenditures by Year");

        // Already set
        //Node categorySummaryHeader = _document.getElementById("category_summary_header_text");
        //categorySummaryHeader.setTextContent("Expenditures by Category");
    }

    private void setupTimeSummaryTable()
    {
        _timeRows = 0;
        String postElementID = "time_summary_total_row";
        switch (_timeFrame)
        {
            case day:

                break;
            case days:

                break;
            case weeks:

                break;
            case months:

                break;
            case years:
                makeSummaryTableRow(postElementID, "2019", 1.0f, 1.0f);
                _timeRows++;
                break;
        }

        Element totalExpCell = _document.getElementById("time_total_exp_cell");
        Element totalBudCell = _document.getElementById("time_total_bud_cell");
        Element totalRemCell = _document.getElementById("time_total_rem_cell");
        if (_timeRows > 0)
        {
            totalExpCell.setAttribute("table:formula",
                    "of:=SUM([.B" + TIME_ROW_START + ":.B" + (TIME_ROW_START + _timeRows - 1) + "])");
            totalBudCell.setAttribute("table:formula",
                    "of:=SUM([.C" + TIME_ROW_START + ":.C" + (TIME_ROW_START + _timeRows - 1) + "])");
            totalRemCell.setAttribute("table:formula",
                    "of:=SUM([.D" + TIME_ROW_START + ":.D" + (TIME_ROW_START + _timeRows - 1) + "])");
        }
        else
        {
            totalExpCell.setAttribute("table:formula", "of:=0");
            totalBudCell.setAttribute("table:formula", "of:=0");
            totalRemCell.setAttribute("table:formula", "of:=0");
        }
    }

    private void makeSummaryTableRow(String postElementID, String label, float amount, float budget)
    {
        Element summarySheet = _document.getElementById("summary_sheet");
        Element postElement = _document.getElementById(postElementID);

        // Append a new row
        Element row = _document.createElement("table:table-row");
        row.setAttribute("table:style-name", "ro2");

        // Label
        Element tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "string");
        tableCell.setAttribute("office:value", label);
        Element textCell = _document.createElement("text:p");
        textCell.setTextContent(label);
        tableCell.appendChild(textCell);
        row.appendChild(tableCell);

        // Amount
        tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "float");
        String amountString = Currencies.formatCurrency(Currencies.default_currency, amount);
        tableCell.setAttribute("office:value", ""+amount);
        row.appendChild(tableCell);

        // Budget
        tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "float");
        String budgetString = Currencies.formatCurrency(Currencies.default_currency, budget);
        tableCell.setAttribute("office:value", ""+budget);
        row.appendChild(tableCell);

        // Remaining
        tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "float");
        float remaining = budget - amount;
        String remainingString = Currencies.formatCurrency(Currencies.default_currency, remaining);
        tableCell.setAttribute("office:value", ""+remaining);
        row.appendChild(tableCell);

        summarySheet.insertBefore(row, postElement);
    }

    private void setupCategorySummaryTable()
    {
        String postElementID = "category_summary_total_row";
        String[] categories = Categories.getCategories();
        for (int i = 0; i < categories.length; i++)
        {
            makeSummaryTableRow(postElementID, categories[i], 1.0f, 1.0f);
        }

        // There will always be at least one category
        Element totalExpCell = _document.getElementById("category_total_exp_cell");
        Element totalBudCell = _document.getElementById("category_total_bud_cell");
        Element totalRemCell = _document.getElementById("category_total_rem_cell");
        int start = TIME_ROW_START + _timeRows + CAT_ROW_START_OFFSET - 1;
        int end = start + categories.length - 1;
        totalExpCell.setAttribute("table:formula",
                "of:=SUM([.B" + start + ":.B" + end + "])");
        totalBudCell.setAttribute("table:formula",
                "of:=SUM([.C" + start + ":.C" + end + "])");
        totalRemCell.setAttribute("table:formula",
                "of:=SUM([.D" + start + ":.D" + end + "])");
    }

    private void addExpendituresSheet()
    {
        Element postElement = _document.getElementById("expenditures_sheet_anchor");
        String commentString = " Expenditures Sheet ";
        String sheetName = "Expenditures";
        String headerString = "Total Expenditures";

        int colSpan = 5;
        Element tableElement = createSheetNode(postElement, commentString, sheetName, headerString, colSpan);

        String[] headers = { "Date", "Amount", "Conversion", "Category", "Note" };

        int year = 0; // TODO Temporary - use different variables for different time frames
        int count = _expenditures.size();
        for (int i = 0; i < count; i++)
        {
            ExpenditureEntity entity = _expenditures.get(i);
            if (year < entity.getYear())
            {
                if (year != 0)
                {
                    addEmptyRow(tableElement, colSpan);
                }
                else { }
                addTableTitleAndHeaders(tableElement, (""+entity.getYear()), headers);
                year = entity.getYear();
            }
            else { }
            addExpenditureRow(tableElement, entity);
        }
    }

    private void addBudgetsSheet()
    {
        Element postElement = _document.getElementById("budgets_sheet_anchor");
        String commentString = " Budgets Sheet ";
        String sheetName = "Budgets";
        String headerString = "Total Budgets";

        int colSpan = 4;
        Element tableElement = createSheetNode(postElement, commentString, sheetName, headerString, colSpan);

        String[] headers = { "Date", "Amount", "Category", "Note" };

        int year = 0; // TODO Temporary - use different variables for different time frames
        int count = _budgets.size();
        for (int i = 0; i < count; i++)
        {
            BudgetEntity entity = _budgets.get(i);
            if (year < entity.getYear())
            {
                if (year != 0)
                {
                    addEmptyRow(tableElement, colSpan);
                }
                else { }
                addTableTitleAndHeaders(tableElement, (""+entity.getYear()), headers);
                year = entity.getYear();
            }
            else { }
            addBudgetRow(tableElement, entity);
        }
    }

    // Returns the sheet node (table element) and adds it to the spreadsheet
    private Element createSheetNode(Element postElement, String commentString, String sheetName, String headerString, int colSpan)
    {
        Element spreadSheetElement = _document.getElementById("bt_spreadsheet");

        Node sheetNode = _document.createDocumentFragment();

        // "\n\n<!--{commentString}-->\n\n"
        Comment comment = _document.createComment(commentString);
        sheetNode.appendChild(comment);
        sheetNode.appendChild(_document.createTextNode("\n\n"));

        // "<table:table table:name=\"{sheetName}\" table:style-name=\"ta1\" table:print=\"false\">
        Element tableElement = _document.createElement("table:table");
        tableElement.setAttribute("table:name", sheetName);
        tableElement.setAttribute("table:style-name", "ta1");
        tableElement.setAttribute("table:print", "false");
        sheetNode.appendChild(tableElement);

        // "<table:table-column table:style-name=\"co1\" table:default-cell-style-name=\"ce9\"/>\n\n"
        Element columnElement = _document.createElement("table:table-column");
        columnElement.setAttribute("table:style-name", "co1");
        columnElement.setAttribute("table:default-cell-style-name", "ce9");
        tableElement.appendChild(columnElement);
        tableElement.appendChild(_document.createTextNode("\n\n"));
        // "<!-- Header -->\n"
        tableElement.appendChild(_document.createComment(" Header "));
        tableElement.appendChild(_document.createTextNode("\n"));

        // "<table:table-column table:style-name=\"co1\" table:default-cell-style-name=\"ce4\"/>"
        columnElement = _document.createElement("table:table-column");
        columnElement.setAttribute("table:style-name", "co1");
        columnElement.setAttribute("table:default-cell-style-name", "ce4");
        tableElement.appendChild(columnElement);
        // "<table:table-column table:style-name=\"co1\" table:number-columns-repeated=\"{colSpan}\" table:default-cell-style-name=\"ce5\"/>"
        columnElement = _document.createElement("table:table-column");
        columnElement.setAttribute("table:style-name", "co1");
        columnElement.setAttribute("table:number-columns-repeated", ""+colSpan);
        columnElement.setAttribute("table:default-cell-style-name", "ce5");
        tableElement.appendChild(columnElement);
        // "<table:table-row table:style-name=\"ro1\">
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro1");
        tableElement.appendChild(rowElement);
        // <table:table-cell table:style-name=\"ce6\" office:value-type=\"string\" table:number-columns-spanned=\"{colSpan}\" table:number-rows-spanned=\"1\">"
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:default-cell-style-name", "ce6");
        cellElement.setAttribute("table:style-name", "ce1");
        cellElement.setAttribute("office:value-type", "string");
        cellElement.setAttribute("table:number-columns-spanned", ""+colSpan);
        cellElement.setAttribute("table:number-rows-spanned", "1");
        rowElement.appendChild(cellElement);
        // "<text:p>{headerString}</text:p>"
        Element textElement = _document.createElement("text:p");
        textElement.setTextContent(headerString);
        cellElement.appendChild(textElement);
        // "</table:table-cell>"
        // "<table:covered-table-cell table:number-columns-repeated=\"{colSpan}\" table:style-name=\"ce10\"/>"
        Element coveredCell = _document.createElement("table:covered-table-cell");
        coveredCell.setAttribute("table:number-columns-repeated", ""+colSpan);
        coveredCell.setAttribute("table:style-name", "ce10");
        //rowElement.appendChild(coveredCell);
        // "</table:table-row>"
        // "<table:table-row table:style-name=\"ro2\">"
        rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);
        // "<table:table-cell table:style-name=\"ce7\" table:number-columns-repeated=\"{colSpan}\"/>
        cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:style-name", "ce7");
        cellElement.setAttribute("table:number-columns-repeated", ""+colSpan);
        rowElement.appendChild(cellElement);
        // </table:table-row>
        // \n\n
        tableElement.appendChild(_document.createTextNode("\n\n"));

        spreadSheetElement.insertBefore(sheetNode, postElement);

        return tableElement;
    }

    private void addTableTitleAndHeaders(Element tableElement, String tableName, String[] headers)
    {
        int colSpan = headers.length;

        // "<!-- " + tableName + " -->\n"
        Comment comment = _document.createComment(" " + tableName + " ");
        tableElement.appendChild(comment);
        tableElement.appendChild(_document.createTextNode("\n"));

        // "<table:table-row table:style-name=\"ro2\">
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell table:style-name=\"ce2\" office:value-type=\"float\" office:value=\"">
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:number-columns-spanned", ""+colSpan);
        cellElement.setAttribute("table:number-rows-spanned", "1");
        cellElement.setAttribute("table:style-name", "ce2");
        cellElement.setAttribute("office:value-type", "string");
        rowElement.appendChild(cellElement);

        // "<text:p>{tableName}</text:p>"
        Element textElement = _document.createElement("text:p");
        textElement.setTextContent(tableName); // TODO: Format
        cellElement.appendChild(textElement);
        // "</table:table-cell>"

        // "<table:covered-table-cell table:number-columns-repeated=\"{colSpan}\" table:style-name=\"ce2\"/>"
        Element coveredCellElement = _document.createElement("covered-table-cell");
        coveredCellElement.setAttribute("table:number-columns-repeated", ""+colSpan);
        coveredCellElement.setAttribute("table:style-name", "ce2");
        rowElement.appendChild(coveredCellElement);
        // "</table:table-row>"

        // "<table:table-row table:style-name=\"ro2\">"
        rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // Add the headers
        for (int i = 0; i < colSpan; i++)
        {
            rowElement.appendChild(createHeaderCell(headers[i]));
        }
        //"</table:table-row>"

        // Whitespace
        tableElement.appendChild(_document.createTextNode("\n\n"));
    }

    private Element createHeaderCell(String text)
    {
        // "<table:table-cell table:style-name=\"ce3\" office:value-type=\"string\">
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:style-name", "ce3");
        cellElement.setAttribute("office:value-type", "string");
        //rowElement.appendChild(cellElement); // Handled after returning
        // <text:p>{text}</text:p>
        Element textElement = _document.createElement("text:p");
        textElement.setTextContent(text);
        cellElement.appendChild(textElement);
        // </table:table-cell>"
        return cellElement;
    }

    private void addExpenditureRow(Element tableElement, ExpenditureEntity entity)
    {
        // TODO: Currency and conversion
        String conversion = "($1 @ " + entity.getConversionRate() + ")";

        // "<table:table-row table:style-name=\"ro2\">"
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell office:value-type=\"date\" office:date-value=\"" + entity.getDay() // TODO Fix
        rowElement.appendChild(createCell("date", "office:date-value", (""+entity.getDay())));
        // "<table:table-cell office:value-type=\"currency\" office:currency=\"USD\" office:value=\"\">" + entity.getAmount()
        Element amountElement = createCell("currency", "office:value", (""+entity.getBaseAmount()));
        amountElement.setAttribute("office:currency", "USD"); // TODO Fix currency
        rowElement.appendChild(amountElement);
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + conversion
        rowElement.appendChild(createCell("string", "", (conversion)));
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getCategoryName()
        rowElement.appendChild(createCell("string", "", (entity.getCategoryName())));
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getNote()
        rowElement.appendChild(createCell("string", "", (entity.getNote())));
        // "</table:table-row>"

        // Whitespace
        tableElement.appendChild(_document.createTextNode("\n\n"));
    }

    private void addBudgetRow(Element tableElement, BudgetEntity entity)
    {
        // "<table:table-row table:style-name=\"ro2\">"
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell office:value-type=\"date\" office:date-value=\"" + entity.getDay() // TODO Fix
        rowElement.appendChild(createCell("date", "office:date-value", (""+entity.getMonth())));
        // "<table:table-cell office:value-type=\"currency\" office:currency=\"USD\" office:value=\"\">" + entity.getAmount()
        Element amountElement = createCell("currency", "office:value", (""+entity.getAmount()));
        amountElement.setAttribute("office:currency", "USD"); // TODO Fix currency
        rowElement.appendChild(amountElement);
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getCategoryName()
        rowElement.appendChild(createCell("string", "", (entity.getCategoryName())));
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getNote()
        rowElement.appendChild(createCell("string", "", (entity.getNote())));
        // "</table:table-row>"

        // Whitespace
        tableElement.appendChild(_document.createTextNode("\n\n"));
    }

    // TODO - Cleanup
    private Element createCell(String valueType, String valueTypeParam, String text)
    {
        // "<table:table-cell office:value-type=\"{valueType}\" {valueTypeParam}=\"{text}\"\>"
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("office:value-type", valueType);
        if (valueTypeParam.length() > 0)
        {
            cellElement.setAttribute(valueTypeParam, text);
        }
        else { }

        // <text:p></text:p>"
        Element textCell = _document.createElement("text:p");
        if (!(valueTypeParam.length() > 0))
        {
            textCell.setTextContent(text);
        }
        else { }
        cellElement.appendChild(textCell);
        // "</table:table-cell>"

        return cellElement;
    }

    private void addEmptyRow(Element tableElement, int colSpan)
    {
        // "<table:table-row table:style-name=\"ro2\">"
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell table:style-name=\"Default\" table:number-columns-repeated=\"{colSpan}\"/>"
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:style-name", "Default");
        cellElement.setAttribute("table:number-columns-repeated", ""+colSpan);
        rowElement.appendChild(cellElement);
        // "</table:table-row>"

        // Whitespace
        tableElement.appendChild(_document.createTextNode("\n\n"));
    }

    private void closeFile()
    {
        try
        {
            // Write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(_document);
            StreamResult result = new StreamResult(_contentFile);
            transformer.transform(source, result);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    private void createODS()
    {
        try
        {
            ZipFile template = new ZipFile(_zipFile);
            ZipOutputStream append = new ZipOutputStream(new FileOutputStream(_folder + _fileName + ".ods"));

            Enumeration<? extends ZipEntry> entries = template.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry zipEntry = entries.nextElement();
                ZipEntry outEntry = new ZipEntry(zipEntry.getName());
                append.putNextEntry(outEntry);
                if (!zipEntry.isDirectory())
                {
                    copyFileContents(template.getInputStream(outEntry), append);
                }
                else { }
                append.closeEntry();
            }

            ZipEntry e = new ZipEntry(_contentFile.getAbsolutePath());
            FileInputStream contentStream = new FileInputStream(e.getName());
            append.putNextEntry(e);
            copyFileContents(contentStream, append);
            append.closeEntry();

            template.close();
            append.close();

            // Delete the temporary files
            _zipFile.delete();
            //_contentFile.delete();
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    public void copyFileContents(InputStream input, OutputStream output) throws IOException
    {
        int bytesRead;
        byte[] buffer = new byte[1024];
        while ((bytesRead = input.read(buffer))!= -1)
        {
            output.write(buffer, 0, bytesRead);
        }
    }
}
