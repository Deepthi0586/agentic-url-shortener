# Agentic URL Shortener

A prototype demonstrating agentic SDLC orchestration: an orchestration engine
that takes a software requirement, decomposes it into tasks, executes them
through a stateful, gated workflow, and produces a reviewable engineering
outcome — demonstrated by using it to build a URL shortener service across
greenfield, brownfield, and ambiguous scenarios.

## Repository Structure
### Why one repo?

The `orchestrator` and `shortener-service` are different kinds of artifacts —
the orchestrator is a build-time tool, the shortener is the production-style
service it builds. In a real engineering org, these would likely live in
**separate repositories** with independent versioning, CI/CD pipelines, and
access boundaries — especially in a regulated financial context, where
change-control and audit trails are typically scoped per deployable service,
and a change to internal tooling shouldn't be able to trigger a redeploy of a
customer-facing API (or vice versa).

For this prototype, both are co-located in a single repo for reviewer
convenience — so the orchestration logic and the artifacts it produces can be
inspected together without navigating across multiple links. This is a
deliberate scoping decision for the assessment context, not a recommendation
for how this would be structured in production.

## Modules

- **`orchestrator/`** — see [docs/orchestration-design.md](docs/orchestration-design.md)
  for the stage graph, state schema, gates, and retry/rollback policy.
- **`shortener-service/`** — the URL shortener API, analytics, and reliability
  features, generated/modified by the orchestrator across the three scenarios.

## Status

Project scaffold in place. Orchestration design and stage implementation in
progress.
