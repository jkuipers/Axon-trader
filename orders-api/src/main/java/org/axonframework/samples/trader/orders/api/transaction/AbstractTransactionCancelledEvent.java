/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.samples.trader.orders.api.transaction;

import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.domain.DomainEvent;

/**
 * @author Jettro Coenradie
 */
public abstract class AbstractTransactionCancelledEvent extends DomainEvent {

    private long totalAmountOfItems;
    private long amountOfExecutedItems;

    public AbstractTransactionCancelledEvent(long totalAmountOfItems, long amountOfExecutedItems) {
        this.totalAmountOfItems = totalAmountOfItems;
        this.amountOfExecutedItems = amountOfExecutedItems;
    }

    public AggregateIdentifier getTransactionIdentifier() {
        return super.getAggregateIdentifier();
    }

    public long getAmountOfExecutedItems() {
        return amountOfExecutedItems;
    }

    public long getTotalAmountOfItems() {
        return totalAmountOfItems;
    }
}
