package com.saigangili.orchestrator.stages;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.saigangili.orchestrator.core.DecisionEntry;
import com.saigangili.orchestrator.core.Stage;
import com.saigangili.orchestrator.core.StageContext;
import com.saigangili.orchestrator.core.StageResult;

/** STUB — Phase 2. Real behavior generates the shortener-service source code. */
public class ImplementationStage implements Stage {

    @Override
    public String name() {
        return "implementation";
    }

    @Override
    public List<String> dependsOn() {
        return List.of("design");
    }

    @Override
    public StageResult execute(StageContext context) throws Exception {
        System.out.println("[implementation] Generating source code from design");
        Thread.sleep(300);

        Map<String, Object> output = new LinkedHashMap<>();
        output.put("files_changed", List.of("STUB: ShortUrlController.java", "STUB: ShortUrlService.java"));

        DecisionEntry entry = new DecisionEntry(
                Instant.now(),
                "Generated controller/service/repository from approved design",
                "Stub stage — placeholder reasoning; real LLM call added in Phase 3");

        return StageResult.of(output, entry);
    }
}
