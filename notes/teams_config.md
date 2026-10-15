# Microsoft Teams Notifications for Claude Code (Workflows Webhook)

Get a Teams message whenever Claude Code is waiting on you, either for a permission prompt or because it has gone idle waiting for input.

> [!NOTE]
> Teams' old **Incoming Webhook** connector (Office 365 Connectors) is being retired. This guide uses a **Workflows** (Power Automate) webhook instead. Workflows expect an Adaptive Card payload rather than plain `{"text": ...}`, and `teams-notify.js` already sends that format.

**What you'll set up:**

1. A Teams Workflow that posts to a channel when it receives a webhook request
2. The workflow URL, stored privately in your Claude Code settings
3. A `Notification` hook that calls `teams-notify.js`
4. A test to confirm it works

---

## Step 1: Pick or create a channel

1. In Teams, open the team you want to use.
2. Use an existing channel, or create one: **⋯** next to the team name → **Add channel** → name it `Claude Notifications`.

## Step 2: Create the workflow

1. Click **⋯** (More options) next to the channel name, then **Workflows**.
2. Choose the template **Post to a channel when a webhook request is received**.
3. Name it `Claude Code` and click **Next**.
4. Confirm the **Team** and **Channel**, then click **Add workflow**.
5. Copy the URL shown on the confirmation screen, then click **Done**. It looks like this:

   ```text
   https://<region>.logic.azure.com:443/workflows/.../triggers/manual/paths/invoke?api-version=...&sig=XXXX
   ```

   Newer tenants may get a `...environment.api.powerplatform.com/...` URL instead. Both work.

> [!WARNING]
> The workflow URL is a secret. The `sig=` part works like a password, so anyone who has the URL can post to your channel. Don't paste it into chat, tickets, or any file that gets committed.

If you lose the URL, open <https://make.powerautomate.com>, go to **My flows**, open the flow, and click **Edit**. Expand the first step (**When a Teams webhook request is received**) to see the URL again.

> [!TIP]
> Messages are posted by the Workflows app on behalf of the account that created the flow. If your organization has disabled Workflows or Power Automate, the template won't be offered. Ask your Teams admin to enable it.

## Step 3: Store the URL in a personal settings file

Don't put the URL in `.claude/settings.json`, because that file is committed to Git. Use one of these instead:

| File | Scope |
|---|---|
| `.claude/settings.local.json` | This project only, not meant to be committed |
| `~/.claude/settings.json` | Your user settings, applies to all projects |

Add an `env` block:

```json
{
  "env": {
    "TEAMS_WEBHOOK_URL": "https://<region>.logic.azure.com:443/workflows/...&sig=XXXX"
  }
}
```

If the file already has content (such as `permissions`, or `SLACK_WEBHOOK_URL` in an existing `env` block), add to it. Don't replace the file or add a second `env` block.

**Check that the local file really is ignored by Git** before you save the URL in it:

```bash
git check-ignore -v .claude/settings.local.json
```

- It prints a matching `.gitignore` rule: you're safe.
- It prints nothing: the file is **not** ignored. Add `.claude/settings.local.json` to `.gitignore`. If the file is already tracked, also run `git rm --cached .claude/settings.local.json` before you save the URL in it.

## Step 4: Configure the hook

The hook configuration contains no secrets, so it can go in the shared `.claude/settings.json`. Add a `Notification` entry inside the existing `"hooks"` block:

```json
"Notification": [
  {
    "matcher": "permission_prompt|idle_prompt",
    "hooks": [
      {
        "type": "command",
        "command": "node \"$CLAUDE_PROJECT_DIR/.claude/hooks/teams-notify.js\""
      }
    ]
  }
]
```

- `matcher` limits the hook to two events: permission prompts and idle prompts.
- `$CLAUDE_PROJECT_DIR` makes the path work from any working directory. Keep the quotes, because the path may contain spaces.
- `teams-notify.js` must exist at `.claude/hooks/teams-notify.js`. It reads `TEAMS_WEBHOOK_URL` and exits quietly when the variable isn't set, so teammates without a webhook aren't affected.
- To notify both Slack and Teams, list both scripts in the same `hooks` array. See [slack_config.md](slack_config.md) for the Slack side.

Restart Claude Code after you edit the settings, because hooks are loaded at startup.

## Step 5: Test it

1. Start `claude` in the project.
2. Ask for something that triggers a permission prompt, such as a command covered by an `ask` rule like `npm install`.
3. A card should arrive in your Teams channel. It can take a few seconds longer than Slack, because the message goes through Power Automate.

### Nothing arrived?

First test the hook script directly from the project folder, outside Claude Code. This sends a sample payload exactly as Claude Code would.

**Bash / Git Bash:**

```bash
echo '{"notification_type":"permission_prompt","message":"Test from Claude Code","cwd":"demo-project"}' \
  | TEAMS_WEBHOOK_URL='<your workflow URL>' node .claude/hooks/teams-notify.js
```

**PowerShell:**

```powershell
$env:TEAMS_WEBHOOK_URL = '<your workflow URL>'
'{"notification_type":"permission_prompt","message":"Test from Claude Code","cwd":"demo-project"}' | node .claude/hooks/teams-notify.js
```

Unlike Slack, a Workflows webhook doesn't reply `ok`. It returns HTTP `202 Accepted` with an empty body, and the script ignores the response either way. To see whether the post succeeded, open the flow at <https://make.powerautomate.com> and check its **Run history**:

| Symptom | Check |
|---|---|
| No run in the history | The request never reached the flow. Copy the URL again from the workflow, because it may be mistyped or the flow may be turned off. |
| Run shows **Failed** | Open the run to see which step failed. The usual cause is a payload that isn't an Adaptive Card, such as a plain `{"text":"..."}` body. |
| Run succeeded, no card in channel | Check that the flow posts to the channel you expect, and that the account that created the flow still has access to it. |
| Script test works, no hook message | Run `/hooks` in Claude Code to confirm the `Notification` hook is loaded, and restart the session after settings changes. |
| Hook loaded, still nothing | Confirm `TEAMS_WEBHOOK_URL` is in the `env` block of a settings file Claude Code reads, and that `teams-notify.js` exists at the configured path. |
| Messages for the wrong events | Adjust the `matcher` value. |
