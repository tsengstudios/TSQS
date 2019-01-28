#!/bin/bash
set -o nounset
set -e

#delete the chores collection
echo "Deleting the chores collection under project"
firebase firestore:delete "chores" -r -y --project="$PROJECT_ID"

#create a test account test@mailinator.com
echo "Creating test accounts"
firebase auth:import accounts.json --hash-algo=SHA256 --rounds=1 --project="$PROJECT_ID"
