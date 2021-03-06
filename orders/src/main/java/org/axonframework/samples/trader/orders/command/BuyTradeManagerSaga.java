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

package org.axonframework.samples.trader.orders.command;

import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.saga.annotation.EndSaga;
import org.axonframework.saga.annotation.SagaEventHandler;
import org.axonframework.saga.annotation.StartSaga;
import org.axonframework.samples.trader.orders.api.portfolio.item.AddItemsToPortfolioCommand;
import org.axonframework.samples.trader.orders.api.portfolio.money.CancelMoneyReservationFromPortfolioCommand;
import org.axonframework.samples.trader.orders.api.portfolio.money.ConfirmMoneyReservationFromPortfolionCommand;
import org.axonframework.samples.trader.orders.api.portfolio.money.MoneyReservedFromPortfolioEvent;
import org.axonframework.samples.trader.orders.api.portfolio.money.NotEnoughMoneyInPortfolioToMakeReservationEvent;
import org.axonframework.samples.trader.orders.api.portfolio.money.ReserveMoneyFromPortfolioCommand;
import org.axonframework.samples.trader.orders.api.transaction.BuyTransactionCancelledEvent;
import org.axonframework.samples.trader.orders.api.transaction.BuyTransactionConfirmedEvent;
import org.axonframework.samples.trader.orders.api.transaction.BuyTransactionExecutedEvent;
import org.axonframework.samples.trader.orders.api.transaction.BuyTransactionPartiallyExecutedEvent;
import org.axonframework.samples.trader.orders.api.transaction.BuyTransactionStartedEvent;
import org.axonframework.samples.trader.orders.api.transaction.ConfirmTransactionCommand;
import org.axonframework.samples.trader.orders.api.transaction.ExecutedTransactionCommand;
import org.axonframework.samples.trader.tradeengine.api.order.CreateBuyOrderCommand;
import org.axonframework.samples.trader.tradeengine.api.order.TradeExecutedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jettro Coenradie
 */
public class BuyTradeManagerSaga extends TradeManagerSaga {

    private final static Logger logger = LoggerFactory.getLogger(BuyTradeManagerSaga.class);

    @StartSaga
    @SagaEventHandler(associationProperty = "transactionIdentifier")
    public void handle(BuyTransactionStartedEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "A new buy transaction is started with identifier {}, for portfolio with identifier {} and orderbook with identifier {}",
                    new Object[]{event.getTransactionIdentifier(),
                            event.getPortfolioIdentifier(),
                            event.getOrderbookIdentifier()});
            logger.debug("The new buy transaction with identifier {} is for buying {} items for the price of {}",
                         new Object[]{event.getTransactionIdentifier(),
                                 event.getTotalItems(),
                                 event.getPricePerItem()});
        }
        setTransactionIdentifier(event.getTransactionIdentifier());
        setOrderbookIdentifier(event.getOrderbookIdentifier());
        setPortfolioIdentifier(event.getPortfolioIdentifier());
        setPricePerItem(event.getPricePerItem());
        setTotalItems(event.getTotalItems());

        ReserveMoneyFromPortfolioCommand command = new ReserveMoneyFromPortfolioCommand(getPortfolioIdentifier(),
                                                                                        getTransactionIdentifier(),
                                                                                        getTotalItems()
                                                                                                * getPricePerItem());
        getCommandBus().dispatch(command);
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    public void handle(MoneyReservedFromPortfolioEvent event) {
        logger.debug("Money for transaction with identifier {} is reserved", getTransactionIdentifier());
        ConfirmTransactionCommand command = new ConfirmTransactionCommand(getTransactionIdentifier());
        getCommandBus().dispatch(command, new CommandCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onFailure(Throwable cause) {
                logger.error("********* WOW!!!", cause);
            }
        });
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    @EndSaga
    public void handle(NotEnoughMoneyInPortfolioToMakeReservationEvent event) {
        logger.debug(
                "Not enough money was available to make reservation in transaction {} for portfolio {}. Required: {}",
                new Object[]{getTransactionIdentifier(),
                        event.getPortfolioIdentifier(),
                        event.getAmountToPayInCents()});
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    public void handle(BuyTransactionConfirmedEvent event) {
        logger.debug("Buy Transaction {} is approved to make the buy order", event.getTransactionIdentifier());
        CreateBuyOrderCommand command = new CreateBuyOrderCommand(getPortfolioIdentifier(),
                                                                  getOrderbookIdentifier(),
                                                                  getTransactionIdentifier(),
                                                                  getTotalItems(),
                                                                  getPricePerItem());
        getCommandBus().dispatch(command);
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    public void handle(BuyTransactionCancelledEvent event) {
        long amountToCancel = (event.getTotalAmountOfItems() - event.getAmountOfExecutedItems()) * getPricePerItem();
        logger.debug("Buy Transaction {} is cancelled, amount of money reserved to cancel is {}",
                     event.getTransactionIdentifier(),
                     amountToCancel);
        CancelMoneyReservationFromPortfolioCommand command = new CancelMoneyReservationFromPortfolioCommand(
                getPortfolioIdentifier(),
                getTransactionIdentifier(),
                amountToCancel);
        getCommandBus().dispatch(command);
    }

    @SagaEventHandler(associationProperty = "buyTransactionId", keyName = "transactionIdentifier")
    public void handle(TradeExecutedEvent event) {
        logger.debug("Buy Transaction {} is executed, items for transaction are {} for a price of {}",
                     new Object[]{getTransactionIdentifier(), event.getTradeCount(), event.getTradePrice()});
        ExecutedTransactionCommand command = new ExecutedTransactionCommand(getTransactionIdentifier(),
                                                                            event.getTradeCount(),
                                                                            event.getTradePrice());
        getCommandBus().dispatch(command);
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    @EndSaga
    public void handle(BuyTransactionExecutedEvent event) {
        logger.debug("Buy Transaction {} is executed, last amount of executed items is {} for a price of {}",
                     new Object[]{event.getTransactionIdentifier(), event.getAmountOfItems(), event.getItemPrice()});
        ConfirmMoneyReservationFromPortfolionCommand confirmCommand =
                new ConfirmMoneyReservationFromPortfolionCommand(getPortfolioIdentifier(),
                                                                 getTransactionIdentifier(),
                                                                 event.getAmountOfItems() * event.getItemPrice());
        getCommandBus().dispatch(confirmCommand);
        AddItemsToPortfolioCommand addItemsCommand =
                new AddItemsToPortfolioCommand(getPortfolioIdentifier(),
                                               getOrderbookIdentifier(),
                                               event.getAmountOfItems());
        getCommandBus().dispatch(addItemsCommand);
    }

    @SagaEventHandler(associationProperty = "transactionIdentifier")
    public void handle(BuyTransactionPartiallyExecutedEvent event) {
        logger.debug("Buy Transaction {} is partially executed, amount of executed items is {} for a price of {}",
                     new Object[]{event.getTransactionIdentifier(),
                             event.getAmountOfExecutedItems(),
                             event.getItemPrice()});
        ConfirmMoneyReservationFromPortfolionCommand confirmCommand =
                new ConfirmMoneyReservationFromPortfolionCommand(getPortfolioIdentifier(),
                                                                 getTransactionIdentifier(),
                                                                 event.getAmountOfExecutedItems() * event
                                                                         .getItemPrice());
        getCommandBus().dispatch(confirmCommand);
        AddItemsToPortfolioCommand addItemsCommand =
                new AddItemsToPortfolioCommand(getPortfolioIdentifier(),
                                               getOrderbookIdentifier(),
                                               event.getAmountOfExecutedItems());
        getCommandBus().dispatch(addItemsCommand);
    }
}
