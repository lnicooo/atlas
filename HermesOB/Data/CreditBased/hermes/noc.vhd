--
--! @file noc.vhd
--! @brief Hermes generic NoC.
--! @details Generates a NoC external interface that connects to the local ports.
--! Grounding occurs when there are no neighbor nodes.
--! @author ?
--! @author Angelo Elias Dalzotto (angelo.dalzotto@edu.pucrs.br)
--! @author Nicolas Lodea (nicolas.lodea@edu.pucrs.br)
--! @date 2020/04
-- 

library ieee;
use ieee.std_logic_1164.all;
use work.constants.all;
use work.standards.all;

entity noc is
	port(
		clock: in reg_node_no;
		reset: in std_logic;

		--! The external inputs are only for local ports
		clock_rx_local:	in	reg_node_no;
		rx_local:		in	reg_node_no;
		data_in_local:	in	node_no_reg_flit_size;
		credit_o_local: out	reg_node_no;

		--! The external outputs are only for local ports
		clock_tx_local:	out	reg_node_no;
		tx_local:		out	reg_node_no;
		data_out_local:	out	node_no_reg_flit_size;
		credit_i_local:	in	reg_node_no
	);
end entity;

architecture rtl of noc is
	--! Control and data signals for all ports from all nodes
	signal clock_tx: 	node_no_reg_port_no;
	signal tx:			node_no_reg_port_no;
	signal data_out:	node_no_port_no_reg_flit_size;
	signal credit_i:	node_no_reg_port_no;

	signal clock_rx:	node_no_reg_port_no;
	signal rx:			node_no_reg_port_no;
	signal data_in:		node_no_port_no_reg_flit_size;
	signal credit_o:	node_no_reg_port_no;

begin

	nodes: for i in 0 to NODE_NO-1 generate

		node: entity work.node
		generic map(
			address => router_address(i)
		)
		port map(
			clock => clock(i),
			reset => reset,

			--! Input Ports
			clock_rx => clock_rx(i),
			rx => rx(i),
			data_in	=> data_in(i),
			credit_o => credit_o(i),

			--! Output ports
			clock_tx => clock_tx(i),
			tx => tx(i),
			data_out => data_out(i),
			credit_i => credit_i(i)
		);

		--! Local port connections
		--! Local input port
		clock_rx(i)(LOCAL) <= clock_rx_local(i);
		rx(i)(LOCAL) <= rx_local(i);
		data_in(i)(LOCAL) <= data_in_local(i);
		credit_o_local(i) <= credit_o(i)(LOCAL);
		--! Local output port
		clock_tx_local(i) <= clock_tx(i)(LOCAL);
		tx_local(i) <= tx(i)(LOCAL); 
		data_out_local(i) <= data_out(i)(LOCAL);						
		credit_i(i)(LOCAL) <= credit_i_local(i);

		--! Ground east port of easternmost nodes
		east_grounding: if ((i+1) mod X_SIZE) = 0 generate
			rx(i)(EAST) <= '0';
			clock_rx(i)(EAST) <= '0';
			credit_i(i)(EAST) <= '0';
			data_in(i)(EAST) <= (others => '0');
		end generate;

		--! Connect east port of not easternmost nodes
		east_connection: if ((i+1) mod X_SIZE) /= 0 generate
			rx(i)(EAST) <= tx(i+1)(WEST);
			clock_rx(i)(EAST) <= clock_tx(i+1)(WEST);
			credit_i(i)(EAST) <= credit_o(i+1)(WEST);
			data_in(i)(EAST) <= data_out(i+1)(WEST);
		end generate;

		--! Ground west port of westernmost nodes
		west_grounding: if (i mod X_SIZE) = 0 generate
			rx(i)(WEST) <= '0';
			clock_rx(i)(WEST) <= '0';
			credit_i(i)(WEST) <= '0';
			data_in(i)(WEST) <= (others => '0');
		end generate;

		--! Connect west port of not westernmost nodes
		west_connection: if (i mod X_SIZE) /= 0 generate
			rx(i)(WEST) <= tx(i-1)(EAST);
			clock_rx(i)(WEST) <= clock_tx(i-1)(EAST);
			credit_i(i)(WEST) <= credit_o(i-1)(EAST);
			data_in(i)(WEST) <= data_out(i-1)(EAST);
		end generate;

		--! Ground north port of northernmost nodes
		north_grounding: if i >= (NODE_NO-X_SIZE) generate
			rx(i)(NORTH) <= '0';
			clock_rx(i)(NORTH) <= '0';
			credit_i(i)(NORTH) <= '0';
			data_in(i)(NORTH) <= (others => '0');
		end generate;

		--! Connect north port of not northernmost nodes
		north_connection: if i < (NODE_NO-X_SIZE) generate
			rx(i)(NORTH) <= tx(i+X_SIZE)(SOUTH);
			clock_rx(i)(NORTH) <= clock_tx(i+X_SIZE)(SOUTH);
			credit_i(i)(NORTH) <= credit_o(i+X_SIZE)(SOUTH);
			data_in(i)(NORTH) <= data_out(i+X_SIZE)(SOUTH);
		end generate;

		--! Ground sourth port of southernmost nodes
		south_grounding: if i < X_SIZE generate
			rx(i)(SOUTH) <= '0';
			clock_rx(i)(SOUTH) <= '0';
			credit_i(i)(SOUTH) <= '0';
			data_in(i)(SOUTH) <= (others => '0');
		end generate;

		--! Connect south port of not southernmost nodes
		south_connection: if i >= X_SIZE generate
			rx(i)(SOUTH) <= tx(i-X_SIZE)(NORTH);
			clock_rx(i)(SOUTH) <= clock_tx(i-X_SIZE)(NORTH);
			credit_i(i)(SOUTH) <= credit_o(i-X_SIZE)(NORTH);
			data_in(i)(SOUTH) <= data_out(i-X_SIZE)(NORTH);
		end generate;

	end generate;

	--! SystemC router sniffer
	router_output: Entity work.outmodulerouter
	port map(
		clock => clock(0),
		reset => reset,

		tx_r0p0 => tx(0)(EAST),
		out_r0p0 => data_out(0)(EAST),
		credit_ir0p0 => credit_i(0)(EAST),

		tx_r0p2 => tx(0)(NORTH),
		out_r0p2 => data_out(0)(NORTH),
		credit_ir0p2 => credit_i(0)(NORTH),

		tx_r1p0 => tx(1)(EAST),
		out_r1p0 => data_out(1)(EAST),
		credit_ir1p0 => credit_i(1)(EAST),

		tx_r1p1 => tx(1)(WEST),
		out_r1p1 => data_out(1)(WEST),
		credit_ir1p1 => credit_i(1)(WEST),
		
		tx_r1p2 => tx(1)(NORTH),
		out_r1p2 => data_out(1)(NORTH),
		credit_ir1p2 => credit_i(1)(NORTH),
		
		tx_r2p1 => tx(2)(WEST),
		out_r2p1 => data_out(2)(WEST),
		credit_ir2p1 => credit_i(2)(WEST),
		
		tx_r2p2 => tx(2)(NORTH),
		out_r2p2 => data_out(2)(NORTH),
		credit_ir2p2 => credit_i(2)(NORTH),
		
		tx_r3p0 => tx(3)(EAST),
		out_r3p0 => data_out(3)(EAST),
		credit_ir3p0 => credit_i(3)(EAST),
		
		tx_r3p2 => tx(3)(NORTH),
		out_r3p2 => data_out(3)(NORTH),
		credit_ir3p2 => credit_i(3)(NORTH),
		
		tx_r3p3 => tx(3)(SOUTH),
		out_r3p3 => data_out(3)(SOUTH),
		credit_ir3p3 => credit_i(3)(SOUTH),
		
		tx_r4p0 => tx(4)(EAST),
		out_r4p0 => data_out(4)(EAST),
		credit_ir4p0 => credit_i(4)(EAST),
		
		tx_r4p1 => tx(4)(WEST),
		out_r4p1 => data_out(4)(WEST),
		credit_ir4p1 => credit_i(4)(WEST),
		
		tx_r4p2 => tx(4)(NORTH),
		out_r4p2 => data_out(4)(NORTH),
		credit_ir4p2 => credit_i(4)(NORTH),
		
		tx_r4p3 => tx(4)(SOUTH),
		out_r4p3 => data_out(4)(SOUTH),
		credit_ir4p3 => credit_i(4)(SOUTH),
		
		tx_r5p1 => tx(5)(WEST),
		out_r5p1 => data_out(5)(WEST),
		credit_ir5p1 => credit_i(5)(WEST),
		
		tx_r5p2 => tx(5)(NORTH),
		out_r5p2 => data_out(5)(NORTH),
		credit_ir5p2 => credit_i(5)(NORTH),
		
		tx_r5p3 => tx(5)(SOUTH),
		out_r5p3 => data_out(5)(SOUTH),
		credit_ir5p3 => credit_i(5)(SOUTH),
		
		tx_r6p0 => tx(6)(EAST),
		out_r6p0 => data_out(6)(EAST),
		credit_ir6p0 => credit_i(6)(EAST),
		
		tx_r6p3 => tx(6)(SOUTH),
		out_r6p3 => data_out(6)(SOUTH),
		credit_ir6p3 => credit_i(6)(SOUTH),
		
		tx_r7p0 => tx(7)(EAST),
		out_r7p0 => data_out(7)(EAST),
		credit_ir7p0 => credit_i(7)(EAST),
		
		tx_r7p1 => tx(7)(WEST),
		out_r7p1 => data_out(7)(WEST),
		credit_ir7p1 => credit_i(7)(WEST),
		
		tx_r7p3 => tx(7)(SOUTH),
		out_r7p3 => data_out(7)(SOUTH),
		credit_ir7p3 => credit_i(7)(SOUTH),
		
		tx_r8p1 => tx(8)(WEST),
		out_r8p1 => data_out(8)(WEST),
		credit_ir8p1 => credit_i(8)(WEST),
		
		tx_r8p3 => tx(8)(SOUTH),
		out_r8p3 => data_out(8)(SOUTH),
		credit_ir8p3 => credit_i(8)(SOUTH)
	);

end architecture;
