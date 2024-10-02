echo "Enter your commit message:"
read commit_message

git add .
git commit -m "$commit_message"
git push origin main


if [ $? -ne 0 ]; then
    echo "Rebase in progress, attempting to resolve conflicts..."
    git pull --rebase
    git rebase --continue || git rebase --abort
    git push origin main
fi
