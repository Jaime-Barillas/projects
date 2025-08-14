#!/usr/bin/env sh
set -euxo pipefail

project_name="robobots"

podman pod start "${project_name}"
