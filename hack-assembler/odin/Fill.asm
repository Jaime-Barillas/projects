// Fill
// Fill entire screen with black when a keyboard key is pressed.
// Note: Screen is 512 by 256 (or 8192 "chunks" of 16 pixels).
// Consider: Not filling the entire screen in one go. Maybe
// batch the screen updates (e.g. Mobile device tile renderer).

// Initialize Variables
@colour
M=0     // White

@chunk  // Current screen pixel "chunk"
M=0

@8192
D=A
@total_chunks
M=D

// for (;;)
(LOOP)

// Select colour.
@colour
M=0        // Default to white
@KBD
D=M
@END_SELECT_COLOUR
D;JEQ
@colour
M=-1       // Black for entire chunk
(END_SELECT_COLOUR)

@chunk
M=0
(DRAW)
  // Loop condition
  @chunk
  D=M
  @total_chunks
  D=D-M
  @LOOP
  D;JGE

  // Fill screen
  @chunk
  D=M
  @SCREEN
  D=D+A
  @loc
  M=D
  @colour
  D=M
  @loc
  A=M
  M=D

  // Increment chunk
  @chunk
  M=M+1

@DRAW
0;JMP

@LOOP
0;JMP