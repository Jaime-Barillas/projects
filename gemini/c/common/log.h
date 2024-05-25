#ifndef COMMON_LOG_H
#define COMMON_LOG_H


#include <slog.h>
#include <stdlib.h>

/** Prefer the LOG_AND_DIE macro over using this function directly.
 *  Log `message`. Shuts down slog and exits with error code 1.
 */
void log_and_die_impl(char *file, size_t line, const char *message) {
  const char *msg = message ? message : "NULL error message.";
  slog_display(SLOG_FATAL, 1, "[%s:%d] %s", file, line, msg);
  slog_destroy();
  exit(1);
}

 /** Log `message`. Shuts down slog and exits with error code 1.
  */
#define LOG_AND_DIE(message) log_and_die_impl(__FILE__, __LINE__, (message))


#endif
