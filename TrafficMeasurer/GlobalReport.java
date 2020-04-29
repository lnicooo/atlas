package TrafficMeasurer;

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.nio.channels.*;
import AtlasPackage.*;

/**
 * This class creates the Global Report allowing visualize the general information about the traffic evaluation.
 * @author Aline Vieira de Mello
 * @version
 */
public class GlobalReport extends Thread{
	private Project project;
	private NoC noc;
	private String header = new String();
	private String body = new String();
	private String footer = new String();
	private String line = new String();
	private String path;
	private int nHops[][][];
	private int nRot;
	private int nRotX;
	private int nRotY;
	private int nChannels=0;
	private int flitSize;
	private int cyclesPerFlit;
	private int cyclesToRoute;
	private int interval;
	private int percentual;
	private int current;
	private boolean isHermesSR;
    private ProgressBarFrame pb;
    private String sourcePath = Default.atlashome + File.separator + "TrafficMeasurer" + File.separator;

	/**
	 * Constructor class.
	 * @param project The selected project.
	 * @param path
	 */
    public GlobalReport(Project project){
    	this.project = project;
    	path = project.getPath() + File.separator + "Traffic" + File.separator + project.getSceneryName() + File.separator + "Out";
		noc = project.getNoC();
		nRot = noc.getNumRotX() * noc.getNumRotY();
		flitSize = noc.getFlitSize();
		nRotX = noc.getNumRotX();
		nRotY = noc.getNumRotY();
		nChannels = noc.getVirtualChannel();
		cyclesPerFlit = noc.getCyclesPerFlit();
		cyclesToRoute = noc.getCyclesToRoute();
		percentual = 0; //temporary
		interval = 30; //temporary
		current = 0;
		isHermesSR=noc.getType().equalsIgnoreCase("hermessr");
			
		pb = new ProgressBarFrame("Generating Global Report",nRot);
		loadHops();
		start();
    }
    
	public void run(){
		generateReport();
	}

	/**
	 * Read routing files when NoC type is HermesSR.
	 */
	private void loadHops(){
		StringTokenizer st;
		String filename;
		int lSource,lTarget,lHops, nLacos=(nChannels==1)?1:3;		
		
		if(isHermesSR){
			nHops = new int[nLacos][nRotX*nRotY][nRotX*nRotY];
			for(int i=0; i<nLacos; i++){
				switch(i){
					case 0: filename=(nChannels==1)?noc.getRouting():noc.getCTRLRouting(); break;
					case 1: filename=noc.getGSRouting(); break;
					case 2: filename=noc.getBERouting(); break;
					default: filename="INVALID OPTION FILE";
				}
				try{
					FileInputStream inFile=new FileInputStream(new File(filename));
					BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
					do{
						line=buff.readLine();
						st = new StringTokenizer(line, ";");
						if(st.countTokens()>0){
							lSource=Integer.parseInt(st.nextToken());
							lTarget=Integer.parseInt(st.nextToken());
							if(nChannels!=1) st.nextToken();
							lHops=Integer.parseInt(st.nextToken())+1;
							nHops[i][lSource][lTarget]=lHops;
						}
					}while(line != null);
					buff.close();
					inFile.close();
				}//end try
				catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"ERROR while reading routing file.","Internal ERROR", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch(Exception e){}
			}
		}
	}

	/**	
	 * Analyze the flows and generate the global report
	 */
	public void generateReport(){
		DistrLat dL;
		DistrThroughput dT;
		String nameFile,source,target;
		double idealLatency=0,averageLatency=0,standardDeviationLatency=0,minimumLatency=0,maximalLatency=0;
		double averageThroughput=0,standardDeviationThroughput=0,sourceAverageThroughput=0,minimumThroughput=0,maximalThroughput=0;
		double sumLatencyTotal=0,sumThroughputTotal=0;
		int nFlows=0,nPackets=0,nTransmittedPackets=0,sourcePackets=0,nGeneratedPackets=0;

		// global averages
		double averageMinimumLatency=0, averageMaximalLatency=0, averageAverageLatency=0, averageStandardDeviationLatency=0;

		//open HTLM model
		openModel();

		try{
			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(sourcePath+"btnGraph.gif").getChannel();
			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(path + File.separator + "reports" + File.separator + "images" + File.separator + "btnGraph.gif").getChannel();
			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
			// Close the channels
			srcChannel.close();
			dstChannel.close();

			for(int s = 0; s < nRot; s++){
				source = Convert.getXYAddress(s,nRotX,flitSize);
				for(int t = 0; t < nRot; t++){

					target = Convert.getXYAddress(t,nRotX,flitSize);
					//get the latency data and generate the graph
					nameFile = "GraphLatency" + Convert.getXYAddress(s,nRotX,16)+"-" + Convert.getXYAddress(t,nRotX,16);
					dL = new DistrLat(project, nameFile, 1, source, target, interval, percentual);

					if(dL.getNPackets()!=0){

						//get the throughput data and generate the graph
						nameFile = "GraphThroughput" + Convert.getXYAddress(s,nRotX,16)+"-" + Convert.getXYAddress(t,nRotX,16);
						dT = new DistrThroughput(project, nameFile, 1, source, target, interval, percentual);

						nFlows++;
						nPackets=dL.getNPackets();
						averageLatency=dL.getAverageLatency();
						standardDeviationLatency=dL.getStandardDeviationLatency();
						minimumLatency=dL.getMinimumLatency();
						maximalLatency=dL.getMaximalLatency();
						idealLatency = calcIdealLatency(s,t,dL.getPacketSize());
						averageThroughput=dT.getAverageThroughput();
						standardDeviationThroughput=dT.getStandardDeviationThroughput();
						minimumThroughput=dT.getMinimumThroughput();
						maximalThroughput=dT.getMaximalThroughput();
						sourceAverageThroughput=dT.getSourceAverageThroughput();
						sourcePackets=dT.getSourcePackets();

						nGeneratedPackets = nGeneratedPackets + sourcePackets;
						nTransmittedPackets = nTransmittedPackets + nPackets;
						if(!new Double(averageLatency).isNaN())
							sumLatencyTotal = sumLatencyTotal + averageLatency;
						if(!new Double(averageThroughput).isNaN())
							sumThroughputTotal = sumThroughputTotal + averageThroughput;

						averageMinimumLatency += minimumLatency;
						averageMaximalLatency += maximalLatency;
						averageAverageLatency += averageLatency;
						averageStandardDeviationLatency += standardDeviationLatency;

						//insert a line in the HTLM table
						insertLine(Convert.getXYAddress(s,nRotX,16),Convert.getXYAddress(t,nRotX,16),sourcePackets,nPackets,idealLatency,averageLatency,standardDeviationLatency,minimumLatency,maximalLatency,sourceAverageThroughput,averageThroughput,standardDeviationThroughput,minimumThroughput,maximalThroughput);
					}
				}
				current++;
				pb.setValue(current);
			}

			double averageLatencyTotal = sumLatencyTotal / nFlows;
			double averageThroughputTotal = sumThroughputTotal / nFlows;

			//write the HTLM file
			writeHTML(nGeneratedPackets,nTransmittedPackets,averageLatencyTotal,averageThroughputTotal);

		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in generateReport: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * Insert a line in the HTML table
	 * @param source The source router.
	 * @param target The target router.
	 * @param sourcePackets The number of generated packets in the source router with destination equals to target router.
	 * @param targetPackets The number of received packets in the target router with source equals to source router.
	 * @param idealLatency The ideal latency between the source router and the target router.
	 * @param averageLatency The average latency between the source router and the target router.
	 * @param standardDesviationLatency The standard deviation between the source router and the target router.
	 * @param minimumLatency The minimal latency between the source router and the target router.
	 * @param maximalLatency The maximal latency between the source router and the target router.
	 * @param sourceAverageThroughput The ideal throughput between the source router and the target router.
	 * @param averageThroughput The average throughput between the source router and the target router.
	 * @param standardDesviationThroughput The standard deviation throughput between the source router and the target router.
	 * @param minimumThroughput The minimal throughput between the source router and the target router.
	 * @param maximalThroughput The maximal throughput between the source router and the target router.
	 */
	private void insertLine(String source,String target,int sourcePackets,int targetPackets,double idealLatency,double averageLatency,double standardDesviationLatency,double minimumLatency,double maximalLatency,double sourceAverageThroughput,double averageThroughput, double standardDesviationThroughput,double minimumThroughput,double maximalThroughput){
		String strLinha =  new String();
		strLinha = line;
		strLinha = replaceWord(strLinha,"%source", 			source);
		strLinha = replaceWord(strLinha,"%target", 			target);
		strLinha = replaceWord(strLinha,"%sourcePackets",	""+sourcePackets);

		if(noc.getType().equalsIgnoreCase("Mercury")){
				strLinha = replaceWord(strLinha,"%sourceAvgThr", "NI");
		}
		else{
			if(sourcePackets==1)
				strLinha = replaceWord(strLinha,"%sourceAvgThr", 	"NA");
			else
				strLinha = replaceWord(strLinha,"%sourceAvgThr", 	DuasCasasDepoisDaVirgula(""+sourceAverageThroughput));
		}

		if(noc.getType().equalsIgnoreCase("Mercury") || noc.getType().equalsIgnoreCase("HermesTB")){
			strLinha = replaceWord(strLinha,"%sourceIdealLat", "NI");
		}
		else strLinha = replaceWord(strLinha,"%sourceIdealLat",	DuasCasasDepoisDaVirgula(""+idealLatency));

		strLinha = replaceWord(strLinha,"%targetPackets",	""+targetPackets);

		if(targetPackets==1){
			strLinha = replaceWord(strLinha,"%avgThr",	 		"NA");
			strLinha = replaceWord(strLinha,"%stdDevThr",	 	"NA");
			strLinha = replaceWord(strLinha,"%minThr",		 	"NA");
			strLinha = replaceWord(strLinha,"%maxThr",		 	"NA");
		}
		else{
			strLinha = replaceWord(strLinha,"%avgThr",	 		DuasCasasDepoisDaVirgula(""+averageThroughput));
			strLinha = replaceWord(strLinha,"%stdDevThr",	 	DuasCasasDepoisDaVirgula(""+standardDesviationThroughput));
			strLinha = replaceWord(strLinha,"%minThr",		 	DuasCasasDepoisDaVirgula(""+minimumThroughput));
			strLinha = replaceWord(strLinha,"%maxThr",		 	DuasCasasDepoisDaVirgula(""+maximalThroughput));
		}

		strLinha = replaceWord(strLinha,"%avgLat",	 		DuasCasasDepoisDaVirgula(""+averageLatency));
		strLinha = replaceWord(strLinha,"%stdDevLat",	 	DuasCasasDepoisDaVirgula(""+standardDesviationLatency));
		strLinha = replaceWord(strLinha,"%minLat",		 	DuasCasasDepoisDaVirgula(""+minimumLatency));
		strLinha = replaceWord(strLinha,"%maxLat",		 	DuasCasasDepoisDaVirgula(""+maximalLatency));
		strLinha = replaceWord(strLinha,"%imgLat","<a href=\"javascript:OpenPage('images"+File.separator+File.separator+"Latency%Row.png','toolbar=no,location=no,directories=no,status=no,width=620,height=420,scrollbars=no,menubar=no,resizable=no')\"><img src=\"images"+File.separator+"btnGraph.gif\" border=\"0\"></a>");

		strLinha = replaceWord(strLinha,"%imgThr","<a href=\"javascript:OpenPage('images"+File.separator+File.separator+"Throughput%Row.png','toolbar=no,location=no,directories=no,status=no,width=620,height=420,scrollbars=no,menubar=no,resizable=no')\"><img src=\"images"+File.separator+"btnGraph.gif\" border=\"0\"></a>");

		strLinha = replaceWord(strLinha,"%Row", 			source+"-"+target);
		strLinha = replaceWord(strLinha,"%Row", 			source+"-"+target);
		body = body.concat(strLinha);
	}

	/**
	 * open the HTLM model file to get the header, the body, the footer and the model line.
	 */
	private void openModel(){
		String model;
		StringBuffer buffer = new StringBuffer();
		int chrLetra,initialLine, finalLine;

		try
		{
			FileInputStream fis = new FileInputStream(sourcePath+"ModelReport.htm");
			InputStreamReader isr = new InputStreamReader(fis);
			Reader in = new BufferedReader(isr);

			while((chrLetra=in.read()) !=-1){
				buffer.append((char)chrLetra);
			}

			model = buffer.toString();
			initialLine = model.indexOf("<!--Line-->");
			finalLine = model.indexOf("<!--/Line-->");
			header = model.substring(0,initialLine);
			line = model.substring(initialLine,finalLine);
			footer = model.substring(finalLine,model.length());
			body = "";
			
			in.close();
			isr.close();
			fis.close();
			
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"Report could not be generated, it resulted in the following error(Java error that is): \"" + ex.getMessage() + "\"","ERROR",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * 	Write the HTML file containing the general information about the NoC evaluation.
	 * @param nGeneratedPackets The number of generated packets.
	 * @param nTransmittedPackets The number of transmitted packets.
	 * @param averageLatencyTotal The average latency among all packets.
	 * @param averageThroughputTotal The average throughput among all packets.
	 */
	private void writeHTML(int nGeneratedPackets,int nTransmittedPackets,double averageLatencyTotal,double averageThroughputTotal){

		String strDate;
		Calendar date =  new GregorianCalendar();
		date.get(Calendar.DAY_OF_MONTH);
		date.get(Calendar.MONTH);
		date.get(Calendar.HOUR_OF_DAY);
		strDate = "This Variable has been initialized with this silly sentence(Please note that this sentence will be erased in a brief moment)";
		strDate = "Maybe this is the Silliest Variable ever to walk the earth";

		switch(date.get(Calendar.DAY_OF_WEEK)){
			case 1: strDate = "Sunday";	break;
			case 2:	strDate = "Monday";	break;
			case 3:	strDate = "Tuesday"; break;
			case 4:	strDate = "Wednesday"; break;
			case 5:	strDate = "Thursday"; break;
			case 6:	strDate = "Friday";	break;
			case 7:	strDate = "Saturday"; break;
			default: break;
		};
		strDate +=  ", ";
		switch(date.get(Calendar.MONTH)){
			case 0:	strDate += "January"; break;
			case 1:	strDate += "February"; break;
			case 2:	strDate += "March"; break;
			case 3:	strDate += "April"; break;
			case 4:	strDate += "May"; break;
			case 5:	strDate += "June"; break;
			case 6:	strDate += "July"; break;
			case 7: strDate += "August"; break;
			case 8: strDate += "September"; break;
			case 9:	strDate += "October"; break;
			case 10: strDate += "November";	break;
			case 11: strDate += "December"; break;
			default: break;
		}
		strDate+= " " + Integer.toString(date.get(Calendar.DAY_OF_MONTH)) + ", ";
		strDate += date.get(Calendar.YEAR);
		strDate += (date.get(Calendar.ERA) == 1)? ", AD" : ", BC" ;

		header = replaceWord(header,"%type",noc.getType());
		header = replaceWord(header,"%flowControl",noc.getFlowControl());
		header = replaceWord(header,"%cyclesPerFlit",""+cyclesPerFlit);
		header = replaceWord(header,"%virtualChannel",""+nChannels);
		header = replaceWord(header,"%scheduling",noc.getScheduling());
		header = replaceWord(header,"%nRotX",""+nRotX);
		header = replaceWord(header,"%nRotY",""+nRotY);
		header = replaceWord(header,"%nRot",Integer.toString(nRot));

		if( noc.getType().equalsIgnoreCase("Mercury"))
			header = replaceWord(header,"%flitPhit","Phit");
		else
			header = replaceWord(header,"%flitPhit","Flit");

		header = replaceWord(header,"%flitSize",""+flitSize);
		header = replaceWord(header,"%bufferDepth",""+noc.getBufferDepth());
		header = replaceWord(header,"%algorithm",noc.getRoutingAlgorithm());
		footer = replaceWord(footer,"%nGeneratedPackets",""+nGeneratedPackets);
		footer = replaceWord(footer,"%nTransmittedPackets",""+nTransmittedPackets);
		footer = replaceWord(footer,"%averageThroughputTotal",DuasCasasDepoisDaVirgula(""+averageThroughputTotal));
		footer = replaceWord(footer,"%averageLatencyTotal",DuasCasasDepoisDaVirgula(""+averageLatencyTotal));
		footer = replaceWord(footer,"%TIME", Integer.toString(date.get(Calendar.HOUR_OF_DAY)) + ":" + Integer.toString(date.get(Calendar.MINUTE)) + ":" + Integer.toString(date.get(Calendar.SECOND)));
		footer = replaceWord(footer,"%DATE", strDate);

		try{
			FileOutputStream  fos =  new FileOutputStream(path + File.separator + "reports" + File.separator + "Report.htm");
			OutputStreamWriter osw =  new OutputStreamWriter(fos);
			Writer on = new BufferedWriter(osw);
			on.write(header + body + footer);
			on.close();
			osw.close();
			fos.close();
			Help.show(new File(path + File.separator + "reports" + File.separator + "Report.htm"));
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in writeHTML: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Calculate the ideal(minimal) latency between a source router and a target router considering the packet size.
	 * @param source The source router.
	 * @param target The target router.
	 * @param packetSize The packet size.
	 * @return The minimal latency.
	 */
	private double calcIdealLatency(int source,int target,int packetSize){
		int nRoutersX = 0,nRoutersY = 0,nRouters = 0;
		int sourceX = source % nRotX;
		int sourceY = source / nRotX;
		int targetX = target % nRotX;
		int targetY = target / nRotX;

		if(noc.getType().equalsIgnoreCase("Hermes")){

			if(targetX > sourceX)
				nRoutersX = targetX - sourceX;
			else
				nRoutersX = sourceX - targetX;

			if(targetY > sourceY)
				nRoutersY = (targetY - sourceY);
			else
				nRoutersY = (sourceY - targetY);

			nRouters = nRoutersX + nRoutersY + 1; // 1 representa o roteamento para sair da rede (porta LOCAL)
		}
		else if(noc.getType().equalsIgnoreCase("HermesTU")){
			if(targetX > sourceX)
				nRoutersX = targetX - sourceX;
			else
				nRoutersX = (nRotX - sourceX) + targetX;

			if(targetY > sourceY)
				nRoutersY = (targetY - sourceY);
			else
				nRoutersY = (nRotY - sourceY) + targetY;

			nRouters = nRoutersX + nRoutersY + 1; // 1 representa o roteamento para sair da rede (porta LOCAL)
		}
		else if(noc.getType().equalsIgnoreCase("HermesSR")){
			nRouters = nHops[0][source][target]; 
		}

		return ((nRouters*cyclesToRoute) + (packetSize)); //- 1));
	}

	final private String DuasCasasDepoisDaVirgula(String Vitima){
		int diferenca=0;
		if(Vitima.indexOf(".")>0){
			diferenca =Vitima.length() - Vitima.indexOf(".");
			if(diferenca>3)
				diferenca=3;
			Vitima = Vitima.substring(0, Vitima.indexOf(".")+diferenca);
		}
		return Vitima;
	}

	//as if this bit would make HUGE performance difference :-)
	final private String replaceWord(String original, String find, String replacement){
		String partBefore =  new String();
		String partAfter =  new String();

		try{
			int i = original.indexOf(find);
			if (i < 0)
				return original;  // return original if 'find' is not in it.

			partBefore = original.substring(0, i);
			partAfter  = original.substring(i + find.length());
			return (partBefore + replacement + partAfter);
		}catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in replaceWord: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		return "";
	}
}