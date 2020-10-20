package com.games.commonappsstuff.presentation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.games.commonappsstuff.R;
import com.games.commonappsstuff.utils.NumberExtensionsKt;

/**
 Copyright 2016 Evander Palacios

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

public class StrokedTextView extends AppCompatTextView {
    private static final int DEFAULT_STROKE_WIDTH = 0;

    // fields
    private int _strokeColor;
    private float _strokeWidth;
    private boolean isDrawing;

    public StrokedTextView(Context context) {
        super(context);
    }

    public StrokedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public StrokedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextView);
            _strokeColor = a.getColor(R.styleable.StrokedTextView_textStrokeColor,
                    getCurrentTextColor());
            _strokeWidth = a.getFloat(R.styleable.StrokedTextView_textStrokeWidth,
                    DEFAULT_STROKE_WIDTH);
            a.recycle();
        } else {
            _strokeColor = getCurrentTextColor();
            _strokeWidth = DEFAULT_STROKE_WIDTH;
        }
        setStrokeWidth(_strokeWidth);
    }

    @Override
    public void invalidate() {
        // Ignore invalidate() calls when isDrawing == true
        // (setTextColor(color) calls will trigger them,
        // creating an infinite loop)
        if(isDrawing) return;
        super.invalidate();
    }

    public void setStrokeColor(int color) {
        _strokeColor = color;
    }

    public void setStrokeWidth(float width) {
        //convert values specified in dp in XML layout to
        //px, otherwise stroke width would appear different
        //on different screens
        _strokeWidth = NumberExtensionsKt.spToPx(width);
    }

    public void setStrokeWidth(int unit, float width) {
        _strokeWidth = TypedValue.applyDimension(
                unit, width, getContext().getResources().getDisplayMetrics());
    }

    // overridden methods
    @Override
    protected void onDraw(Canvas canvas) {
        if(_strokeWidth > 0) {
            isDrawing = true;
            //set paint to fill mode
            Paint p = getPaint();
            //save the text color
            int currentTextColor = getCurrentTextColor();
            //set paint to stroke mode and specify
            //stroke color and width
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(_strokeWidth);
            setTextColor(_strokeColor);
            //draw text stroke
            super.onDraw(canvas);
            //revert the color back to the one
            //initially specified
            setTextColor(currentTextColor);
            p.setStyle(Paint.Style.FILL);
            //draw the fill part of text
            super.onDraw(canvas);
            isDrawing = false;
        } else {
            super.onDraw(canvas);
        }
    }
}
