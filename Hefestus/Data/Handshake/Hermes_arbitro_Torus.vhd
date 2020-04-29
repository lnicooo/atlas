---------------------------------------------------------------------------------------	
--                          ARBITRO
--                         --------------
--                         |                 | 
--             H         |                  |
--       de todas =>|                 |-> REQ_ROT
--       as portas   |                 |
--                         |                 |<- ACK_ROT
--                         |                 |
--                         |                 |-> INCOMING
--      ACK_ H      |                 |
--   para todas <=|                 |-> DEADLOCK
--    as portas      |                 |
--                          --------------
--
--  A chave suporta at� cinco conex�es simultaneamente, mas apenas uma pode ser 
--  estabelecida a cada instante. Logo, existe a necessidade de um �rbitro para 
--  determinar qual o pacote deve ser chaveado primeiro, quando mais de um chega � 
--  chave em um mesmo instante de tempo.
--  A arbitragem din�mica rotativa permite o chaveamento do pacote da porta de 
--  entrada com maior prioridade. A prioridade de cada porta depende da �ltima que 
--  obteve permiss�o de chaveamento. Por exemplo, se a porta de entrada Local(�ndice 4)
--  foi a �ltima a ter permiss�o de chaveamento, a porta de entrada Leste (�ndice 0) 
--  ter� a maior prioridade seguida das portas de entrada Oeste, Norte, Sul e Local.
--  Neste caso, se a porta de entrada Norte(�ndice 2) e Oeste (�ndice 1) solicitarem 
--  chaveamento simultaneamente, a porta de entrada Oeste ter� sua solicita��o atendida,
--  porque tem maior prioridade. � importante observar que a prioridade das portas de 
--  entrada � vari�vel e dependente da �ltima porta de entrada a ter a permiss�o de 
--  chaveamento. Isto significa que todas as portas de entrada ser�o atendidas, mesmo 
--  as de menor prioridade.
--  O �rbitro, ap�s atender uma solicita��o, aguarda 4 ciclos de rel�gio para que o 
--  algoritmo de chaveamento interno seja executado e somente ap�s este per�odo volta 
--  a atender solicita��es. Se o algoritmo de chaveamento interno n�o consegue 
--  estabelecer uma conex�o, a porta de entrada volta a solicitar chaveamento ao 
--  �rbitro, por�m com a menor prioridade.
---------------------------------------------------------------------------------------	
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- interface do Arbitro
entity Arbitro is
port(
	clock:    in  std_logic;
	reset:    in  std_logic;
	h:        in  regNport;
	ack_h:    out regNport;
	req_rot:  out std_logic;
	ack_rot:  in  std_logic;
	incoming: out reg3;
	deadlock: out std_logic);
end Arbitro;

-- implementa��o do Arbitro
architecture Arbitro of Arbitro is

type state is (S0,S1,S2,S3,S4);
signal ES, PES: state;
signal ask: std_logic;
signal dead: regNport;
signal counter_dead: arrayNport_reg8;
signal counter,timeout: std_logic_vector(5 downto 0);
signal sel,prox: integer range 0 to (NPORT-1); 

begin
	timeout <= "000100"; -- timeout igual a 4 ciclos 

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';

	process(reset,clock)
	begin
			if reset='1' then
					ES<=S0;
			elsif clock'event and clock='1' then
					ES<=PES;
			end if;
	end process;

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
					if h(EAST)='1' then prox<=EAST;
					elsif h(WEST)='1' then  prox<=WEST;
					elsif h(NORTH)='1' then prox<=NORTH;
					elsif h(SOUTH)='1' then prox<=SOUTH;
					else prox<=LOCAL; end if;
			when EAST=> 
					if h(WEST)='1' then     prox<=WEST;
					elsif h(NORTH)='1' then prox<=NORTH;
					elsif h(SOUTH)='1' then prox<=SOUTH;
					elsif h(LOCAL)='1' then prox<=LOCAL;
					else prox<=EAST; end if;
			when WEST=> 
					if h(NORTH)='1' then prox<=NORTH;
					elsif h(SOUTH)='1' then prox<=SOUTH;
					elsif h(LOCAL)='1' then prox<=LOCAL;
					elsif h(EAST)='1' then prox<=EAST;
					else prox<=WEST; end if;
			when NORTH=> 
					if h(SOUTH)='1' then prox<=SOUTH;
					elsif h(LOCAL)='1' then prox<=LOCAL;
					elsif h(EAST)='1' then prox<=EAST;
					elsif h(WEST)='1' then prox<=WEST;
					else prox<=NORTH; end if;
			when SOUTH=> 
					if h(LOCAL)='1' then prox<=LOCAL;
					elsif h(EAST)='1' then prox<=EAST;
					elsif h(WEST)='1' then prox<=WEST;
					elsif h(NORTH)='1' then prox<=NORTH;
					else prox<=SOUTH; end if;
			when others => prox<=LOCAL;
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PR�XIMO ESTADO DA M�QUINA.
	--
	-- SO -> O estado S0 � o estado de inicializa��o da m�quina. Este estado somente � 
	--       atingido quando o sinal reset � ativado.
	-- S1 -> O estado S1 � o estado de espera por requisi��o de chaveamento. Quando o 
	--       �rbitro recebe uma ou mais requisi��es o contador (respons�vel por encerrar o 
	--       processo de chaveamento quando o timeout � alcan�ado) � zerado e o sinal ask � 
	--       ativado fazendo a m�quina avan�ar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento � selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade � a selecionada.
	-- S3 -> No estado S3 � requisitado o chaveamento (req_rot='1') informando a porta de 
	--       entrada selecionada (incoming=sel). A m�quina permanece no estado S3 incrementando
	--       o contador a cada ciclo de rel�gio at� que a l�gica de chaveamento informe que o
	--       chaveamento foi realizado (ack_rot='1') ou que o contador atinja o timeout. Se o
	--       contador atingir o timeout a m�quina retorna ao estado S1, caso o chaveamento 
	--       tenha sido realizado a m�quina avan�a para o estado S4. 
	-- S4 -> No estado S4 o sinal ack_h � ativado indicando que o chaveamento foi realizado 
	--       com sucesso. Do estado S4 a m�quina retorna para o estado S1 onde aguarda novas 
	--       requisi��es. 
	------------------------------------------------------------------------------------------------------  
	process(ES,ask,counter,timeout,ack_rot, h) --sel nao precisa
	begin
			case ES is
					when S0 => PES <= S1;
					when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
					when S2 => PES <= S3;
					when S3 =>
							if ack_rot='1' then PES <= S4;
								elsif counter=timeout then PES<= S1;
								else PES <= S3;
							end if;
					when S4 => if h(sel)='0' then PES<=S1; else PES<=S4; end if;
			end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as a��es correspondente ao estado atual da m�quina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				 -- Zera vari�veis
				 when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					counter <= (others => '0');
				 -- Chegou um header
				 when S1=>
					ack_h <= (others => '0');
					counter <= (others => '0');
				 -- Seleciona quem tera direito a requisitar roteamento
				 when S2=>
					sel <= prox;
				 -- Espera resposta de roteamento OK ou cai fora quando expirar timeout
				 when S3=>
					counter <= counter + '1';
				 -- Envia ack confirmando roteamento
				 when S4=>
					ack_h(sel) <= '1';                             
			 end case;
		end if;
	end process;    

	-- Converte o indice da porta selecionada (tipo inteiro) para o tipo std_logic_vector.
	incoming <= conv_vector(sel);

	req_rot <= '1' when ES=S3 else '0';

	process(reset,clock,h,counter_dead)
	begin
		if reset='1' then
			dead(EAST)<= '0';
			dead(WEST)<= '0';
			dead(NORTH)<= '0';
			dead(SOUTH)<= '0';
			dead(LOCAL)<= '0';
			counter_dead(EAST)<= (others=>'0');
			counter_dead(WEST)<= (others=>'0');
			counter_dead(NORTH)<= (others=>'0');
			counter_dead(SOUTH)<= (others=>'0');
			counter_dead(LOCAL)<= (others=>'0');
		elsif clock'event and clock='0' then
			if h(EAST)='1' and counter_dead(EAST)=x"FF" then
				dead(EAST)<='1';
				counter_dead(EAST)<=(others=>'0');
			elsif h(EAST)='1' then
				counter_dead(EAST)<= counter_dead(EAST) + '1';
			else
				dead(EAST)<='0';
				counter_dead(EAST)<= (others=>'0');
			end if;
			
			if h(WEST)='1' and counter_dead(WEST)=x"FF" then
				dead(WEST)<='1';
				counter_dead(WEST)<=(others=>'0');
			elsif h(WEST)='1' then
				counter_dead(WEST)<= counter_dead(WEST) + '1';
			else
				dead(WEST)<='0';
				counter_dead(WEST)<= (others=>'0');
			end if;

			if h(NORTH)='1' and counter_dead(NORTH)=x"FF" then
				dead(NORTH)<='1';
				counter_dead(NORTH)<=(others=>'0');
			elsif h(NORTH)='1' then
				counter_dead(NORTH)<= counter_dead(NORTH) + '1';
			else
				dead(NORTH)<='0';
				counter_dead(NORTH)<= (others=>'0');
			end if;

			if h(SOUTH)='1' and counter_dead(SOUTH)=x"FF" then
				dead(SOUTH)<='1';
				counter_dead(SOUTH)<=(others=>'0');
			elsif h(SOUTH)='1' then
				counter_dead(SOUTH)<= counter_dead(SOUTH) + '1';
			else
				dead(SOUTH)<='0';
				counter_dead(SOUTH)<= (others=>'0');
			end if;

			if h(LOCAL)='1' and counter_dead(LOCAL)=x"FF" then
				dead(LOCAL)<='1';
				counter_dead(LOCAL)<=(others=>'0');
			elsif h(LOCAL)='1' then
				counter_dead(LOCAL)<= counter_dead(LOCAL) + '1';
			else
				dead(LOCAL)<='0';
				counter_dead(LOCAL)<= (others=>'0');
			end if;
		end if;
	end process;

	deadlock<='1' when dead(sel)='1' else '0';

end Arbitro;
