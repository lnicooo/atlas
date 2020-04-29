package TrafficMeasurer;

import java.io.*;

import javax.swing.*;
import java.util.*;
import AtlasPackage.*;

/**
 * This class evaluates the latency of a flow between a source and a target router.
 * @author Aline Vieira de Mello
 * @version
 */
public class DistrLat{

	private NoC noc;
	private int numIntervalos, perc, numCV, flitSize;
	private String path, nameFile, origem, destino;
	private GraphPoint[] ponto;
	private int nRotX;
	private int size,nPackets=0;
	private double averageLatency=0,minimumLatency=0,maximalLatency=0;
	private double standardDeviationLatency=0;

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
	public DistrLat(Project project, String nameFile, int type, String source, String target, int interval, int percentage){
		this.nameFile=nameFile;
		origem = source;
		destino = target;
		numIntervalos = interval;
		perc = percentage;

		path = project.getPath() + File.separator + "Traffic" + File.separator + project.getSceneryName() + File.separator + "Out";
		noc = project.getNoC();
		nRotX = noc.getNumRotX();
		numCV = noc.getVirtualChannel();
		flitSize = noc.getFlitSize();

		try{
			ponto = new GraphPoint[numIntervalos];
			for(int i=0; i<numIntervalos; i++){
				ponto[i] = new GraphPoint();
			}
			//verify if DAT file was created
			if(writeDat())
				writeTxt(type);
			else if(type==0)
				JOptionPane.showMessageDialog(null,"There are no packets in this flow!","Information",JOptionPane.INFORMATION_MESSAGE);
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in the DistLat class: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Generate the distr_lat.dat file that contains the throughput distribution. 
	 * @return True if the file was created.
	 */
	private boolean writeDat(){

		StringTokenizer st;
		String line;
		String target="",source="";
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double latency,latencyMinVir=0,latencyMaxVir=0;
		double descarte=0;
		double latencyAcum=0,sum_var=0;

		FileInputStream fis;
		BufferedReader br;
		File f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + ".txt");

		try{
			for(int cont=0;cont<numCV;cont++){
				if(numCV > 1)
					f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + "L" + cont + ".txt");
				fis=new FileInputStream(f);
				br=new BufferedReader(new InputStreamReader(fis));

				line=br.readLine();
				while(line!=null){
					st = new StringTokenizer(line, " ");
					int nTokens = st.countTokens();

					if(nTokens!=0){
						//advance until two tokens before the end of line
						for(int count=0;count<(nTokens-2);count++){
							if(count==0){
								target=destino;
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

						//get the packet latency between the source and target
						latency = Integer.valueOf(st.nextToken()).intValue();

						if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							//store the smallest latency
							if(minimumLatency==0 || minimumLatency>latency)
								minimumLatency=latency;

							//store the greatest latency
							if(maximalLatency==0 || maximalLatency<latency)
								maximalLatency=latency;

							latencyAcum+=latency;
							nPackets++;
						}
					}
					line=br.readLine();
				}
				br.close();
				fis.close();
			}

			if(nPackets!=0){
				descarte=((maximalLatency-minimumLatency)*perc)/100;
				latencyMinVir=minimumLatency+(int)descarte;
				latencyMaxVir=maximalLatency-(int)descarte;
				interval=(int)(latencyMaxVir-latencyMinVir)/numIntervalos;
				averageLatency=latencyAcum/nPackets;

				for(int i=0; i<numIntervalos; i++){
					if(i==0){
						limitInf=latencyMinVir;
						limitSup=limitInf+interval;
					}
					else{
						limitInf=limitSup+1;
						limitSup=limitInf+interval;
					}
					ponto[i].setCoordX((int)((limitSup+limitInf)/2));
				}


				FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "dat" + File.separator+nameFile+".dat"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				for(int cont=0;cont<numCV;cont++){
					if(numCV > 1)
						f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + "L"+cont + ".txt");
					fis=new FileInputStream(f);
					br=new BufferedReader(new InputStreamReader(fis));

					line=br.readLine();
					while(line!=null){
						st = new StringTokenizer(line, " ");
						int nTokens = st.countTokens();

						if(nTokens!=0){

							//advance until two tokens before the end of line
							for(int count=0;count<(nTokens-2);count++){
								if(count==0){
									target =destino;
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

							//get the packet latency between source ant target
							latency = Integer.valueOf(st.nextToken()).intValue();

							if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
								inferior=latencyMinVir;
								superior=latencyMinVir+interval;
								for(int j=0; j<numIntervalos; j++){
									if(latency>=inferior && latency<=superior){
										ponto[j].setCoordY(ponto[j].getCoordY()+1);
									}
									inferior=superior+1;
									superior=inferior+interval;
								}

								sum_var+=((latency-averageLatency)*(latency-averageLatency));
							}
						}
						line=br.readLine();
					}
					br.close();
					fis.close();
				}

				standardDeviationLatency=Math.sqrt(sum_var/nPackets);

				for(int i=0; i<numIntervalos; i++){
					out.write(ponto[i].getCoordX() + " " + ponto[i].getCoordY() + "\n");
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
	 * Generate the TXT file used to show the latency distribution in a GUI or using GNUPLOT.
	 * @param type The format type: O = GUI and !0 = GNUPLOT.
	 */
	public void writeTxt(int type){
		String aux = new String();

		try{
			FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator + nameFile + ".txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			if(type==0){ // show in gnuplot format
				aux = aux + "reset\n";
				if(((new String(""+averageLatency)).length()>=7)&&((new String(""+standardDeviationLatency)).length()>=7))
					aux = aux + "set title \"Latency distribution " + "   Average = " + (new String(""+averageLatency)).substring(0,7) + "   Standard deviation = " + (new String(""+standardDeviationLatency)).substring(0,7) + "\"\n";
				else aux = aux + "set title \"Latency distribution " + "   Average = " + averageLatency + "   Standard deviation = " + standardDeviationLatency + "\"\n";
				aux = aux + "set xlabel \"Latency (cycles)\"\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set ylabel \"Number of packets\"\n";
				String str = path + File.separator + "reports" + File.separator + "dat" + File.separator + nameFile + ".dat";
				aux = aux + "plot '"+str+"' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "pause -1 \"Press ENTER to continue\"";
			}
			else{// show in the figure format
				String source = Convert.getXYAddress(Convert.getNumberOfRouter(origem,nRotX,flitSize),nRotX,16);
				String target = Convert.getXYAddress(Convert.getNumberOfRouter(destino,nRotX,flitSize),nRotX,16);
				aux = aux + "reset\n";
				aux = aux + "cd '" + path + File.separator + "reports'\n";
				//Alright! here you can set how you want the Graphs to be!
				//just like this!        Type        ///Size!   bgcolor                  color of the 'curve'
				if(Default.isWindows){
				    aux = aux + "set terminal png medium size 800,600 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				else{
				    aux = aux + "set terminal png medium xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				aux = aux + "set yrange [0:]\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set pointsize 0.7\n";
				aux = aux + "set output 'images" + File.separator + "Latency"  + source+ "-" + target + ".png'\n";
				aux = aux + "set title \"Latency Distribution  Source = " + source + " Target = " + target + " Average = " + averageLatency + " Standard deviation = " + standardDeviationLatency +  " \"\n";
				aux = aux + "set xlabel \"Latency\"\n";
				aux = aux + "set ylabel \"Number of packets\"\n";
				aux = aux + "plot 'dat" + File.separator+nameFile+".dat" + "' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "set output";
			}

			out.write(aux);
			out.close();
			osw.close();
			fos.close();

			String nameGraph = path + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + nameFile + ".txt";
			Default.showGraph(nameGraph);
		}
		catch(Exception ex){
		    JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Return the average latency.
	 * @return The average latency.
	 */
	public double getAverageLatency(){return averageLatency;}

	/**
	 * Return the standard deviation latency.
	 * @return The standard deviation latency.
	 */
	public double getStandardDeviationLatency(){return standardDeviationLatency;}

	/**
	 * Return the minimal latency.
	 * @return The minimal latency.
	 */
	public double getMinimumLatency(){return minimumLatency;}

	/**
	 * Return the maximal latency.
	 * @return The maximal latency.
	 */
	public double getMaximalLatency(){return maximalLatency;}

	/**
	 * Return the number of generated packets.
	 * @return The number of generated packets.
	 */
	public int getNPackets(){return nPackets;}

	/**
	 * Return the number of flits of the last packet.<p>
	 * Ideal when all packets belong to the flow have the same number of flits.
	 * @return The number of flits of the last packet.
	 */
	public int getPacketSize(){return size;}
}