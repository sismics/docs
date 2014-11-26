package com.sismics.docs.ui.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;

public class DatePickerView extends TextView implements DatePickerDialog.OnDateSetListener{
 
    public DatePickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DatePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes();
    }

    public DatePickerView(Context context) {
        super(context);
        setAttributes();
    }

    private void setAttributes() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                new DatePickerDialog(
                        DatePickerView.this.getContext(), DatePickerView.this,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
 
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        setText(String.format("%s/%s/%s", monthOfYear, dayOfMonth, year));
    }
}