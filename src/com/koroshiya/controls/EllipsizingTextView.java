/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koroshiya.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

public class EllipsizingTextView extends AppCompatTextView {
    private static final int MAX_ELLIPSIZE_LINES = 100;

    private int mMaxLines;
    private final int ellipsize;

    public EllipsizingTextView(Context context) {
        this(context, null, 0);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressWarnings("ResourceType")
    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.maxLines,
                android.R.attr.ellipsize
        }, defStyle, 0);

        mMaxLines = a.getInteger(0, 1);
        ellipsize = a.getInteger(1, 1); //1 = ellipsize start
        a.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        TextUtils.TruncateAt truncateAt;
        switch (ellipsize){
            case 2:
                truncateAt = TextUtils.TruncateAt.MIDDLE;
                break;
            case 3:
                truncateAt = TextUtils.TruncateAt.END;
                break;
            case 4:
                truncateAt = TextUtils.TruncateAt.MARQUEE;
                break;
            case 0:
            case 1:
            default:
                truncateAt = TextUtils.TruncateAt.START;
                break;
        }

        CharSequence newText = getWidth() == 0 || mMaxLines > MAX_ELLIPSIZE_LINES ? text :
                TextUtils.ellipsize(text, getPaint(), getWidth() * mMaxLines, truncateAt, false, null);
        super.setText(newText, type);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        if (width > 0 && oldWidth != width) {
            setText(getText());
        }
    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        mMaxLines = maxlines;
    }
}