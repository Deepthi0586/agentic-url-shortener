package com.saigangili.orchestrator.stages;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.saigangili.orchestrator.core.DecisionEntry;
import com.saigangili.orchestrator.core.Stage;
import com.saigangili.orchestrator.core.StageContext;
import com.saigangili.orchestrator.core.StageResult;

/**
 * STUB — Phase 2. Real behavior (interpret intent, identify ambiguity,
 * normalize into a clear engineering problem) is wired in during Phase 3
 * via a real Claude API call. This stub exists to prove the graph, gates,
 * retries, and approval checkpoint work correctly before adding that
 * complexity.
 */
public class RequirementsStage implements Stage {

    @Override
    public String name() {
        return "requirements";
    }

    @Override
    public List<String> dependsOn() {
        return List.of();
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }

    @Override
    public StageResult execute(StageContext context) throws Exception {
        System.out.println("[requirements] Interpreting: \"" + context.requirementRaw() + "\"");
        Thread.sleep(300);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("normalized_spec", "STUB: functional + non-functional requirements normalized from input");
        output.put("assumptions", List.of("STUB assumption — no auth required for v1"));
        output.put("open_ambiguities", List.of());

        DecisionEntry entry = new DecisionEntry(
                Instant.now(),
                "Normalized requirement into structured spec",
                "Stub stage — placeholder reasoning; real LLM call added in Phase 3");

        return StageResult.of(output, entry);
    }
}
