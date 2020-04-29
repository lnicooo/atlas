package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Generate a NoC.
 * @author Aline Vieira de Mello
 * @version
 */
public class NoCGeneration{

	private String sourceDir, projectDir, nocDir, scDir;
	private String flitWidth, crcType;
	private int dimX, dimY, flitW, nChannels, bufferDepth;
	private boolean isSC, isSR4;

	/**
	 * Generate a NoC with Virtual Channels.
	 * @param project The NoC project.
	 * @param source The path where are the source files. 
	 */
	public NoCGeneration(Project project, String source){
		sourceDir  = source;
		initialize(project);
	}

	/**
	 * Initialize variables.
	 * @param project The NoC project.
	 */
	private void initialize(Project project){
		NoC noc = project.getNoC();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		flitW = noc.getFlitSize();
		flitWidth = ""+flitW;
		nChannels = noc.getVirtualChannel();
		bufferDepth = noc.getBufferDepth();
		isSC = noc.isSCTB();
		isSR4 = noc.isSR4();
		crcType = noc.getCrcType();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator;
		scDir      = projectDir + "SC_NoC" + File.separator;
	}
	
/*********************************************************************************
* DIRECTORIES AND FILES (HERMES_BUFFER AND HERMES_SWITCHCONTROL)
*********************************************************************************/
	/**
	 * Create the project directory tree.
	 */
	public void makeDiretories(){
		//create the project directory
		File diretory = new File(projectDir);
		diretory.mkdirs();

		//create the NoC directory
		File nocDir=new File(diretory +File.separator + "NOC");
		nocDir.mkdirs();

		//If the SC test bench option is selected, create the SC_NoC directory
		if(isSC){
			File scDir=new File(diretory +File.separator + "SC_NoC");
			scDir.mkdirs();
		}
	}

/*********************************************************************************
* PACKAGE
*********************************************************************************/
	/**
	 * Create the package vhdl file, replacing the flags.
	 * @param nameFile The name of package file.
	 */
	public void createPackage(String nameFile){
		StringTokenizer st;
		String addrX,addrY,addrXHexa,addrYHexa;
		String line, word, change_parameter;

		try{
		    FileInputStream inFile = new FileInputStream(new File(sourceDir + nameFile));
		    BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
		    
		    DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + nameFile));
			
			int n_lines=0;
			change_parameter="";
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					change_parameter="";
					if(word.equalsIgnoreCase("n_nodos")){
						int contRouters=0;

						for(int y=0;y<dimY;y++){
							addrY = Convert.decToBin(y,(flitW/4));
							addrYHexa = Convert.decToHex(y,(flitW/8));

							for(int x=0;x<dimX;x++){
								addrX = Convert.decToBin(x,(flitW/4));
								addrXHexa = Convert.decToHex(x,(flitW/8));

								data_output.writeBytes("\tconstant N"+addrXHexa+addrYHexa+": integer :="+ contRouters + ";\n");
								data_output.writeBytes("\tconstant ADDRESSN"+addrXHexa+addrYHexa+": std_logic_vector("+((flitW/2)-1)+" downto 0) :=\""+addrX+addrY+"\";\n");
								contRouters++;
							}
						}
					}
					else if(word.equalsIgnoreCase("n_lanes")){
						for(int i=0;i<nChannels;i++){
							if(isSR4)
								data_output.writeBytes("\tconstant ID_CV"+(i)+": integer := "+i+";\n");
							else
								data_output.writeBytes("\tconstant L"+(i+1)+": integer := "+i+";\n");
						}
					}
					else{
						if(word.equalsIgnoreCase("n_port"))
							word = "5";
						else if (word.equalsIgnoreCase("tam_line"))
							word = ""+(flitW/4);
						else if (word.equalsIgnoreCase("flit_size"))
							word = flitWidth;
						else if(word.equalsIgnoreCase("buff_depth"))
							word = ""+bufferDepth;
						else if(word.equalsIgnoreCase("pointer_size")){
							if (bufferDepth<=4) word="3";
							else if (bufferDepth<=8) word="4";
							else if (bufferDepth<=16) word="5";
							else if (bufferDepth<=32) word="6";
						}
						else if(word.equalsIgnoreCase("n_rot"))
							word = ""+(dimX*dimY);
						else if(word.equalsIgnoreCase("max_X"))
							word = ""+(dimX-1);
						else if(word.equalsIgnoreCase("max_Y"))
							word = ""+(dimY-1);
						else if(word.equalsIgnoreCase("n_lane"))
							word = ""+nChannels;
						else if (word.equalsIgnoreCase("buff1")){
							if (!crcType.equalsIgnoreCase(NoC.SOURCE_CRC))
								word = "";
							else
								word = "--";
						}
						else if (word.equalsIgnoreCase("buff2")){
							if (crcType.equalsIgnoreCase(NoC.SOURCE_CRC))
								word = "";
							else
								word = "--";
						}

						change_parameter = change_parameter.concat(word);
						data_output.writeBytes(change_parameter);
					}
				}
				data_output.writeBytes("\r\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write " + nameFile + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

/*********************************************************************************
* GENERAL FUNCTIONS
*********************************************************************************/

	
	/**
	 * Return a vector containing the name of all routers.
	 * @return A vector containing the name of all routers.
	 */
	public Vector<String> getRoutersName(){
		String xHexa="", yHexa="";
		Vector<String> name = new Vector<String>();
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitW/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitW/8));
				name.addElement("N"+xHexa+yHexa);
			}
		}
		return name;
	}
	
	/**
	 * Instance a signal of all router in the same X-dimension.
	 * @param data_output 
	 * @param name The signal name.
	 * @param type The signal type.
	 * @param yHexa The address in Y-dimension. 
	 * @throws Exception 
	 */
	public void writeSignal(DataOutputStream data_output,String name,String type,String yHexa) throws Exception {
		String xHexa;
		data_output.writeBytes("\tsignal ");
		for (int x=0; x<dimX; x++){
			xHexa = Convert.decToHex(x,(flitW/8));
			if (x==0)
				data_output.writeBytes(name+"N"+xHexa+yHexa);
			else
				data_output.writeBytes(", "+name+"N"+xHexa+yHexa);
		}
		data_output.writeBytes(" : "+type+";\n");
	}
	
/*********************************************************************************
* SystemC
*********************************************************************************/
	
	/**
	* copy the SC files to the SC_NoC project directory.
	*/
   	public void copySCFiles(){
		//copy CPP files to SC_NoC directory
		ManipulateFile.copy(new File(sourceDir+"SC_InputModule.cpp"), scDir);
		ManipulateFile.copy(new File(sourceDir+"SC_OutputModule.cpp"), scDir);
		ManipulateFile.copy(new File(sourceDir+"SC_OutputModuleRouter.cpp"), scDir);
   	}

/*********************************************************************************
* SCRIPTS
*********************************************************************************/
   	
	/**
	 * Write the simulate header (vlib and vmap).
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSimulateHeader(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("if {[file isdirectory work]} { vdel -all -lib work }\n\n");
		data_output.writeBytes("vlib work\n");
		data_output.writeBytes("vmap work work\n\n");
	}
	
	/**
	 * Write the SCCOM command (SystemC compilation) for all SC files and link.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSCCOMFiles(DataOutputStream data_output) throws Exception {
		writeSCCOM(data_output, "SC_NoC/SC_InputModule.cpp");
		writeSCCOM(data_output, "SC_NoC/SC_OutputModule.cpp");
		writeSCCOM(data_output, "SC_NoC/SC_OutputModuleRouter.cpp");
		writeSCCOMLink(data_output);
	}
	
	/**
	 * Write the SCCOM command (SystemC compilation) for a specific file.
	 * @param data_output 
	 * @param file The file path.
	 * @throws Exception 
	 */
	public void writeSCCOM(DataOutputStream data_output, String file) throws Exception {
		data_output.writeBytes("sccom -B/usr/bin -B/usr/bin -g "+file+"\n");
	}
	
	/**
	 * Write the SCCOM link.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSCCOMLink(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("sccom -B/usr/bin -B/usr/bin -link\n\n");
	}
	
	/**
	 * Write the VCOM command (VHDL compilation) for all routers.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeVCOMRouters(DataOutputStream data_output) throws Exception {
		String routerName;
		int xD = dimX;
		int yD = dimY;
		if (dimX>3) xD = 3;
		if (dimY>3) yD = 3;
		for(int y =0; y < yD; y++){
			for(int x =0; x < xD; x++){
				routerName = Router.getRouterType(x, y, xD, yD);
				writeVCOM(data_output, "NOC/"+routerName+".vhd");
			}
		}
	}
	
	/**
	 * Write the VCOM command (VHDL compilation) for a specific file.
	 * @param data_output 
	 * @param file The file path.
	 * @throws Exception 
	 */
	public void writeVCOM(DataOutputStream data_output, String file) throws Exception {
		data_output.writeBytes("vcom -work work -93 -explicit "+file+"\n");
	}

	/**
	 * Write the VSIM command (Modelsim Simulation) for a specific top entity.
	 * @param data_output 
	 * @param nameTop The name of top entity.
	 * @throws Exception 
	 */
	public void writeVSIM(DataOutputStream data_output, String nameTop) throws Exception {
		//data_output.writeBytes("\nvsim -valgrind -t 10ps work."+nameTop+"\n\n");
		data_output.writeBytes("\nvsim -t 10ps work."+nameTop+"\n\n");
	}	
	
	/**
	 * Write set StdArithNoWarnings.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoWarnings(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("set StdArithNoWarnings 1\n");
		data_output.writeBytes("set StdVitalGlitchNoWarnings 1\n");
	}

	/**
	 * Write do for a specific file.
	 * @param data_output 
	 * @param file The file path.
	 * @throws Exception 
	 */
	public void writeDO(DataOutputStream data_output, String file) throws Exception {
		data_output.writeBytes("do "+file+"\n");
	}
	
	/**
	 * Write Add list for a specific signal.
	 * @param data_output 
	 * @param signal The all path signal.
	 * @throws Exception 
	 */
	public void writeAddList(DataOutputStream data_output, String signal) throws Exception {
		data_output.writeBytes("add list -radix decimal "+signal+"\n");
	}
	
	/**
	 * Write list result in a specific file.
	 * @param data_output 
	 * @param file The file name.
	 * @throws Exception 
	 */
	public void writeList(DataOutputStream data_output, String file) throws Exception {
		data_output.writeBytes("write list "+file+"\n");			
	}
	
	/**
	 * Write run for a default simulation time and time resolution (1 ms).
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeRUN(DataOutputStream data_output) throws Exception {
		writeRUN(data_output, 1, "ms");
	}

	/**
	 * Write run for a specific simulation time and time resolution.
	 * @param data_output 
	 * @param time The simulation time.
	 * @param resolution The time resolution? For instance; ms, us, ns, etc.
	 * @throws Exception 
	 */
	public void writeRUN(DataOutputStream data_output, int time, String resolution) throws Exception {
		data_output.writeBytes("\nrun "+time+" "+resolution+"\n\n");
	}

	
	/**
	 * Write the end of Simulate file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSimulateFooter(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("quit -sim\n");
		data_output.writeBytes("quit -f\n\n");
	}
   	
}