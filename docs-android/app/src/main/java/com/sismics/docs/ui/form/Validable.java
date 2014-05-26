package com.sismics.docs.ui.form;

import android.view.View;

public class Validable {

    private View view;
    
    private boolean isValidated = false;

    /**
     * Getter of view.
     * @return view
     */
    public View getView() {
        return view;
    }

    /**
     * Setter of view.
     * @param view view
     */
    public void setView(View view) {
        this.view = view;
    }

    /**
     * Getter of isValidated.
     * @return isValidated
     */
    public boolean isValidated() {
        return isValidated;
    }

    /**
     * Setter of isValidated.
     * @param isValidated isValidated
     */
    public void setValidated(boolean isValidated) {
        this.isValidated = isValidated;
    }
}
