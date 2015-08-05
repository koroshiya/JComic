/*
<<<<<<< HEAD
 * Copyright 2014 Google Inc. All rights reserved.
=======
 * Copyright (C) 2011 Micah Hainline
 * Copyright (C) 2012 Triposo
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
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

package com.japanzai.koroshiya.controls;

<<<<<<< HEAD
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class EllipsizingTextView extends TextView {
    private static final int MAX_ELLIPSIZE_LINES = 100;

    private int mMaxLines;

    public EllipsizingTextView(Context context) {
        this(context, null, 0);
=======
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.widget.TextView;

public class EllipsizingTextView extends TextView { //TODO: ellipsize middle
    private static final String ELLIPSIS = "\u2026";
    private static final Pattern DEFAULT_END_PUNCTUATION = Pattern.compile("[\\.,\u2026;\\:\\s]*$", Pattern.DOTALL);

    public interface EllipsizeListener {
        void ellipsizeStateChanged(boolean ellipsized);
    }

    private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<>();
    private boolean isEllipsized;
    private boolean isStale;
    private boolean programmaticChange;
    private CharSequence fullText;
    private int maxLines;
    private float lineSpacingMultiplier = 1.0f;
    private float lineAdditionalVerticalPadding = 0.0f;
    /**
     * The end punctuation which will be removed when appending #ELLIPSIS.
     */
    private Pattern endPunctuationPattern;

    public EllipsizingTextView(Context context) {
        this(context, null);
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
    }

    public EllipsizingTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EllipsizingTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
<<<<<<< HEAD

        // Attribute initialization
        final TypedArray a = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.maxLines
        }, defStyle, 0);

        mMaxLines = a.getInteger(0, 1);
        a.recycle();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        CharSequence newText = getWidth() == 0 || mMaxLines > MAX_ELLIPSIZE_LINES ? text :
                TextUtils.ellipsize(text, getPaint(), getWidth() * mMaxLines,
                        TextUtils.TruncateAt.END, false, null);
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
=======
        super.setEllipsize(null);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.maxLines });
        setMaxLines(a.getInt(0, Integer.MAX_VALUE));
        a.recycle();
        setEndPunctuationPattern(DEFAULT_END_PUNCTUATION);
    }

    public void setEndPunctuationPattern(Pattern pattern) {
        this.endPunctuationPattern = pattern;
    }

    public void addEllipsizeListener(EllipsizeListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        ellipsizeListeners.add(listener);
    }

    public void removeEllipsizeListener(EllipsizeListener listener) {
        ellipsizeListeners.remove(listener);
    }

    public boolean isEllipsized() {
        return isEllipsized;
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        this.maxLines = maxLines;
        isStale = true;
    }

    @SuppressLint("Override")
    public int getMaxLines() {
        return maxLines;
    }

    public boolean ellipsizingLastFullyVisibleLine() {
        return maxLines == Integer.MAX_VALUE;
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        this.lineAdditionalVerticalPadding = add;
        this.lineSpacingMultiplier = mult;
        super.setLineSpacing(add, mult);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before,
                                 int after) {
        super.onTextChanged(text, start, before, after);
        if (!programmaticChange) {
            fullText = text.toString();
            isStale = true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        if (ellipsizingLastFullyVisibleLine()) {
            isStale = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isStale) {
            resetText();
        }
        super.onDraw(canvas);
    }

    private void resetText() {
        CharSequence workingText = fullText;
        boolean ellipsized = false;
        Layout layout = createWorkingLayout(workingText);
        int linesCount = getLinesCount();
        if (layout.getLineCount() > linesCount) {
            // We have more lines of text than we are allowed to display.
            workingText = fullText.subSequence(0, layout.getLineEnd(linesCount - 1));
            while (createWorkingLayout(workingText + ELLIPSIS).getLineCount() > linesCount) {
                int lastSpace = workingText.toString().lastIndexOf(' ');
                if (lastSpace == -1) {
                    break;
                }
                workingText = workingText.subSequence(0, lastSpace);
            }
            // We should do this in the loop above, but it's cheaper this way.
            if(workingText instanceof Spannable) {
                SpannableStringBuilder sb = new SpannableStringBuilder(workingText);

                Matcher m = endPunctuationPattern.matcher(workingText);
                if(m.find()) {
                    int start = m.start();
                    sb.replace(start, workingText.length(), ELLIPSIS);
                }

                workingText = sb;

            } else {
                workingText = endPunctuationPattern.matcher(workingText).replaceFirst("");
                workingText = workingText + ELLIPSIS;
            }

            ellipsized = true;
        }
        if (!workingText.equals(getText())) {
            programmaticChange = true;
            try {
                setText(workingText);
            } finally {
                programmaticChange = false;
            }
        }
        isStale = false;
        if (ellipsized != isEllipsized) {
            isEllipsized = ellipsized;
            for (EllipsizeListener listener : ellipsizeListeners) {
                listener.ellipsizeStateChanged(ellipsized);
            }
        }
    }

    /**
     * Get how many lines of text we are allowed to display.
     */
    private int getLinesCount() {
        if (ellipsizingLastFullyVisibleLine()) {
            int fullyVisibleLinesCount = getFullyVisibleLinesCount();
            if (fullyVisibleLinesCount == -1) {
                return 1;
            } else {
                return fullyVisibleLinesCount;
            }
        } else {
            return maxLines;
        }
    }

    /**
     * Get how many lines of text we can display so their full height is visible.
     */
    private int getFullyVisibleLinesCount() {
        Layout layout = createWorkingLayout("");
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int lineHeight = layout.getLineBottom(0);
        return height / lineHeight;
    }

    private Layout createWorkingLayout(CharSequence workingText) {
        return new StaticLayout(workingText, getPaint(),
                getWidth() - getPaddingLeft() - getPaddingRight(),
                Alignment.ALIGN_NORMAL, lineSpacingMultiplier,
                lineAdditionalVerticalPadding, false /* includepad */);
    }

    @Override
    public void setEllipsize(TruncateAt where) {
        // Ellipsize settings are not respected
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
    }
}