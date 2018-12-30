package cc.corbin.budgettracker.auxilliary;

import android.arch.persistence.room.ColumnInfo;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TableRow;

import cc.corbin.budgettracker.R;

public class PieChart extends RelativeLayout
{
    private final String TAG = "PieChart";

    private Context _context;

    private Paint _shadowPaint;
    private Paint _emptyPiePaint;
    private Paint _piePaint;

    private Paint[] _piecePaints;

    private String _title;
    private Paint _titlePaint;
    private Paint _textPaint;

    private final float PIE_RATIO = 0.75f;
    private final float PIE_PADDING = 0.1f;
    private final float PIE_OFFSET = 0.125f;
    private final float VERTICAL_OFFSET = 0.05f;
    private final float LEGEND_START_OFFSET = 0.25f;
    private final float LEGEND_OFFSET = 0.01f;
    private final float SHADOW_OFFSET = 0.015f;
    private final float TITLE_SCALING = 0.05f;
    private final float TEXT_SCALING = 0.035f;
    private final float TEXT_PADDING = 0.065f;
    private final float TITLE_OFFSET = 0.065f;
    private final float LEGEND_BOX_SIZE = 0.01f;
    private final float PROGRESS_BAR_PADDING = 0.25f;
    private final float PROGRESS_BAR_OFFSET = 0.125f;

    private final int MAX_CHARACTERS = 10;

    private final double TWO_PI = 2.0 * Math.PI;
    private final double RED = Math.atan2(Math.sin(TWO_PI * 0.0 / 3.0), Math.cos(TWO_PI * 0.0 / 3.0));
    private final double GREEN = Math.atan2(Math.sin(TWO_PI * 1.0 / 3.0), Math.cos(TWO_PI * 1.0 / 3.0));
    private final double BLUE = Math.atan2(Math.sin(TWO_PI * 2.0 / 3.0), Math.cos(TWO_PI * 2.0 / 3.0));

    private ProgressBar _progressBar;

    private float[] _amounts;
    private String[] _labels;
    private float _total;
    private float[] _arcs;

    private boolean _dataAvailable;

    public PieChart(Context context)
    {
        super(context);

        _context = context;

        init();
    }

    public PieChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        _context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);

        init();
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr)
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

        _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _textPaint.setColor(Color.BLACK);
        _textPaint.setTextAlign(Paint.Align.LEFT);

        _shadowPaint = new Paint(0);
        _shadowPaint.setColor(Color.DKGRAY);
        _shadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        _emptyPiePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _emptyPiePaint.setColor(Color.WHITE);
        _emptyPiePaint.setStyle(Paint.Style.FILL);

        _piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _piePaint.setColor(Color.BLACK);
        _piePaint.setStyle(Paint.Style.STROKE);
    }

    public void setTitle(String title)
    {
        _title = title;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int sizeW = MeasureSpec.getSize(widthMeasureSpec);
        int sizeH = ((int)(sizeW * PIE_RATIO));

        heightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeH, MeasureSpec.AT_MOST);

        /*if (widthMeasureSpec > heightMeasureSpec)
        {
            heightMeasureSpec = widthMeasureSpec;
        }
        else
        {
            widthMeasureSpec = heightMeasureSpec;
        }*/

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
        int hW = w / 2 - (int)(2 * PIE_PADDING * w);
        //int hH = h / 2 - (int)(2 * PIE_PADDING * h); // Not used

        int pieOffset = (int)(w * PIE_OFFSET);
        int shadowOffset = (int)(w * SHADOW_OFFSET);
        int legendStartOffset = (int)(w * LEGEND_START_OFFSET);
        int legendOffset = (int)(w * LEGEND_OFFSET);
        int legendBoxSize = (int)(w * LEGEND_BOX_SIZE);

        _titlePaint.setTextSize(TITLE_SCALING * w);
        _textPaint.setTextSize(TEXT_SCALING * w);

        int textSize = (int)((_textPaint.descent() + _textPaint.ascent()) / 2);
        int textPadding = (int)(w * TEXT_PADDING);

        // Draw the title
        canvas.drawText(_title, cX, cY - hW - (TITLE_OFFSET * w), _titlePaint);

        canvas.drawCircle(cX-shadowOffset-pieOffset, cY+shadowOffset, hW, _shadowPaint);
        canvas.drawCircle(cX-pieOffset, cY, hW, _emptyPiePaint);
        canvas.drawCircle(cX-pieOffset, cY, hW, _piePaint);

        if (_dataAvailable)
        {
            // Draw the arcs and legend
            int startAngle = 0;
            int x = cX + legendStartOffset;
            int textX = x + legendOffset + legendBoxSize;
            int y = cY - (_arcs.length * (textPadding + textSize) / 2) - ((_arcs.length % 2 == 1) ? 0 : (textSize / 2));
            int textY = y - (textSize / 2);
            for (int i = 0; i < _arcs.length; i++)
            {
                canvas.drawArc(cX - hW - pieOffset, cY - hW, cX + hW - pieOffset, cY + hW,
                        startAngle, _arcs[i], true, _piecePaints[i % _piecePaints.length]);
                startAngle += _arcs[i];

                canvas.drawRect(x - legendBoxSize, y - legendBoxSize, x + legendBoxSize, y + legendBoxSize,
                        _piecePaints[i % _piecePaints.length]);

                canvas.drawText(_labels[i], textX, textY, _textPaint);
                y += textPadding + textSize;
                textY += textPadding + textSize;
            }
        }
        else { }
    }

    public void setData(float[] amounts, String[] labels)
    {
        _amounts = amounts.clone();
        _labels = labels.clone();

        _total = 0.0f;
        for (int i = 0; i < _amounts.length; i++)
        {
            // Ensure the amount is greater than zero and then add it to the total
            _amounts[i] = _amounts[i] >= 0.0f ? _amounts[i] : 0.0f;
            _total += _amounts[i];
        }

        _arcs = new float[_amounts.length];
        float remainder = 0.0f;
        for (int i = 0; i < _amounts.length; i++)
        {
            _arcs[i] = (_amounts[i] / _total) * 360.0f;
            remainder += _arcs[i] - ((int)_arcs[i]);
            if (_labels[i].length() > MAX_CHARACTERS)
            {
                _labels[i] = _labels[i].substring(0, MAX_CHARACTERS-3) + "...";
            }
            else { }
        }

        int index = 0;
        while (remainder > 0.0f)
        {
            float arcRemain = _arcs[index] - ((int)_arcs[index]);
            if (remainder > 1.0f)
            {
                if (arcRemain >= 0.5f)
                {
                    _arcs[index] += 1;
                    remainder--;
                }
                else { }
            }
            else if (arcRemain > 0.0f) // If the remaining value is not substantial, then just place it in any non-zero slice
            {
                _arcs[index] += remainder;
                remainder = 0.0f;
            }
            else { }
            index = (index + 1) % _arcs.length;
        }

        // ProgressBar cannot be set to GONE or the canvas disappears
        _progressBar.setVisibility(INVISIBLE);

        setupColors(_arcs.length);

        _dataAvailable = true;
    }

    public void clearData()
    {
        _amounts = null;
        _labels = null;
        _total = 0.0f;

        _progressBar.setVisibility(VISIBLE);

        _dataAvailable = false;
    }

    // Use roots of unity
    private void setupColors(int colors)
    {
        double TWO_THIRD_PI = TWO_PI / 3.0;
        int mod = (colors > 1) ? (colors - ((colors % 2 == 0) ? 0 : 1)) : 1; // Subtract off 1 from odd numbers
        int div = colors / 2;
        _piecePaints = new Paint[colors];
        for (int i = 0; i < colors; i++)
        {
            double k = i;
            double n = colors;
            double colorReal = Math.cos(TWO_PI * k / n);
            double colorImaginary = Math.sin(TWO_PI * k / n);

            double angle = Math.atan2(colorImaginary, colorReal);

            double C = 255.0 / TWO_THIRD_PI;
            int red = (int)(C * (Math.max(TWO_THIRD_PI - Math.abs(RED - angle), 0.0)));
            int green = (int)(C * (Math.max(TWO_THIRD_PI - Math.abs(GREEN - angle), 0.0)));
            int blue = (int)(C * (Math.max(TWO_THIRD_PI - Math.abs(BLUE - angle), 0.0)));

            // Move odd colors to the opposite side
            int index = ((i % 2 == 0) ? i : ((i + div) % colors + ((i > div) ? 1 : 0)));
            _piecePaints[index] = new Paint(Paint.ANTI_ALIAS_FLAG);
            _piecePaints[index].setStyle(Paint.Style.FILL);
            _piecePaints[index].setColor(Color.argb(255, red, green, blue));
        }

        for (int j = 1; j <= 10; j++)
        {
            Log.e(TAG, "Colors: " + j);
            div = j / 2;
            for (int i = 0; i < j; i++)
            {
                int index = ((i % 2 == 0) ? i : ((i + div) % j + ((i > div) ? 1 : 0)));
                Log.e(TAG, "Index: " + index);
            }
        }
    }

    /*private void setupColors(int colors)
    {
        if (colors == 7) // Do not want to repeat if the last and first piece are the same color
        {
            _piecePaints = new Paint[7];
            for (int i = 0; i < _piecePaints.length; i++)
            {
                _piecePaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
                _piecePaints[i].setStyle(Paint.Style.FILL);
            }
            _piecePaints[0].setColor(Color.RED);
            _piecePaints[1].setColor(Color.argb(255, 255, 165, 0));
            _piecePaints[2].setColor(Color.YELLOW);
            _piecePaints[3].setColor(Color.GREEN);
            _piecePaints[4].setColor(Color.BLUE);
            _piecePaints[5].setColor(Color.argb(255, 128, 0, 128));
            _piecePaints[6].setColor(Color.BLACK);
        }
        else
        {
            _piecePaints = new Paint[6];
            for (int i = 0; i < _piecePaints.length; i++)
            {
                _piecePaints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
                _piecePaints[i].setStyle(Paint.Style.FILL);
            }
            _piecePaints[0].setColor(Color.RED);
            _piecePaints[1].setColor(Color.argb(255, 255, 165, 0));
            _piecePaints[2].setColor(Color.YELLOW);
            _piecePaints[3].setColor(Color.GREEN);
            _piecePaints[4].setColor(Color.BLUE);
            _piecePaints[5].setColor(Color.argb(255, 128, 0, 128));
        }
    }*/
}
