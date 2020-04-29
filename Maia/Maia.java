package Maia;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import AtlasPackage.*;

/**
 * Maia is the NoC Generation tool able to generate a Hermes Mesh2D NoC.
 */
public class Maia{

	/**
	 * Show Maia tool according to the informed parameters. 
	 * @param s
	 */
	public Maia(String s[]){
		if(s!=null && s.length==2){
			//s[0] = absolute path of the project. For example, c:\HardNoCs\NoC3x3
			String nameProject = s[1];
			File f = new File(s[0]+File.separator+nameProject+".noc");
			if(f.exists()){
				Project project = new Project(f);
				MaiaInterface mi = new MaiaInterface(project);
				mi.addWindowListener(new WindowAdapter(){
					public void windowClosed(WindowEvent e){
						System.exit(0);
					}
				});
			}
			else{
			    JOptionPane.showMessageDialog(null,"The project informed does not exit.","Error",JOptionPane.ERROR_MESSAGE);
			}
		}
		else
			JOptionPane.showMessageDialog(null,"Inform the project's name.","Error", JOptionPane.ERROR_MESSAGE);
 	}

	/**
	 * Launch the Maia (NoC Generation) tool.
	 * @param s
	 */
	public static void main(String s[]){
		JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
		new Maia(s);
	}
}
