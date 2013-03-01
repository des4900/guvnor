package org.kie.guvnor.testscenario.backend.server;

import java.util.HashSet;
import java.util.Set;

import org.kie.event.rule.AfterMatchFiredEvent;
import org.kie.event.rule.AgendaEventListener;
import org.kie.event.rule.AgendaGroupPoppedEvent;
import org.kie.event.rule.AgendaGroupPushedEvent;
import org.kie.event.rule.BeforeMatchFiredEvent;
import org.kie.event.rule.MatchCancelledEvent;
import org.kie.event.rule.MatchCreatedEvent;
import org.kie.event.rule.RuleFlowGroupActivatedEvent;
import org.kie.event.rule.RuleFlowGroupDeactivatedEvent;

/**
 * Measure the rule coverage.
 */
public class RuleCoverageListener implements AgendaEventListener {

    final Set<String> rules;
    private int       totalCount;

    /**
     * Pass in the expected rules to fire.
     * @param expectedRuleNames
     */
    public RuleCoverageListener(HashSet<String> expectedRuleNames) {
        this.rules = expectedRuleNames;
        this.totalCount = expectedRuleNames.size();
    }

    /**
     * @return A set of rules that were not fired.
     */
    public String[] getUnfiredRules() {
        return rules.toArray( new String[rules.size()] );
    }

    public int getPercentCovered() {
        float left = totalCount - rules.size();

        return (int) ((left / totalCount) * 100);
    }

    @Override
    public void matchCreated(MatchCreatedEvent event) {
    }

    @Override
    public void matchCancelled(MatchCancelledEvent event) {
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        this.rules.remove( event.getMatch().getRule().getName() );
    }

    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
    }

    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
    }

    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
    }

    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }

    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
    }

}
