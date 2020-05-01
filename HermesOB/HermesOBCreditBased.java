package HermesOB;

import javax.swing.*;

import java.io.*;
import java.util.*;

import AtlasPackage.*;

/**
 * Generate a HermesOB NoC.
 * @author Nicolas Lodéa e Angelo Elias Dalzotto
 * @version
 */
public class HermesOBCreditBased extends NoCGenerationCB{
	private static String sourceDir = Default.atlashome + File.separator + "HermesOB" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;

	private String projectDir, nocDir, hermesDir,nocHermesDir,nocModulesDir,modulesDir;
    private String algorithm, nocType;
    private int dimX, dimY, dimension, flitSize, bufferSize;

	/**
	 * Generate a HermesOB NoC.
	 * @param project The NoC project.
	 */
	public HermesOBCreditBased (Project project){
		super(project, sourceDir);
		NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
		bufferSize = noc.getBufferDepth();

		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator; 

		nocHermesDir = projectDir + "hermes" + File.separator;
		hermesDir = sourceDir + "hermes" + File.separator;
		nocModulesDir = projectDir + "modules" + File.separator;
		modulesDir = sourceDir + "modules" + File.separator;

	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		//makeDiretories();
		// Copy and create NoC VHDL files for synthesis
		copyNoCFiles();		
		updateConstantHpp();
		updateConstantVhd();
	}

/*********************************************************************************
* DIRECTORIES AND FILES (HERMES_BUFFER AND HERMES_SWITCHCONTROL)
*********************************************************************************/

	/**
	* copy the VHDL files to the project directory.
	*/
  	public void copyNoCFiles(){
  		
  		//create the hermes directory
		File dir=new File(nocHermesDir);
		dir.mkdirs();

		//create the hermes directory
		dir=new File(nocModulesDir);
		dir.mkdirs();


  		ManipulateFile.copy(new File(hermesDir + "arbiter.vhd"), nocHermesDir);
 		ManipulateFile.copy(new File(hermesDir + "noc.vhd"), nocHermesDir);
  		ManipulateFile.copy(new File(hermesDir + "ringbuffer.vhd"),nocHermesDir);
  		ManipulateFile.copy(new File(hermesDir + "standards.vhd"), nocHermesDir);
  		ManipulateFile.copy(new File(hermesDir + "constants.vhd"), nocHermesDir);
  		ManipulateFile.copy(new File(hermesDir + "node.vhd"), nocHermesDir);
  		ManipulateFile.copy(new File(hermesDir + "router.vhd"), nocHermesDir);
  		ManipulateFile.copy(new File(modulesDir + "InputModule.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "OutputModule.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "RouterOutputModule.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "InputModule.hpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "OutputModule.hpp"), nocModulesDir);
		ManipulateFile.copy(new File(modulesDir + "RouterOutputModule.hpp"), nocModulesDir);
		ManipulateFile.copy(new File(modulesDir + "standards.hpp"), nocModulesDir);
		ManipulateFile.copy(new File(modulesDir + "constants.hpp"), nocModulesDir);

  		ManipulateFile.copy(new File(sourceDir + "simulate.do"), projectDir);
  		ManipulateFile.copy(new File(sourceDir + "wave.do"), projectDir);
  		ManipulateFile.copy(new File(sourceDir + "testbench.vhd"), projectDir);
  		
  	}



	/**
	 * Update the simulate.do according to Internal Simulation option.
	 */
	public void updateConstantHpp(){
		DataOutputStream dos;

		StringTokenizer st;
		String line, word;

		String nameTemp = nocModulesDir + File.separator + "temp.txt";
		String constFile = nocModulesDir + File.separator + "constants.hpp";

		//create a temporary file, after update data
		copyFile(new File(constFile), new File(nameTemp));	

		try{
			FileInputStream fis=new FileInputStream(nameTemp);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			dos=new DataOutputStream(new FileOutputStream(constFile));

			line=br.readLine();
			while(line!=null){
				st = new StringTokenizer(line, " ");
				int nTokens = st.countTokens();
				
				if(nTokens!=0){
					while (st.hasMoreTokens()){

						word = st.nextToken();
						
						if(word.equalsIgnoreCase("FLIT_SIZE")){
						    line = "	const unsigned int " + word + " = " + flitSize + ";";
							
						}

						if(word.equalsIgnoreCase("X_SIZE")){
						    line = "	const unsigned int X_SIZE = " + dimX + ";";
						    
						}

						if(word.equalsIgnoreCase("Y_SIZE")){
						     line = "	const unsigned int Y_SIZE = " + dimY + ";";
						    
						}
					}

				}
				
				dos.writeBytes(line+"\n");
				line=br.readLine();
			}
			br.close();
			fis.close();
			dos.close();
			//delete the temporary file
			//File file = new File(nameTemp);
			//file.delete();
			
			
		}
		
		catch(Exception e){
			System.out.println("Erro");
			System.exit(0);
		}
	}

	/**
	 * Update the simulate.do according to Internal Simulation option.
	 */
	public void updateConstantVhd(){

		DataOutputStream dos;

		StringTokenizer st;
		String line, word;

		String nameTemp = nocHermesDir + File.separator + "temp.txt";
		String constFile = nocHermesDir + File.separator + "constants.vhd";

		//create a temporary file, after update data
		copyFile(new File(constFile), new File(nameTemp));	

		try{
			FileInputStream fis=new FileInputStream(nameTemp);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			dos=new DataOutputStream(new FileOutputStream(constFile));

			line=br.readLine();
			
			while(line!=null){
				st = new StringTokenizer(line, " ");
				int nTokens = st.countTokens();
				
				if(nTokens!=0){
					while (st.hasMoreTokens()){

						word = st.nextToken();
							
						if(word.equalsIgnoreCase("BUFFER_SIZE")){
						    line = "	constant BUFFER_SIZE: integer := " + bufferSize + ";";
						    
						}

						if(word.equalsIgnoreCase("FLIT_SIZE")){
						    line = "	constant FLIT_SIZE: integer range 1 to 64 := " + flitSize + ";";
						    
						}

						if(word.equalsIgnoreCase("X_SIZE")){
						    line = "	constant X_SIZE : integer := " + dimX + ";";
						    
						}

						if(word.equalsIgnoreCase("Y_SIZE")){
						    line = "	constant Y_SIZE : integer := " + dimY + ";";
						    
						}
					}

				}
				dos.writeBytes(line+"\n");
				line=br.readLine();
			}

			br.close();
			fis.close();
			dos.close();
			//delete the temporary file
			//File file = new File(nameTemp);
			//file.delete();
			
		}
		
		catch(Exception e){
			System.out.println("Erro");
			System.exit(0);
		}
	}


	private void copyFile(File fileIn,File fileOut){
		try{
			FileReader in = new FileReader(fileIn);
			FileWriter out = new FileWriter(fileOut);
			int c;
			while ((c = in.read()) != -1){
				out.write(c);
			}
			in.close();
			out.close();
		}
		catch(Exception e){
			System.out.println("Erro");
			System.exit(0);
		}

		
	}
}