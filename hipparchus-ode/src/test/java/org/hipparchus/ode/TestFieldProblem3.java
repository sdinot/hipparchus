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

package org.hipparchus.ode;

import org.hipparchus.Field;
import org.hipparchus.CalculusFieldElement;
import org.hipparchus.util.MathArrays;

/**
 * This class is used in the junit tests for the ODE integrators.

 * <p>This specific problem is the following differential equation :
 * <pre>
 *    y1'' = -y1/r^3  y1 (0) = 1-e  y1' (0) = 0
 *    y2'' = -y2/r^3  y2 (0) = 0    y2' (0) =sqrt((1+e)/(1-e))
 *    r = sqrt (y1^2 + y2^2), e = 0.9
 * </pre>
 * This is a two-body problem in the plane which can be solved by
 * Kepler's equation
 * <pre>
 *   y1 (t) = ...
 * </pre>
 * </p>

 * @param <T> the type of the field elements
 */
public class TestFieldProblem3<T extends CalculusFieldElement<T>>
extends TestFieldProblemAbstract<T> {

    /** Eccentricity */
    T e;

    /**
     * Simple constructor.
     * @param e eccentricity
     */
    public TestFieldProblem3(T e) {
        super(convert(e.getField(), 0.0),
              createY0(e),
              convert(e.getField(), 20.0),
              convert(e.getField(), 1.0, 1.0, 1.0, 1.0));
        this.e = e;
    }

    private static <T extends CalculusFieldElement<T>> T[] createY0(final T e) {
        T[] y0 = MathArrays.buildArray(e.getField(), 4);
        y0[0] = e.subtract(1).negate();
        y0[1] = e.getField().getZero();
        y0[2] = e.getField().getZero();
        y0[3] = e.add(1).divide(y0[0]).sqrt();
        return y0;
    }

    /**
     * Simple constructor.
     * @param field field to which elements belong
     */
    public TestFieldProblem3(Field<T> field) {
        this(field.getZero().add(0.1));
    }

    @Override
    public T[] doComputeDerivatives(T t, T[] y) {

        final T[] yDot = MathArrays.buildArray(getField(), getDimension());

        // current radius
        T r2 = y[0].multiply(y[0]).add(y[1].multiply(y[1]));
        T invR3 = r2.multiply(r2.sqrt()).reciprocal();

        // compute the derivatives
        yDot[0] = y[2];
        yDot[1] = y[3];
        yDot[2] = invR3.negate().multiply(y[0]);
        yDot[3] = invR3.negate().multiply(y[1]);

        return yDot;

    }

    @Override
    public T[] computeTheoreticalState(T t) {

        final T[] y = MathArrays.buildArray(getField(), getDimension());

        // solve Kepler's equation
        T E = t;
        T d = convert(t.getField(), 0);
        T corr = convert(t.getField(), 999.0);
        for (int i = 0; (i < 50) && (corr.norm() > 1.0e-12); ++i) {
            T f2  = e.multiply(E.sin());
            T f0  = d.subtract(f2);
            T f1  = e.multiply(E.cos()).subtract(1).negate();
            T f12 = f1.add(f1);
            corr  = f0.multiply(f12).divide(f1.multiply(f12).subtract(f0.multiply(f2)));
            d = d.subtract(corr);
            E = t.add(d);
        }

        T cosE = E.cos();
        T sinE = E.sin();

        y[0] = cosE.subtract(e);
        y[1] = e.multiply(e).subtract(1).negate().sqrt().multiply(sinE);
        y[2] = sinE.divide(e.multiply(cosE).subtract(1));
        y[3] = e.multiply(e).subtract(1).negate().sqrt().multiply(cosE).divide(e.multiply(cosE).subtract(1).negate());

        return y;

    }

}
