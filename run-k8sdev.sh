#!/usr/bin/env bash
set -euo pipefail

exec mvn spring-boot:run -Dspring-boot.run.profiles=k8sdev "$@"
