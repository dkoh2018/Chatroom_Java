#!/bin/bash

# Ask for a commit message
echo "Enter your commit message:"
read commit_message

# Add changes
git add .

# Commit with the provided message
git commit -m "$commit_message"

# Push changes to the main branch
git push origin main

# If there is a rebase conflict, it will try to fix it automatically
if [ $? -ne 0 ]; then
    echo "Rebase in progress, attempting to resolve conflicts..."
    git pull --rebase
    git rebase --continue || git rebase --abort
    git push origin main
fi
