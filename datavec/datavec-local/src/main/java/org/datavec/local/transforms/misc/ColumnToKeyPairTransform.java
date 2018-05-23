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

package org.datavec.local.transforms.misc;

import lombok.AllArgsConstructor;

import org.datavec.api.writable.Writable;
import org.nd4j.linalg.function.Function;
import org.nd4j.linalg.primitives.Pair;

import java.util.List;

/**
 * Extract out one writable, and map it to a pair with count 1.
 * Used to count the N most frequent values in a column,
 *
 * @author Alex Black
 */
@AllArgsConstructor
public class ColumnToKeyPairTransform implements Function<List<Writable>, Pair<Writable, Long>> {
    private final int columnIndex;

    @Override
    public Pair<Writable, Long> apply(List<Writable> list) {
        return Pair.of(list.get(columnIndex), 1L);
    }
}
