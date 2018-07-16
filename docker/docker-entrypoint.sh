#!/usr/bin/env bash

# Exit immediately if a *pipeline* returns a non-zero status. (Add -x for command tracing)
set -e

# Process the argument to this container
case $1 in
    start)
        if [ "$DEBUG" == true ]; then
            set -x
            printenv
        fi
        if [ -z "$TTYD_PORT" ]; then
            TTYD_PORT=8080
        fi

        exec ttyd -p ${TTYD_PORT} bash
    ;;
esac

# Otherwise just run the specified command
exec "$@"