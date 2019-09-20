package cc.corbin.budgettracker.importexport;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import cc.corbin.budgettracker.auxilliary.Currencies;
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
    private final int CAT_ROW_START_OFFSET = 3;
    private int _timeRows;

    private AssetManager _assetManager;
    private File _zipFile;
    private File _contentFile;

    public ExportXMLHelper(Context context, ExpenditureViewModel viewModel, TimeFrame timeFrame,
                           String folder, String fileName, String query)
    {
        _context = context;
        _viewModel = viewModel;
        _timeFrame = timeFrame;
        _folder = folder;
        _fileName = fileName;
        _query = query;
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

    public void export()
    {
        try
        {
            copyAssets();
            openFile();
            setupSheetNames();
            setupSummaryHeaders();
            setupTimeSummaryTable();
            closeFile();
            createODS();
        }
        catch (Exception e)
        {
            Log.e(TAG, e.toString());
        }
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

    private void setupSheetNames()
    {
        // Update table:name attribute
        Element firstSheet = _document.getElementById("summarySheet");
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
                makeSummaryTableRow("2019", 1.0f, 1.0f);
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
            totalExpCell.setAttribute("table:formula",
                    "of:=0");
            totalBudCell.setAttribute("table:formula",
                    "of:=0");
            totalRemCell.setAttribute("table:formula",
                    "of:=0");
        }
    }

    private void makeSummaryTableRow(String label, float amount, float budget)
    {
        Node summarySheet = _document.getElementById("summarySheet");
        Node timeSummaryTotalRow = _document.getElementById("time_summary_total_row");

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
        textCell = _document.createElement("text:p");
        textCell.setTextContent(amountString);
        textCell.setTextContent(label);
        tableCell.appendChild(textCell);
        row.appendChild(tableCell);

        // Budget
        tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "float");
        String budgetString = Currencies.formatCurrency(Currencies.default_currency, budget);
        tableCell.setAttribute("office:value", ""+budget);
        textCell = _document.createElement("text:p");
        textCell.setTextContent(budgetString);
        textCell.setTextContent(label);
        tableCell.appendChild(textCell);
        row.appendChild(tableCell);

        // Remaining
        tableCell = _document.createElement("table:table-cell");
        tableCell.setAttribute("office:value-type", "float");
        float remaining = budget - amount;
        String remainingString = Currencies.formatCurrency(Currencies.default_currency, remaining);
        tableCell.setAttribute("office:value", ""+remaining);
        textCell = _document.createElement("text:p");
        textCell.setTextContent(remainingString);
        textCell.setTextContent(label);
        tableCell.appendChild(textCell);
        row.appendChild(tableCell);

        summarySheet.insertBefore(row, timeSummaryTotalRow);
        _timeRows++;
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
            _contentFile.delete();
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
