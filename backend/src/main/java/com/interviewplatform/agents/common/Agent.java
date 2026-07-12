package com.interviewplatform.agents.common;

/**
 * Shared agent abstraction that all AI evaluation agents and question generators must implement.
 */
public interface Agent {
    
    /**
     * Executes the agent's logic given the current interview context.
     *
     * @param context the current state of the interview
     * @return the result of the agent's execution
     */
    AgentResult execute(InterviewContext context);
}
