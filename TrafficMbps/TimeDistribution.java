package TrafficMbps;

import javax.swing.*;
import java.io.*;
import java.util.*;
import AtlasPackage.*;

/**
 * This class generate the time of packets according to a traffic distribution.
 * The time of packet determines when a packet should be transmit to NoC.
 * @author Aline Vieira de Mello
 *
 */
public class TimeDistribution{

	private	String distribution;
	private	double channelRate;
	private	double packetTime;
	private	int numberPackets,burstSize;
	private	double minRate,maxRate,incRate,avgRate,dvRate;
	private	double ipRate;
	private	double ipRateOn;

	/**
	 * Creates the time distribution of packets.
	 * @param normalFilename The filename of normal graph. The extension is only txt. 
	 * @param traffic The traffic with its parameters.
	 * @param flitWidth The number of bits of a flit.
	 * @param flitClockCycles The number of cycles to transmit a flit.
	 */
	 public TimeDistribution(RouterTraffic traffic, int flitWidth,int flitClockCycles){
		distribution = traffic.getDistribution();
		channelRate = traffic.getFrequency() * flitWidth;
		if(traffic.isSR4()){
			numberPackets = traffic.getTotalNumberOfPackets();
			packetTime = traffic.getAveragePacketSize() * flitClockCycles;
			ipRate = traffic.getRate();
		}
		else{
			numberPackets = traffic.getNumberOfPackets();
			packetTime = traffic.getPacketSize() * flitClockCycles;
			ipRate = traffic.getUniformRate();
		}
		minRate = traffic.getNormalMinimalRate();
		maxRate = traffic.getNormalMaximalRate();
		incRate = traffic.getNormalIncrement();
		avgRate = traffic.getNormalAverageRate();
		dvRate = traffic.getNormalStandardDeviation();
		ipRateOn = traffic.getParetoRateOnPeriod();
		burstSize = traffic.getParetoBurstSize();
	}

	/**
	 * Generate the time of packets according to traffic distribution.
	 * @return The vector of time distribution of packets.
	 */
	public Vector<String> defineTime(){
		Vector<String> vet;
		if(distribution.equalsIgnoreCase(Traffic.UNIFORM))
			vet=uniform();
		else if(distribution.equalsIgnoreCase(Traffic.NORMAL))
			vet=normal();
		else
			vet=pareto();
		return vet;
	}


	/**
	 * Generate the time of packets to Uniform distribution.
	 * @return The vector of time distribution of packets.
	 */
	private Vector<String> uniform(){

		double idleTime =(double)((channelRate/ipRate - 1) * packetTime);
		double totalTime = idleTime + packetTime;
		double timestamp = 1;
		Vector<String> vet = new Vector<String>();

		for(int j=0;j<numberPackets;j++){
			vet.add(Integer.toHexString((int)timestamp).toUpperCase());
			timestamp = timestamp + totalTime;
		}
		return vet;
	}

	private double on(double r, int fator){
		double alfa_on=1.9;
		return Math.pow((1-r),(-1/alfa_on))*fator;
	}

	private double off(double r, int fator){
		double alfa_off=1.25;
		return Math.pow((1-r),(-1/alfa_off))*fator;
	}

	/**
	 * Generate the time of packets to Pareto On/Off distribution.
	 * @return The vector of time distribution of packets.
	 */
	private Vector<String> pareto(){
		double periodo_on=0,periodo_off=0;
		double idleTime =(double)((channelRate/ipRateOn - 1) * packetTime);
		double totalTime = idleTime + packetTime;
		double timestamp = 1,timestampBurst=0;
		int totalPackets=0, fator;
		int pronto=0;
		Vector<String> vet;
		double r=0;

		fator=(int)((((numberPackets*packetTime)/burstSize)*(channelRate/ipRateOn))/1.5);

		do{
			vet = new Vector<String>();
			totalPackets = 0;

			timestamp = Math.random()*packetTime;
			timestampBurst = timestamp;

			for (int i=0;i<burstSize;i++){
				do{
					r= Math.random();
				}while (r>0.9);

				periodo_on = on(r,fator);
				periodo_off = off(r,fator);
				int npacks=(int)((periodo_on/packetTime)*(ipRateOn/channelRate));
				totalPackets = totalPackets + npacks;

				for (int k=0;k<npacks;k++){
					vet.add(Integer.toHexString((int)timestamp).toUpperCase());
					timestamp = timestamp + totalTime;
				}
				timestampBurst = timestampBurst + periodo_on + periodo_off;

				timestamp = timestampBurst;
			}
			if (totalPackets<numberPackets){
				vet.clear();
				pronto=0;
				burstSize++;
			}
			else
				pronto=1;
		}while(pronto==0);
		return vet;
	}

	private double calcNormal(double avgRate, double dvRate, double x){
		double a=0;
		double b=0;
		double c=0;

		a = dvRate * Math.sqrt(2*Math.PI);
		b = -Math.pow((x-avgRate),2);
		c = 2 * Math.pow(dvRate,2);
		return ((1/a) * Math.pow(2.71828,(b/c)));
	}

	/**
	 * Generate the time of packets to Normal distribution.
	 * @return The vector of time distribution of packets. 
	 */
	private Vector<String> normal(){
		int numPackets=0, numIntervalos=0, inseridos=0, maior=0, celula_maior=0, falta, timestamp=0;
		double perc=0,percAcum=0, idleTime, rateIp, total=0, pacotes=0;
		Point auxPoint;
		Vector<Point> vAux = new Vector<Point>();
		Vector<String> out = new Vector<String>();
		numIntervalos=(int)((maxRate-minRate)/incRate);

	/* Alterações feitas por R. Schemmer e Ney Calazans em 20/01/2010
	*/
		for(double i=0, j=minRate; j<=(minRate+(incRate*numIntervalos)); i++,j=j+incRate)
				perc = perc + calcNormal(avgRate,dvRate,j); // Calcula o percentual total das taxas de injeção
		total = perc;
		pacotes = numberPackets;

		for(double i=0, j=minRate; j<=(minRate+(incRate*numIntervalos)); i++,j=j+incRate){
			perc = calcNormal(avgRate,dvRate,j);

			numPackets = (int)(((numberPackets * perc)/total)+0.5); // 0.5 força o arredondamento do numero de pacotes ao converter para int
			pacotes = pacotes - numPackets;
			if(pacotes < 0) break;

			idleTime = ( ( (channelRate/(float)j) -1) * packetTime);
			inseridos = inseridos + numPackets;
			percAcum = percAcum + perc;
			rateIp = channelRate/((idleTime/(packetTime))+1);

			vAux.add(new Point(numPackets,(int)idleTime,rateIp));
		}

	/* Fim do trecho com alterações feitas por R. Schemmer e Ney Calazans em 20/01/2010
	*/
		falta = numberPackets - inseridos;

		for(int i=0; i<(numIntervalos + 1); i++){
			auxPoint = (Point) vAux.get(i);
			if(auxPoint.getNumPackets()>=maior){
				maior = auxPoint.getNumPackets();
				celula_maior = i;
			}
		}

		auxPoint = (Point) vAux.get(celula_maior);
		auxPoint.setNumPackets(auxPoint.getNumPackets()+falta);

		for(int a_inserir=numberPackets;a_inserir>0;){
			int i = (int)(Math.random()*(numIntervalos+1));
			auxPoint = (Point)vAux.get(i);

			if (auxPoint.getNumPackets()>0){
				a_inserir=a_inserir-1;
				if (a_inserir==(numberPackets-1)){
					out.add(Integer.toHexString(0).toUpperCase());
				}
				else{
					timestamp=timestamp+auxPoint.getIdleTime()+(int)packetTime;
					out.add(Integer.toHexString(timestamp).toUpperCase());
				}
				auxPoint.setNumPackets(auxPoint.getNumPackets()-1);
			}
		}
		return out;
	}

	/**
	 * Generate the gnuplot graph of the Normal distribution.
	 * @param filename The filename where the gnuplot commands will be write. 
	 */
	public void normalGraph(String filename){
		int numPackets=0, numIntervalos=0, inseridos=0, maior=0, celula_maior=0, falta;
		double perc=0,percAcum=0, idleTime, rateIp, total=0, pacotes=0;
		Point auxPoint;
		Vector<Point> vAux = new Vector<Point>();

		numIntervalos=(int)((maxRate-minRate)/incRate);

	/* Alterações feitas por R. Schemmer e Ney Calazans em 20/01/2010
	*/
		for(double i=0, j=minRate; j<=(minRate+(incRate*numIntervalos)); i++,j=j+incRate)
				perc = perc + calcNormal(avgRate,dvRate,j); // Calcula o percentual total das taxas de injeção
		total = perc;
		pacotes = numberPackets;

		for(double i=0, j=minRate; j<=(minRate+(incRate*numIntervalos)); i++,j=j+incRate){
			perc = calcNormal(avgRate,dvRate,j);

			numPackets = (int)(((numberPackets * perc)/total)+0.5);
			pacotes = pacotes - numPackets;
			if(pacotes < 0) break;

			idleTime = ( ( (channelRate/(float)j) -1) * packetTime);
			inseridos = inseridos + numPackets;
			percAcum = percAcum + perc;
			rateIp = channelRate/((idleTime/(packetTime))+1);

			vAux.add(new Point(numPackets,(int)idleTime,rateIp));
		}

	/* Fim do trecho com alterações feitas por R. Schemmer e Ney Calazans em 20/01/2010
	*/
		falta = numberPackets - inseridos;

		for(int i=0; i<(numIntervalos + 1); i++){
			auxPoint = (Point) vAux.get(i);
			if(auxPoint.getNumPackets()>=maior){
				maior = auxPoint.getNumPackets();
				celula_maior = i;
			}
		}

		auxPoint = (Point) vAux.get(celula_maior);
		auxPoint.setNumPackets(auxPoint.getNumPackets()+falta);

		//Write the file with the data to be plot.
		writeGNUDat(filename,vAux);

		for(int a_inserir=numberPackets;a_inserir>0;a_inserir--){
			int i = (int)(Math.random()*(numIntervalos+1));
			auxPoint = (Point)vAux.get(i);
			if (auxPoint.getNumPackets()>0){
				auxPoint.setNumPackets(auxPoint.getNumPackets()-1);
			}
		}
		//write the file to show the gnuplot graph
		writeGNUTxt(filename,"Normal Distribution","Rate (Mbps)","Number of Packets");
	}
	
	/**
	 * Write the file containing the gnuplot commands.
	 * @param nameFile The name file.
	 * @param title The graph title.
	 * @param xLabel The X-coordinate label.
	 * @param yLabel The Y-coordinate label.
	 */
	private void writeGNUTxt(String nameFile,String title, String xLabel, String yLabel){
		try{

			FileOutputStream fos = new FileOutputStream(new File(nameFile+".txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
	       	Writer w = new BufferedWriter(osw);

			String dat = nameFile+".dat";

			w.write("reset\n");
			w.write("set title \""+title+"\"\n");
			w.write("set xlabel \"+"+xLabel+"\"\n");
			//w.write("set xrange [0:1]\n");
			w.write("set ylabel \""+yLabel+"\"\n");
			w.write("plot '"+dat+"' using ($1):($2) t\"Normal\" with linespoints 5 5\n");
			w.write("pause -1 \"Press ENTER to continue\"\n");
			w.close();
			osw.close();
			fos.close();
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Write the file containing the data that will plot.
	 * @param nameFile The name file.
	 * @param vector The vector of points.
	 */
	private void writeGNUDat(String nameFile, Vector<Point> vector){
		try{
			FileOutputStream fos = new FileOutputStream(new File(nameFile+".dat"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer w = new BufferedWriter(osw);

			Point auxPoint;
			for(int i=0;i<vector.size();i++){
				auxPoint = (Point)vector.get(i);
				w.write(""+auxPoint.getRateIp()+" "+auxPoint.getNumPackets()+"\n");
			}
			w.close();
			osw.close();
			fos.close();
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
		}
	}
}

class Point{
	int numPackets;
	int idleTime;
	double rateIp;

	public Point(int numPackets,int idleTime,double rateIp){
		this.numPackets = numPackets;
		this.idleTime = idleTime;
		this.rateIp = rateIp;
	}

	public int getNumPackets(){return numPackets;}
	public int getIdleTime(){return idleTime;}
	public double getRateIp(){return rateIp;}

	public void setNumPackets(int n){numPackets=n;}
	public void setIdleTime(int i){idleTime=i;}
	public void setRateIp(int r){rateIp=r;}

}