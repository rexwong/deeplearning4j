/*-
 *
 *  * Copyright 2016 Skymind,Inc.
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
 *
 */
package org.deeplearning4j.arbiter.optimize.runner;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple helper class to store status of a candidate that is/has been/will be executed
 */
@AllArgsConstructor
@Data
public class CandidateInfo {

    public CandidateInfo() {
        //No arg constructor for Jackson
    }

    private int index;
    private CandidateStatus candidateStatus;
    private Double score;
    private long createdTime;
    private Long startTime;
    private Long endTime;
    private double[] flatParams; //Same as parameters in Candidate class
    private String exceptionStackTrace;
}
