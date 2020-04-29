package AtlasPackage;

import javax.swing.*;

import java.io.*;

/**
 * <i>Project</i> class contains information about a Project. <p>

 * <b>Project general information:</b>
 *  {Name}MyProject <p>
 * 
 * <b>Project information associated to NoC Generation phase:</b>
 *  {Type}Hermes
 *  {NumRotX}2
 *  {NumRotY}2
 *  {FlitSize}16
 *  {BufferDepth}16
 *  {FlowControl}CreditBased
 *  {VirtualChannel}2
 *  {RoutingAlgorithm}XY
 *  {CyclesPerFlit}1
 *  {CyclesToRoute}5
 *  {Scheduling}RoundRobin
 *  {AdmissionControl}false
 *  {SCTB}true 
 *  {Saboteur}false
 *  {SaboteurTypes}
 *  {CRCType}
 *  {NoCGenerate}false <p>
 *  
 * <b>Project information associated to traffic Generation phase:</b>
 *  {TrafficGenerate}true
 *  {ScenaryName}MyScenery <p>
 * 
 * <b>Project information associated to mapping phase:</b>
 * {MapCores}true
 * {RouterCore00}serial
 * {RouterCore01}tester
 * {RouterCore10}tester
 * {RouterCore11}tester
 * 
 * <b>Project information associated to synthesis phase:</b>
 * {Synthesis}true
 * {Family}Virtex-II Pro
 * {Device}xc2vp30
 * {Package}896
 * {Speed}-7
 */
public class Project{

	private File projFile;
	private Scenery scenery;
	private NoC noc;
	private String path,name;
	/** The name of last open scenery.*/
	private String sceneryName;
	private String sceneryPath;
	private File sceneryFile;
	private String timeWindow;
	private String family, device, pack, speed;
	private boolean nocGenerate,trafficGenerate,mapCores,synthesis,powerEstimated;

	/**
	 * Creates a new project with the default parameters.
	 * @param dir The absolute path of directory where the project will be created.
	 * @param name The project name
	 * @param nocType The NoC type
	 */
	public Project(String dir,String name,String nocType){
		//general parameters
		path = dir + File.separator + name;
	    this.name = name;
		createDirectory();
		projFile = new File(path + File.separator + name+".noc");
		//NoC generation parameters
		noc = new NoC(nocType);
		//nocGenerate = false;
		//Traffic generation parameters
		defaultTrafficParameters();
		defaultPowerParameters();
		defaultMappingParameters();
		defaultSynthesisParameters();
	}

	/**
	 * Creates a new project with the default parameters.
	 * @param name The project name
	 * @param nocType The NoC type
	 */
	public Project(String name,String nocType){
		//general parameters
		this.name = name;
		path = null;
		projFile = null;
		//NoC generation parameters
		noc = new NoC(nocType);
		nocGenerate = false;
		//Traffic generation parameters
		defaultTrafficParameters();
		defaultPowerParameters();
		defaultMappingParameters();
		defaultSynthesisParameters();
	}
	
	/**
	 * Create a new project with the parameters informed in file.
	 * @param file
	 */
	public Project(File file){
		path = file.getParent();
		projFile = file;
		noc = new NoC(file);
		read();
	}

	/**
	 * Initializes the Traffic Generation parameters.
	 */
	private void defaultTrafficParameters(){
		trafficGenerate = false;
		sceneryName = "";
		sceneryPath = "";
		sceneryFile = null;
		scenery = null;
	}
	
	/**
	 * Initializes the Power Evaluation parameters.
	 */
	private void defaultPowerParameters(){
		timeWindow = "03E8";
		powerEstimated=false;
	}
	
	/**
	 * Initializes the Mapping core parameters.
	 */
	private void defaultMappingParameters(){
		mapCores=false;
	}
	
	/**
	 * Initializes the Synthesis parameters.
	 */
	private void defaultSynthesisParameters(){
		synthesis=false;
		family="virtex2p";
		device="xc2vp30";
		pack="ff896";
		speed="-7";
	}
		
	////////////////////////////////////////////////////////////////////
	// Project parameters
	////////////////////////////////////////////////////////////////////
	/**
	 * Returns the project file. It has .noc extension. 
	 * For instance: c:\hermes3x3\hermes3x3.noc
	 * @return file
	 */
	public File getProjFile(){return projFile;}
	
	/**
	 * Returns the pathname string of the project file. 
	 * For instance: "c:\hermes3x3\hermes3x3.noc"
	 * @return s
	 */
	public String getStringProjFile(){return projFile.getAbsolutePath();}
	
	/**
	 * Returns the absolute pathname string of the project directory. 
	 * For instance: "c:\hermes3x3"
	 * @return path
	 */
	public String getPath(){return path;}
	
	/**
	 * Returns the project name. For instance: "hermes3x3"
	 * @return name
	 */
	public String getName(){return name;}

	////////////////////////////////////////////////////////////////////
	// NoC generation parameters
	////////////////////////////////////////////////////////////////////
	/**
	 * Returns NoC object associated to the project.
	 * @return NoC
	 */
	public NoC getNoC(){return noc;}
	
	/**
	 * Test if the NoC has been already generated. 
	 * @return b
	 */
	public boolean isNoCGenerate(){return nocGenerate;}

	/**
	 * Determines whether a NoC traffic has been generated.
	 * @param b
	 */
	public void setNoCGenerate(boolean b){
		nocGenerate=b;
		if(b){
			trafficGenerate = false;
			scenery = null;
		}
	}

	////////////////////////////////////////////////////////////////////
	// Traffic generation parameters
	////////////////////////////////////////////////////////////////////
	/**
	 * Tests whether the Traffic has been already generated. 
	 * @return b
	 */
	public boolean isTrafficGenerate(){return trafficGenerate;}
	
	/**
	 * Return the name of the last open scenery.
	 * @return sceneryName
	 */
	public String getSceneryName(){return sceneryName;}
	
	/**
	 * Return the pathname String of the last open scenery.
	 * @return sceneryPath
	 */
	public String getSceneryPath(){return sceneryPath;}

	/**
	 * Return the file of the last open scenery.
	 * @return The file
	 */
	public File getSceneryFile(){return sceneryFile;}

	/**
	 * Return the current Scenery. 
	 * @return scenery
	 */
	public Scenery getScenery(){return scenery;}

	/**
	 * Test if the project has been already simulated at least one time. 
	 * @return b
	 */
	public boolean isSimulate(){
		File f = new File(path + File.separator + "Traffic");
		File[] list = f.listFiles();
		if(list!=null){
			for(int i=0;i<list.length;i++){
				File out = new File(list[i] + File.separator + "Out");
				if(out.exists()){
					if(out.listFiles()!=null && out.listFiles().length!=0){
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Determines whether a scenery traffic has been generated.
	 * @param b
	 */
	public void setTrafficGenerate(boolean b){ trafficGenerate=b; }

	/**
	 * Sets the current scenery path.
	 * @param s
	 */
	public void setSceneryName(String s){ 
		sceneryName = s;
		sceneryPath = path + File.separator + "Traffic" + File.separator + sceneryName;
		sceneryFile = new File(sceneryPath + File.separator + sceneryName + ".traffic");

		//creates the scenery directory if it does not exist.
		File f = new File(sceneryPath);
		f.mkdirs();
	}

	/**
	 * Sets the current scenery.
	 * @param s
	 */
	public void setScenery(Scenery s){
		scenery=s;
		setSceneryName(s.getName());
	}

	////////////////////////////////////////////////////////////////////
	// Mapping parameters
	////////////////////////////////////////////////////////////////////
	/**
	 * Tests whether the mapping phase is active.
	 * @return b
	 */
	public boolean isMapCores(){return mapCores;}
	
	/**
	* Tests whether mapping has been modified.
	* @return True if the mapping has any modification.
	*/
	public boolean isModifyedMapCores(){
		try{
			if(nocGenerate){
				String atualCore, oldCore;
				for(int x=0;x<noc.getNumRotX();x++){
					for(int y=0;y<noc.getNumRotY();y++){
						atualCore = scenery.getRouter(x,y).getCore();
						oldCore = Default.readInfo(projFile, "RouterCore"+x+"-"+y);
						if(!atualCore.equalsIgnoreCase(oldCore))
							return true;
					}
				}
			}
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error in isModifyedMapCores project method", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return false;
	}

	/**
	 * Determines whether the mapping phase is active.
	 * @param b
	 */
	public void setMapCores(boolean b){mapCores=b;}
	
	////////////////////////////////////////////////////////////////////
	// Power evaluation parameters 
	////////////////////////////////////////////////////////////////////
	/**
	 * Returns the time window power parameter. 
	 * @return timeWindow
	 */
	public String getTimeWindow(){return timeWindow;}
	
	/**
	 * Tests whether the power estimation is active.
	 * @return powerEstimated
	 */
	public boolean isPowerEstimated(){return powerEstimated;}
	
	/**
	 * Sets the time window power parameter.
	 * @param s
	 */
	public void setTimeWindow(String s){ timeWindow=s;}

	/**
	 * Determines whether the power estimation and evaluation are active.
	 * @param b
	 */
	public void setPowerEstimated(boolean b){powerEstimated=b;}

	////////////////////////////////////////////////////////////////////
	// Synthesis parameters
	////////////////////////////////////////////////////////////////////
	/**
	 * Tests whether the synthesis phase is active.
	 * @return powerEstimated
	 */
	public boolean isSynthesis(){return synthesis;}
	
	/**
	 * Returns the family synthesis parameter. 
	 * @return family
	 */
	public String getFamily(){return family;}

	/**
	 * Returns the device synthesis parameter. 
	 * @return device
	 */
	public String getDevice(){return device;}
	
	/**
	 * Returns the package synthesis parameter. 
	 * @return package
	 */
	public String getPackage(){return pack;}

	/**
	 * Returns the speed synthesis parameter. 
	 * @return speed
	 */
	public String getSpeed(){return speed;}

	/**
	 * Determines whether the synthesis phase is active.
	 * @param b
	 */
	public void setSynthesis(boolean b){synthesis=b;}

	/**
	 * Sets the family synthesis parameter.
	 * @param s
	 */
	public void setFamily(String s){family=s;}
	
	/**
	 * Sets the device synthesis parameter.
	 * @param s
	 */
	public void setDevice(String s){device=s;}

	/**
	 * Sets the package synthesis parameter.
	 * @param s
	 */
	public void setPackage(String s){pack=s;}
	
	/**
	 * Sets the speed synthesis parameter.
	 * @param s
	 */
	public void setSpeed(String s){speed=s;}

	////////////////////////////////////////////////////////////////////
	// General Methods
	////////////////////////////////////////////////////////////////////
	/**
	 * Creates the project directory.
	 */
	public void createDirectory(){
		File f = new File(path);
		f.mkdirs();
	}
	
	/**
	* Returns the Project parameters in a single string.
	* @return s
	*/
	public String getInfo(){
		String s = ("{Project}" +
					"\n{Name}" + name +
					"\n\n");
		s = s + getNoCParameters();
		s = s + getTrafficParameters();
		s = s + getPowerParameters();
		s = s + getMappingParameters();
		s = s + getSynthesisParameters();
		return s;
	}	

	/**
	* Returns the NoC generation parameters.
	* @return s
	*/
	public String getNoCParameters(){
		return (noc.getParameters() +
				"\n{NocGenerate}" + Boolean.toString(nocGenerate) +
				"\n\n");
	}	
	
	/**
	* Returns the Traffic generation parameters.
	* @return s
	*/
	public String getTrafficParameters(){
		return ("{Traffic}" +
				"\n{TrafficGenerate}" + Boolean.toString(trafficGenerate) +
				"\n{SceneryName}" + sceneryName +
				"\n\n");
	}	

	/**
	* Returns the Power evaluation parameters.
	* @return s
	*/
	public String getPowerParameters(){
		return ("{Power}" +
				"\n{TimeWindow}" + timeWindow +
				"\n{PowerEstimated}" + Boolean.toString(powerEstimated) +
				"\n\n");
	}	

	/**
	* Returns the Mapping core parameters.
	* @return s
	*/
	public String getMappingParameters(){
		String s = ("{Mapping}" +
					"\n{MapCores}" + mapCores);
		if(scenery!=null){
			Router router;
			for(int x=0;x<noc.getNumRotX();x++){
				for(int y=0;y<noc.getNumRotY();y++){
					router = scenery.getRouter(x,y);
					s = s + "\n{RouterCore"+x+"-"+y+"}"+router.getCore();
				}
			}
		}
		s = s + "\n\n";
		return s;
	}	
	
	/**
	* Returns the Power evaluation parameters.
	* @return s
	*/
	public String getSynthesisParameters(){
		return ("{Synthesis}" +
				"\n{Family}" + family +
				"\n{Device}" + device +
				"\n{Pack}" + pack +
				"\n{Speed}" + speed +
				"\n\n");
	}	

	/**
	 * Write project parameters in project file.
	 */
	public void write(){
		BufferedWriter bw;
		try{
			bw = new BufferedWriter(new FileWriter(projFile));
			bw.write(getInfo());
			bw.close();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Write project file", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Write project parameters in the informed file.
	 * @param f The file where the project parameters will be written.
	 */
	public void write(File f){
		BufferedWriter bw;
		try{
			bw = new BufferedWriter(new FileWriter(f));
			bw.write(getInfo());
			bw.close();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Write project file", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Read parameters in project file.
	 */
	private void read(){
		try{
			name = Default.readInfo(projFile, "Name");
			readNoCParameters();
			readTrafficParameters();
			readPowerParameters();
			readMappingParameters();
			readSynthesisParameters();
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error in read project Parameters", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Read NoC generation parameters in project file.
	 */
	private void readNoCParameters() throws Exception{
		noc.readParameters(projFile);
		nocGenerate = Boolean.parseBoolean(Default.readInfo(projFile, "NocGenerate"));
	}
	
	/**
	 * Read Traffic generation parameters in project file.
	 */
	private void readTrafficParameters() throws Exception{
		trafficGenerate = Boolean.parseBoolean(Default.readInfo(projFile, "TrafficGenerate"));
		if(trafficGenerate)
			setSceneryName(Default.readInfo(projFile, "SceneryName"));
	}

	/**
	 * Read Power evaluation parameters in project file.
	 */
	private void readPowerParameters() throws Exception{
		timeWindow = Default.readInfo(projFile, "TimeWindow");
		powerEstimated = Boolean.parseBoolean(Default.readInfo(projFile, "PowerEstimated"));
	}

	/**
	 * Read Mapping core parameters in project file.
	 */
	private void readMappingParameters() throws Exception{
		mapCores = Boolean.parseBoolean(Default.readInfo(projFile, "MapCores"));
		if(scenery!=null){
			for(int x=0;x<noc.getNumRotX();x++){
				for(int y=0;y<noc.getNumRotY();y++){
					scenery.getRouter(x,y).setCore(Default.readInfo(projFile, "RouterCore"+x+"-"+y));
				}
			}
		}
	}	

	/**
	 * Read Synthesis parameters in project file.
	 */
	private void readSynthesisParameters() throws Exception{
		family = Default.readInfo(projFile, "Family");
		device = Default.readInfo(projFile, "Device");
		pack = Default.readInfo(projFile, "Pack");
		speed = Default.readInfo(projFile, "Speed");
	}

	/**
	 * Delete all files and directories in the project path. Except the project file.
	 */
	public void delete(){
		ManipulateFile.deleteFiles(getPath(), projFile.getAbsolutePath());
	}

	/**
	 * Updates the project information.
	 */
	public void refresh(){
		read();
	}
}