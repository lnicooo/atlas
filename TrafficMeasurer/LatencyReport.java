package TrafficMeasurer;

import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import AtlasPackage.*;

/**
 * This class creates the Latency Report GUI allowing visualize the list of packets ordered by latency.
 * @author Aline Vieira de Mello
 * @version
 */
public class LatencyReport extends JFrame implements ActionListener, MouseListener{
	private Project project;
	private JPanel_Noc canvas_noc;
    private JButton bOk;
    private JTextField tPackets;
    private JRadioButton cbMaior;
    private JRadioButton cbMenor;
    private int numRot,numRotX,numRotY,numCV;
    private String path;
    private JTable tabela;
    private int flitSize;
	private Object[][] dados;

	/**
	 * Constructor class.
	 * @param project The selected project.
	 */
    public LatencyReport(Project project){
        super("Global Latency Analysis Report");
        getContentPane().setLayout(null);
		setSize(270,160);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-270)/2,(resolucao.height-160)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
        this.project = project;
        NoC noc = project.getNoC();
        numRot = noc.getNumRotX()*noc.getNumRotY();
        numRotX = noc.getNumRotX();
        numRotY = noc.getNumRotY();
        numCV = noc.getVirtualChannel();
        path = project.getSceneryPath()+File.separator + "Out";
        flitSize=noc.getFlitSize();

        addComponents();
    }

	/**
	 * Add components to GUI.
	 */
	private void addComponents(){
        JLabel lPackets = new JLabel("Number of Packets");
        lPackets.setBounds(10,10,120,20);
        getContentPane().add(lPackets);

        tPackets = new JTextField(8);
        tPackets.setBounds(130,10,100,20);
        getContentPane().add(tPackets);

        cbMaior = new JRadioButton("Higher Latency",true);
        cbMaior.setBounds(10,50,120,20);
        getContentPane().add(cbMaior);

        cbMenor = new JRadioButton("Lower Latency",false);
        cbMenor.setBounds(130,50,120,20);
        getContentPane().add(cbMenor);

		ButtonGroup grupo=new ButtonGroup();
		grupo.add(cbMaior);
		grupo.add(cbMenor);

        bOk = new JButton("Ok");
        bOk.setBounds(90,90,80,20);
        bOk.addActionListener(this);
        getContentPane().add(bOk);

        setVisible(true);
	}

	/**
	 * Creates a list of <i>n</i> packets ordered by <i>m</i>, 
	 * where <i>m</i> is <i>minimal</i> or <i>maximal</i> latency and show it in a GUI. 
	 * @param n The number of packets.
	 * @param m The order: minimal or maximal
	 */
    private void showTable(int n, String m){
        JFrame frame = new JFrame("Global Latency Analysis Report");
        frame.getContentPane().setLayout(null);
		frame.setSize(610,613);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((resolucao.width-610)/2,(resolucao.height-613)/2);
		frame.setResizable(false);

		Vector<Packet> vetor = getOrderedLatency();
		if(vetor.size()<n){
			JOptionPane.showMessageDialog(null,"The Number of Transmitted Packets is "+vetor.size()+".","Information", JOptionPane.INFORMATION_MESSAGE);
			n = vetor.size();
		}

		Object[] nome = {"","Latency","Sequency Number","Source","Target"};
		int [] dimension = {5,40,80,40,40};
		dados = new Object[n][5];
		if(m.equals("menor")){
			for(int i=0;i<n;i++){
        	    dados[i][0] = ""+(i+1);
        	    dados[i][1] = ""+((Packet)vetor.get(i)).getLatency();
        	    dados[i][2] = ""+((Packet)vetor.get(i)).getSequenceNumber();
        	    int source = Convert.getNumberOfRouter(((Packet)vetor.get(i)).getSource(),numRotX,flitSize);
        	    dados[i][3] = Convert.getXYAddress(source,numRotX,16);
        	    int target = Convert.getNumberOfRouter(((Packet)vetor.get(i)).getTarget(),numRotX,flitSize);
        	    dados[i][4] = Convert.getXYAddress(target,numRotX,16);
        	}
		}
		else{
			for(int i=0,j=vetor.size()-1;i<n;i++,j--){
        	    dados[i][0] = ""+(i+1);
        	    dados[i][1] = ""+((Packet)vetor.get(j)).getLatency();
        	    dados[i][2] = ""+((Packet)vetor.get(j)).getSequenceNumber();
        	    int source = Convert.getNumberOfRouter(((Packet)vetor.get(j)).getSource(),numRotX,flitSize);
        	    dados[i][3] = Convert.getXYAddress(source,numRotX,16);
        	    int target = Convert.getNumberOfRouter(((Packet)vetor.get(j)).getTarget(),numRotX,flitSize);
        	    dados[i][4] = Convert.getXYAddress(target,numRotX,16);
        	}
		}

		DefaultTableModel displayModel = new DefaultTableModel(dados,nome) {
			public boolean isCellEditable(int row, int column) {
			    return false;
			}
		    };

		tabela = new JTable(displayModel);

		//Dimension table columns
		for(int i =0;i<5;i++){
			TableColumn column = tabela.getColumnModel().getColumn(i);
			column.setPreferredWidth(dimension[i]);
		}
		Scenery scenery = new Scenery(project.getNoC().getNumRotX(),project.getNoC().getNumRotY());
		scenery.open(project.getSceneryFile());
		if(scenery.isInternalSimulation())
			tabela.addMouseListener(this);

		JPanel p = new JPanel(new BorderLayout());
		p.setBounds(0,0,600,180);
		p.add(new JScrollPane(tabela),BorderLayout.CENTER);
		frame.getContentPane().add(p);

		canvas_noc = new JPanel_Noc(0,180,600,400,project.getNoC().getType());
		canvas_noc.setNoCDimension(numRotX,numRotY);
		frame.getContentPane().add(canvas_noc);

		frame.setVisible(true);
    }

    /**
     * Generate a vector of packets ordered by latency.
     * @return The vector of packets ordered by latency.
     */
	private Vector<Packet> getOrderedLatency(){
		Packet packet;
		Vector<Packet> vetor = new Vector<Packet>();
		StringTokenizer st;
		String line;
		String target="",source="",sequencyNumber="";
		double latency=0;
		int seqNumber=0,i=0;
		boolean insert=false;

		FileInputStream fis;
		BufferedReader br;

		for(int rot=0;rot<numRot;rot++){
			File f = new File(path.concat(File.separator + "out"+rot+".txt"));

			try{
				for(int cont=0;cont<numCV;cont++){
					if(numCV > 1)
						f = new File(path.concat(File.separator + "out"+rot+"L"+cont+".txt"));
					fis=new FileInputStream(f);
					br=new BufferedReader(new InputStreamReader(fis));

					line=br.readLine();
					while(line!=null){
						st = new StringTokenizer(line, " ");
						int nTokens = st.countTokens();

						if(nTokens!=0){
							//advance until two tokens before end of line
							for(int count=0;count<(nTokens-2);count++){
								if(count==0){
									target = st.nextToken();
									target = target.substring(target.length()/2);
								}
								else if(count==2){
									source = st.nextToken();
									source = source.substring(source.length()/2);
								}
								else if(count==7){
									sequencyNumber = st.nextToken();
								}
								else if(count==8){
									sequencyNumber = sequencyNumber + st.nextToken();
									seqNumber = Integer.parseInt(sequencyNumber,16);
								}
								else
									st.nextToken();
							}

							//get the latency
							latency = Integer.valueOf(st.nextToken()).intValue();

							packet = new Packet(source,target,seqNumber,latency);
							i=0;
							insert=false;
							while(i<vetor.size() && !insert){
								if(((Packet)vetor.get(i)).getLatency()>latency){
									vetor.insertElementAt(packet,i);
									insert=true;
								}
								i++;
							}
							if(!insert)
								vetor.add(packet);
						}
						line=br.readLine();
					}
					br.close();
					fis.close();
				}
			}catch(FileNotFoundException exc){
				JOptionPane.showMessageDialog(null,"Can't open "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"Error in "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		return vetor;
	}

	/**
	 * Return a vector containing all routers in the path of the packet.
	 * @param sequencyNumber The sequence number of the packet.
	 * @param source The source router of the packet. 
	 * @param target The target router of the packet.
	 * @return The vector containing all routers in the path of the packet.
	 */
	private Vector<Router> getPath(int sequenceNumber, String source, String target){
		FileInputStream fis;
		BufferedReader br;
		StringTokenizer st;
		String sequency="",line="";
		boolean outport,packet;
		Vector<Router> v = new Vector<Router>();
		int s = Convert.getNumberOfRouter(source,numRotX,flitSize);
		int t = Convert.getNumberOfRouter(target,numRotX,flitSize);
		int seqNumber=0,port, vc, rot = s;

		while(rot!=t){
			outport = false;
			port=0;
			while(!outport && port<5){
				File f = new File(path.concat(File.separator + "r"+rot+"p"+port+".txt"));
				try{
					vc = 0;
					packet = false;
					while (vc<numCV && !packet){
						if(numCV > 1)
							f = new File(path.concat(File.separator + "r"+rot+"p"+port+"l"+vc+".txt"));
						fis=new FileInputStream(f);
						br=new BufferedReader(new InputStreamReader(fis));

						line=br.readLine();
						while(line!=null && !packet){
							st = new StringTokenizer(line, " ()");
							int nTokens = st.countTokens();

							if(nTokens!=0){
								//read the packet flits
								for(int count=0;count<17;count++){
									if(count==14){
										sequency = st.nextToken();
									}
									else if(count==16){
										sequency = sequency + st.nextToken();
										seqNumber = Integer.parseInt(sequency,16);
									}
									else
										st.nextToken();
								}
								if(sequenceNumber == seqNumber){
									outport=true;
									packet=true;
									int x = Convert.getAddressX(rot, numRotX);
									int y = Convert.getAddressY(rot, numRotX);
									v.add(new Router(x, y, port));
									if(port==Router.EAST){
										if((rot%numRotX)==(numRotX-1)) //network limit
											rot = rot - (numRotX-1);
										else
											rot = rot + 1;
									}
									else if(port==Router.WEST){
										if(rot%numRotX==0) //network limit
											rot = rot + (numRotX-1);
										else
											rot = rot - 1;
									}
									else if(port==Router.NORTH){
										if((rot+numRotX)>(numRot-1)) //network limit
											rot = rot % numRotX;
										else
											rot=rot+numRotX;
									}
									else{ //south
										if((rot-numRotX)<0) //network limit
											rot = rot + (numRotX * (numRotY-1));
										else
											rot = rot - numRotX;
									}
								}
							}
							line=br.readLine();
						}
						br.close();
						fis.close();
						vc++;
					}
					port++;
				}catch(FileNotFoundException exc){
					//router do not have all ports (east, west, north and south)
					port++;
				}catch(Exception e){
					JOptionPane.showMessageDialog(null,"Error in "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		}
		return v;
	}

	/**
	 * Show the path of the packet selected in the table.
	 */
	private void showPath(){
		try{
			String sequency = (String)dados[tabela.getSelectedRow()][2];
			String source = (String)dados[tabela.getSelectedRow()][3];
			String target = (String)dados[tabela.getSelectedRow()][4];
			int sequencyNumber = Integer.parseInt(sequency);
			//JOptionPane.showMessageDialog(null,"Source "+source+" Target "+target,"Information", JOptionPane.INFORMATION_MESSAGE);
			Vector<Router> path = getPath(sequencyNumber, source, target);
			canvas_noc.markPath(path,true);
			canvas_noc.repaint();
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}

    public void actionPerformed(ActionEvent e){
    	if(e.getSource() == bOk){
    	    if(tPackets.getText().equals(""))
				JOptionPane.showMessageDialog(null,"Inform the number of packets.","Error",JOptionPane.ERROR_MESSAGE);
			else{
    	    	try{
					int nPackets = Integer.parseInt(tPackets.getText());
					if(nPackets<=0)
						JOptionPane.showMessageDialog(null,"The minimum number of packets is 1.","Error",JOptionPane.ERROR_MESSAGE);
					else{
						if(cbMaior.isSelected())
							showTable(Integer.parseInt(tPackets.getText()),"maior");
						else
							showTable(Integer.parseInt(tPackets.getText()),"menor");
						this.dispose();
					}
				}catch(NumberFormatException exc){
					JOptionPane.showMessageDialog(null,"The number of packets informed is incorrect.","Error",JOptionPane.ERROR_MESSAGE);
				}catch(Exception exc){
					JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
				}
            }
		}
    }

    public void mouseClicked(MouseEvent e){
		if(e.getButton() == MouseEvent.BUTTON1){
			showPath();
		}
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    /**
     * This class contains the packet information.
     * @author Aline Vieira de Mello
     * @version
     *
     */
	public class Packet{
		private String source;
		private String target;
		private int sequenceNumber;
		private double latency;

		/**
		 * Constructor of class.
		 * @param source The source router of packet.
		 * @param target The target router of packet.
		 * @param sequenceNumber The sequence number of packet.
		 * @param latency The packet latency.
		 */
		public Packet(String source,String target,int sequenceNumber, double latency){
			this.source = source;
			this.target = target;
			this.sequenceNumber = sequenceNumber;
			this.latency = latency;
		}

		/**
		 * Return the source router of packet.
		 * @return The source router.
		 */
		public String getSource(){return source;}

		/**
		 * Return the target router of packet.
		 * @return The target router.
		 */
		public String getTarget(){return target;}
		
		/**
		 * Return the sequence number of packet.
		 * @return The sequence number.
		 */
		public int getSequenceNumber(){return sequenceNumber;}

		/**
		 * Return the packet latency.
		 * @return The latency.
		 */
		public double getLatency(){return latency;}
	}
}