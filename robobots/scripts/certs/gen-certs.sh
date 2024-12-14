## robobots CA
openssl genpkey -outform PEM -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out priv.key
openssl req -new -nodes -key priv.key -config csrconfig.txt -nameopt utf8 -utf8 -out cert.csr
openssl req -x509 -nodes -in cert.csr -days 365 -key priv.key -config certconfig.txt -extensions req_ext -nameopt utf8 -utf8 -out cert.crt


## robobots API
openssl genpkey -outform PEM -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out priv.key
openssl req -new -nodes -key priv.key -config csrconfig.txt -nameopt utf8 -utf8 -out cert.csr
openssl x509 -req -in cert.csr -days 365 -CA ca.crt -CAkey priv.key -extfile certconfig.txt -extensions req_ext -CAserial /tmp/tmp-1-LLfkBMmT6HzA -CAcreateserial -nameopt utf8 -sha256 -out cert.crt


## robobots admin interface
openssl genpkey -outform PEM -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out priv.key
openssl req -new -nodes -key priv.key -config csrconfig.txt -nameopt utf8 -utf8 -out cert.csr
openssl x509 -req -in cert.csr -days 365 -CA ca.crt -CAkey priv.key -extfile certconfig.txt -extensions req_ext -CAserial /tmp/tmp-1-N8hhFoneDLAL -CAcreateserial -nameopt utf8 -sha256 -out cert.crt


## robobots Postgres SSL cert
openssl genpkey -outform PEM -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out priv.key
openssl req -new -nodes -key priv.key -config csrconfig.txt -nameopt utf8 -utf8 -out cert.csr
openssl x509 -req -in cert.csr -days 365 -CA ca.crt -CAkey priv.key -extfile certconfig.txt -extensions req_ext -CAserial /tmp/tmp-1-U7wKm3pDU77a -CAcreateserial -nameopt utf8 -sha256 -out cert.crt

