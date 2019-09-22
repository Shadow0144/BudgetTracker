package cc.corbin.budgettracker.importexport;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import cc.corbin.budgettracker.auxilliary.Categories;
import cc.corbin.budgettracker.auxilliary.Currencies;
import cc.corbin.budgettracker.day.ExpenditureItem;
import cc.corbin.budgettracker.expendituredatabase.ExpenditureEntity;
import cc.corbin.budgettracker.workerthread.ExpenditureViewModel;

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
    private String _query;

    private Document _document;

    private final int TIME_ROW_START = 5; // B5
    private final int CAT_ROW_START_OFFSET = 5;
    private int _timeRows;

    private AssetManager _assetManager;
    private File _zipFile;
    private File _contentFile;

    private boolean _createExpenditureSheet;
    private boolean _createBudgetSheet;

    public ExportXMLHelper(Context context, ExpenditureViewModel viewModel, TimeFrame timeFrame,
                           String folder, String fileName, String query)
    {
        _context = context;
        _viewModel = viewModel;
        _timeFrame = timeFrame;
        _folder = folder;
        _fileName = fileName;
        _query = query;
        _createExpenditureSheet = true;
        _createBudgetSheet = false;
    }

    // Time summary table row
    /*
        <table:table-row table:style-name="ro2">
            <table:table-cell office:value-type="float" office:value="2018"><text:p>2018</text:p></table:table-cell>
            <table:table-cell table:number-columns-repeated="2" office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
            <table:table-cell table:formula="of:=[.B5]-[.C5]" office:value-type="currency" office:currency="USD" office:value="0"><text:p>$0.00</text:p></table:table-cell>
        </table:table-row>
     */

    // Category summary table row
    /*
        <table:table-row table:style-name="ro2">
            <table:table-cell office:value-type="string"><text:p>Food</text:p></table:table-cell>
            <table:table-cell table:number-columns-repeated="2" office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
            <table:table-cell table:formula="of:=[.B11]-[.C11]" office:value-type="currency" office:currency="USD" office:value="0"><text:p>$0.00</text:p></table:table-cell>
        </table:table-row>
     */

    // Expenditures table
    /*
        <!-- 2018 -->
            <table:table-row table:style-name="ro2">
                <table:table-cell table:style-name="ce8" office:value-type="float" office:value="2018" table:number-columns-spanned="5" table:number-rows-spanned="1"><text:p>2018</text:p></table:table-cell>
                    <table:covered-table-cell table:number-columns-repeated="4" table:style-name="ce7"/>
                </table:table-row>
                <table:table-row table:style-name="ro2">
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Date</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Amount</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Conversion</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Category</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Note</text:p></table:table-cell>
                </table:table-row>
        <!-- 2018 content -->
            <table:table-row table:style-name="ro2"><table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell><table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell><table:table-cell/><table:table-cell office:value-type="string"><text:p>Food</text:p></table:table-cell><table:table-cell/></table:table-row><table:table-row table:style-name="ro2"><table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell><table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell><table:table-cell table:style-name="ce11" office:value-type="string"><text:p>₩1 @ 1000.00</text:p></table:table-cell><table:table-cell office:value-type="string"><text:p>Travel</text:p></table:table-cell><table:table-cell office:value-type="string"><text:p>This is a note</text:p></table:table-cell></table:table-row><table:table-row table:style-name="ro2"><table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell><table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell><table:table-cell/><table:table-cell office:value-type="string"><text:p>Travel</text:p></table:table-cell><table:table-cell/></table:table-row><table:table-row table:style-name="ro2"><table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell><table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell><table:table-cell/><table:table-cell office:value-type="string"><text:p>Other</text:p></table:table-cell><table:table-cell/></table:table-row><table:table-row table:style-name="ro2"><table:table-cell table:style-name="Default" table:number-columns-repeated="5"/></table:table-row>
    */

    // Budget table
    /*
        <!-- 2018 -->
            <table:table-row table:style-name="ro2">
                <table:table-cell table:style-name="ce12" office:value-type="float" office:value="2018" table:number-columns-spanned="4" table:number-rows-spanned="1"><text:p>2018</text:p></table:table-cell>
                    <table:covered-table-cell table:style-name="ce5"/><table:covered-table-cell table:number-columns-repeated="2"/><table:table-cell/>
                </table:table-row>
                <table:table-row table:style-name="ro2">
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Date</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Amount</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Category</text:p></table:table-cell>
                    <table:table-cell table:style-name="ce3" office:value-type="string"><text:p>Note</text:p></table:table-cell><table:table-cell/>
                </table:table-row>
        <!-- 2018 content -->
            <table:table-row table:style-name="ro2">
                <table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell>
                <table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
                <table:table-cell office:value-type="string"><text:p>Food</text:p></table:table-cell>
                <table:table-cell table:number-columns-repeated="2"/>
            </table:table-row>
            <table:table-row table:style-name="ro2">
                <table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell>
                <table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
                <table:table-cell office:value-type="string"><text:p>Travel</text:p></table:table-cell>
                <table:table-cell office:value-type="string"><text:p>This is a note</text:p></table:table-cell><table:table-cell/>
            </table:table-row>
            <table:table-row table:style-name="ro2">
                <table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell>
                <table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
                <table:table-cell office:value-type="string"><text:p>Travel</text:p></table:table-cell>
                <table:table-cell table:number-columns-repeated="2"/>
            </table:table-row>
            <table:table-row table:style-name="ro2">
                <table:table-cell office:value-type="date" office:date-value="2018-01-01"><text:p>01/01/18</text:p></table:table-cell>
                <table:table-cell office:value-type="currency" office:currency="USD" office:value="1"><text:p>$1.00</text:p></table:table-cell>
                <table:table-cell office:value-type="string"><text:p>Other</text:p></table:table-cell>
                <table:table-cell table:number-columns-repeated="2"/>
            </table:table-row>
            <table:table-row table:style-name="ro2">
                <table:table-cell table:style-name="Default" table:number-columns-repeated="4"/><table:table-cell/>
            </table:table-row>
     */

    public boolean export()
    {
        boolean succeeded = false;

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
                closeExpendituresSheet();
            }
            else { }
            if (_createBudgetSheet)
            {

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
        Element spreadSheetElement = _document.getElementById("bt_spreadsheet");
        Element postElement = _document.getElementById("expenditures_sheet_anchor");

        Node expendituresSheetNode = _document.createDocumentFragment();

        // "\n\n<!-- Expenditures Sheet -->\n\n"
        Comment comment = _document.createComment(" Expenditures Sheet ");
        expendituresSheetNode.appendChild(comment);
        expendituresSheetNode.appendChild(_document.createTextNode("\n\n"));

        // "<table:table table:name=\"Expenditures\" table:style-name=\"ta1\" table:print=\"false\">
        Element tableElement = _document.createElement("table:table");
        tableElement.setAttribute("table:name", "Expenditures");
        tableElement.setAttribute("table:style-name", "ta1");
        tableElement.setAttribute("table:print", "false");
        expendituresSheetNode.appendChild(tableElement);

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
        // "<table:table-column table:style-name=\"co1\" table:number-columns-repeated=\"3\" table:default-cell-style-name=\"ce5\"/>"
        columnElement = _document.createElement("table:table-column");
        columnElement.setAttribute("table:style-name", "co1");
        columnElement.setAttribute("table:number-columns-repeated", "3");
        columnElement.setAttribute("table:default-cell-style-name", "ce5");
        tableElement.appendChild(columnElement);
        // "<table:table-row table:style-name=\"ro1\">
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro1");
        tableElement.appendChild(rowElement);
        // <table:table-cell table:style-name=\"ce6\" office:value-type=\"string\" table:number-columns-spanned=\"5\" table:number-rows-spanned=\"1\">"
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:default-cell-style-name", "ce6");
        cellElement.setAttribute("office:value-type", "string");
        cellElement.setAttribute("table:number-columns-spanned", "5");
        cellElement.setAttribute("table:number-rows-spanned", "1");
        rowElement.appendChild(cellElement);
        // "<text:p>Total Expenditures</text:p>"
        Element textElement = _document.createElement("text:p");
        textElement.setTextContent("Total Expenditures");
        cellElement.appendChild(textElement);
        // "</table:table-cell>"
        // "<table:covered-table-cell table:number-columns-repeated=\"5\" table:style-name=\"ce10\"/>"
        Element coveredCell = _document.createElement("table:covered-table-cell");
        coveredCell.setAttribute("table:number-columns-repeated", "5"); // TODO - Border not reaching
        coveredCell.setAttribute("table:style-name", "ce10");
        rowElement.appendChild(coveredCell);
        // "</table:table-row>"
        // "<table:table-row table:style-name=\"ro2\">"
        rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);
        // "<table:table-cell table:style-name=\"ce7\" table:number-columns-repeated=\"5\"/>
        cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:style-name", "ce7");
        cellElement.setAttribute("table:number-columns-repeated", "5");
        rowElement.appendChild(cellElement);
        // </table:table-row>
        // \n\n
        tableElement.appendChild(_document.createTextNode("\n\n"));

        int years = 2; // TODO - Temporary!
        ExpenditureEntity temp = new ExpenditureEntity();
        temp.setDay(Calendar.getInstance().get(Calendar.DATE));
        temp.setBaseAmount(1.0f);
        temp.setCategory(0, Categories.getCategories()[0]);
        temp.setNote("Temporary");
        for (int i = 0; i < years; i++)
        {
            addExpendituresTableHeader(tableElement, (""+(i+2018)));
            addExpenditureRow(tableElement, temp);
            addExpenditureRow(tableElement, temp);
            addExpenditureRow(tableElement, temp);
            addEmptyRow(tableElement);
            tableElement.appendChild(_document.createTextNode("\n\n"));
        }

        spreadSheetElement.insertBefore(expendituresSheetNode, postElement);
    }

    private void addExpendituresTableHeader(Element tableElement, String tableName)
    {
        // "<!-- " + tableName + " -->\n"
        Comment comment = _document.createComment(" " + tableName + " ");
        tableElement.appendChild(comment);
        tableElement.appendChild(_document.createTextNode("\n"));

        // "<table:table-row table:style-name=\"ro2\">
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell table:style-name=\"ce8\" office:value-type=\"float\" office:value=\"">
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:number-columns-spanned", "5");
        cellElement.setAttribute("table:style-name", "ce8");
        cellElement.setAttribute("office:value-type", "float");
        cellElement.setAttribute("office:value", tableName);
        //cellElement.setTextContent(tableName);
        rowElement.appendChild(cellElement);
        // </table:table-cell>

        // "<table:covered-table-cell table:number-columns-repeated=\"5\" table:style-name=\"ce7\"/>"
        Element coveredCellElement = _document.createElement("covered-table-cell");
        coveredCellElement.setAttribute("table:number-columns-repeated", "5");
        coveredCellElement.setAttribute("table:style-name", "ce7");
        rowElement.appendChild(coveredCellElement);
        // "</table:table-row>"

        // "<table:table-row table:style-name=\"ro2\">"
        rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        rowElement.appendChild(createHeaderCell("Date"));
        rowElement.appendChild(createHeaderCell("Amount"));
        rowElement.appendChild(createHeaderCell("Conversion"));
        rowElement.appendChild(createHeaderCell("Category"));
        rowElement.appendChild(createHeaderCell("Note"));
        //"</table:table-row>"
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

        // "<table:table-cell office:value-type=\"date\" office:date-value=\"" + entity.getDay()
        rowElement.appendChild(createExpenditureCell("date", "office:date-value", (""+entity.getDay())));
        // "<table:table-cell office:value-type=\"currency\" office:currency=\"USD\" office:value=\"\">" + entity.getAmount()
        Element amountElement = createExpenditureCell("currency", "office:value", (""+entity.getBaseAmount()));
        amountElement.setAttribute("office:currency", "USD"); // TODO Fix currency
        rowElement.appendChild(amountElement);
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + conversion
        rowElement.appendChild(createExpenditureCell("string", "", (conversion)));
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getCategoryName()
        rowElement.appendChild(createExpenditureCell("string", "", (entity.getCategoryName())));
        // "<table:table-cell/><table:table-cell office:value-type=\"string\">" + entity.getNote()
        rowElement.appendChild(createExpenditureCell("string", "", (entity.getNote())));
        // "</table:table-row>"
    }

    // TODO - Cleanup
    private Element createExpenditureCell(String valueType, String valueTypeParam, String text)
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

    private void addEmptyRow(Element tableElement)
    {
        // "<table:table-row table:style-name=\"ro2\">"
        Element rowElement = _document.createElement("table:table-row");
        rowElement.setAttribute("table:style-name", "ro2");
        tableElement.appendChild(rowElement);

        // "<table:table-cell table:style-name=\"Default\" table:number-columns-repeated=\"5\"/>"
        Element cellElement = _document.createElement("table:table-cell");
        cellElement.setAttribute("table:style-name", "Default");
        cellElement.setAttribute("table:number-columns-repeated", "5");
        rowElement.appendChild(cellElement);
        // "</table:table-row>"
    }

    private void closeExpendituresSheet()
    {
        //Element rootElement = _document.getElementById("expenditures_sheet");

        //Element closingElement = _document.createElement("/table:table");
        //_document.insertBefore(closingElement, rootElement);
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
