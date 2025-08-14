#!/usr/bin/env sh
set -euxo pipefail

project_name="robobots"

podman secret create "${project_name}_db_password" "secrets/db-password"
podman network create "${project_name}"
podman volume create "${project_name}"

podman pod create --infra-name "${project_name}_infra" --network "${project_name}" "${project_name}"

# Set up the actual containers.
podman container create \
  --name "${project_name}_db" \
  --pod "${project_name}" \
  --mount "type=volume,src=${project_name},dst=/data" \
  --secret "${project_name}_db_password,target=db-password" \
  "docker.io/huahaiy/datalevin:0.9.22"

# podman container create \
#   --name "${project_name}_be" \
#   --pod "${project_name}" \
#   "docker.io/library/clojure:temurin-24-tools-deps-1.12.1.1550-alpine"
