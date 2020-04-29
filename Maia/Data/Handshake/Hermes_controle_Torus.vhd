---------------------------------------------------------------------------------------	
--                                     CONTROLE
--                                     --------------
--               ADDRESS ->|                 |
--                                     |                |
--               REQ_ROT ->|                |=> FREE
--                ACK_ROT <-|                |
--   DIR_INCOMING ->|                |=> MUX_IN
--                   HEADER ->|                |
--              DEADLOCK ->|                |=> MUX_OUT
--                                      |                |
--                    SENDER   |                |
--                    de todas =>|                |
--                    as portas   |                |
--                                       --------------
--
-- O controle � o m�dulo da chave respons�vel port realizar o chaveamento entre as portas.
-- Ao receber um pacote, a chave utiliza-se de um algoritmo de chaveamento para 
-- determinar por qual porta de sa�da o pacote deve ser enviado. 
-- O algoritmo de chaveamento faz a compara��o do endere�o da chave atual com o 
-- endere�o da chave destino do pacote (armazenado no primeiro flit do pacote). O pacote
-- deve ser chaveado para a porta Local da chave quando o endere�o xLyL* da chave atual
-- for igual ao endere�o xTyT* da chave destino do pacote. Caso contr�rio, � realizada, 
-- primeiramente a compara��o horizontal de endere�os. A compara��o horizontal determina
-- se o pacote deve ser chaveado para o Oeste quando deltaX > ((MAX_X + 1)/2)), para o 
-- Leste em caso contr�rio, ou se o mesmo j� est� horizontalmente alinhado � chave destino
-- (xL=xT). Caso esta �ltima condi��o seja verdadeira � realizada a compara��o vertical que
-- determina se o pacote deve ser chaveado para o Norte quando deltaY > ((MAX_Y + 1)/2)) ou
-- para o Sul em caso contr�rio. Caso a porta de sa�da escolhida esteja ocupada, � realizado
-- o bloqueio dos flits do pacote at� que o pacote possa ser chaveado.
-- Quando o algoritmo de chaveamento interno retorna uma porta de sa�da livre, a conex�o 
-- entre a porta de entrada e a porta de sa�da � estabelecida e � preenchida a tabela de
-- chaveamento. A tabela de chaveamento � composta pelos vetores in(mux_in), out(mux_out)
-- e free. O vetor in � indexado pela porta de entrada e preenchido com a porta de sa�da
-- da conex�o. O vetor out funciona de forma an�loga, sendo indexado pela porta de sa�da
-- e preenchido com a porta de entrada. O vetor free serve para alterar o estado da porta
-- de sa�da que no momento encontra-se livre (1), passando para o estado de ocupado (0).
--
-- * xLyL � o endere�o da chave atual composto pelas coordenadas X e Y da rede.
--   (L de Local)
-- * xTyT � o endere�o da chave destino do pacote composto pelas coordenadas X e Y. 
--   (T de Target)
---------------------------------------------------------------------------------------	

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- interface do Controle
entity Controle is
port(
	clock:        in  std_logic;
	reset:        in  std_logic;
	address:      in  regflit;
	req_rot:      in  std_logic;
	ack_rot:      out std_logic;
	dir_incoming: in  reg3;
	header:       in  regflit;
	deadlock:     in  std_logic;
	sender:       in  regNport;
	free:         out regNport;
	mux_in:       out arrayNport_reg3;
	mux_out:      out arrayNport_reg3);
end Controle;

architecture Controle of Controle is

type state_controle is (SC0,SC1,SC2,SC3);
signal SC,PSC: state_controle;

signal xL,yL,xT,yT: regmetadeflit;
signal xD,yD,deltaX,deltaY,dir: integer;
signal auxfree: regNport;
signal source:  arrayNport_reg3;
signal sender_ant: regNport;

begin
	free <= auxfree;

	mux_in <= source;

	xL <= address((TAM_FLIT-1) downto (TAM_FLIT/2));
	yL <= address((TAM_FLIT/2 - 1) downto 0);

	xT <= header((TAM_FLIT-1) downto (TAM_FLIT/2));
	yT <= header((TAM_FLIT/2 - 1) downto 0);

	deltaX <= CONV_INTEGER(xT) - CONV_INTEGER(xL);
	deltaY <= CONV_INTEGER(yT) - CONV_INTEGER(yL);

	process(req_rot, reset,clock)
	begin
		if req_rot='0' or reset='1' then
			SC <= SC0;
		elsif clock'event and clock='1' then
			SC <= PSC;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR o pr�ximo estado da m�quina.
	------------------------------------------------------------------------------------------------------  
	process(SC,req_rot,xL,xT,dir,auxfree)
	begin
		case SC is
			when SC0 => if req_rot='1' then PSC<= SC1; else PSC<=SC0; end if;
			when SC1 =>
				if xL = xT and yL = yT then 
					if auxfree(LOCAL)='1' then
						dir <= LOCAL;
						PSC <=SC2;
					elsif deadlock='1' then
						if auxfree(NORTH)='1' then dir <= NORTH; PSC <=SC2;
						elsif auxfree(SOUTH)='1' then dir <= SOUTH; PSC <=SC2;
						elsif auxfree(EAST)='1' then dir <= EAST; PSC <=SC2;
						elsif auxfree(WEST)='1' then dir <= WEST; PSC <=SC2;
						else PSC <=SC1;
						end if;
					else 
						PSC <=SC1;
					end if;
				elsif xL /= xT then
					if xD > (( MAX_X+1)/2) then
						if auxfree(WEST)='1' then
							dir <= WEST;
							PSC <=SC2;
						elsif deadlock='1' then
							if auxfree(NORTH)='1' then dir <= NORTH; PSC <=SC2;
							elsif auxfree(SOUTH)='1' then dir <= SOUTH; PSC <=SC2;
							elsif auxfree(EAST)='1' then dir <= EAST; PSC <=SC2;
							else PSC <=SC1;
							end if;
						else 
							PSC<=SC1;
						end if;
					else
						if auxfree(EAST)='1' then
							dir <= EAST;
							PSC <=SC2;
						elsif deadlock='1' then
							if auxfree(NORTH)='1' then dir <= NORTH; PSC <=SC2;
							elsif auxfree(SOUTH)='1' then dir <= SOUTH; PSC <=SC2;
							elsif auxfree(WEST)='1' then dir <= WEST; PSC <=SC2;
							else PSC <=SC1;
							end if;
						else 
							PSC<=SC1;
						end if;
					end if;
				else
					if yD > (( MAX_Y+1)/2) then
						if auxfree(NORTH)='1' then
							dir <= NORTH;
							PSC <=SC2;														   
						elsif deadlock='1' then
							if auxfree(SOUTH)='1' then dir <= SOUTH; PSC <=SC2;
							elsif auxfree(EAST)='1' then dir <= EAST; PSC <=SC2;
							elsif auxfree(WEST)='1' then dir <= WEST; PSC <=SC2;
							else PSC <=SC1;
							end if;
						else 
							PSC<=SC1;
						end if;
					else 
						if auxfree(SOUTH)='1' then
							dir <= SOUTH;
							PSC <=SC2;
						elsif deadlock='1' then
							if auxfree(NORTH)='1' then dir <= NORTH; PSC <=SC2;
							elsif auxfree(EAST)='1' then dir <= EAST; PSC <=SC2;
							elsif auxfree(WEST)='1' then dir <= WEST; PSC <=SC2;
							else PSC <=SC1;
							end if;
						else 
							PSC<=SC1;
						end if;
					end if;
				end if;
			when SC2 => PSC<=SC3; 
			when SC3 => PSC<=SC3; 
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as a��es correspondente ao estado atual da m�quina de estados controle_mux
	------------------------------------------------------------------------------------------------------  
	process(reset,clock)
	begin
		if reset='1' then
			auxfree <= (others=>'1');
			sender_ant <= (others=>'0');
			mux_out <= (others=>(others=>'0'));
			source <= (others=>(others=>'0'));
		elsif clock'event and clock='0' then
			case SC is
				when SC0 =>
					if deltaX < 0 then xD <= MAX_X + 1 + deltaX;
					else xD <= deltaX;
					end if;
					if deltaY < 0 then yD <= MAX_Y + 1 + deltaY;
					else yD <= deltaY;
					end if;
					ack_rot<='0';
				when SC2 =>
					source(CONV_INTEGER(dir_incoming)) <= CONV_VECTOR(dir);
					mux_out(dir) <= dir_incoming;
					auxfree(dir) <= '0';
					ack_rot<='1';
				when others => ack_rot<='0';
			end case;
			-- fecha as conex�es QUANDO O sender SOBE!!! 
			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;
end Controle;