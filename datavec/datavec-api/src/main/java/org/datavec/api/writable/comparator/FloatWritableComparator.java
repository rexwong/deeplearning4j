/*-
 *  * Copyright 2016 Skymind, Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 */

package org.datavec.api.writable.comparator;

import org.datavec.api.writable.Writable;

public class FloatWritableComparator implements WritableComparator {
    @Override
    public int compare(Writable o1, Writable o2) {
        return Float.compare(o1.toFloat(), o2.toFloat());
    }

    @Override
    public String toString() {
        return "FloatWritableComparator()";
    }
}
