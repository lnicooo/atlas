--
--! @file arbiter.vhd
--! @brief Hermes generic NoC output arbiter block for output buffers
--! @details This executes the round-robin algorithm to ensure no starvation is
--! achieved. 
--! @author Angelo Elias Dalzotto (angelo.dalzotto@edu.pucrs.br)
--! @author Nicolas Lodea (nicolas.lodea@edu.pucrs.br)
--! @date 2020/04
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use work.standards.all;

entity arbiter is
	port(
		clock:	in	std_logic;
		reset:	in	std_logic;

		--! Next node interface
		data_out:	out	reg_flit_size;
		tx:			out	std_logic;
		credit_i:	in	std_logic;

		--! Buffers interface
		data_in:	in	port_no_reg_flit_size;
		data_av:	in	reg_port_no;
		data_ack:	out	reg_port_no
	);
end entity;

architecture rtl of arbiter is
	--! State machine
	type state is (S_INIT, S_SENDHEADER, S_PKTSIZE, S_PAYLOAD);
	signal active_state: state;

	--! Round-robin control
	signal last:		port_t;
	signal target:		port_t;
	signal target_set:	std_logic;

	--! Flow control
	signal flit_counter: reg_flit_size;
begin

	--! Output data bus muxing
	data_out <= (others => '0') when target_set = '0' else data_in(target);

	--! Output TX muxing
	tx <= '0' when target_set = '0' else data_av(target);

	--! Input data_ack demuxing
	ack: for i in 0 to PORT_NO-1 generate
		data_ack(i) <= credit_i when target_set = '1' and target = i else '0';
	end generate;

	process(reset, clock)
	begin
		if reset = '1' then
			--! RNG to set last priority starter. The RNG was done by rolling a dice.
			last <= EAST;
			target_set <= '0';
			active_state <= S_INIT;
		elsif rising_edge(clock) then
			case active_state is
				--! No data in any buffer.
				when S_INIT =>
					--! Check if any buffer has data
					if	data_av(EAST) = '1' or data_av(WEST) = '1' or 
						data_av(NORTH) = '1' or	data_av(SOUTH) = '1' or 
													data_av(LOCAL) = '1' then

						--! Perform RR algorithm to select an output buffer
						case last is
							when EAST =>
								if data_av(WEST) = '1' then target <= WEST;
								elsif data_av(NORTH) = '1' then target <= NORTH;
								elsif data_av(SOUTH) = '1' then target <= SOUTH;
								elsif data_av(LOCAL) = '1' then target <= LOCAL;
								else target <= EAST;
								end if;
							when WEST =>
								if data_av(NORTH) = '1' then target <= NORTH;
								elsif data_av(SOUTH) = '1' then target <= SOUTH;
								elsif data_av(LOCAL) = '1' then target <= LOCAL;
								elsif data_av(EAST) = '1' then target <= EAST;
								else target <= WEST;
								end if;
							when NORTH =>
								if data_av(SOUTH) = '1' then target <= SOUTH;
								elsif data_av(LOCAL) = '1' then target <= LOCAL;
								elsif data_av(EAST) = '1' then target <= EAST;
								elsif data_av(WEST) = '1' then target <= WEST;
								else target <= NORTH;
								end if;
							when SOUTH =>
								if data_av(LOCAL) = '1' then target <= LOCAL;
								elsif data_av(EAST) = '1' then target <= EAST;
								elsif data_av(WEST) = '1' then target <= WEST;
								elsif data_av(NORTH) = '1' then target <= NORTH;
								else target <= SOUTH;
								end if;
							when others =>
								if data_av(EAST) = '1' then target <= EAST;
								elsif data_av(WEST) = '1' then target <= WEST;
								elsif data_av(NORTH) = '1' then target <= NORTH;
								elsif data_av(SOUTH) = '1' then target <= SOUTH;
								else target <= LOCAL;
								end if;
						end case;
						
						--! Set the last selected and enable the mux/demux
						last <= target;
						target_set <= '1';
						active_state <= S_SENDHEADER;
					end if;

				--! Transmit first flit
				when S_SENDHEADER =>
					--! Just wait for ack
					if credit_i = '1' then
						active_state <= S_PKTSIZE;
					end if;

				--! Process second flit
				when S_PKTSIZE =>
					--! Save for flow control
					if data_av(target) = '1' and credit_i = '1' then
						flit_counter <= data_in(target);
						active_state <= S_PAYLOAD;
					end if;

				--! Send the payload
				when S_PAYLOAD =>
					--! Wait full packet transmission
					if flit_counter = 0 then
						--! End of packet, disables muxing
						target_set <= '0';
						active_state <= S_INIT;
					elsif credit_i = '1' and data_av(target) = '1' then
						flit_counter <= flit_counter - 1;
					end if;

			end case;
		end if;
	end process;
end architecture;