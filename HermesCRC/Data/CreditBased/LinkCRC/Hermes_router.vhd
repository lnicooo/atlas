---------------------------------------------------------------------------------------	
--                                              ROUTER
--
--                                              NORTH        LOCAL
--                      -----------------------------------------------
--                      |                       ******          ****** |
--                      |                       *FILA*          *FILA* |
--                      |                       ******          ****** |
--                      |                 *************                |
--                      |                 *  ARBITRO  *                |
--                      | ******          *************         ****** |
--             	   WEST | *FILA*          *************         *FILA* | EAST
--                      | ******          *  CONTROLE *         ****** |
--                      |                 *************                |
--                      |                       ******                 |
--                      |                       *FILA*                 |
--                      |                       ******                 |
--                      -----------------------------------------------
--                                              SOUTH
--
--  As chaves realizam a transfer�ncia de mensagens entre n�cleos. 
--  A chave possui uma l�gica de controle de chaveamento e 5 portas bidirecionais:
--  East, West, North, South e Local. Cada porta possui uma fila para o armazenamento 
--  tempor�rio de flits. A porta Local estabelece a comunica��o entre a chave e seu 
--  n�cleo. As demais portas ligam a chave �s chaves vizinhas.
--  Os endere�os das chaves s�o compostos pelas coordenadas XY da rede de interconex�o, 
--  onde X � a posi��o horizontal e Y a posi��o vertical. A atribui��o de endere�os �s 
--  chaves � necess�ria para a execu��o do algoritmo de chaveamento.
--  Os m�dulos principais que comp�em a chave s�o: fila, �rbitro e l�gica de 
--  chaveamento implementada pelo controle_mux. Cada uma das filas da chave (E, W, N, 
--  S e L), ao receber um novo pacote requisita chaveamento ao �rbitro. O �rbitro 
--  seleciona a requisi��o de maior prioridade, quando existem requisi��es simult�neas, 
--  e encaminha o pedido de chaveamento � l�gica de chaveamento. A l�gica de 
--  chaveamento verifica se � poss�vel atender � solicita��o. Sendo poss�vel, a conex�o
--  � estabelecida e o �rbitro � informado. Por sua vez, o �rbitro informa a fila que 
--  come�a a enviar os flits armazenados. Quando todos os flits do pacote foram 
--  enviados, a conex�o � conclu�da pela sinaliza��o, por parte da fila, atrav�s do 
--  sinal sender.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

entity $Chave$ is
generic( address: regmetadeflit);
port(
	clock:     in  std_logic;
	reset:     in  std_logic;
	clock_rx:  in  regNport;
	rx:        in  regNport;
	data_in:   in  arrayNport_regflit;
	--sinais de controle de erro
	crc_in:	   in  arrayCrc;
	error_in:  in  reg4;
	crc_out:   out arrayCrc;
	error_out: out reg4;
	----------------------------
	credit_o:  out regNport;    
	clock_tx:  out regNport;
	tx:        out regNport;
	data_out:  out arrayNport_regflit;
	credit_i:  in  regNport);
end $Chave$;

architecture $Chave$ of $Chave$ is

signal h, ack_h, data_av, sender, data_ack: regNport := (others=>'0');
signal data: arrayNport_regflit := (others=>(others=>'0'));
signal mux_in, mux_out: arrayNport_reg3 := (others=>(others=>'0'));
signal free: regNport := (others=>'0');

signal crossbar_out: arrayNport_regflit := (others=>(others=>'0')); -- adicionado para EC
signal credit_ixbar: regNport;
signal error: regNport;
signal error_ixbar,error_oxbar: regNport;

begin
$filas$
  process(clock)
	begin
		if clock'event and clock='1' then
			error_out <= error(3 downto 0);
		end if;
	end process;
	
	data_out <= crossbar_out;
	error_ixbar <= '0' & error_in;
	
	
	error(4) <= '0';		--n�o tem sinal de erro na porta local
	credit_ixbar(4) <= credit_i(4); --sinal de cr�dito da porta local passa reto
	
	SwitchControl : Entity work.SwitchControl
	port map(
		clock => clock,
		reset => reset,
		h => h,
		ack_h => ack_h,
		address => address,
		data => data,
		sender => sender,
		free => free,
		mux_in => mux_in,
		mux_out => mux_out);

	CrossBar : Entity work.Hermes_crossbar
	port map(
		data_av => data_av,
		data_in => data,
		data_ack => data_ack,
		sender => sender,
		free => free,
		tab_in => mux_in,
		tab_out => mux_out,
		tx => tx,
		data_out => crossbar_out,
		error_i => error_ixbar,
		error_o => error_oxbar,
		credit_i => credit_ixbar);

	CLK_TX : for i in 0 to(NPORT-1) generate
		clock_tx(i) <= clock;
	end generate CLK_TX; 
	
end $Chave$;