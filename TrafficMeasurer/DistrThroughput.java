package TrafficMeasurer;

import java.io.*;

import javax.swing.*;
import java.util.*;
import AtlasPackage.*;

/**
 * This class evaluates the throughput of a flow between a source and a target router.
 * @author Aline Vieira de Mello
 * @version
 */
public class DistrThroughput{

	private NoC noc;
	private int numIntervalos, perc, numCiclosFlit,numCV, flitSize,nRotX;
	private String path, nameFile, origem, destino;
	private GraphPoint[] interval_vector;
	private double averageThroughput=0,sourceAverageThroughput=0;
	private double standardDeviationThroughput=0,minimumThroughput=0,maximalThroughput=0;
	private int sourcePackets=0;
	private boolean isHermesSRVC;

	/**
	 * Constructor class.
	 * @param project The selected project.
	 * @param nameFile The name file of graph.
	 * @param type The format type: 0 = TXT format showed in a GUI or !0 = GNUPLOT format showed by GNUPLOT.
	 * @param source The source router.
	 * @param target The target router.
	 * @param interval The interval.
	 * @param percentage The discarded percentage.
	 */
	public DistrThroughput(Project project, String nameFile, int type, String source, String target, int interval, int percentage){
		this.nameFile=nameFile;
		origem = source;
		destino = target;
		numIntervalos = interval;
		perc = percentage;
		
		path = project.getPath() + File.separator + "Traffic" + File.separator + project.getSceneryName() + File.separator + "Out";
		noc = project.getNoC();
		nRotX = noc.getNumRotX();
		numCV = noc.getVirtualChannel();
		numCiclosFlit = noc.getCyclesPerFlit();
		flitSize = noc.getFlitSize();
		isHermesSRVC = noc.isSR4();

		try{
			interval_vector = new GraphPoint[numIntervalos];
			for(int i=0; i<numIntervalos; i++){
				interval_vector[i] = new GraphPoint();
			}
			//get data about throughput in the traffic source
			getSourceThroughput();

			//verify if the DAT file was created
			if(writeDat()){
				writeTxt(type);
			}
			else if(type==0)
				JOptionPane.showMessageDialog(null,"There are no packets in this flow!","Information",JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in DistrThroughput class: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Generate the distr_throughput.dat file that contains the throughput distribution. 
	 * @return True if the file was created.
	 */
	private boolean writeDat(){
		Vector<int[]> vetor = new Vector<int[]>();
		StringTokenizer st;
		String line;
		String target="",source="";
		int nTokens=0,nPackets=0;
		int size=0,time=0,lastTime=0;
		double throughput;
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double minimumThroughputVir=0,maximalThroughputVir=0;
		double descarte=0;
		double throughputAcum=0,sum_var=0;

		FileInputStream fis;
		BufferedReader br;

		File f = new File(path.concat(File.separator + "out"+Convert.getNumberOfRouter(destino,nRotX,flitSize)+".txt"));

		try{
			for(int cont=0;cont<numCV;cont++){
				if(numCV > 1)
					f = new File(path.concat(File.separator + "out"+Convert.getNumberOfRouter(destino,nRotX,flitSize)+"L"+cont+".txt"));
				fis=new FileInputStream(f);
				br=new BufferedReader(new InputStreamReader(fis));

				line=br.readLine();
				while(line!=null){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();

					if(nTokens!=0){
						//advance until three token before end of packet
						for(int count=0;count<(nTokens-3);count++){
							if(count==0){
								target = destino;
								st.nextToken();
							}
							else if(count==1)
								size = Integer.parseInt(st.nextToken(),16)+2;
							else if(count==2){
								source = st.nextToken();
								source = source.substring(source.length()/2);
							}
							else
								st.nextToken();
						}

						//get time of delivery packet
						time = Integer.valueOf(st.nextToken()).intValue();
						
						if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							//insert in a ordered vector
							int [] novo = {time,size};
							if(vetor.size()==0)
								vetor.add(novo);
							else{
								int i=0;
								boolean insert=false;
								while(!insert){
									int [] aux = (int [])vetor.get(i);
									if(aux[0]>novo[0]){
										vetor.insertElementAt(novo,i);
										insert=true;
									}
									else if(i==vetor.size()-1){
										vetor.add(novo);
										insert=true;
									}
									i++;
								}
							}
						}
					}
					line=br.readLine();
				}
				br.close();
			}

			if(vetor.size()!=0){

				//get the maximal and minimal throughput
				for(int i=0;i<vetor.size();i++){
					time = ((int[])vetor.get(i))[0];
					size = ((int[])vetor.get(i))[1];
					if(lastTime!=0){ //discarded the throughput of first packet
						throughput=((100*size*numCiclosFlit)/(time - lastTime));

						//store the minimal throughput
						if(minimumThroughput==0 || minimumThroughput>throughput)
							minimumThroughput=throughput;

						//store the maximal throughput
						if(maximalThroughput==0 || maximalThroughput<throughput)
							maximalThroughput=throughput;

						throughputAcum+=throughput;
						nPackets++;
					}
					lastTime = time;
				}
				descarte=((maximalThroughput-minimumThroughput)*perc)/100;
				minimumThroughputVir=minimumThroughput+(int)descarte;
				maximalThroughputVir=maximalThroughput-(int)descarte;
				interval=(int)(maximalThroughputVir-minimumThroughputVir)/numIntervalos;
				averageThroughput=throughputAcum/nPackets;

				for(int i=0; i<numIntervalos; i++){
					if(i==0){
						limitInf=minimumThroughputVir;
						limitSup=limitInf+interval;
					}
					else{
						limitInf=limitSup+1;
						limitSup=limitInf+interval;
					}
					interval_vector[i].setCoordX((int)((limitSup+limitInf)/2));
				}

				lastTime=0;
				//put each throughput packet in a graph point
				for(int i=0;i<vetor.size();i++){
					time = ((int[])vetor.get(i))[0];
					size = ((int[])vetor.get(i))[1];
					if(lastTime!=0){ //discarded the first packet
						throughput=((100*size*numCiclosFlit)/(time - lastTime));

						sum_var+=((throughput-averageThroughput)*(throughput-averageThroughput));

						inferior=minimumThroughputVir;
						superior=minimumThroughputVir+interval;
						for(int j=0; j<numIntervalos; j++){
							if(throughput>=inferior && throughput<=superior){
								interval_vector[j].setCoordY(interval_vector[j].getCoordY()+1);
							}
							inferior=superior+1;
							superior=inferior+interval;
						}
					}
					lastTime = time;
				}

				standardDeviationThroughput=Math.sqrt(sum_var/nPackets);

				FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "dat" + File.separator+nameFile+".dat"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				for(int i=0; i<numIntervalos; i++){
					out.write(interval_vector[i].getCoordX() + " " + interval_vector[i].getCoordY() + "\n");
				}
				out.close();
				osw.close();
				fos.close();
				return true;
			}
		}
		catch(FileNotFoundException exc){
			JOptionPane.showMessageDialog(null,"Can't open "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return false;
	}

	/**
	 * Generate the TXT file used to show the throughput distribution in a GUI or using GNUPLOT.
	 * @param type The format type: O = GUI and !0 = GNUPLOT.
	 */
	private void writeTxt(int type){
		String aux = new String();

		try{
			FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator+nameFile+".txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			if(type==0){ // show graph in TXT format
				aux = aux + "reset\n";
				if(((new String(""+averageThroughput)).length()>=7)&&((new String(""+standardDeviationThroughput)).length()>=7))
					aux = aux + "set title \"Throughput distribution " + "   Average = " + (new String(""+averageThroughput)).substring(0,7) + "   Standard deviation = " + (new String(""+standardDeviationThroughput)).substring(0,7) + "\"\n";
				else aux = aux + "set title \"Throughput distribution " + "   Average = " + averageThroughput + "   Standard deviation = " + standardDeviationThroughput + "\"\n";
				aux = aux + "set xlabel \"Load (%)\"\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set ylabel \"Number of packets\"\n";
				String str = path + File.separator + "reports" + File.separator + "dat" + File.separator + nameFile + ".dat";
				aux = aux + "plot '" + str + "' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "pause -1 \"Press ENTER to continue\"\n";
			}
			else{ //show graph in GNUPLOT format
				String source = Convert.getXYAddress(Convert.getNumberOfRouter(origem,nRotX,flitSize),nRotX,16);
				String target = Convert.getXYAddress(Convert.getNumberOfRouter(destino,nRotX,flitSize),nRotX,16);
				aux = aux + "reset\n";
				aux = aux + "cd '" + path + File.separator + "reports'\n";

				if(Default.isWindows){
				    aux = aux + "set terminal png medium size 800,600 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				else{
				    aux = aux + "set terminal png medium xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				
				aux = aux + "set yrange [0:]\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set pointsize 0.7\n";
				aux = aux + "set output 'images" + File.separator + "Throughput"  + source  + "-" + target + ".png'\n";
				aux = aux + "set title \"Throughput Distribution  Source = " + source + " Target = " + target + " Average = " + averageThroughput+ " Standard deviation = " + standardDeviationThroughput+  " \"\n";
				aux = aux + "set xlabel \"Throughput\"\n";
				aux = aux + "set ylabel \"Number of packets\"\n";
				aux = aux + "plot 'dat" + File.separator+nameFile+".dat' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "set output";
			}

			out.write(aux);
			out.close();
			osw.close();
			fos.close();

			String nameGraph = path + File.separator + "reports" + File.separator + "graphs_txt" + File.separator+nameFile+".txt";
			Default.showGraph(nameGraph);
		}
		catch(Exception ex){
		    JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * verify the throughput in the source router. 
	 */
	private void getSourceThroughput(){
		StringTokenizer st;
		String line;
		String target="",source="";
		String pathIn, fileIn;
		double throughput=0,sumThroughtput=0;
		int nTokens=0, nPackets=0, lastTime=0, time=0, size=0, nLacos=1, nodo;
		File trafficF;
		FileInputStream trafficFIS;
		DataInputStream trafficDIS;
		BufferedReader trafficBR;
		
		pathIn = path.substring(0,path.indexOf("Out"))+"In" + File.separator;
		nodo = Convert.getNumberOfRouter(origem,nRotX,flitSize);
		
		if(isHermesSRVC)
			nLacos = 3;
		
		sourceAverageThroughput=0;
		for(int index=0; index<nLacos; index++){

			target="";
			source="";
			throughput=0;
			sumThroughtput=0;
			nTokens=0;
			nPackets=0;
			lastTime=0;
			time=0;
			size=0;
			
			switch(index){
				case 0:
					if(nLacos==1)
						fileIn = pathIn + "in" + nodo + ".txt";
					else
						fileIn = pathIn + "inCTRL" + nodo + ".txt";
					break;
				case 1: 
					fileIn = pathIn + "inGS" + nodo + ".txt";
					break;
				case 2: 
					fileIn = pathIn + "inBE" + nodo + ".txt";
					break;
				default: 
					fileIn = "INVALID FILE OPTION";
			}
			
			try{
				trafficF = new File(fileIn);
				if(!trafficF.exists()) continue;
				trafficFIS=new FileInputStream(trafficF);
				trafficDIS=new DataInputStream(trafficFIS);
				trafficBR=new BufferedReader(new InputStreamReader(trafficDIS));

				line=trafficBR.readLine();

				while(line!=null){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();

					if(nTokens!=0){
						//advance until two tokens before the end of line
						for(int count=0;count<nTokens;count++){
							if(count==0) time = Integer.parseInt(st.nextToken(),16);
							else if(count==1){
								target = st.nextToken();
								target = target.substring(target.length()/2);
							}
							else if(count==2)
								size = Integer.parseInt(st.nextToken(),16)+6; //6=> 2 header + 4 timestamp
							else if(count==3){
								source = st.nextToken();
								source = source.substring(source.length()/2);
							}
							else
								st.nextToken();
						}
						if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							if(lastTime!=0){ //discarded the first packet
								throughput=((100*size*numCiclosFlit)/(time - lastTime));
								sumThroughtput = sumThroughtput + throughput;
								nPackets++;
							}
							lastTime = time;
							sourcePackets++;
						}
					}
					line=trafficBR.readLine();
				}
				
				trafficBR.close();
				trafficDIS.close();
				trafficFIS.close();

				if(nPackets!=0){
					sourceAverageThroughput+= sumThroughtput / nPackets;
				}
			}
			catch(FileNotFoundException exc){
				JOptionPane.showMessageDialog(null,"Can't Open File "+fileIn,"Input error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null,"ERROR: "+e.getMessage(),"Input Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}		
		}
	}

	/**
	 * Return the average throughput in the source router.
	 * @return The average throughput in the source router.
	 */
	public double getSourceAverageThroughput(){return sourceAverageThroughput;}

	/**
	 * Return the number of generated packets.
	 * @return The number of generated packets.
	 */
	public int getSourcePackets(){return sourcePackets;}
	
	/**
	 * Return the average throughput.
	 * @return The average throughput.
	 */
	public double getAverageThroughput(){return averageThroughput;}

	/**
	 * Return the standard deviation throughput.
	 * @return The standard deviation throughput.
	 */
	public double getStandardDeviationThroughput(){return standardDeviationThroughput;}

	/**
	 * Return the minimal throughput.
	 * @return The minimal throughput.
	 */
	public double getMinimumThroughput(){return minimumThroughput;}

	/**
	 * Return the maximal throughput.
	 * @return The maximal throughput.
	 */
	public double getMaximalThroughput(){return maximalThroughput;}

}