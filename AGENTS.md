# AI Agent Git Identity & Commit Rules

Whenever an AI coding assistant (pi agent, Antigravity, etc.) is asked to commit or push changes:
1. **Always use the CS-Orion account identity**:
   - Name: `CS-Orion`
   - Email: `codingsolver0@gmail.com`

2. **Commit Command Execution**:
   Always execute git commit commands with inline author configuration:
   ```bash
   git -c user.name="CS-Orion" -c user.email="codingsolver0@gmail.com" commit --author="CS-Orion <codingsolver0@gmail.com>" -m "<commit message>"
   ```

3. **Critical Guardrail**:
   - NEVER modify or overwrite the user's global git configuration (`git config --global`).
   - The user's manual commits in terminal must continue to use their personal identity (`Lanvry <wardilanang46@gmail.com>`).
