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
    private int dimX, dimY, dimension, flitSize;
	private boolean isSC, isSabot, isDr, isDf, isGn, isGp;
	private boolean isLinkCRC, isSourceCRC, isHammingCRC;
	private Vector<String> vectorSwitch;

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

		vectorSwitch = new Vector<String>();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator; 

		nocHermesDir = nocDir + "hermes" + File.separator;
		hermesDir = sourceDir + "hermes" + File.separator;
		nocModulesDir = nocDir + "modules" + File.separator;
		modulesDir = sourceDir + "modules" + File.separator;

	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		makeDiretories();
		// Copy and create NoC VHDL files for synthesis
		copyNoCFiles();		
		//create HermesPackage.vhd 
		//createPackage("Hermes_package.vhd");
		
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
  		ManipulateFile.copy(new File(modulesDir + "SC_InputModule.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "SC_OutputModule.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "SC_OutputModuleRouter.cpp"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "SC_InputModule.h"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "SC_OutputModule.h"), nocModulesDir);
  		ManipulateFile.copy(new File(modulesDir + "SC_OutputModuleRouter.h"), nocModulesDir);

  		ManipulateFile.copy(new File(sourceDir + "simulate.do"), projectDir);
  		ManipulateFile.copy(new File(sourceDir + "wave.do"), projectDir);
  		ManipulateFile.copy(new File(sourceDir + "testbench.vhd"), projectDir);
  		
  	}

}