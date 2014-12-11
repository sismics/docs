package com.sismics.docs.ui.form;

import android.view.View;

import com.sismics.docs.ui.form.validator.ValidatorType;

public class Validable {

    private final ValidatorType[] validatorTypes;

    private View view;
    
    private boolean isValidated = false;

    public Validable(ValidatorType... validatorTypes) {
        this.validatorTypes = validatorTypes;
    }

    /**
     * Getter of view.
     *
     * @return view
     */
    public View getView() {
        return view;
    }

    /**
     * Setter of view.
     *
     * @param view view
     */
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Getter of isValidated.
     *
     * @return isValidated
     */
    public boolean isValidated() {
        return isValidated;
    }

    /**
     * Setter of isValidated.
     *
     * @param isValidated isValidated
     */
    public void setValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }

    /**
     * Getter of validatorTypes.
     *
     * @return validatorTypes
     */
    public  ValidatorType[] getValidatorTypes() {
        return validatorTypes;
    }
}
