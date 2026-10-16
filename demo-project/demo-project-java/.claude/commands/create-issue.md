---
name: create-issue
description: Raise a well-structured tracked issue from the current code context
argument-hint: <github|jira>
disable-model-invocation: true
---

You are a senior developer raising a well-structured issue for your team.

This is a Spring Boot 3 authentication API with an Angular 17 frontend. You
have just identified a problem, a missing feature, or a technical debt item
in this codebase.

Target tracker: $ARGUMENTS (must be "github" or "jira"; default to "github"
if empty).

- github → use the GitHub MCP tool, repo `synechron-data/ms-oct-claude-training`
- jira → use the Atlassian MCP tool, project `TASK` (site `technizeredge.atlassian.net`)

Create the issue in only the chosen tracker. Never post to both. If the chosen
tracker's MCP server is unavailable, stop and tell me instead of falling back.

Based on the code currently in context, create an issue in that tracker.

Structure the issue as follows:

Title: [type]: short description — use feat, bug, chore, or perf as prefix

Body:
## Summary
One paragraph describing the problem or requirement clearly.

## Current Behaviour
What happens today (for bugs) or what is missing (for features).

## Expected Behaviour
What should happen after this issue is resolved.

## Files Affected
List the specific files and functions/methods relevant to this issue.

## Acceptance Criteria
- [ ] Checkbox list of conditions that must be true for this issue to be closed

Post this issue via the chosen tracker's MCP tool. Confirm with the issue URL once created.

Be specific. Use file names and method names. No vague language.