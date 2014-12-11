package com.sismics.docs.ui.form;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.sismics.docs.listener.CallbackListener;
import com.sismics.docs.ui.form.validator.ValidatorType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for form validation.
 * 
 * @author bgamard
 */
public class Validator {

    /**
     * List of validable elements.
     */
    private Map<EditText, Validable> validables = new HashMap<EditText, Validable>();
    
    /**
     * Callback when the validation of one element has changed.
     */
    private CallbackListener onValidationChanged;
    
    /**
     * True if the validator show validation errors.
     */
    private boolean showErrors;

    /**
     * Context.
     */
    private Context context;
    
    /**
     * Constructor.
     *
     * @param showErrors True to display validation errors
     */
    public Validator(Context context, boolean showErrors) {
        this.context = context;
        this.showErrors = showErrors;
    }
    
    /**
     * Setter of onValidationChanged.
     * @param onValidationChanged onValidationChanged
     */
    public void setOnValidationChanged(CallbackListener onValidationChanged) {
        this.onValidationChanged = onValidationChanged;
        onValidationChanged.onComplete();
    }

    /**
     * Add a validable element.
     *
     * @param editText Edit text
     * @param validatorTypes Validators
     */
    public void addValidable(final EditText editText, final ValidatorType... validatorTypes) {
        final Validable validable = new Validable(validatorTypes);
        validables.put(editText, validable);
        
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // NOP
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // NOP
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                validate(editText, validable);
            }
        });
    }
    
    /**
     * Returns true if the element is validated.
     *
     * @param view View
     * @return True if the element is validated
     */
    public boolean isValidated(View view) {
        return validables.get(view).isValidated();
    }

    /**
     * Validate a specific EditText.
     *
     * @param editText EditText
     * @param validable Validable
     */
    private void validate(EditText editText, Validable validable) {
        validable.setValidated(true);
        for (ValidatorType validatorType : validable.getValidatorTypes()) {
            if (!validatorType.validate(editText.getEditableText().toString())) {
                if (showErrors) {
                    editText.setError(validatorType.getErrorMessage(context));
                }
                validable.setValidated(false);
                break;
            }
        }

        if (validable.isValidated()) {
            editText.setError(null);
        }

        if (onValidationChanged != null) {
            onValidationChanged.onComplete();
        }
    }

    /**
     * Validate everything now.
     */
    public void validate() {
        for (Map.Entry<EditText, Validable> entry : validables.entrySet()) {
            validate(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns true if all elements are validated.
     *
     * @return True if all elements are validated
     */
    public boolean isValidated() {
        for (Validable validable : validables.values()) {
            if (!validable.isValidated()) {
                return false;
            }
        }
        return true;
    }
}
