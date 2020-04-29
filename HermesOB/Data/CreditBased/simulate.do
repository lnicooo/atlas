if {[file isdirectory work]} { vdel -all -lib work }

vlib work
vmap work work

sccom -B/usr/bin -g NOC/modules/SC_InputModule.cpp
sccom -B/usr/bin -g NOC/modules/SC_OutputModule.cpp
sccom -B/usr/bin -g NOC/modules/SC_OutputModuleRouter.cpp
sccom -B/usr/bin -link

vcom -work work -93 -explicit NOC/hermes/constants.vhd
vcom -work work -93 -explicit NOC/hermes/standards.vhd
vcom -work work -93 -explicit NOC/hermes/router.vhd
vcom -work work -93 -explicit NOC/hermes/ringbuffer.vhd
vcom -work work -93 -explicit NOC/hermes/arbiter.vhd
vcom -work work -93 -explicit NOC/hermes/node.vhd
vcom -work work -93 -explicit NOC/hermes/noc.vhd
vcom -work work -93 -explicit testbench.vhd

vsim -t 10ps work.testbench

set StdArithNoWarnings 1
set StdVitalGlitchNoWarnings 1
