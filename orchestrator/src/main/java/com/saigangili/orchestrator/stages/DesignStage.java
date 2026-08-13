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
 * STUB — Phase 2. Dependencies differ by scenario: greenfield/ambiguous
 * runs depend only on "requirements"; brownfield runs additionally
 * depend on "codebase_reasoning" (wired in GraphFactory).
 */
public class DesignStage implements Stage {

    private final List<String> dependsOn;

    public DesignStage(List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    @Override
    public String name() {
        return "design";
    }

    @Override
    public List<String> dependsOn() {
        return dependsOn;
    }

    @Override
    public boolean requiresApproval() {
        return true;
    }

    @Override
    public StageResult execute(StageContext context) throws Exception {
        System.out.println("[design] Designing API contract and data model");
        Thread.sleep(300);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("api_contract", "STUB: POST /shorten, GET /{code}, GET /{code}/analytics");
        output.put("data_model", "STUB: ShortUrl(code, longUrl, createdAt), ClickEvent(code, timestamp)");
        output.put("short_code_strategy", "STUB: base62 counter");
        output.put("caching_strategy", "STUB: none yet — deferred to reliability phase");

        DecisionEntry entry = new DecisionEntry(
                Instant.now(),
                "Chose base62 counter for short codes over random+collision-check",
                "Stub stage — placeholder reasoning; real LLM call added in Phase 3");

        return StageResult.of(output, entry);
    }
}
