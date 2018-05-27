package cc.corbin.budgettracker.auxilliary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import cc.corbin.budgettracker.R;

public class PieChart extends SurfaceView
{
    private final String TAG = "PieChart";

    private Paint _textPaint;
    private Paint _piePaint;
    private Paint _shadowPaint;

    private int _textColor;

    private float _textHeight;

    private SurfaceHolder _holder;

    public PieChart(Context context)
    {
        super(context);

        init();
    }

    public PieChart(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);

        init();
    }

    public PieChart(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TableCell,
                0, 0);

        init();
    }

    private void init()
    {
        _holder = getHolder();
        
        _textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _textPaint.setColor(_textColor);
        if (_textHeight == 0)
        {
            _textHeight = _textPaint.getTextSize();
        }
        else
        {
            _textPaint.setTextSize(_textHeight);
        }

        _piePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _piePaint.setStyle(Paint.Style.FILL);
        _piePaint.setTextSize(_textHeight);

        _shadowPaint = new Paint(0);
        _shadowPaint.setColor(0xff101010);
        _shadowPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

    }
}
