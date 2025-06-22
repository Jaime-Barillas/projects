all: asm Fill.hack Mult.hack

asm: main.odin
	odin build . -out:asm

Fill.hack: asm Fill.asm
	./asm Fill.asm

Mult.hack: asm Mult.asm
	./asm Mult.asm
