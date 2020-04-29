--
--! @file node.vhd
--! @brief Hermes generic NoC node.
--! @details Generates and connects an arbiter, an router and 5 output buffers.
--! One output buffer will be grounded (as exiting by the entering node is forbidden).
--! @author Angelo Elias Dalzotto (angelo.dalzotto@edu.pucrs.br)
--! @author Nicolas Lodea (nicolas.lodea@edu.pucrs.br)
--! @date 2020/04
-- 

library ieee;
use ieee.std_logic_1164.all;
use work.standards.all;

entity node is
	generic(
		address: reg_half_flit
	);
	port(
		clock:	in	std_logic;
		reset:	in	std_logic;

		--! Input ports
		clock_rx:	in	reg_port_no;
		rx:			in	reg_port_no;
		data_in:	in	port_no_reg_flit_size;
		credit_o:	out	reg_port_no;

		--! Output ports
		clock_tx: 	out	reg_port_no;
		data_out:	out port_no_reg_flit_size;
		credit_i:	in	reg_port_no;
		tx:			out	reg_port_no
	);
end entity;

architecture rtl of node is
	type port_no_port_no_reg_flit_size is array((PORT_NO-1) downto 0) of port_no_reg_flit_size;
	type port_no_reg_port_no is array((PORT_NO-1) downto 0) of reg_port_no;

	signal tx_router2buffer:		port_no_reg_port_no;
	signal credit_buffer2router:	port_no_reg_port_no;

	signal available_buffer2arbiter:	port_no_reg_port_no;
	signal ack_arbiter2buffer:			port_no_reg_port_no;
	
	signal data_buffer2arbiter:	port_no_port_no_reg_flit_size;

begin
	--! Generate all ports
	ports : for i in 0 to (PORT_NO-1) generate
		--! Clock TX: I really don't know why this is needed.
		clock_tx(i) <= clock;

		--! Arbiter
		arbiter: entity work.arbiter
		port map(
			clock => clock,
			reset => reset,

			data_out => data_out(i),
			tx => tx(i),
			credit_i => credit_i(i),

			data_in => data_buffer2arbiter(i),
			data_av => available_buffer2arbiter(i),
			data_ack => ack_arbiter2buffer(i)
		);

		--! Router
		router: entity work.router
		generic map(
			address => address
		)
		port map(
			clock => clock,
			reset => reset,

			clock_rx => clock_rx(i),
			rx => rx(i),
			data_in => data_in(i),
			credit_o => credit_o(i),

			credit_i => credit_buffer2router(i),
			tx => tx_router2buffer(i)
		);

		--! Output buffers
		buffers: for j in 0 to PORT_NO-1 generate
			connection: if i /= j generate
				ringbuffer: entity work.ringbuffer
				port map(
					clock => clock,
					reset => reset,

					clock_rx => clock_rx(i),
					rx => tx_router2buffer(i)(j),
					data_in => data_in(i),
					credit_o => credit_buffer2router(i)(j),

					data_out => data_buffer2arbiter(j)(i),
					data_av => available_buffer2arbiter(j)(i),
					data_ack => ack_arbiter2buffer(j)(i)
				);
			end generate;

			grounding: if i = j generate
				ringbuffer: entity work.ringbuffer
				port map(
					clock => '0',
					reset => reset,

					clock_rx => '0',
					rx => '0',
					data_in => (others => '0'),
					credit_o => credit_buffer2router(i)(j),

					data_out => data_buffer2arbiter(j)(i),
					data_av => available_buffer2arbiter(j)(i),
					data_ack => '0'
				);
			end generate;

		end generate;

	end generate;
end architecture;