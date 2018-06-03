package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import cc.corbin.budgettracker.R;

public class LineGraph extends RelativeLayout
{
    private final String TAG = "LineGraph";

    private Context _context;

    private Paint _borderPaint;
    private Paint _emptyPiePaint;
    private Paint _linePaint;
    private Paint _markerPaint;
    private Paint _pointPaint;
    private Paint _pointLinePaint;

    private String _title;
    private Paint _titlePaint;
    private Paint _labelPaint;
    private Paint _scalePaint;

    private final float GRAPH_RATIO = 0.75f;
    private final float GRAPH_PADDING = 0.1f;
    private final float VERTICAL_OFFSET = 0.05f;

    private final float TITLE_SCALING = 0.05f;
    private final float TITLE_OFFSET = 0.065f;

    private final float LABEL_SCALING = 0.035f;
    private final float LABEL_OFFSET_Y = 0.075f;

    private final float SCALE_SCALING = 0.035f;
    private final float SCALE_OFFSET_Y = 0.01f;
    private final float SCALE_OFFSET_R = 0.95f;

    private final float PROGRESS_BAR_PADDING = 0.25f;
    private final float PROGRESS_BAR_OFFSET = 0.0f;

    private final float BOX_L = 0.1f;
    private final float BOX_R = 0.9f;
    private final float BOX_T = 0.15f;
    private final float BOX_B = 0.9f;

    private final int NUM_LINES = 9;
    private final float LINE_SPACING = 0.08f;
    private final float LINE_HEIGHT = 0.05f;

    private final float POINT_PADDING_Y = 0.03f;

    private final int BORDER_STROKE = 3;
    private final int MARKER_STROKE = 3;
    private final int POINT_STROKE = 20;
    private final int POINT_LINE_STROKE = 10;

    private final int MAX_CHARACTERS = 7;

    private ProgressBar _progressBar;

    private float[] _amounts;
    private String[] _labels;
    private float[] _points;
    private String[] _scale;

    private boolean _dataAvailable;

    public LineGraph(Context context)
    {
        super(context);

        _context = context;

        init();
    }

    public LineGraph(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);

        init();
    }

    public LineGraph(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);

        init();
    }

    private void init()
    {
        setWillNotDraw(false);

        _dataAvailable = false;

        _title = "";

        _progressBar = new ProgressBar(_context);
        RelativeLayout.LayoutParams layoutParams =
                //new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams.addRule(CENTER_IN_PARENT);
        _progressBar.setLayoutParams(layoutParams);
        addView(_progressBar);

        _titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _titlePaint.setColor(Color.BLACK);
        _titlePaint.setTextAlign(Paint.Align.CENTER);
        _titlePaint.setUnderlineText(true);

        _labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _labelPaint.setColor(Color.BLACK);
        _labelPaint.setTextAlign(Paint.Align.CENTER);

        _scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _scalePaint.setColor(Color.BLACK);
        _scalePaint.setTextAlign(Paint.Align.RIGHT);

        _borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _borderPaint.setColor(Color.BLACK);
        _borderPaint.setStrokeWidth(BORDER_STROKE);

        _emptyPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _emptyPiePaint.setColor(Color.WHITE);
        _emptyPiePaint.setStyle(Paint.Style.FILL);

        _linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _linePaint.setColor(Color.DKGRAY);
        _linePaint.setStyle(Paint.Style.STROKE);

        _markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _markerPaint.setColor(Color.BLACK);
        _markerPaint.setStrokeWidth(MARKER_STROKE);

        _pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _pointPaint.setColor(Color.BLACK);
        _pointPaint.setStrokeWidth(POINT_STROKE);

        _pointLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _pointLinePaint.setColor(Color.BLACK);
        _pointLinePaint.setStrokeWidth(POINT_LINE_STROKE);
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int sizeH = ((int)(sizeW * GRAPH_RATIO));

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeH, MeasureSpec.AT_MOST);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
        int w = r - l;
        int h = b - t; // Not used

        _progressBar.setPadding(
                ((int)((w * PROGRESS_BAR_PADDING) - (w * PROGRESS_BAR_OFFSET))),
                ((int)((w * PROGRESS_BAR_PADDING) + (h * VERTICAL_OFFSET))),
                ((int)((w * PROGRESS_BAR_PADDING) + (w * PROGRESS_BAR_OFFSET))),
                ((int)((w * PROGRESS_BAR_PADDING) - (h * VERTICAL_OFFSET))));

        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();

        int cX = w / 2;
        int cY = (int)((h / 2) + (h * VERTICAL_OFFSET));
        int hW = w / 2 - (int)(2 * GRAPH_PADDING * w);
        //int hH = h / 2 - (int)(2 * GRAPH_PADDING * h); // Not used

        _titlePaint.setTextSize(TITLE_SCALING * w); // Carry over from adjusting in PieChart
        _labelPaint.setTextSize(LABEL_SCALING * h);
        _scalePaint.setTextSize(SCALE_SCALING * h);

        // Draw the title
        canvas.drawText(_title, cX, cY - hW - (TITLE_OFFSET * w), _titlePaint);

        int boxL = (int)(w * BOX_L);
        int boxR = (int)(w * BOX_R);
        int boxT = (int)(h * BOX_T);
        int boxB = (int)(h * BOX_B);

        int lineSpacing = (int)(h * LINE_SPACING);

        canvas.drawLine(boxL, boxB, boxR, boxB, _borderPaint);
        canvas.drawLine(boxL, boxT, boxL, boxB, _borderPaint);

        int line = boxB - lineSpacing;
        for (int i = 0; i < NUM_LINES; i++)
        {
            canvas.drawLine(boxL, line, boxR, line, _linePaint);
            line -= lineSpacing;
        }

        if (_dataAvailable)
        {
            int lineHHeight = (int)(h * LINE_HEIGHT / 2.0f);
            int drawingW = boxR - boxL;
            int spacing = drawingW / (_amounts.length + 1);
            int currentX = spacing + boxL;
            int paddingY = (int)(h * POINT_PADDING_Y);
            int boxH = boxB - boxT - paddingY;
            int labelY = (int)(h * LABEL_OFFSET_Y);
            int scaleOffetY = (int)(h * SCALE_OFFSET_Y);

            for (int i = 0; i < _amounts.length; i++)
            {
                canvas.drawLine(currentX, boxB - lineHHeight, currentX, boxB + lineHHeight, _markerPaint);
                canvas.drawPoint(currentX, boxB - (boxH * _points[i]), _pointPaint);
                canvas.drawText(_labels[i], currentX, boxB + labelY, _labelPaint);
                currentX += spacing;
            }

            currentX = spacing + boxL;
            for (int i = 0; i < (_amounts.length-1); i++)
            {
                canvas.drawLine(currentX, boxB - (boxH * _points[i]), currentX + spacing, boxB - (boxH * _points[i+1]), _pointLinePaint);
                currentX += spacing;
            }

            line = boxB;
            int textX = (int)(w * SCALE_OFFSET_R);
            for (int i = 0; i <= NUM_LINES; i++)
            {
                canvas.drawText(_scale[i], textX, line + scaleOffetY, _scalePaint);
                line -= lineSpacing;
            }
        }
        else { }
    }

    public void setData(float[] amounts, String[] labels)
    {
        _amounts = amounts;
        _labels = labels;
        _points = new float[_amounts.length];
        _scale = new String[NUM_LINES+1];

        float minValue = 0.0f;//Float.MAX_VALUE;
        float maxValue = 0.0f;
        for (int i = 0; i < _amounts.length; i++)
        {
            if (_amounts[i] > maxValue)
            {
                maxValue = _amounts[i];
            }
            else { }
            /*if (_amounts[i] < minValue)
            {
                minValue = _amounts[i];
            }
            else { }*/
        }

        float span = maxValue - minValue;
        for (int i = 0; i < _amounts.length; i++)
        {
            _points[i] = (_amounts[i] / span);
            if (_labels[i].length() > MAX_CHARACTERS)
            {
                _labels[i] = _labels[i].substring(0, MAX_CHARACTERS-3) + "...";
            }
            else { }
        }

        float scaling = span / NUM_LINES;
        float amount = 0.0f;
        for (int i = 0; i <= NUM_LINES; i++)
        {
            _scale[i] = Currencies.formatCurrency(Currencies.default_currency, amount);
            amount += scaling;
        }

        // ProgressBar cannot be set to GONE or the canvas disappears
        _progressBar.setVisibility(INVISIBLE);

        _dataAvailable = true;
    }

    public void clearData()
    {
        _amounts = null;
        _labels = null;

        _progressBar.setVisibility(VISIBLE);

        _dataAvailable = false;
    }
}
