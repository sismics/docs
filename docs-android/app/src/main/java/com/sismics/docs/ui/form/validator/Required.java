package com.sismics.docs.ui.form.validator;

import android.content.Context;

import com.sismics.docs.R;

/**
 * Text presence validator.
 * 
 * @author bgamard
 */
public class Required implements ValidatorType {

    @Override
    public boolean validate(String text) {
        return text.trim().length() != 0;
    }

    @Override
    public String getErrorMessage(Context context) {
        return context.getString(R.string.validate_error_required);
    }

}
