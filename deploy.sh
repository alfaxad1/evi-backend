#!/bin/bash

# --- Configuration Variables ---
REMOTE_HOST="admin@91.99.63.98"
APP_DIR="/home/admin/loanapp.co.ke"
TARGET_JAR_NAME="loanApp-0.0.1-SNAPSHOT.jar"
TARGET_JAR_PATH="target/${TARGET_JAR_NAME}"
APP_JAR_PATH="${APP_DIR}/${TARGET_JAR_NAME}"

# 1. Build the new JAR file (assuming success)
echo "-> Building new JAR (skipping tests)..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "🚨 Build failed. Aborting deployment."
    exit 1
fi

# 2. Preparation: Temporarily give 'admin' ownership of the directory
echo "-> Setting temporary ownership to 'admin' to allow file transfer..."
# Requires 'sudo NOPASSWD: /usr/bin/chown' permission.
ssh "${REMOTE_HOST}" "sudo chown -R admin:admin \"${APP_DIR}\""

# 3. Deploy the JAR directly
echo "-> Copying JAR directly to application directory: ${APP_DIR}..."
# This SCP command now runs successfully because 'admin' is the owner.
scp "${TARGET_JAR_PATH}" "${REMOTE_HOST}:${APP_DIR}/"

# Check if the SCP transfer was successful
if [ $? -ne 0 ]; then
    echo "🚨 SCP transfer failed. Aborting."
    # WARNING: If this fails, ownership remains 'admin', which might break service startup.
    exit 1
fi

# 4. Finalization: Fix permissions (back to www-data) and restart
echo "-> Restoring runtime permissions and restarting service..."
ssh "${REMOTE_HOST}" "
    # 4a. Restore Runtime Permissions
    # Give ownership back to 'www-data' for application runtime safety.
    # Requires 'sudo NOPASSWD: /usr/bin/chown' permission.
    sudo chown -R www-data:www-data \"${APP_DIR}\"

    # 4b. Restart the Service
    # Requires 'sudo NOPASSWD: /bin/systemctl restart loanapp.service' permission.
    sudo systemctl restart loanapp.service

    echo '✅ Service restart initiated.'
"

echo "--- Deployment Complete ---"