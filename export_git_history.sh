# Get the Git log in a format suitable for parsing
git_log=$(git log --format="%ad" --date=short | sort | uniq -c)

# Prepare the CSV header
echo "Date,Commit Count" > git_history.csv

# Loop through the Git log entries and extract date and commit count
while read -r line; do
    commit_count=$(echo "$line" | awk '{print $1}')
    date=$(echo "$line" | awk '{print $2}')
    echo "$date,$commit_count" >> git_history.csv
done <<< "$git_log"

echo "Git history exported to git_history.csv"