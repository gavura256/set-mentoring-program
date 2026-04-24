# System Prompt: CLAUDE.md - Repository Guide

⚡️ **[IMPORTANT] Always begin each task in Plan Mode unless the change is trivial.**  
This ensures clarity, reduces mistakes, and optimizes context usage.  
Refer to and follow ["Claude Code Best Practices"](https://code.claude.com/docs/en/best-practices) throughout.

---

## Documentation Index

- All Claude Code documentation:  
  https://code.claude.com/docs/llms.txt  
  Use this as the starting point for discovering available guides and details.

---

## Instructions

- **Start every non-trivial task in Plan Mode.** Explore, generate a plan, and obtain approval before implementing any changes.
- **Before proposing a solution to the user, always verify it.**
  - Run all applicable tests, commands, or checks.
  - Critically assess your own results: If the solution is difficult to fix, fragile, or clearly poor quality, **abandon it and restart from a new plan**.
  - Only present solutions you have verified are correct, effective, and maintainable.
- **All new features must be fully covered with appropriate tests.**
  - Write unit tests and integration tests for all business logic, workflows, and data operations.
  - If a feature includes or changes UI, provide automated UI tests as needed.
  - Do not propose a new feature as "done" without comprehensive test coverage.
- Keep all prompt context minimal and focused. Reference files directly (e.g., `@src/BookingService.java`) rather than pasting code blocks.
- Prefer verifiable work—provide test cases, command outputs, or expected results whenever possible.
- Do not make changes that violate documented business rules, security, or repository conventions.
- If requirements are ambiguous, pause for clarification before proceeding.

---

## Steps to Follow

1. **Plan Mode First**
  - Enter Plan Mode to:
    - Explore: Read relevant files or documentation.
    - Draft a step-wise implementation plan.
    - Present your plan _before_ modifying any code.
  - Skip planning only for trivial, well-scoped, or one-line fixes (e.g. typo correction).
  - Example Plan Mode prompts:
    - _"Explore how booking creation works in `BookingService`. Summarize the flow and list all business rule checks."_
    - _"Review `ProductController` endpoints. Plan how to add an endpoint for bulk product updates and specify the affected files."_
    - _"Investigate the bug where user deletion is incorrectly allowed. Create a plan to reproduce and fix it."_
2. **Context Management**
  - Use `/clear` between unrelated tasks or when context gets noisy.
  - Minimize file reads and only refer to what's relevant.
3. **Implementation & Verification**
  - Only switch to Normal Mode and code _after_ a plan is approved or confirmed.
  - Implement changes in single-purpose, small increments.
  - Follow repo build/test/run procedures (`./mvnw` scripts, etc.), and adhere to N-tier patterns.
  - **After implementation, always verify your solution:**
    - Run tests, scripts, or manual checks.
    - Review the approach and result; if it is subpar or requires excessive effort to clean up, **restart with a new plan** instead of attempting to patch a bad solution.
    - Only propose solutions you have verified as correct and practical.
  - **For all new features:**
    - Create or update unit tests.
    - Add or update integration and (if applicable) UI tests.
    - Ensure all new/affected code is fully covered; do not consider a feature complete without comprehensive, passing tests.
4. **Commit/PR**
  - Write descriptive commit messages.
  - Follow repository PR and branch procedures.

---

## Constraints

- **Do NOT skip Plan Mode** except for trivial changes.
- Do not propose or submit unverified or broken solutions.
- If verification fails or solution is poor, be proactive: drop the current approach, clear unnecessary context, and begin again with a revised plan.
- No repository lookups in mappers.
- Do not change endpoint paths, code structures, or conventions without explicit instruction.
- Do not bloat context; reference rather than paste.
- If context, scope, or requirements are unclear, always ask for user input or clarification before moving forward.

---

## Verification & Success Criteria

- Every non-trivial change must include at least one verifiable step: a test, command output, or comparison to a defined expected output.
- **All new features must have passing unit, integration, and UI tests (if applicable).**
- Summarize and note pass/fail after verification.
- Only share or propose verified, effective, and well-tested solutions.

---

## Use Cases

- **Add a Feature:** Plan the change and review with the user, then implement, write comprehensive tests (unit/integration/UI as needed), and run all tests.
- **Fix a Bug:** Explore and reproduce, plan the approach, then implement, write or expand tests as needed, and verify.
- **Refactor:** List all impacted areas, plan the migration, execute, update tests as necessary, and verify.
- **Answer Questions:** Explore, provide concise answers with direct references (not code dumps).

---

## Example Plan Mode Prompts

- `"Explore @src/service/ProductService.java and summarize how product stock is checked. Plan the changes needed to centralize stock validation."`
- `"Investigate controller exception handling patterns. Summarize existing flows and plan an update to standardize error responses."`
- `"Read @src/AuthController.java for password logic. Plan how to enforce stronger validation rules, including test cases."`

---

## Modularization Guidance

- Extract advanced or recurring workflows into `.claude/skills/` as your usage evolves.  
  For example, implement `.claude/skills/git-flow/SKILL.md` for team-wide PR etiquette, or `.claude/skills/security-audit/SKILL.md` for reusable security review patterns.
- Regularly prune/iterate CLAUDE.md to keep focus on context efficiency and high-leverage rules.

---

## Related Resources

- [Claude Code Best Practices](https://code.claude.com/docs/en/best-practices)
- [How Claude Code Works](https://code.claude.com/docs/en/how-claude-code-works)
- [Context Management](https://code.claude.com/docs/en/context-window)
- [Features Overview](https://code.claude.com/docs/en/features-overview)