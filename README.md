# NexusATP

NexusATP is an algorithmic trading platform under development. It is being developed to enable rapid design, 
backtesting and deployment of systematic trading algorithms operating on market or alternative data.

## Algos
These programs consume historic and real-time changes to market or alternative data, and produce a decision on
which stocks are best to acquire.

### Congress Trades
This algo will consume the historic trades of US Congress members within a lookback window, to decide
which stocks are profitable.

## Positions
An engine that will manage the current stocks that there is a position in. It will consume real-time market data 
updates to decide whether to hold or sell stocks, based on clearly defined controls. On receiving an algo 
decision, the engine will evaluate the recommended stocks based on validation rules, and will acquire a long
position in them.

## Market Data
Provides an interface to consume historic and real-time updates to stock market data.

## Trade Execution Gateway
Will execute the decisions made by the positions engine on which stocks to buy or sell, and in what quantity.
