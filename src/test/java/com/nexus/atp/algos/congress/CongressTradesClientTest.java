package com.nexus.atp.algos.congress;

import com.nexus.atp.algos.congress.api.CongressTradesFetcher;
import com.nexus.atp.algos.congress.api.CongressTransactionsSubscriber;
import com.nexus.atp.algos.congress.engine.CongressTradesEngineConfig;
import com.nexus.atp.algos.congress.engine.CongressTradesEngineSetting;
import com.nexus.atp.algos.congress.manager.CongressPositionsManager;
import com.nexus.atp.algos.congress.position.CongressPosition;
import com.nexus.atp.algos.congress.position.CongressPositionsSubscriber;
import com.nexus.atp.common.scheduled.ScheduledTimer;
import com.nexus.atp.common.transaction.TradingSide;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.nexus.atp.utils.Mock.captureArgument;

public class CongressTradesClientTest {
    private final CongressTradesEngineConfig config = new CongressTradesEngineConfig(
            CongressTradesEngineSetting.AUTO_RANKED,
            Duration.of(30, ChronoUnit.MINUTES),
            LocalTime.of(6, 0)
    );

    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();

    private final CongressPositionsSubscriber positionsSubscriber = context.mock(CongressPositionsSubscriber.class);
    private final CongressPositionsManager positionsManager = context.mock(CongressPositionsManager.class);
    private final CongressTradesFetcher tradesFetcher = context.mock(CongressTradesFetcher.class);
    private final ScheduledTimer scheduledTimer = context.mock(ScheduledTimer.class);

    private void buildClient() {
        new CongressTransactionsClient(
                config,
                positionsSubscriber,
                positionsManager,
                tradesFetcher,
                scheduledTimer
        );
    }

    @Test
    public void clientDoesLoadCongressPositionsToSubscriberAtScheduledTime() {
        final Runnable[] congressPositionsLoader = new Runnable[1];

        context.checking(new Expectations() {{
            oneOf(scheduledTimer).addScheduledCallback(with(any(LocalTime.class)), with(any(Runnable.class)));
            will(captureArgument(congressPositionsLoader, 1));

            ignoring(tradesFetcher);
        }});

        buildClient();

        Map<String, CongressPosition> dummyPositions = Map.of();
        context.checking(new Expectations() {{
            oneOf(positionsManager).getAllCongressPositions();
            will(returnValue(dummyPositions));

            oneOf(positionsSubscriber).initializeCongressPositions(dummyPositions);
        }});

        congressPositionsLoader[0].run();
    }

    @Test
    public void clientDoesNotifySubscriberOfNewPositions() {
        final CongressTransactionsSubscriber[] congressTransactionsSubscriber = new CongressTransactionsSubscriber[1];

        context.checking(new Expectations() {{
            ignoring(scheduledTimer);

            oneOf(tradesFetcher).subscribe(with(any(CongressTransactionsClient.class)));
            will(captureArgument(congressTransactionsSubscriber, 0));
        }});

        buildClient();

        List<CongressTransaction> congressTransactions = List.of(
                new CongressTransaction("AAPL", "M000355", 5, 30.19, TradingSide.BUY, Date.from(Instant.now()), Date.from(Instant.now())),
                new CongressTransaction("GOOGL", "M000355", 2000, 692.38, TradingSide.BUY, Date.from(Instant.now()), Date.from(Instant.now()))
        );

        Set<CongressPosition> positions = Set.of();
        context.checking(new Expectations() {{
            oneOf(positionsManager).addCongressTransaction(congressTransactions.getFirst());
            oneOf(positionsManager).addCongressTransaction(congressTransactions.getLast());

            oneOf(positionsManager).getNewCongressPositions();
            will(returnValue(positions));
            oneOf(positionsSubscriber).updateNewCongressPositions(positions);
        }});

        congressTransactionsSubscriber[0].onNewTransactions(congressTransactions);
    }
}
