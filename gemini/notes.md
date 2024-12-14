# Gemini - v0.16.1

The tentative specification is at http://web.archive.org/web/20240227121011/https://geminiprotocol.net/docs/specification.gmi

+ client-server protocol.
+ request-response transactions.
+ TCP/IP.
+ connections are closed at the end of a single transaction.
+ connections can't be reused.
+ port: 1965.

# 1.1 Gemini Transactions

There is one kind of transaction, roughly equivalent to an HTTP GET request.

1. [Client] opens connection
2. [Server] accepts connection
3. [Client/Server] perform TLS handshake
4. [Client] validates server certificate
5. [Client] sends request
6. [Server] sends response header
   + closes connection upon error
7. [Server] sends response body
8. [Server] closes connection (including TLS close_notify)
9. [Client] handles response

+ the client does not need to wait until the server closes the connection to
  start processing the response.
+ responsibility for closing the connection lies with the server.
+ connections should be closed immediately after completion of the response
  body.

# 1.2 Gemini URI Scheme

+ scheme: "gemini".
+ compatible with generic URI syntax from RFC 3986.
+ does NOT support all components of RFC 3986.
  - authority component required, userinfo subcomponent is NOT allowed.
  - host component is required.
  - port subcomponent is optional, defaults to 1965.
  - path, query, & fragment are allowed and have no special meaning beyond
    those given in RFC 3986.
  - an empty path is equivalent to: "/".
  - spaces are encoded as %20 not "+".
+ clients SHOULD normalise URIs before sending requests.
+ servers SHOULD normalise URIs before processing requests.
+ see section 6.2.3 of RFC 3986 for normalization rules.

# 2 Gemini Requests

+ single <CR><LF> terminated line.
+ URL followd by crlf: <URL><CR><LF>
+ <URL> is UTF-8.
+ <URL> is an absolute URL.
+ <URL> including the scheme, maximum of 1024 bytes.
+ MUST NOT begin with a byte-order-mark (U+FEFF).
+ Clients MUST NOT send anything after the first <CR><LF> in a request.
+ Servers MUST ignore anything sent after the first occurrence <CR><LF>.

# 3 Gemini Responses

+ single <CR><LF> terminated header line.
+ optionally followed by a response body.

## 3.1 Response Headers

+ <STATUS><SPACE><META><CR><LF>
+ <STATUS> - two-digit numeric status code, described below.
+ <SPACE> - single space character.
+ <META> - UTF-8 encoded string, max 1024 bytes, meaning is dependent on
  <STATUS>.
+ the response header as a whole, and the <META> sub-string, MUST NOT begin
  with a byte-order-mark (U+FEFF).
+ if <STATUS> is not in the range of SUCCESS status codes, the Server MUST
  close the connection after sending the header and MUST NOT send a response
  body.
+ if a Server sends a <STATUS> that is not a two-digit number or a <META> that
  is longer than 1024 bytes in length, the Client SHOULD close the connection
  and disregard the response header, informing the user of an error.

## 3.3 Response Bodies

+ raw content, text or binary.
+ no compression, chunking, or other content/transfer encoding.
+ the Server closes the connection after the final byte.
  - no "end of response" signal.
+ only sent if corresponding header indicates a SUCCESS status (2X).
  - <META> is a MIME media type as in RFC 2046.
+ media types transferred via gemini must be represented in their canonical
  form, except for "text" types.
+ canonically, media subtypes of "text" use <CR><LF> as a line break.
  - gemini allows plain <LF>.
  - gemini Clients MUST accept both <CR><LF> and <LF> line breaks in text media.
+ "text" MIME (sub)types with no explicit "charset" parameter should be assumed
  to be UTF-8.
+ Clients MUST support UTF-8 encoded, "text" MIME type response bodies.
+ Client MAY support other encodings.
+ Clients that receive a response in an encoding they don't support SHOULD
  inform the user instead of displaying garbage.
+ if <META> is an empty string, it MUST default to "text/gemini; charset=utf-8".

## 3.4 Response Body Handling

+ handling by Clients should be informed by the MIME type.
+ Clients should do something "sensible" based on the MIME type.
+ The "text/gemini" MIME type is described below.

## 3.2 Status Codes

This is a summary of the status code groups. For full information, see the
spec link at the top of this file.

+ two numeric digits.
+ related status codes share the same first digit.
+ the first digit alone provides enough information for a Client to process the
  response.
+ six categories
+ each category determines the semantics of the <MIME> header line:

+ **1X (INPUT)**  
  The Server requires a line of textual input from the user. The <META> line is
  a prompt to present to the user. The same resource should be requested again
  with the user's input included in the URL as a query component.
+ **2X (SUCCESS)**  
  Request handled successfully, response body follows the header. <META> is the
  MIME type (RFC 2046) of the body.
+ **3X (REDIRECT)**  
  Redirect, <META> contains the new URL of the resource (absolute or relative.)
  No response body.
+ **4X (TEMPORARY FAILURE)**  
  Request failed, no response body. <META> may contain additional information
  to be presented to the user.
+ **5X (PERMANENT FAILURE)**  
  Request failed, no response body. <META> may contain additional information
  to be presented to the user.
+ **6X (CLIENT CERTIFICATE REQUIRED)**  
  The requested resource requires a client certificate to access. <META> may
  contain additional information on certificate requirements or the reason the
  certificate was rejected.

# 4 TLS

+ TLS is mandatory.
+ The Server Name Indication (SNI) TLS extension is mandatory (for name-based
  virtual hosting.)
+ Servers must send a TLS `close_notify` prior to closing a connection after
  sending a complete response.
  - Disambiguates completed responses from prematurely closed responses.

# 4.1 TLS Version Requirements

+ TLS v1.2 minumum. Strongly prefer v1.3.
+ Clients are allowed to reject TLS v1.2 connections.

# 4.2 Verification of the Server Certificate

+ Clients can optionally validate TLS connections.
+ It's strongly Recommended to implement Trust On First Use.
  - The first time a client connects to a server, it accepts whatever
    certificate it receives.
  - The certificate's fingerprint and expiry date are saved, associated with
    the server's hostname.
  - On subsequent connections, the received certificate's fingerprint is
    checked against the stored fingerprint. If they don't match, a warning is
    displayed to the user.

# 4.3 Client Certificates

+ Clients can provide certificates to identify themselves if required.
+ Can be used as session identifiers for server-side state similar to HTTP
  cookies.
+ Can be used as account credentials for multi-user web applications.
+ Can be used to secure single-user, self-hosted applications.
+ Requests are typically made without a client certificate.
+ If a resource requires a certificate, but one is not included in the request,
  the server should respond with a 6X error code.
+ A generated client certificate is scoped for use to the hostname of the
  server, under the path it was generated for:
  - If a certificate was generated for `gemini://example.com/a/b`, that same
    certificate should also be used for `gemini://example.com/a/b/...`
+ The user decides when to delete or deactivate the certificate.
  - The expiry date of client certificates is _not_ taken into account.

# 5 The text/gemini MIME Type

TODO
