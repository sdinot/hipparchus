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
package org.hipparchus.analysis.interpolation;

import java.util.Arrays;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.analysis.BivariateFunction;
import org.hipparchus.analysis.FieldBivariateFunction;
import org.hipparchus.analysis.polynomials.FieldPolynomialSplineFunction;
import org.hipparchus.analysis.polynomials.PolynomialSplineFunction;
import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.exception.MathIllegalArgumentException;
import org.hipparchus.exception.NullArgumentException;
import org.hipparchus.util.MathArrays;

/**
 * Function that implements the
 * <a href="http://www.paulinternet.nl/?page=bicubic">bicubic spline</a>
 * interpolation.
 * This implementation currently uses {@link AkimaSplineInterpolator} as the
 * underlying one-dimensional interpolator, which requires 5 sample points;
 * insufficient data will raise an exception when the
 * {@link #value(double,double) value} method is called.
 *
 */
public class PiecewiseBicubicSplineInterpolatingFunction
    implements BivariateFunction, FieldBivariateFunction {

    /** The minimum number of points that are needed to compute the function. */
    private static final int MIN_NUM_POINTS = 5;
    /** Samples x-coordinates */
    private final double[] xval;
    /** Samples y-coordinates */
    private final double[] yval;
    /** Set of cubic splines patching the whole data grid */
    private final double[][] fval;

    /** Simple constructor.
     * @param x Sample values of the x-coordinate, in increasing order.
     * @param y Sample values of the y-coordinate, in increasing order.
     * @param f Values of the function on every grid point. the expected number
     *        of elements.
     * @throws MathIllegalArgumentException if {@code x} or {@code y} are not
     *         strictly increasing.
     * @throws NullArgumentException if any of the arguments are null
     * @throws MathIllegalArgumentException if any of the arrays has zero length.
     * @throws MathIllegalArgumentException if the length of x and y don't match the row, column
     *         height of f
     */
    public PiecewiseBicubicSplineInterpolatingFunction(double[] x,
                                                       double[] y,
                                                       double[][] f)
        throws MathIllegalArgumentException, NullArgumentException {
        if (x == null ||
            y == null ||
            f == null ||
            f[0] == null) {
            throw new NullArgumentException();
        }

        final int xLen = x.length;
        final int yLen = y.length;

        if (xLen == 0 ||
            yLen == 0 ||
            f.length == 0 ||
            f[0].length == 0) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.NO_DATA);
        }

        if (xLen < MIN_NUM_POINTS ||
            yLen < MIN_NUM_POINTS ||
            f.length < MIN_NUM_POINTS ||
            f[0].length < MIN_NUM_POINTS) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.INSUFFICIENT_DATA);
        }

        if (xLen != f.length) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   xLen, f.length);
        }

        if (yLen != f[0].length) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.DIMENSIONS_MISMATCH,
                                                   yLen, f[0].length);
        }

        MathArrays.checkOrder(x);
        MathArrays.checkOrder(y);

        xval = x.clone();
        yval = y.clone();
        fval = f.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double value(double x,
                        double y)
        throws MathIllegalArgumentException {
        final AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator();
        final int offset = 2;
        final int count = offset + 3;
        final int i = searchIndex(x, xval, offset, count);
        final int j = searchIndex(y, yval, offset, count);

        final double[] xArray = new double[count];
        final double[] yArray = new double[count];
        final double[] zArray = new double[count];
        final double[] interpArray = new double[count];

        for (int index = 0; index < count; index++) {
            xArray[index] = xval[i + index];
            yArray[index] = yval[j + index];
        }

        for (int zIndex = 0; zIndex < count; zIndex++) {
            for (int index = 0; index < count; index++) {
                zArray[index] = fval[i + index][j + zIndex];
            }
            final PolynomialSplineFunction spline = interpolator.interpolate(xArray, zArray);
            interpArray[zIndex] = spline.value(x);
        }

        final PolynomialSplineFunction spline = interpolator.interpolate(yArray, interpArray);

        return spline.value(y);

    }

    /**
     * {@inheritDoc}
     * @since 1.5
     */
    @Override
    public <T extends CalculusFieldElement<T>> T value(final T x, final T y)
        throws MathIllegalArgumentException {
        final AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator();
        final int offset = 2;
        final int count = offset + 3;
        final int i = searchIndex(x.getReal(), xval, offset, count);
        final int j = searchIndex(y.getReal(), yval, offset, count);

        final double[] xArray = new double[count];
        final T[] yArray = MathArrays.buildArray(x.getField(), count);
        final double[] zArray = new double[count];
        final T[] interpArray = MathArrays.buildArray(x.getField(), count);

        final T zero = x.getField().getZero();
        for (int index = 0; index < count; index++) {
            xArray[index] = xval[i + index];
            yArray[index] = zero.add(yval[j + index]);
        }

        for (int zIndex = 0; zIndex < count; zIndex++) {
            for (int index = 0; index < count; index++) {
                zArray[index] = fval[i + index][j + zIndex];
            }
            final PolynomialSplineFunction spline = interpolator.interpolate(xArray, zArray);
            interpArray[zIndex] = spline.value(x);
        }

        final FieldPolynomialSplineFunction<T> spline = interpolator.interpolate(yArray, interpArray);

        return spline.value(y);

    }

    /**
     * Indicates whether a point is within the interpolation range.
     *
     * @param x First coordinate.
     * @param y Second coordinate.
     * @return {@code true} if (x, y) is a valid point.
     */
    public boolean isValidPoint(double x,
                                double y) {
        return !(x < xval[0]) && !(x > xval[xval.length - 1]) && !(y < yval[0]) && !(y > yval[yval.length - 1]);
    }

    /**
     * @param c Coordinate.
     * @param val Coordinate samples.
     * @param offset how far back from found value to offset for querying
     * @param count total number of elements forward from beginning that will be
     *        queried
     * @return the index in {@code val} corresponding to the interval containing
     *         {@code c}.
     * @throws MathIllegalArgumentException if {@code c} is out of the range defined by
     *         the boundary values of {@code val}.
     */
    private int searchIndex(double c,
                            double[] val,
                            int offset,
                            int count) {
        int r = Arrays.binarySearch(val, c);

        if (r == -1 || r == -val.length - 1) {
            throw new MathIllegalArgumentException(LocalizedCoreFormats.OUT_OF_RANGE_SIMPLE,
                                                   c, val[0], val[val.length - 1]);
        }

        if (r < 0) {
            // "c" in within an interpolation sub-interval, which returns
            // negative
            // need to remove the negative sign for consistency
            r = -r - offset - 1;
        } else {
            r -= offset;
        }

        if (r < 0) {
            r = 0;
        }

        if ((r + count) >= val.length) {
            // "c" is the last sample of the range: Return the index
            // of the sample at the lower end of the last sub-interval.
            r = val.length - count;
        }

        return r;
    }
}
