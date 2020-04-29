--
--! @file ringbuffer.vhd
--! @brief Hermes generic NoC ringbuffer.
--! @details Simple ringbuffer supporting Hermes wormhole flow-control without
--! empty slot.
--! @author Angelo Elias Dalzotto (angelo.dalzotto@edu.pucrs.br)
--! @author Nicolas Lodea (nicolas.lodea@edu.pucrs.br)
--! @date 2020/04
-- 

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use work.standards.all;

entity ringbuffer is
	port(
		clock:	in	std_logic;
		reset:	in	std_logic;

		--! Write to buffer (To/From Router)
		clock_rx:	in	std_logic;
		rx:			in	std_logic;
		data_in:	in 	reg_flit_size;
		credit_o:	out	std_logic;

		--! Read from buffer (To/From Arbiter)
		data_out:	out reg_flit_size;
		data_av:	out std_logic;
		data_ack:	in	std_logic
	);
end entity;

architecture rtl of ringbuffer is
	--! Storage and pointers
	signal storage: buffer_t;
	signal read_pointer, write_pointer: pointer;

	--! State Machine
	type state is (S_EMPTY, S_BURST);
	signal active_state:	state;

begin
	--
	--! write_pointer    /= read_pointer	:	FIFO WITH SPACE TO WRITE
	--! read_pointer + 1 == write_pointer	:	FIFO EMPTY
	--! write_pointer    == read_pointer	:	FIFO FULL
	--

	--! If fifo isn't empty, credit is high. Else, low.
	credit_o <= '1' when write_pointer /= read_pointer else '0';

	--! Output is connected to data pointing from the read pointer.
	data_out <= storage(conv_integer(read_pointer));

	--! Buffer write process
	process(reset, clock)
	begin
		if reset='1' then
			write_pointer <= (others => '0');
		elsif rising_edge(clock) then
			--! If receiving data and fifo isn't empty, record data in fifo and increase write pointer
			if rx = '1' and write_pointer /= read_pointer then
				storage(conv_integer(write_pointer)) <= data_in;
				write_pointer <= write_pointer + 1;
			end if;
		end if;
	end process;

	--! Buffer read process
	process(reset, clock)
	begin
		if reset = '1' then
			--! Initialize the read pointer with one position before the write pointer
			read_pointer <= (others => '1');
			data_av <= '0';
			active_state <= S_EMPTY;
		elsif rising_edge(clock) then
			case active_state is
				--! Buffer has been emptied
				when S_EMPTY =>
					--! When buffer has been emptied, check when a new data is available
					if rx = '1' then
						--! The buffer reader starts at "-1", so is needed to increment 1 to set data as available.
						read_pointer <= read_pointer + 1;
						data_av <= '1';
						active_state <= S_BURST;
					end if;

				--! The buffer has at least 1 data
				when S_BURST =>
					--! If data has been read
					if data_ack = '1' then
						if (read_pointer + 1 = write_pointer) and rx = '0' then
							--! The buffer has been emptied by the read
							data_av <= '0';
							active_state <= S_EMPTY;
						else
							--! The buffer still not empty
							read_pointer <= read_pointer + 1;
						end if;
					end if;
			end case;
		end if;
	end process;
	
end architecture;