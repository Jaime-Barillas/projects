#include <netinet/in.h>
#include <slog.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <tls.h>
#include <unistd.h>

#include "../common/log.h"

#define ERROR_TLS_INIT 1
#define PORT 1965

int main(void) {
  const int enabled_slog_levels = SLOG_FLAGS_ALL;
  slog_init("server", enabled_slog_levels, 1);
  slog_config_t slog_conf;
  slog_config_get(&slog_conf);
  slog_conf.nKeepOpen = 1;
  slog_conf.nToFile = 1;
  slog_conf.nFlush = 1;
  slog_config_set(&slog_conf);

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

  if (tls_config_set_key_file(tls_config, "../cert/server.key") != 0) {
    LOG_AND_DIE(tls_config_error(tls_config));
  }

  struct tls *tls_ctx = tls_server();
  if (tls_ctx == NULL) {
    LOG_AND_DIE("(tls_server): Failed to initialize the server context.");
  }

  if (tls_configure(tls_ctx, tls_config) != 0) {
    tls_free(tls_ctx);
    LOG_AND_DIE(tls_error(tls_ctx));
  }

  tls_config_free(tls_config);


  struct sockaddr_in server_address;
  int server_socket;

  memset(&server_address, 0, sizeof(server_address));
  server_address.sin_family = AF_INET;
  server_address.sin_port = htons(PORT);
  server_address.sin_addr.s_addr = htonl(INADDR_ANY);

  server_socket = socket(AF_INET, SOCK_STREAM, 0);
  if (server_socket == -1) {
    slog_fatal("Server init: Failed to create server socket.");

    slog_destroy();
    return 1;
  }

  if (bind(server_socket, (struct sockaddr*) &server_address, sizeof(server_address)) == -1) {
    slog_fatal("Server init: Failed to bind server socket.");

    close(server_socket);
    slog_destroy();
    return 1;
  }

  if (listen(server_socket, 1) == -1) {
    slog_fatal("Server init: Failed to listen on server socket.");

    close(server_socket);
    slog_destroy();
    return 1;
  }
  slog("Server listening for connections on port %d", PORT);


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
