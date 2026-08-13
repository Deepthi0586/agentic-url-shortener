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
 * STUB — Phase 2. Runs in parallel with DocumentationStage (both depend
 * only on earlier stages, not on each other) — see GraphFactory and
 * docs/orchestration-design.md, section 2.
 */
public class TestingStage implements Stage {

    @Override
    public String name() {
        return "testing";
    }

    @Override
    public List<String> dependsOn() {
        return List.of("implementation");
    }

    @Override
    public StageResult execute(StageContext context) throws Exception {
        System.out.println("[testing] Running unit/integration tests");
        Thread.sleep(400);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("tests_run", 0);
        output.put("pass_rate", 1.0);

        DecisionEntry entry = new DecisionEntry(
                Instant.now(),
                "Test run completed",
                "Stub stage — placeholder reasoning; real test generation added in Phase 3");

        return StageResult.of(output, entry);
    }
}
