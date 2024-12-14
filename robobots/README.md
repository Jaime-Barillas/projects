# Robobots

A website for building Robo-Robots!

## Overview

```mermaid
flowchart TD
  db[Database]
  api[API]
  admin[Admin Interface]
  frontend[Robobots Website]

  api-->db
  admin-->api
  frontend-->api
```
