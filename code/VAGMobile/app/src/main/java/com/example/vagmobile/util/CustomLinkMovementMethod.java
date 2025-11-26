package com.example.vagmobile.util;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Method;

public class CustomLinkMovementMethod extends LinkMovementMethod {

    private LinkClickListener linkClickListener;

    public interface LinkClickListener {
        void onLinkClick(String url);
    }

    public void setLinkClickListener(LinkClickListener listener) {
        this.linkClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] links = buffer.getSpans(off, off, ClickableSpan.class);
            if (links.length != 0) {
                ClickableSpan link = links[0];

                // Получаем URL из ClickableSpan через рефлексию
                String url = extractUrlFromClickableSpan(link);

                if (linkClickListener != null && url != null) {
                    linkClickListener.onLinkClick(url);
                    return true; // Перехватываем событие, чтобы браузер не открывался
                }
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }

    private String extractUrlFromClickableSpan(ClickableSpan span) {
        try {
            // Получаем URL через рефлексию, так как Markwon хранит его в приватном поле
            Method getUrlMethod = span.getClass().getMethod("getURL");
            return (String) getUrlMethod.invoke(span);
        } catch (Exception e) {
            // Альтернативный способ через toString()
            String spanString = span.toString();
            if (spanString.contains("url=")) {
                int start = spanString.indexOf("url=") + 4;
                int end = spanString.indexOf(",", start);
                if (end == -1) end = spanString.length() - 1;
                return spanString.substring(start, end);
            }
            return null;
        }
    }
}