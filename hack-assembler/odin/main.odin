package main

import "core:fmt"
import "core:io"
import "core:os"
import "core:strconv"
import "core:strings"

line := 0
parsing := Parsing.Line
stream: io.Reader
var_table := make(map[string]int)
next_free_slot := 16

Parsing :: enum {
	Line,
	Whitespace,
	Comment,
	Instruction,
	Label,
	Variable,
}

CInstr :: struct {
	comp: string,
	dest: string,
	jump: string,
}

//===== Predicates =====//

is_whitespace :: proc(c: u8) -> bool {
	switch c {
	// Cheat on Windows by interpreting \r as whitespace. This allows treating
	// newlines and line counting the same across Windows and Linux.
	case ' ', '\t', '\r':
		return true
	case:
		return false
	}
}

is_comment :: proc(c: u8) -> bool {
	switch c {
	case '/':
		return true
	case:
		return false
	}
}

is_line_end :: proc(c: u8) -> bool {
	switch c {
	case '\n':
		return true
	case:
		return false
	}
}

is_label_declaration :: proc(c: u8) -> bool {
	switch c {
	case '(':
		return true
	case:
		return false
	}
}

is_variable_reference :: proc(c: u8) -> bool {
	switch c {
	case '@':
		return true
	case:
		return false
	}
}

is_identifier_char :: proc(c: u8) -> bool {
	switch c {
	case '0' ..= '9', 'a' ..= 'z', 'A' ..= 'Z', '_':
		return true
	case:
		return false
	}
}

//===== Parsing Functions =====//

eat_whitespace :: proc() {
	next, err := io.read_byte(stream)
	for err == .None && is_whitespace(next) {
		next, err = io.read_byte(stream)
	}

	// Avoid rewinding EOF.
	if err != .EOF do io.seek(stream, -1, .Current)
}

eat_comment :: proc() {
	next, err := io.read_byte(stream)
	for err == .None && !is_line_end(next) {
		next, err = io.read_byte(stream)
	}

	// Avoid rewinding EOF.
	if err != .EOF do io.seek(stream, -1, .Current)
}

eat_line :: proc() {
	next, err := io.read_byte(stream)
	for err == .None && !is_line_end(next) {
		next, err = io.read_byte(stream)
	}

	if err != .EOF && next != ')' do io.seek(stream, -1, .Current)
}

parse_label :: proc() {
	label, alloc_err := strings.builder_make_len_cap(0, 64)
	defer strings.builder_destroy(&label)
	if alloc_err != .None {
		fmt.println("Failed to allocate label string builder")
		os.exit(1)
	}

	next, err := io.read_byte(stream)
	for err == .None && is_identifier_char(next) {
		strings.write_byte(&label, next)
		next, err = io.read_byte(stream)
	}

	label_str := strings.clone(strings.to_string(label))
	var_table[label_str] = line
	// Saw closing ')', no need to rewind.
	//io.seek(stream, -1, .Current)
}

parse_variable_reference :: proc(sb: ^strings.Builder) {
	var, alloc_err := strings.builder_make_len_cap(0, 64)
	defer strings.builder_destroy(&var)
	if alloc_err != .None {
		fmt.println("Failed to allocate var string builder")
		os.exit(1)
	}

	next, err := io.read_byte(stream)
	for err == .None && is_identifier_char(next) {
		strings.write_byte(&var, next)
		next, err = io.read_byte(stream)
	}

	var_str := strings.clone(strings.to_string(var))
	loc, exists := var_table[var_str]
	if !exists {
		loc = next_free_slot
		var_table[var_str] = loc
		next_free_slot += 1
	}

	fmt.sbprintfln(sb, "%16b", loc)
	// Avoid rewinding EOF.
	if err != .EOF do io.seek(stream, -1, .Current)
}

parse_a_instruction :: proc(sb: ^strings.Builder) {
	instr, alloc_err := strings.builder_make_len_cap(0, 64)
	defer strings.builder_destroy(&instr)
	if alloc_err != .None {
		fmt.println("Failed to allocate var string builder")
		os.exit(1)
	}

	next, err := io.read_byte(stream)
	for err == .None && is_identifier_char(next) {
		strings.write_byte(&instr, next)
		next, err = io.read_byte(stream)
	}

	addr_str := strconv.atoi(strings.to_string(instr))

	fmt.sbprintfln(sb, "%16b", addr_str)
	// Avoid rewinding EOF.
	if err != .EOF do io.seek(stream, -1, .Current)
}

extract_c_data :: proc(instr: string) -> (instr_info: CInstr) {
	parts := strings.split_multi(instr, {"=", ";"})

	if len(parts) == 3 {
		instr_info.dest = parts[0]
		instr_info.comp = parts[1]
		instr_info.jump = parts[2]
	} else if len(parts) == 2 {
		// If the second part contains a "J" then we got: <comp>;<jmp>
		// otherwise we got: <dest>=<comp>
		if strings.contains(parts[1], "J") {
			instr_info.comp = parts[0]
			instr_info.jump = parts[1]
		} else {
			instr_info.dest = parts[0]
			instr_info.comp = parts[1]
		}
	} else if len(parts) == 1 {
		instr_info.comp = parts[0]
	}

	return instr_info
}

parse_c_instruction :: proc(ascii: u8, sb: ^strings.Builder) {
	instr, alloc_err := strings.builder_make_len_cap(0, 64)
	defer strings.builder_destroy(&instr)
	if alloc_err != .None {
		fmt.println("Failed to allocate var string builder")
		os.exit(1)
	}

	strings.write_byte(&instr, ascii)

	next, err := io.read_byte(stream)
	for err == .None && !is_whitespace(next) && !is_line_end(next) && !is_comment(next) {
		strings.write_byte(&instr, next)
		next, err = io.read_byte(stream)
	}

	instr_info := extract_c_data(strings.to_string(instr))
	// TODO: Not hard code this bit.
	// odinfmt: disable
	strings.write_string(sb, "111")
	switch instr_info.comp {
	case "0":   strings.write_string(sb, "0101010")
	case "1":   strings.write_string(sb, "0111111")
	case "-1":  strings.write_string(sb, "0111010")
	case "D":   strings.write_string(sb, "0001100")
	case "A":   strings.write_string(sb, "0110000")
	case "M":   strings.write_string(sb, "1110000")
	case "!D":  strings.write_string(sb, "0001101")
	case "!A":  strings.write_string(sb, "0110001")
	case "!M":  strings.write_string(sb, "1110001")
	case "D+1": strings.write_string(sb, "0011111")
	case "A+1": strings.write_string(sb, "0110111")
	case "M+1": strings.write_string(sb, "1110111")
	case "D-1": strings.write_string(sb, "0001110")
	case "A-1": strings.write_string(sb, "0110010")
	case "M-1": strings.write_string(sb, "1110010")
	case "D+A": strings.write_string(sb, "0000010")
	case "D+M": strings.write_string(sb, "1000010")
	case "D-A": strings.write_string(sb, "0010011")
	case "D-M": strings.write_string(sb, "1010011")
	case "A-D": strings.write_string(sb, "0000111")
	case "M-D": strings.write_string(sb, "1000111")
	case "D&A": strings.write_string(sb, "0000000")
	case "D&M": strings.write_string(sb, "1000000")
	case "D|A": strings.write_string(sb, "0010101")
	case "D|M": strings.write_string(sb, "1010101")
	// Default to computing "0". TODO: Error reporting.
	case:       strings.write_string(sb, "0101010")
	}
	switch instr_info.dest {
	case "M":   strings.write_string(sb, "001")
	case "D":   strings.write_string(sb, "010")
	case "MD":  strings.write_string(sb, "011")
	case "A":   strings.write_string(sb, "100")
	case "AM":  strings.write_string(sb, "101")
	case "AD":  strings.write_string(sb, "110")
	case "AMD": strings.write_string(sb, "111")
	case:       strings.write_string(sb, "000")
	}
	switch instr_info.jump {
	case "JGT": strings.write_string(sb, "001")
	case "JEQ": strings.write_string(sb, "010")
	case "JGE": strings.write_string(sb, "011")
	case "JLT": strings.write_string(sb, "100")
	case "JNE": strings.write_string(sb, "101")
	case "JLE": strings.write_string(sb, "110")
	case "JMP": strings.write_string(sb, "111")
	case:       strings.write_string(sb, "000")
	}
	// odinfmt: enable

	strings.write_byte(sb, '\n')
	// Avoid rewinding EOF.
	if err != .EOF do io.seek(stream, -1, .Current)
}

//===== Main =====//

main :: proc() {
	if len(os.args) < 2 {
		fmt.println("Usage: asm <path-to-hack-file>")
		os.exit(1)
	}

	path := os.args[1]
	source, read_err := os.read_entire_file_from_filename_or_err(path)
	if read_err != nil {
		fmt.println("Failed to read", path)
		os.exit(1)
	}

	var_table["SP"] = 0
	var_table["LCL"] = 1
	var_table["ARG"] = 2
	var_table["THIS"] = 3
	var_table["THAT"] = 4
	var_table["R0"] = 0
	var_table["R1"] = 1
	var_table["R2"] = 2
	var_table["R3"] = 3
	var_table["R4"] = 4
	var_table["R5"] = 5
	var_table["R6"] = 6
	var_table["R7"] = 7
	var_table["R8"] = 8
	var_table["R9"] = 9
	var_table["R10"] = 10
	var_table["R11"] = 11
	var_table["R12"] = 12
	var_table["R13"] = 13
	var_table["R14"] = 14
	var_table["R15"] = 15
	var_table["SCREEN"] = 16384
	var_table["KBD"] = 24576

	reader := strings.Reader{}
	stream = strings.to_reader(&reader, string(source))

	// We'll assume hack assembly consists of only ASCII.
	ascii, err := io.read_byte(stream)
	for err == .None {
		switch {
		case is_whitespace(ascii):
			eat_whitespace()
		case is_comment(ascii):
			eat_comment()
		case is_line_end(ascii):
			break
		case is_label_declaration(ascii):
			parse_label()
		case is_variable_reference(ascii):
		case:
			/* C Instruction */
			eat_line()
			line += 1
		}

		ascii, err = io.read_byte(stream)
	}

	// Second pass
	parsing = .Line

	assembled, alloc_err := strings.builder_make_len_cap(0, 4096)
	defer strings.builder_destroy(&assembled)
	if alloc_err != .None {
		fmt.println("Failed to allocate builder")
		os.exit(1)
	}

	_, seek_err := io.seek(stream, 0, .Start)
	if seek_err != .None {
		fmt.println("Error initiating second pass")
		os.exit(1)
	}

	ascii, err = io.read_byte(stream)
	for err == .None {
		switch {
		case is_whitespace(ascii):
			eat_whitespace()
		case is_comment(ascii):
			eat_comment()
		case is_line_end(ascii):
			break
		case is_label_declaration(ascii):
			eat_line()
		case is_variable_reference(ascii):
			next, err := io.read_byte(stream)
			if err == .None {
				io.seek(stream, -1, .Current)

				switch next {
				case '0' ..= '9':
					parse_a_instruction(&assembled)
				case:
					parse_variable_reference(&assembled)
				}
			}
		case:
			parse_c_instruction(ascii, &assembled)
		}

		ascii, err = io.read_byte(stream)
	}

	out, out_alloc_err := strings.split(path, ".")
	outpath := strings.concatenate({out[0], ".hack"})
	os.write_entire_file(outpath, assembled.buf[:])
}

