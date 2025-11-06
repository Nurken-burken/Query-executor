# GitHub Setup Instructions

Follow these steps to push this project to a private GitHub repository and add @dkaznacheev as a collaborator.

## Prerequisites

- Git installed on your machine
- GitHub account created
- Project already built and tested locally

## Step-by-Step Instructions

### 1. Initialize Git Repository

Open terminal in the project directory and run:

```bash
cd /path/to/query-executor
git init
```

### 2. Add Files to Git

```bash
git add .
```

### 3. Create Initial Commit

```bash
git commit -m "Initial commit: Query Executor Service

- REST API for storing and executing analytical SQL queries
- Read-only query enforcement (validation + read-only connection)
- Async execution support for long-running queries
- Result caching for performance optimization
- Comprehensive unit and integration tests
- Detailed design document with limitations and improvements"
```

### 4. Create Private Repository on GitHub

1. Go to https://github.com
2. Click the "+" icon in top right → "New repository"
3. Repository name: `query-executor-service` (or your preferred name)
4. Description: "Spring Boot REST API for analytical SQL query execution"
5. **Important**: Select "Private"
6. **Do NOT** initialize with README, .gitignore, or license (we already have these)
7. Click "Create repository"

### 5. Add Remote and Push

Copy the commands from GitHub (they'll look like this, but with your username):

```bash
git remote add origin https://github.com/YOUR_USERNAME/query-executor-service.git
git branch -M main
git push -u origin main
```

### 6. Add Collaborator (@dkaznacheev)

1. On your GitHub repository page, click "Settings"
2. In the left sidebar, click "Collaborators"
3. Click "Add people"
4. Search for username: `dkaznacheev`
5. Select the user and click "Add dkaznacheev to this repository"
6. They will receive an email invitation

### 7. Verify Everything is Pushed

Check your GitHub repository in the browser to ensure all files are there:
- ✅ README.md
- ✅ DESIGN.md
- ✅ src/ folder with all code
- ✅ build.gradle
- ✅ Tests
- ✅ .gitignore
- ✅ postman_collection.json

## Recommended Commit Structure

For future commits, use descriptive messages and keep them small:

### Good commit examples:
```bash
git commit -m "Add query timeout configuration"
git commit -m "Fix: Prevent SQL injection in query validation"
git commit -m "Docs: Update DESIGN.md with caching improvements"
git commit -m "Test: Add edge cases for async execution"
```

### Poor commit example:
```bash
git commit -m "Updated stuff"  # Too vague
git commit -m "Added everything"  # Should be multiple commits
```

## Additional Tips

### Create Feature Branches

For new features:
```bash
git checkout -b feature/query-timeout
# Make changes
git add .
git commit -m "Add query timeout support"
git push origin feature/query-timeout
```

### Check Status

```bash
git status
git log --oneline
```

### Undo Last Commit (if needed)

```bash
git reset --soft HEAD~1  # Keeps changes
git reset --hard HEAD~1  # Discards changes (careful!)
```

## Troubleshooting

### Authentication Issues

If you get authentication errors:
1. Use a Personal Access Token instead of password
2. GitHub → Settings → Developer settings → Personal access tokens
3. Generate new token with "repo" scope
4. Use the token as password when prompted

### Push Rejected

If your push is rejected:
```bash
git pull origin main --rebase
git push origin main
```

## Final Checklist

Before sharing the repository link:

- [ ] All code committed and pushed
- [ ] README.md is clear and complete
- [ ] DESIGN.md explains all design decisions
- [ ] Tests are passing (`./gradlew test`)
- [ ] Application runs successfully (`./gradlew bootRun`)
- [ ] @dkaznacheev added as collaborator
- [ ] Repository is set to Private

## Sharing the Repository

Send @dkaznacheev a message with:
1. Repository URL: `https://github.com/YOUR_USERNAME/query-executor-service`
2. Brief note: "I've added you as a collaborator to my Query Executor assignment. Please let me know if you have any questions!"

---

**Good luck with your interview! 🚀**
