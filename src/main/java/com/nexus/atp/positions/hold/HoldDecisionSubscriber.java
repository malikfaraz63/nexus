package com.nexus.atp.positions.hold;

public interface HoldDecisionSubscriber {
    void notifyNewHoldDecision(HoldDecision decision);
}
