package com.sismics.docs.core.event;

import com.google.common.base.Objects;

/**
 * OCR all files in database event.
 *
 * @author bgamard
 */
public class OcrFileAsyncEvent {
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .toString();
    }
}
