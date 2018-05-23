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

package org.datavec.api.transform.transform.string;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;
import org.nd4j.shade.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This method maps all String values, except those is the specified list, to a single String  value
 *
 * @author Alex Black
 */
@Data
@EqualsAndHashCode
public class MapAllStringsExceptListTransform extends BaseStringTransform {

    private final Set<String> exceptions;
    private final String newValue;

    public MapAllStringsExceptListTransform(@JsonProperty("columnName") String columnName,
                    @JsonProperty("newValue") String newValue, @JsonProperty("exceptions") List<String> exceptions) {
        super(columnName);
        this.newValue = newValue;
        this.exceptions = new HashSet<>(exceptions);
    }

    @Override
    public Text map(Writable writable) {
        String str = writable.toString();
        if (exceptions.contains(str)) {
            return new Text(str);
        } else {
            return new Text(newValue);
        }
    }

    /**
     * Transform an object
     * in to another object
     *
     * @param input the record to transform
     * @return the transformed writable
     */
    @Override
    public Object map(Object input) {
        String str = input.toString();
        if (exceptions.contains(str)) {
            return str;
        } else {
            return newValue;
        }
    }
}
