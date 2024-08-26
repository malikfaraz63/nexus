package com.nexus.atp.congress;

import com.nexus.resources.TradingSide;
import java.util.Date;

public record CongressTransaction(String ticker,
                                  String companyName,
                                  int quantity,
                                  double price,
                                  TradingSide side,
                                  Date transactionDate,
                                  Date reportingDate) {
}
