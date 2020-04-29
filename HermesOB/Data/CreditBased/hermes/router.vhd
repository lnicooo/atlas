--
--! @file router.vhd
--! @brief Hermes NoC input router block for output buffers
--! @details The router block is the block to interact with the input. It will
--! receive flits and route them to the appropriate output buffer with the 
--! standard Hermes input port signals using XY routing algorithm.
--! @author Angelo Elias Dalzotto (angelo.dalzotto@edu.pucrs.br)
--! @author Nicolas Lodea (nicolas.lodea@edu.pucrs.br)
--! @date 2020/04
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use work.standards.all;

entity router is
	generic(
		--! Routing Control
		address:	reg_half_flit
	);
	port(
		clock:	in	std_logic;
		reset:	in	std_logic;
		
		--! To/from output port
		clock_rx:	in	std_logic;
		rx:			in	std_logic;
		data_in:	in	reg_flit_size;
		credit_o:	out	std_logic;

		--! To/from buffers
		credit_i:	in	reg_port_no;
		tx:			out reg_port_no
	);
end entity;

architecture rtl of router is
	--! State Machine
	type	state is (S_INIT, S_SENDHEADER, S_PKTSIZE, S_PAYLOAD);
	signal	active_state: state;

	--! Target routing address identifier
	signal target_x, target_y: reg_quarter_flit;

	--! Target direction identifier
	signal target:		port_t;
	signal target_set:	std_logic;

	--! Flow control
	signal	flit_counter: reg_flit_size;

	--! Local address identifier
	constant local_x: reg_quarter_flit := address((HALF_FLIT - 1) downto QUARTER_FLIT);
	constant local_y: reg_quarter_flit := address((QUARTER_FLIT - 1) downto 0);

begin

	--! Target routing address identifier
	target_x <= data_in((HALF_FLIT - 1) downto QUARTER_FLIT);
	target_y <= data_in((QUARTER_FLIT - 1) downto 0);

	--! Hangs immediately after receiving first flit.
	--! This makes the sender wait for the router to pick the destination buffer
	--! before sending the next flit, eliminating the need of additional
	--! input buffer at a little cost of performance.
	credit_o <= credit_i(target) when target_set = '1' else not rx;

	--! Output buffer muxing
	txb: for i in 0 to PORT_NO-1 generate
		tx(i) <= rx when target = i and target_set = '1' else '0';
	end generate;

	process(reset, clock)
	begin
		if reset = '1' then
			target_set <= '0';
			active_state <= S_INIT;
		elsif rising_edge(clock) then
			case active_state is
				--! No transmission being done, wait first flit
				when S_INIT =>
					--! Receiving data
					if rx = '1'  then
						--! Set the routing as done
						target_set <= '1';
						active_state <= S_SENDHEADER;

						--! Routing algorithm (XY)
						--! Target is local, route to LOCAL
						if local_x = target_x and local_y = target_y then
							target <= LOCAL;
						
						--! Checks if need to route in X first
						elsif local_x /= target_x then
							if target_x > local_x then
								target <= EAST;
							else
								target <= WEST;
							end if;

						--! Else, route in Y
						else
							if target_y > local_y then
								target <= NORTH;
							else
								target <= SOUTH;
							end if;
						end if;
					end if;
				
				--! Routing is set, confirm saving header(1) to buffer
				when S_SENDHEADER =>
					--! Only send if buffer is not full.
					if credit_i(target) = '1' then
						active_state <= S_PKTSIZE;
					end if;

				--! First flit of header sent, send the second
				when S_PKTSIZE =>
					--! Load the payload size to the counter to know when to reroute
					if rx = '1' and credit_i(target) = '1' then
						flit_counter <= data_in;
						active_state <= S_PAYLOAD;
					end if;
				
				when S_PAYLOAD =>
					--! Send until reaching the end of the payload
					if flit_counter = 0 then
						--! Undo routing and restart the state machine
						target_set <= '0';
						active_state <= S_INIT;
					elsif rx = '1' and credit_i(target) = '1' then
						--! Each flit sent reduces from flit counter
						flit_counter <= flit_counter - 1;
					end if;

			end case;
		end if;
	end process;

end architecture;