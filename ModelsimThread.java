import javax.swing.*;
import java.io.*;
import AtlasPackage.*;

/**
 * Thread that monitor the Modelsim simulation using a progress bar.
 * @author Aline Vieira de Mello
 * @version
 */
public class ModelsimThread extends Thread{

    private int max;
    private Process process;
    private ProgressBarFrame pb;

    /**
     * Launch the thread that monitor the Modelsim simulation.
     * @param p
     * @param max
     */
    public ModelsimThread(Process p, int max){
		this.process = p;
		this.max = max;
		pb = new ProgressBarFrame("Modelsim Simulation",max);
		start();
    }
    
    /**
     * Launch the thread to monitor the Modelsim simulation.
     */
    public void run(){
    	modelsimSimulation();
    }

    private void modelsimSimulation(){
		int i, exit;
		try{
		    for(i = 0; i< max; i++){
				try{
				    exit = process.exitValue();
				    if(exit!=0){
						pb.end();
						i = max;
				    }
				    else{
						i = max;
						pb.setValue(max);
						//get the error message
						BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				    	String err="";
				    	String line = br.readLine();
				    	while(line != null){
				    		err = err + line + "\n";
			    			line = br.readLine();
				    	}
				    	if(!err.equals("")){
				    		JOptionPane.showMessageDialog(null,err,"Modelsim Error", JOptionPane.ERROR_MESSAGE);
				    	}
				    }
				}catch(IllegalThreadStateException te){
				    pb.setValue(i);
				    if(i == max-1)  //the progress bar is not finished  before the process
					i = max-2;
				    sleep(100);
				}
		    }
		}catch(Exception e){
		    JOptionPane.showMessageDialog(null,e.getMessage(),"Error message",JOptionPane.ERROR_MESSAGE);
		}
    }
}