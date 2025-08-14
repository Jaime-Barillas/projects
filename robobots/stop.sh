#!/usr/bin/env sh
set -euxo pipefail

project_name="robobots"

podman pod stop "${project_name}"
