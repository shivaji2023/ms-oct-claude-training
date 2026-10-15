#!/usr/bin/env node
// Notification hook: posts to a Microsoft Teams channel via a Workflows
// (Power Automate) webhook when Claude needs attention (a permission prompt,
// or an idle session waiting on input).
const https = require('https');

let raw = '';
process.stdin.on('data', chunk => (raw += chunk));
process.stdin.on('end', () => {
  const webhookUrl = process.env.TEAMS_WEBHOOK_URL;
  if (!webhookUrl) process.exit(0);

  let title = 'Claude Code needs your attention';
  let message = '';
  let project = '';
  try {
    const input = JSON.parse(raw);
    project = require('path').basename(input.cwd ?? '');
    title = `🤖 Claude Code — ${input.notification_type ?? 'notification'}`;
    message = input.message ?? '';
  } catch { /* use defaults */ }

  // Teams Workflows expect an Adaptive Card wrapped in a message envelope.
  const card = {
    type: 'message',
    attachments: [{
      contentType: 'application/vnd.microsoft.card.adaptive',
      content: {
        $schema: 'http://adaptivecards.io/schemas/adaptive-card.json',
        type: 'AdaptiveCard',
        version: '1.4',
        body: [
          { type: 'TextBlock', text: title, weight: 'Bolder', size: 'Medium', wrap: true },
          ...(message ? [{ type: 'TextBlock', text: message, wrap: true }] : []),
          ...(project ? [{ type: 'FactSet', facts: [{ title: 'Project', value: project }] }] : []),
        ],
      },
    }],
  };

  const req = https.request(webhookUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, timeout: 5000 });
  // A failed notification should never disrupt the session, so swallow errors.
  req.on('error', () => process.exit(0));
  req.on('timeout', () => req.destroy());
  req.end(JSON.stringify(card));
});
