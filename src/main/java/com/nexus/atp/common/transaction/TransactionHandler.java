package com.nexus.atp.common.transaction;

public interface TransactionHandler<TRANSACTION extends BaseTransaction> {
    void handleTransaction(TRANSACTION transaction);
}
