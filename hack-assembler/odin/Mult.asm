// Mult: R2 = R0 * R1
// Multiplication by repeated addition.

// Clear/Declare Variables
@counter
M=0

@2
M=0

D=0

// Add R0 to R2 R1 times.
(LOOP)
// Are we done?
@R1
D=D-M
@END
D;JGE

// Addition...
@R0
D=M
@2
M=D+M

// Counter increment...
@counter
MD=M+1

// Loop...
@LOOP
0;JMP

// Done.
(END)
@END
0;JMP
