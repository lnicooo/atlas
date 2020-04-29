---------------------------------------------------------------------------------------	
--                                              ROUTER
--
--                                              NORTH        LOCAL
--                       ---------------------------------------------
--                      |                        ******           ****** |
--                      |                       *FILA*          *FILA* |
--                      |                        ******          ****** |
--                      |                 *************                |
--                      |                 *  ARBITRO  *                  |
--                      | ******    *************    ****** |
--          WEST | *FILA*   *************   *FILA* | EAST
--                      | ******    *  CONTROLE *  ****** |
--                      |                 *************                 |
--                      |                       ******                         |
--                      |                       *FILA*                         |
--                      |                       ******                         |
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
	clock:    in  std_logic;
	reset:    in  std_logic;
	data_in:  in  arrayNport_regflit;
	rx:       in  regNport;
	ack_rx:   out regNport;
	data_out: out arrayNport_regflit;
	tx:       out regNport;
	ack_tx:   in  regNport);
end $Chave$;

architecture $Chave$ of $Chave$ is

signal h, ack_h, data_av, sender, data_ack: regNport := (others=>'0');
signal data: arrayNport_regflit := (others=>(others=>'0'));
signal mux_in,mux_out: arrayNport_reg3 := (others=>(others=>'0'));
signal free: regNport := (others=>'0');

begin

$filas$

	SwitchControl : Entity work.SwitchControl($algorithm$)
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

----------------------------------------------------------------------------------
-- OBSERVACAO:
-- quando eh sinal de saida quem determina eh o sinal mux_out
-- quando eh sinal de entrada quem determina eh mux_in
----------------------------------------------------------------------------------
	MUXS : for i in 0 to (NPORT-1) generate
		data_out(i) <= data(CONV_INTEGER(mux_out(i))) when free(i)='0' else (others=>'0');
		data_ack(i) <= ack_tx(CONV_INTEGER(mux_in(i))) when sender(i)='1' else '0';
		tx(i) <= data_av(CONV_INTEGER(mux_out(i))) when free(i)='0' else '0';
	end generate MUXS;

$zeros$

end $Chave$;