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

package org.axonframework.samples.trader.orders.command.matchers;

import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.samples.trader.orders.api.portfolio.money.CancelMoneyReservationFromPortfolioCommand;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * @author Jettro Coenradie
 */
public class CancelMoneyReservationFromPortfolioCommandMatcher
        extends BaseMatcher<CancelMoneyReservationFromPortfolioCommand> {

    public CancelMoneyReservationFromPortfolioCommandMatcher(AggregateIdentifier portfolioIdentifier,
                                                             long amountOfMoneyToCancel) {
        this.portfolioIdentifier = portfolioIdentifier.asString();
        this.amountOfMoneyToCancel = amountOfMoneyToCancel;
    }

    private String portfolioIdentifier;
    private long amountOfMoneyToCancel;

    @Override
    public boolean matches(Object o) {
        if (!(o instanceof CancelMoneyReservationFromPortfolioCommand)) {
            return false;
        }
        CancelMoneyReservationFromPortfolioCommand command = (CancelMoneyReservationFromPortfolioCommand) o;

        return command.getPortfolioIdentifier().asString().equals(portfolioIdentifier)
                && command.getAmountOfMoneyToCancel() == amountOfMoneyToCancel;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("CancelMoneyReservationFromPortfolioCommand with amountOfMoneyToCancel [")
                   .appendValue(amountOfMoneyToCancel)
                   .appendText("] for Portfolio with identifier [")
                   .appendValue(portfolioIdentifier)
                   .appendText("]");
    }
}
