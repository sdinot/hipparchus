/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This is not the original file distributed by the Apache Software Foundation
 * It has been modified by the Hipparchus project
 */
package org.hipparchus.migration.exception;

import org.hipparchus.exception.Localizable;
import org.hipparchus.exception.MathRuntimeException;
import org.hipparchus.migration.exception.util.LocalizedFormats;

/**
 * Base class for all unsupported features.
 * It is used for all the exceptions that have the semantics of the standard
 * {@link UnsupportedOperationException}, but must also provide a localized
 * message.
 *
 * @deprecated as of 1.0, replaced with {@link MathRuntimeException}
 */
@Deprecated
public class MathUnsupportedOperationException extends MathRuntimeException {
    /** Serializable version Id. */
    private static final long serialVersionUID = -6024911025449780478L;

    /**
     * Default constructor.
     */
    public MathUnsupportedOperationException() {
        this(LocalizedFormats.UNSUPPORTED_OPERATION);
    }
    /**
     * @param pattern Message pattern providing the specific context of
     * the error.
     * @param args Arguments.
     */
    public MathUnsupportedOperationException(Localizable pattern, Object ... args) {
        super(pattern, args);
    }

}
