package com.sismics.docs.ui.form.validator;

import android.content.Context;

import com.sismics.docs.R;

import java.util.regex.Pattern;

/**
 * Email validator.
 * 
 * @author bgamard
 */
public class Email implements ValidatorType {

    /**
     * Pattern de validation.
     */
    private static Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");
    
    @Override
    public boolean validate(String text) {
        return EMAIL_PATTERN.matcher(text).matches();
    }

    @Override
    public String getErrorMessage(Context context) {
        return context.getResources().getString(R.string.validate_error_email);
    }
}
