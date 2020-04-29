---------------------------------------------------------------------------------------
--                                                ROUTER
--
--                                                 NORTH                   LOCAL
--              ------------------------------------------------------------------
--              |                            ****** ******     ******  ****** |
--              |                          *FILA* *FILA*      *FILA* *FILA* |
--              |                          *   L1  * *  L2  *      *   L1  * *   L2  * |
--              |                           ******  ******      ******  ****** |
--              |                                                                                      |
--              | *********      ***************           *********  |
--              | *FILA L1*      * ARBITRAGEM  *          * FILA L1* |
--              | *********      ***************           *********  |
--  WEST | *********      ****************         ********* |
--              | *FILA L2*      * ROTEAMENTO  *        *FILA L2* |
--              | *********      ****************          ********* |
--              |                                                                                      |
--              |                            ****** ******                                 |
--              |                            *FILA* *FILA*                                |
--              |                            *   L1  * *  L2  *                                |
--              |                             ****** ******                                |
--              ------------------------------------------------------------------
--                                    SOUTH
--
--  Os roteadores realizam a transfer�ncia de mensagens entre n�cleos.
--  O roteador possui uma l�gica de controle de chaveamento e 5 portas bidirecionais:
--  East, West, North, South e Local. Cada porta possui dois canais virtuais, cada
--  um com uma fila para o armazenamento tempor�rio de flits. A porta Local estabelece
--  a comunica��o entre o roteador e seu n�cleo. As demais portas ligam o roteador aos
--  roteadores vizinhos.
--  Os endere�os dos roteadores s�o compostos pelas coordenadas XY da rede de
--  interconex�o, onde X � a posi��o horizontal e Y a posi��o vertical. A atribui��o de
--  endere�os aos roteadores � necess�ria para a execu��o do algoritmo de chaveamento.
--  Os m�dulos que comp�em o roteador s�o: Hermes_buffer (porta de entrada e filas),
--  Hermes_switchcontrol (arbitragem e roteamento) e Hermes_outport (porta de sa�da).
--  Cada uma das filas do roteador, ao receber um novo pacote requisita chaveamento ao
--  �rbitro. O �rbitro seleciona a requisi��o de maior prioridade, quando existem
--  requisi��es simult�neas, e encaminha o pedido � l�gica de chaveamento. A l�gica de
--  chaveamento verifica se � poss�vel atender � solicita��o. Sendo poss�vel, a conex�o
--  � estabelecida e a fila come�a a enviar os flits armazenados. Quando todos os flits
--  do pacote s�o enviados, a fila sinaliza atrav�s do sinal sender que a conex�o deve
--  ser finalizada.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;
use work.HermesPackage.all;

entity $Chave$ is
generic( address: regmetadeflit);
port(
	clock:      in  std_logic;
	reset:      in  std_logic;
	clock_rx:   in  regNport;
	rx:         in  regNport;
	lane_rx:    in  arrayNport_regNlane;
	data_in:    in  arrayNport_regflit;
	credit_o:   out arrayNport_regNlane;
	clock_tx:   out regNport;
	tx:         out regNport;
	lane_tx:    out arrayNport_regNlane;
	data_out:   out arrayNport_regflit;
	credit_i:   in  arrayNport_regNlane);
end $Chave$;

architecture $Chave$ of $Chave$ is

signal h, ack_h, data_av, data_ack, sender, free: arrayNport_regNlane := (others=>(others=>'0'));
signal aux_lane_tx, last_lane_tx: arrayNport_regNlane := (others=>(others=>'0'));
signal data: matrixNport_Nlane_regflit := (others=>(others=>(others=>'0')));
signal tableIn, tableOut: matrixNport_Nlane_reg8 := (others=>(others=>(others=>'0')));

begin
	lane_tx <= aux_lane_tx;
$inports$
	SC : Entity work.Hermes_switchcontrol
	port map(
		clock => clock,
		reset => reset,
		h => h,
		ack_h => ack_h,
		address => address,
		data => data,
		sender => sender,
		free => free,
		mux_in => tableIn,
		mux_out => tableOut);
$outports$
end $Chave$;
