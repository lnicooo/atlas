-- *****************************************************************************
-- O m�dulo SwitchOut determina qual fila ganhar� o acesso � sa�da do m�dulo
-- ArbiterOut implementando um esquema de prioridade do tipo Round-Robin.
-- *****************************************************************************
library IEEE;
use IEEE.STD_LOGIC_1164.all;
use work.Mercury_package.all;
					 
entity SwitchOut is
	port(
	reset, clock,
	queue_addr_in_A, 
	queue_addr_in_B, 
	queue_addr_in_C: in std_logic;
	queue_addr_out: out queue_addr);
end SwitchOut;

architecture SwitchOut of SwitchOut is
signal queue_addr_in_reg, queue_addr_out_reg: queue_addr;
signal ce_reg: std_logic;
begin					
	
	-- ********************************************************
	-- registrador que armazena um endereco de fila	
	-- ********************************************************		
	process (clock, reset)
	begin
		if reset = '1' then
			queue_addr_out_reg <= FILA_NONE;
		elsif clock'event and clock='1' then
			if ce_reg = '1' then 
				queue_addr_out_reg <= queue_addr_in_reg; 
			end if;
		end if;
	end process;

	queue_addr_out <= queue_addr_in_reg;
							 
	process(queue_addr_in_A, queue_addr_in_B, queue_addr_in_C, queue_addr_out_reg)
	begin
		-- Prioridade Round-Robin
		------------------------------------------------------------------------------------------------------------
		-- ce_reg = '0' � testado pois quando ele est� ativo em 1 significa que alguma fila est� utilizando a porta,
		-- e desta forma n�o se pode conceder a porta a outra fila. Quando ce_reg est� com valor '0' quer dizer que
		-- nenhuma fila est� utilizando a porta e por isso est� liberado o acesso a mesma. O sinal ce_reg, n�o foi 
		-- criado para essa finalidade, mas devido a necessidade de saber se alguem est� ocupando a porta o mesmo 
		-- est� sendo utilizado, j� que ele consegue desempenhar essa fun��o.
		------------------------------------------------------------------------------------------------------------
		if (queue_addr_in_A = '1') and ce_reg = '0' and		
			(	(queue_addr_out_reg = FILA_C) or 
				((queue_addr_out_reg = FILA_B) and (queue_addr_in_C = '0')) or 
				((queue_addr_in_B = '0') and (queue_addr_in_C = '0')) or
				(queue_addr_out_reg = FILA_NONE)
			)
		then
			queue_addr_in_reg <= FILA_A;
			ce_reg <= '1';
		end if;
		
		if (queue_addr_in_B = '1') and ce_reg = '0' and
			(	(queue_addr_out_reg = FILA_A) or 
				((queue_addr_out_reg = FILA_C) and (queue_addr_in_A = '0')) or 
				((queue_addr_in_C = '0') and (queue_addr_in_A = '0')) or
				((queue_addr_out_reg = FILA_NONE) and (queue_addr_in_A = '0'))
			)
		then
			queue_addr_in_reg <= FILA_B;
			ce_reg <= '1';
		end if;
		
		if (queue_addr_in_C = '1') and ce_reg = '0' and
			(	(queue_addr_out_reg = FILA_B) or
				((queue_addr_in_A = '0') and (queue_addr_in_B = '0')) or
				((queue_addr_out_reg = FILA_A) and (queue_addr_in_B = '0'))
			)
		then
			queue_addr_in_reg <= FILA_C;
			ce_reg <= '1';
		end if;
		
		if (queue_addr_in_A = '0') and (queue_addr_in_B = '0') and (queue_addr_in_C = '0') then
			queue_addr_in_reg <= FILA_NONE;
			ce_reg <= '0';
		end if;
	end process; 
	
end SwitchOut;

