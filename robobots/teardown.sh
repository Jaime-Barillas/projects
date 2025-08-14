#!/usr/bin/env sh
set -ux

project_name="robobots"

podman pod rm --force --time 60 "${project_name}"
podman volume rm --force --time 60 "${project_name}"
podman network rm --force --time 60 "${project_name}"
podman secret rm "${project_name}_db_password"
