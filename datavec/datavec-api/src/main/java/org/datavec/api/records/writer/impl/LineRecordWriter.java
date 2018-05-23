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

package org.datavec.api.records.writer.impl;


import org.datavec.api.split.partition.PartitionMetaData;
import org.datavec.api.writable.Text;
import org.datavec.api.writable.Writable;

import java.io.IOException;
import java.util.List;

/**
 * Line record writer
 * @author Adam Gibson
 */
public class LineRecordWriter extends FileRecordWriter {
    public LineRecordWriter() {}


    @Override
    public PartitionMetaData write(List<Writable> record) throws IOException {
        if (!record.isEmpty()) {
            Text t = (Text) record.iterator().next();
            t.write(out);
            out.write(NEW_LINE.getBytes());
        }


        return PartitionMetaData.builder().numRecordsUpdated(1).build();

    }
}
