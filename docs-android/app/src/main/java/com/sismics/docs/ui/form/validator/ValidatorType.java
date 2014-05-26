package com.sismics.docs.ui.form.validator;

import android.content.Context;

/**
 *  Interface for validation types.
 * 
 * @author bgamard
 */
public interface ValidatorType {

    /**
     * Returns true if the validator is validated.
     * @param text
     * @return
     */
    public boolean validate(String text);

    /**
     * Returns an error message.
     * @param context
     * @return
     */
    public String getErrorMessage(Context context);
}
