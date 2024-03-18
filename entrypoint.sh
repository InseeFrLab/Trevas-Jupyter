#!/bin/bash

# Check if the environment variable is set
if [ -n "$INIT_PROJECT_URL" ]; then
    echo "Environment variable for initial project is set"
    # Download the script from the HTTPS URL
    wget -O script.sh "$INIT_PROJECT_URL"
    # Make the script executable
    chmod +x script.sh
    # Run the downloaded script
    ./script.sh
else
    echo "Environment variable for initial project is not set."
fi

# Start the command passed to the entrypoint
exec "$@"
