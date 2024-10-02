echo "Enter your commit message:"
read commit_message

git add .

git commit -m "$commit_message"

git fetch origin

git rebase origin/main

if [ $? -ne 0 ]; then
    echo "Rebase failed. Attempting to resolve conflicts..."
    git rebase --continue || git rebase --abort
fi

git push origin main
