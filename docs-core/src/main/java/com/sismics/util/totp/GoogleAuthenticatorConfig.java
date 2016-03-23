/*
 * Copyright (c) 2014-2015 Enrico M. Crisostomo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the name of the author nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sismics.util.totp;

import java.util.concurrent.TimeUnit;

public class GoogleAuthenticatorConfig {
    private long timeStepSizeInMillis = TimeUnit.SECONDS.toMillis(30);
    private int windowSize = 3;
    private int codeDigits = 6;
    private int keyModulus = (int) Math.pow(10, codeDigits);
    private KeyRepresentation keyRepresentation = KeyRepresentation.BASE32;

    /**
     * Returns the key module.
     *
     * @return the key module.
     */
    public int getKeyModulus() {
        return keyModulus;
    }

    /**
     * Returns the key representation.
     *
     * @return the key representation.
     */
    public KeyRepresentation getKeyRepresentation() {
        return keyRepresentation;
    }

    /**
     * Returns the number of digits in the generated code.
     *
     * @return the number of digits in the generated code.
     */
    public int getCodeDigits() {
        return codeDigits;
    }

    /**
     * Returns the time step size, in milliseconds, as specified by RFC 6238.
     * The default value is 30.000.
     *
     * @return the time step size in milliseconds.
     */
    public long getTimeStepSizeInMillis() {
        return timeStepSizeInMillis;
    }

    /**
     * Returns an integer value representing the number of windows of size
     * timeStepSizeInMillis that are checked during the validation process, to
     * account for differences between the server and the client clocks. The
     * bigger the window, the more tolerant the library code is about clock
     * skews.
     * <p/>
     * We are using Google's default behaviour of using a window size equal to
     * 3. The limit on the maximum window size, present in older versions of
     * this library, has been removed.
     *
     * @return the window size.
     * @see #timeStepSizeInMillis
     */
    public int getWindowSize() {
        return windowSize;
    }

    public static class GoogleAuthenticatorConfigBuilder {
        private GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig();

        public GoogleAuthenticatorConfig build() {
            return config;
        }

        public GoogleAuthenticatorConfigBuilder setCodeDigits(int codeDigits) {
            if (codeDigits <= 0) {
                throw new IllegalArgumentException("Code digits must be positive.");
            }

            if (codeDigits < 6) {
                throw new IllegalArgumentException("The minimum number of digits is 6.");
            }

            if (codeDigits > 8) {
                throw new IllegalArgumentException("The maximum number of digits is 8.");
            }

            config.codeDigits = codeDigits;
            config.keyModulus = (int) Math.pow(10, codeDigits);
            return this;
        }

        public GoogleAuthenticatorConfigBuilder setTimeStepSizeInMillis(long timeStepSizeInMillis) {
            if (timeStepSizeInMillis <= 0) {
                throw new IllegalArgumentException("Time step size must be positive.");
            }

            config.timeStepSizeInMillis = timeStepSizeInMillis;
            return this;
        }

        public GoogleAuthenticatorConfigBuilder setWindowSize(int windowSize) {
            if (windowSize <= 0) {
                throw new IllegalArgumentException("Window number must be positive.");
            }

            config.windowSize = windowSize;
            return this;
        }

        public GoogleAuthenticatorConfigBuilder setKeyRepresentation(KeyRepresentation keyRepresentation) {
            if (keyRepresentation == null) {
                throw new IllegalArgumentException("Key representation cannot be null.");
            }

            config.keyRepresentation = keyRepresentation;
            return this;
        }
    }
}
