#include <netdb.h>
#include <netinet/in.h>
#include <slog.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <tls.h>
#include <unistd.h>

#include "../common/log.h"

#define PORT "1965"

struct tls *tls_ctx = NULL;
int server_socket = -1;

void setup_tls(void) {
  struct tls_config *tls_config = tls_config_new();
  if (tls_config == NULL) {
    LOG_AND_DIE(tls_config_error(tls_config));
  }

  if (tls_init() != 0) {
    LOG_AND_DIE("(tls_init): Failed to initialize libtls.");
  }

  if (tls_config_set_cert_file(tls_config, "../cert/server.crt") != 0) {
    LOG_AND_DIE(tls_config_error(tls_config));
  }
  slog_debug("Using certificate ../cert/server.crt");

  if (tls_config_set_key_file(tls_config, "../cert/server.key") != 0) {
    LOG_AND_DIE(tls_config_error(tls_config));
  }
  slog_debug("Using key ../cert/server.key");

  tls_ctx = tls_server();
  if (tls_ctx == NULL) {
    LOG_AND_DIE("(tls_server): Failed to initialize the server context.");
  }

  if (tls_configure(tls_ctx, tls_config) != 0) {
    tls_free(tls_ctx);
    LOG_AND_DIE(tls_error(tls_ctx));
  }

  slog_info("TLS configuration complete.");

  tls_config_free(tls_config);
}

void setup_socket(void) {
  struct addrinfo hints;
  struct addrinfo *server_info;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_INET;
  hints.ai_socktype = SOCK_STREAM;

  if (getaddrinfo("localhost", PORT, &hints, &server_info) != 0) {
    tls_free(tls_ctx);
    LOG_AND_DIE("(getaddrinfo): Failed to fetch address info.");
  }

  for (;;) {
    if (server_info == NULL) {
      tls_free(tls_ctx);
      LOG_AND_DIE("(socket/bind): Failed to bind socket.");
    }

    server_socket = socket(server_info->ai_family,
                           server_info->ai_socktype,
                           server_info->ai_protocol);
    if (server_socket == -1) {
      slog_info("(socket): Unable to create socket. Trying again...");
      continue;
    }

    if (bind(server_socket, server_info->ai_addr, server_info->ai_addrlen) == -1) {
      close(server_socket);
      slog_info("(bind): Unable to bind socket. Trying again..");
      continue;
    }

    break;
  }

  if (listen(server_socket, 8) == -1) {
    close(server_socket);
    tls_free(tls_ctx);
    LOG_AND_DIE("(listen): Failed to listen for incomming connections.");
  }

  slog("Server listening for connections on 127.0.0.1:1965");

  freeaddrinfo(server_info);
}

int main(void) {
  const int enabled_slog_levels = SLOG_FLAGS_ALL;
  slog_init("server", enabled_slog_levels, 1);
  slog_config_t slog_conf;
  slog_config_get(&slog_conf);
  slog_conf.nKeepOpen = 1;
  slog_conf.nToFile = 1;
  slog_conf.nFlush = 1;
  slog_config_set(&slog_conf);

  setup_tls();
  setup_socket();


  struct sockaddr_in client_address;
  unsigned int client_address_length = sizeof(client_address);
  int client_socket;
  struct tls *tls_cctx;

  client_socket = accept(server_socket, (struct sockaddr*) &client_address, &client_address_length);
  if (client_socket == -1) {
    slog_error("Server: Failed to accept incomming connection.");
  }

  if (tls_accept_socket(tls_ctx, &tls_cctx, client_socket) == -1) {
    slog_error("Server: Failed to associate TLS context with incomming connection. %s", tls_error(tls_ctx));
  }


  //write
  tls_write(tls_cctx, "Hello, World!\n", 14);


  tls_close(tls_cctx);
  tls_free(tls_cctx);
  close(client_socket);
  close(server_socket);


  tls_close(tls_ctx);
  tls_free(tls_ctx);
  slog_destroy();
}
