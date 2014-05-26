package com.sismics.docs.ui.form.validator;

import android.content.Context;

import com.sismics.docs.R;

/**
 * Text length validator.
 * 
 * @author bgamard
 */
public class Length implements ValidatorType {

    /**
     * Minimum length.
     */
    private int minLength = 0;
    
    /**
     * Maximum length.
     */
    private int maxLength = 0;
    
    /**
     * True if the last validation error was about a string too short.
     */
    private boolean tooShort; 
    
    /**
     * Constructor.
     * @param minLength Minimum length
     * @param maxLength Maximum length
     */
    public Length(int minLength, int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    @Override
    public boolean validate(String text) {
        tooShort = text.trim().length() < minLength;
        return text.trim().length() >= minLength && text.trim().length() <= maxLength;
    }

    @Override
    public String getErrorMessage(Context context) {
        if (tooShort) {
            return context.getResources().getString(R.string.validate_error_length_min, minLength);
        }
        return context.getResources().getString(R.string.validate_error_length_max, maxLength);
    }
}
