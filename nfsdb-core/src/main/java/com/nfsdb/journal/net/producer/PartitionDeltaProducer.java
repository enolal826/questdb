/*
 * Copyright (c) 2014. Vlad Ilyushchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nfsdb.journal.net.producer;

import com.nfsdb.journal.Partition;
import com.nfsdb.journal.column.AbstractColumn;
import com.nfsdb.journal.column.VariableColumn;
import com.nfsdb.journal.exceptions.JournalException;

public class PartitionDeltaProducer extends ChannelProducerGroup<ColumnDeltaProducer> {

    private final Partition partition;

    public PartitionDeltaProducer(Partition partition) {
        this.partition = partition;
        addProducer(new FixedColumnDeltaProducer(partition.getNullsColumn()));
        for (int i = 0; i < partition.getJournal().getMetadata().getColumnCount(); i++) {
            AbstractColumn col = partition.getAbstractColumn(i);
            ColumnDeltaProducer producer;
            producer = col instanceof VariableColumn ? new VariableColumnDeltaProducer((VariableColumn) col) : new FixedColumnDeltaProducer(col);
            addProducer(producer);
        }
    }

    public void configure(long localRowID) throws JournalException {
        partition.open();
        long limit = partition.size();
        for (ColumnDeltaProducer producer : getProducers()) {
            producer.configure(localRowID, limit);
        }
        computeHasContent();
    }

    public Partition getPartition() {
        return partition;
    }
}
