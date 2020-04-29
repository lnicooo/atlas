package TrafficMbps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import AtlasPackage.*;

/**
 * This class shows the GUI allowing configure a router traffic or the standard traffic.
 * @author Aline Vieira de Mello
 * @version
 */
public class InterfaceConfig extends JFrame implements ActionListener{

	private Project project;
	private Scenery scenery;
	private Router router;
	private RouterTraffic traffic;
	private JLayeredPane panelNormal,panelUniform,panelPareto;
	private JTextField tfrequency,tpacketSize,tnumberPackets;
	private JTextField trate,tminrate,tmaxrate,tincrate,tavgrate,tdvrate,trateOn,tburst;
	private JComboBox cbtarget,cbdist,cbpriority;
	private JButton ok,graph;
	private String scheduling;
	private int dimXNet,dimYNet;
	private int flitWidth,flitClockCycles;

	/**
	* Create the GUI allowing configure a standard traffic.
	* @param project The NoC project.
	* @param scenery The scenery that will be configured.
	*/
	public InterfaceConfig(Project project, Scenery scenery){
		super("Standard Configuration");
		this.project = project;
		this.scenery = scenery;
		this.router = null;
		this.traffic = scenery.getStandardTraffic();
		initilize();
 	}
	
	/**
	* Create the GUI allowing configure a router traffic.
	* @param project The NoC project.
	* @param router The router that will be configured.
	*/
	public InterfaceConfig(Project project, Router router){
		super("Router "+router.getAddress());
		this.project = project;
		this.scenery = null;
		this.router = router;
		this.traffic = router.getTraffic();
		initilize();
 	}

	/**
	 * Initialize the variables class.
	 */
	private void initilize(){
		dimXNet = project.getNoC().getNumRotX();
		dimYNet = project.getNoC().getNumRotY();
		flitWidth = project.getNoC().getFlitSize();
		flitClockCycles = project.getNoC().getCyclesPerFlit();
		scheduling = project.getNoC().getScheduling();
		addProperties();
		addComponents();
		update();
		setVisible(true);
	}

	/**
	* add the interface properties.
	*/
	private void addProperties(){
		getContentPane().setLayout(null);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		//the interface is bigger when the scheduling is priority based. 
		if(scheduling.equalsIgnoreCase("Priority")){
			setSize(260,410);
			setLocation((resolucao.width-260)/2,(resolucao.height-410)/2);
		}
		else{
			setSize(260,380);
			setLocation((resolucao.width-260)/2,(resolucao.height-380)/2);
		}
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	/**
	* add components to interface.
	*/
	private void addComponents(){
		int x = 10;
		int y = 10;
		int stepY = 25;
		addFrequency(x,y);
		y = y + stepY;
		addTarget(x,y);
		//when the scheduling is priority based, the traffic priority should be informed.
		if(scheduling.equalsIgnoreCase("Priority")){
			y = y + stepY;
			addPriority(x,y);
		}
		y = y + stepY;
		addNumberPackets(x,y);
		y = y + stepY;
		addPacketSize(x,y);
		y = y + stepY;
		addDistTime(x,y);
		y = y + stepY;
		addPanelUniform(7,y,240,180);
		addPanelNormal(7,y,240,180);
		addPanelPareto(7,y,240,180);
		y = y + 185;
		addOk(x+80,y);
	}

	private void addFrequency(int x,int y){
		JLabel lfrequency = new JLabel("Frequency");
		lfrequency.setBounds(x,y,70,20);
		getContentPane().add(lfrequency);

		x += 115;
		tfrequency = new JTextField();
		tfrequency.setHorizontalAlignment(JTextField.RIGHT);
		tfrequency.setBounds(x,y,50,20);
		getContentPane().add(tfrequency);

		x += 55;
		JLabel lmhz = new JLabel("MHz");
		lmhz.setBounds(x,y,40,20);
		getContentPane().add(lmhz);
	}

	private void addTarget(int x,int y){
		JLabel ltarget = new JLabel("Target");
		ltarget.setBounds(x,y,50,20);
		getContentPane().add(ltarget);

		x += 115;
		cbtarget= new JComboBox();
		cbtarget.setBounds(x,y,120,20);
		getContentPane().add(cbtarget);
		cbtarget.addItem("random");
		if(dimYNet%2==0 && dimXNet%2==0)
			cbtarget.addItem("complement");
		//cbtarget.addItem("bitReversal");
		//cbtarget.addItem("butterfly");
		//cbtarget.addItem("matrixTranspose");
		//cbtarget.addItem("perfectShuffle");
		for(int j=0;j<dimYNet;j++){
			for(int i=0;i<dimXNet;i++){
				if(i<10 && j<10) cbtarget.addItem("0"+i+"0"+j);
				else if(i<10) cbtarget.addItem("0"+i+j);
				else if(j<10) cbtarget.addItem(""+i+"0"+j);
				else cbtarget.addItem(""+i+j);
			}
		}
		if(router!=null) { //router traffic configuration
			//remove this address router from target JCombobox
			// A router cannot send a traffic to itself
			cbtarget.removeItem(router.getAddress());
	
			// If the target selected in the standard configuration is this router
			// this router must not generate traffic 
			if(router.getTraffic().getTarget().equalsIgnoreCase(router.getAddress())){
				traffic.disable();
			}
			else{
				cbtarget.setSelectedItem(router.getTraffic().getTarget());
			}
		}		
	}

	private void addNumberPackets(int x,int y){
		JLabel lnumberPackets = new JLabel("Number of Packets");
		lnumberPackets.setBounds(x,y,110,20);
		getContentPane().add(lnumberPackets);

		x += 115;
		tnumberPackets= new JTextField();
		tnumberPackets.setHorizontalAlignment(JTextField.RIGHT);
		tnumberPackets.setBounds(x,y,50,20);
		getContentPane().add(tnumberPackets);
	}

	private void addPacketSize(int x,int y){
		JLabel lpacketSize = new JLabel("Packet Size");
		lpacketSize.setBounds(x,y,70,20);
		getContentPane().add(lpacketSize);

		x += 115;
		tpacketSize= new JTextField();
		tpacketSize.setHorizontalAlignment(JTextField.RIGHT);
		tpacketSize.setBounds(x,y,50,20);
		getContentPane().add(tpacketSize);

		x += 55;

		if( project.getNoC().getType().equalsIgnoreCase(NoC.MERCURY)){
			JLabel lflits = new JLabel("Phits");			
			tpacketSize.setEnabled(false);
			lflits.setBounds(x,y,40,20);
			getContentPane().add(lflits);
		}
		else{
			JLabel lflits = new JLabel("Flits");
			lflits.setBounds(x,y,40,20);
			lflits.setToolTipText("Each flit contains "+project.getNoC().getFlitSize()+" bits.");
			getContentPane().add(lflits);
		}

	}

	private void addPriority(int x,int y){
		JLabel lpriority = new JLabel("Priority");
		lpriority.setBounds(x,y,70,20);
		getContentPane().add(lpriority);

		x += 115;
		cbpriority = new JComboBox();
		cbpriority.setBounds(x,y,120,20);
		getContentPane().add(cbpriority);
		for(int i=0;i<project.getNoC().getVirtualChannel();i++)
			cbpriority.addItem(""+i);
	}
	
	private void addDistTime(int x,int y){
		JLabel ldist = new JLabel("Distribution");
		ldist.setBounds(x,y,100,20);
		getContentPane().add(ldist);

		x += 115;
		cbdist= new JComboBox();
		cbdist.setBounds(x,y,120,20);
		cbdist.addItem("uniform");
		cbdist.addItem("normal");
		cbdist.addItem("paretoOn/Off");
		getContentPane().add(cbdist);
		cbdist.addActionListener(this);
		cbdist.setEnabled(!project.getNoC().getType().equalsIgnoreCase("hermessr"));
	}

	/**
	* Add the Panel with the parameters of NORMAL traffic distribution.
	*/
	private void addPanelNormal(int x,int y,int dimx,int dimy){
		//Panel Border
		panelNormal=new JLayeredPane();
		panelNormal.setBounds(x,y,dimx,dimy);
		panelNormal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Normal Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		panelNormal.setVisible(false);
		getContentPane().add(panelNormal);

		int i = 10, j = 20;
		addAvgRate(panelNormal,i,j);
		j = j + 25;
		addMinRate(panelNormal,i,j);
		j = j + 25;
		addMaxRate(panelNormal,i,j);
		j = j + 25;
		addDvRate(panelNormal,i,j);
		j = j + 25;
		addIncRate(panelNormal,i,j);
		j = j + 25;
		addGraph(panelNormal,i+125,j);
	}

	/**
	* Add the Panel with the parameters of UNIFORME traffic distribution.
	*/
	private void addPanelUniform(int x,int y,int dimx,int dimy){
		//Panel Border
		panelUniform=new JLayeredPane();
		panelUniform.setBounds(x,y,dimx,dimy);
		panelUniform.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Uniform Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		getContentPane().add(panelUniform);

		addRate(panelUniform,10,20);
	}

	/**
	* Add the Panel with the parameters of PARETO traffic distribution.
	*/
	private void addPanelPareto(int x,int y,int dimx,int dimy){
		//Panel Border
		panelPareto=new JLayeredPane();
		panelPareto.setBounds(x,y,dimx,dimy);
		panelPareto.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Pareto ON/OFF Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		panelPareto.setVisible(false);
		getContentPane().add(panelPareto);

		int i = 10, j = 20;
		addRateOn(panelPareto,i,j);
		j = j + 25;
		addBurst(panelPareto,i,j);
	}

	private void addRate(JLayeredPane panel,int x,int y){
		JLabel lrate = new JLabel("Rate");
		lrate.setBounds(x,y,30,20);
		panel.add(lrate);

		x += 125;
		trate = new JTextField("100");
		trate.setHorizontalAlignment(JTextField.RIGHT);
		trate.setBounds(x,y,50,20);
		panel.add(trate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addMinRate(JLayeredPane panel,int x,int y){
		JLabel lminrate = new JLabel("Minimal Rate");
		lminrate.setBounds(x,y,100,20);
		panel.add(lminrate);

		x += 125;
		tminrate = new JTextField();
		tminrate.setHorizontalAlignment(JTextField.RIGHT);
		tminrate.setBounds(x,y,50,20);
		panel.add(tminrate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addMaxRate(JLayeredPane panel,int x,int y){
		JLabel lmaxrate = new JLabel("Maximal Rate");
		lmaxrate.setBounds(x,y,100,20);
		panel.add(lmaxrate);

		x += 125;
		tmaxrate = new JTextField();
		tmaxrate.setHorizontalAlignment(JTextField.RIGHT);
		tmaxrate.setBounds(x,y,50,20);
		panel.add(tmaxrate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addIncRate(JLayeredPane panel,int x,int y){
		JLabel lincrate = new JLabel("Increment");
		lincrate.setBounds(x,y,100,20);
		panel.add(lincrate);

		x += 125;
		tincrate = new JTextField();
		tincrate.setHorizontalAlignment(JTextField.RIGHT);
		tincrate.setBounds(x,y,50,20);
		panel.add(tincrate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addAvgRate(JLayeredPane panel,int x,int y){
		JLabel lavgrate = new JLabel("Average Rate");
		lavgrate.setBounds(x,y,100,20);
		panel.add(lavgrate);

		x += 125;
		tavgrate = new JTextField();
		tavgrate.setHorizontalAlignment(JTextField.RIGHT);
		tavgrate.setBounds(x,y,50,20);
		panel.add(tavgrate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addDvRate(JLayeredPane panel,int x,int y){
		JLabel ldvrate = new JLabel("Standard Deviation");
		ldvrate.setBounds(x,y,110,20);
		panel.add(ldvrate);

		x += 125;
		tdvrate = new JTextField();
		tdvrate.setHorizontalAlignment(JTextField.RIGHT);
		tdvrate.setBounds(x,y,50,20);
		panel.add(tdvrate);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addGraph(JLayeredPane panel,int x,int y){
		graph= new JButton("Graph");
		graph.setBounds(x,y,85,25);
		graph.addActionListener(this);
		panel.add(graph);
	}

	private void addRateOn(JLayeredPane panel,int x,int y){
		JLabel lrateOn = new JLabel("Rate of On Period");
		lrateOn.setBounds(x,y,100,20);
		panel.add(lrateOn);

		x += 125;
		trateOn = new JTextField();
		trateOn.setHorizontalAlignment(JTextField.RIGHT);
		trateOn.setBounds(x,y,50,20);
		panel.add(trateOn);

		x += 55;
		JLabel lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addBurst(JLayeredPane panel,int x,int y){
		JLabel lburst = new JLabel("Number of Bursts");
		lburst.setBounds(x,y,100,20);
		panel.add(lburst);

		x += 125;
		tburst= new JTextField();
		tburst.setHorizontalAlignment(JTextField.RIGHT);
		tburst.setBounds(x,y,50,20);
		panel.add(tburst);
	}

	private void addOk(int x,int y){
		ok= new JButton("Ok");
		ok.setBounds(x,y,60,25);
		ok.addActionListener(this);
		getContentPane().add(ok);
	}

	/**
	* show the GUI with the traffic parameters.
	*/
	private void update(){
		if(router!=null){
			setLocation(router.getInitialX()+15,router.getInitialY());
		}
		tfrequency.setText(""+traffic.getFrequency());
		cbtarget.setSelectedItem(traffic.getTarget());
		tnumberPackets.setText(""+traffic.getNumberOfPackets());
		tpacketSize.setText(""+(traffic.getPacketSize()));
		if(scheduling.equalsIgnoreCase("Priority"))
			cbpriority.setSelectedItem(""+traffic.getPriority());
		cbdist.setSelectedItem(traffic.getDistribution());
		trate.setText(""+traffic.getUniformRate());
		tminrate.setText(""+traffic.getNormalMinimalRate());
		tmaxrate.setText(""+traffic.getNormalMaximalRate());
		tavgrate.setText(""+traffic.getNormalAverageRate());
		tdvrate.setText(""+traffic.getNormalStandardDeviation());
		tincrate.setText(""+traffic.getNormalIncrement());
		trateOn.setText(""+traffic.getParetoRateOnPeriod());
		tburst.setText(""+traffic.getParetoBurstSize());
	}

	/**
	* Verify if the selected traffic is adapted to NoC dimension.
	*/
	private boolean verifyTarget(){
		if(getTarget().equalsIgnoreCase("bitReversal") || getTarget().equalsIgnoreCase("butterfly") || getTarget().equalsIgnoreCase("complement") || getTarget().equalsIgnoreCase("perfectShuffle")){
			return (dimXNet%2==0 && dimXNet == dimYNet);
		}
		return true;
	}

	private String getTarget(){return (String)cbtarget.getSelectedItem();}

	/**
	* Verify the parameters and configure the traffic.
	*/
	private void configTraffic(){
		if(!verifyTarget()){
			JOptionPane.showMessageDialog(this,"It is not possible to use this "+getTarget()+" standard destination generation\nbecause of the selected network dimension.","Error Message",JOptionPane.ERROR_MESSAGE);
			cbtarget.setSelectedItem("random");
		}
		else{
			String distTime = (String)cbdist.getSelectedItem();
			boolean ok;
			if(distTime.equalsIgnoreCase("uniform"))
				ok = configUniformTraffic();
			else if(distTime.equalsIgnoreCase("normal"))
				ok = configNormalTraffic();
			else
				ok = configParetoTraffic();
			if(ok)
				dispose();
		}
	}

	/**
	* Configure the parameters of Uniform traffic distribution
	* @return True if traffic has been configured
	*/
	private boolean configUniformTraffic(){
		try{
			double rate = Double.valueOf(trate.getText()).doubleValue();
			try{
				float frequency=Float.valueOf(tfrequency.getText()).floatValue();
				try{
					int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
					int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
					if(rate!=0 && frequency==0)
						JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);
					else if(rate!=0 && numberPackets==0)
						JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);
					else if(rate!=0 && packetSize<15)
						JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);
					else{
						int priority = 0;
						if(scheduling.equalsIgnoreCase("Priority"))
							priority = Integer.parseInt(""+cbpriority.getSelectedItem());
						traffic.setDistribution((String)cbdist.getSelectedItem());
						traffic.setFrequency(frequency);
						traffic.setTarget((String)cbtarget.getSelectedItem());
						traffic.setPriority(priority);
						traffic.setNumberOfPackets(numberPackets);
						traffic.setPacketSize(packetSize);
						traffic.setUniformRate(rate);
						if(router==null){
							scenery.setStandardConfigToRouters(project.isMapCores());
						}
						return true;
					}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	* Configure the parameters of Normal traffic distribution
	* @return True if traffic has been configured
	*/
	private boolean configNormalTraffic(){
		try{
			float frequency=Float.valueOf(tfrequency.getText()).floatValue();
			try{
				int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
				try{
					int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
					try{
						double avgRate = Double.valueOf(tavgrate.getText()).doubleValue();
						try{
							double minRate = Double.valueOf(tminrate.getText()).doubleValue();
							try{
								double maxRate = Double.valueOf(tmaxrate.getText()).doubleValue();
								try{
									double dvRate = Double.valueOf(tdvrate.getText()).doubleValue();
									try{
										double incRate = Double.valueOf(tincrate.getText()).doubleValue();
										if(frequency==0)
											JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);
										else if(numberPackets==0)
											JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);
										else if(packetSize<15)
											JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);
										else if(maxRate<minRate)
											JOptionPane.showMessageDialog(this,"Informed maximum rate is smaller than minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);
										else{
											int priority = 0;
											if(scheduling.equalsIgnoreCase("Priority"))
												priority = Integer.parseInt(""+cbpriority.getSelectedItem());
											
											traffic.setDistribution((String)cbdist.getSelectedItem());
											traffic.setFrequency(frequency);
											traffic.setTarget((String)cbtarget.getSelectedItem());
											traffic.setPriority(priority);
											traffic.setNumberOfPackets(numberPackets);
											traffic.setPacketSize(packetSize);
											traffic.setNormalAverageRate(avgRate);
											traffic.setNormalMinimalRate(minRate);
											traffic.setNormalMaximalRate(maxRate);
											traffic.setNormalStandardDeviation(dvRate);
											traffic.setNormalIncrement(incRate);
											if(router==null){
												scenery.setStandardConfigToRouters(project.isMapCores());
											}
											return true;
										}
									}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate increment.","Error Message",JOptionPane.ERROR_MESSAGE);}
								}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate standart deviation .","Error Message",JOptionPane.ERROR_MESSAGE);}
							}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed maximal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
						}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed average rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	* Configure the parameters of Pareto traffic distribution.
	* @return True if traffic has been configured.
	*/
	private boolean configParetoTraffic(){
		try{
			Double.valueOf(trate.getText()).doubleValue();
			try{
				float frequency=Float.valueOf(tfrequency.getText()).floatValue();
				try{
					int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
					try{
						int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
						try{
							double rateOn = Double.valueOf(trateOn.getText()).doubleValue();
							try{
								int burstSize = Integer.valueOf(tburst.getText()).intValue();
								if(rateOn!=0 && frequency==0)
									JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);
								else if(rateOn!=0 && numberPackets==0)
									JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);
								else if(rateOn!=0 && packetSize<15)
									JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);
								else{
									int priority = 0;
									if(scheduling.equalsIgnoreCase("Priority"))
										priority = Integer.parseInt(""+cbpriority.getSelectedItem());

									traffic.setDistribution((String)cbdist.getSelectedItem());
									traffic.setFrequency(frequency);
									traffic.setTarget((String)cbtarget.getSelectedItem());
									traffic.setPriority(priority);
									traffic.setNumberOfPackets(numberPackets);
									traffic.setPacketSize(packetSize);
									traffic.setParetoRateOnPeriod(rateOn);
									traffic.setParetoBurstSize(burstSize);

									if(router==null){
										scenery.setStandardConfigToRouters(project.isMapCores());
									}

									return true;
								}
							}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed burst size.","Error Message",JOptionPane.ERROR_MESSAGE);}
						}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate in the on period.","Error Message",JOptionPane.ERROR_MESSAGE);}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	 * Execute an action associated to the selected event
	 */
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==ok)
			configTraffic();
		else if(e.getSource()==cbdist){
			if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("uniform")){
				panelUniform.setVisible(true);
				panelNormal.setVisible(false);
				panelPareto.setVisible(false);
			}
			else if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("normal")){
				panelUniform.setVisible(false);
				panelNormal.setVisible(true);
				panelPareto.setVisible(false);
			}
			else if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("paretoOn/Off")){
				panelUniform.setVisible(false);
				panelNormal.setVisible(false);
				panelPareto.setVisible(true);
			}
		}
		else if(e.getSource()==graph){
			if(configNormalTraffic()){
				double avgRate = Double.valueOf(tavgrate.getText()).doubleValue();
				double minRate = Double.valueOf(tminrate.getText()).doubleValue();
				double maxRate = Double.valueOf(tmaxrate.getText()).doubleValue();
				if(avgRate==0)
					JOptionPane.showMessageDialog(this,"Error in the informed average rate.","Error Message",JOptionPane.ERROR_MESSAGE);
				else if(avgRate<minRate)
					JOptionPane.showMessageDialog(this,"Informed average rate is smaller than minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);
				else if(avgRate>maxRate)
					JOptionPane.showMessageDialog(this,"Informed average rate is greater than maximum rate.","Error Message",JOptionPane.ERROR_MESSAGE);
				else{
					if(router==null){
						TimeDistribution timeDistribution = new TimeDistribution(traffic, flitWidth,flitClockCycles);
						timeDistribution.normalGraph("normalStandard");
						Default.showGraph("normalStandard.txt");
					}
					else{ //router traffic configuration
						TimeDistribution timeDistribution = new TimeDistribution(traffic,flitWidth,flitClockCycles);
						timeDistribution.normalGraph("normal" + router.getAddress());
						Default.showGraph("normal" + router.getAddress() + ".txt");
					}
				}
			}
		}
	}
}