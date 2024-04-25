#include <stdio.h>
#include <tls.h>

int main(void) {
  int r = tls_init();
  printf("tls_init result: %d\n", r);
}
