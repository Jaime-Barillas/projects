#!/usr/bin/env bash
set -euxo pipefail

# See: https://web.archive.org/web/20240411090241/https://dev.to/deathroll/trusted-self-signed-tls-certificates-for-dummies-w-thorough-explanations-included-da7
# See Also: https://github.com/ChristianLempa/cheet-sheets/blob/main/misc/ssl-certs.md

# TODO: + non-interactive
#       + No password for server certificate
#       + indicate certificate is for server only?
#       + ensure file pertmissions and owner is correctly set
#       + generate keys and certs in subdirectory.

# Root CA Certificate and Key
openssl genrsa -out CA-key.pem 4096
openssl req -new -x509 -sha256 -days 365 -key CA-key.pem -out CA-cert.pem \
        -addext 'subjectAltName=DNS:ca.gemini.internal'

# Certificate Signing Request
openssl genrsa -out gemini-key.pem 4096
openssl req -new -sha256 -key gemini-key.pem -out gemini.csr

echo 'subjectAltName=DNS:*.gemini.internal' > gemini-cert.cnf

# Server Certificate
openssl x509 -req -sha256 -days 365 -in gemini.csr -CA CA-cert.pem -CAkey CA-key.pem -out gemini-cert.pem -extfile gemini-cert.cnf

# https://certificatetools.com
openssl req -new -nodes -key priv.key -config csrconfig.txt -nameopt utf8 -utf8 -out cert.csr
