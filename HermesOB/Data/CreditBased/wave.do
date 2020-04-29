add wave -divider {roteador 00 PORTA LOCAL}
add wave -format Logic /testbench/noc/clock(0)
add wave -format Logic /testbench/noc/rx(0)(4)
add wave -format Logic /testbench/noc/credit_o(0)(4)
add wave -format Literal -radix hexadecimal /testbench/noc/data_in(0)(4)

add wave -divider {roteador 20 PORTA LOCAL}
add wave -format Logic /testbench/noc/clock(0)
add wave -format Logic /testbench/noc/rx(2)(4)
add wave -format Logic /testbench/noc/credit_o(2)(4)
add wave -format Literal -radix hexadecimal /testbench/noc/data_in(2)(4)

add wave -divider {roteador 00 PORTA LESTE}
add wave -format Logic /testbench/noc/tx(0)(0)
add wave -format Logic /testbench/noc/credit_i(0)(0)
add wave -format Literal -radix hexadecimal /testbench/noc/data_out(0)(0)

add wave -divider {roteador 20 PORTA OESTE}
add wave -format Logic /testbench/noc/clock(0)
add wave -format Logic /testbench/noc/rx(2)(1)
add wave -format Logic /testbench/noc/credit_o(2)(1)
add wave -format Literal -radix hexadecimal /testbench/noc/data_out(2)(1)

add wave -divider {roteador 10 PORTA NORTE}
add wave -format Logic /testbench/noc/tx(1)(2)
add wave -format Logic /testbench/noc/credit_i(1)(2)
add wave -format Literal -radix hexadecimal /testbench/noc/data_out(1)(2)

add wave -divider {roteador 11 PORTA NORTE}
add wave -format Logic /testbench/noc/tx(4)(2)
add wave -format Logic /testbench/noc/credit_i(4)(2)
add wave -format Literal -radix hexadecimal /testbench/noc/data_out(4)(2)

add wave -divider {roteador 12 PORTA LOCAL}
add wave -format Logic /testbench/noc/tx(7)(4)
add wave -format Logic /testbench/noc/credit_i(7)(4)
add wave -format Literal -radix hexadecimal /testbench/noc/data_out(7)(4)
