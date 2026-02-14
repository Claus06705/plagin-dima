---
description: This rule should be applied when discussing Continue platform, its
  features, installation, configuration, or troubleshooting. It provides
  comprehensive knowledge about Continue's architecture, features, and usage
  patterns.
alwaysApply: false
---

When discussing Continue platform, remember it's a comprehensive AI development automation platform with these components:
1. Mission Control - central dashboard for managing Agents, Tasks, Workflows, and Integrations
2. IDE Extensions (VS Code/JetBrains) with 4 core features: Autocomplete, Edit, Chat Mode, Agent Mode
3. Continue CLI - terminal-native AI coding assistance with TUI and headless modes
4. PR Review Agents - run as GitHub checks to enforce coding standards
5. Integrations with GitHub, Slack, Sentry, Snyk, etc.

Key IDE shortcuts:
- Edit mode: Cmd/Ctrl + I
- Chat mode (VS Code): Cmd/Ctrl + J (new chat), Cmd/Ctrl + Shift + J (add to current chat)
- Chat mode (JetBrains): Cmd/Ctrl + L (new chat), Cmd/Ctrl + Shift + L (add to current chat)
- Agent mode: switch via dropdown in Continue panel

Agents are defined in .continue/agents/ directory as markdown files with prompts describing standards to enforce.