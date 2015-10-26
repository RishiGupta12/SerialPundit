package serialterminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.JPanel;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import com.embeddedunveiled.serial.SerialComManager;

public final class SerialTerminalMain extends JFrame {

	private static final long serialVersionUID = 6980955779913654766L;

	private static SerialTerminalMain serialterm;
	private SerialComManager scm;

	private JFrame frame;
	private JMenuBar menuBar;
	private MenuBar menuBarObj;
	private JPanel centerPanel;
	// receive
	private JTextField datarcvtextfield;
	// send data stuff
	private JPanel sendDataPanel;
	private JTextField sendDataTextField;
	private JButton sendDataButton;
	private JTabbedPane tabbedPane;
	// serial port tab
	private JPanel comTopPanel;
	private JPanel comportPanel;
	@SuppressWarnings("rawtypes")
	private JComboBox comportComboBox;
	private JButton comportRefreshButton;
	private JPanel parityPanel;
	private JRadioButton parnoneRbutton;
	private JRadioButton paroddRbutton;
	private JRadioButton parevenRbutton;
	private JRadioButton parmarkRbutton;
	private JRadioButton parspaceRbutton;
	private ButtonGroup dbitsButtonGroup;
	private JPanel databitsPanel;
	private JRadioButton dbits8Rbutton;
	private JRadioButton dbits7Rbutton;
	private JRadioButton dbits6Rbutton;
	private JRadioButton dbits5Rbutton;
	private ButtonGroup parityButtonGroup;
	private JRadioButton sbits1Rbutton;
	private JRadioButton sbits2Rbutton;
	private ButtonGroup stopbitsButtonGroup;
	private JPanel flowctrlPanel;
	private JRadioButton fctrlnoneRbutton;
	private JRadioButton fctrlswRbutton;
	private JRadioButton fctrlhwRbutton;
	private ButtonGroup fctrlButtonGroup;
	private JPanel flowctrlCharPanel;
	private JCheckBox swflctrlrcv;
	private JCheckBox swflctrlsnt;
	private JTextField xonCharText;
	private JTextField xoffCharText;
	private JButton openButton;
	private JButton closeButton;
	private JPanel modemLinesStatusPanel;
	private JLabel dsrStatusLabel;
	private JLabel dcdStatusLabel;
	private JLabel ctsStatusLabel;
	private JLabel riStatusLabel;
	private ImageIcon iconRed = new ImageIcon("images/red.png");
	private ImageIcon iconGreen = new ImageIcon("images/green.png");
	private ImageIcon iconSmiley = new ImageIcon("images/smiley.png");
	// program status
	private JPanel programStatusPanel;
	private JLabel programStatusLabel;
	private JTextField programStatusText;

	public SerialTerminalMain() { }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createAndShowGUI() {

		JFrame.setDefaultLookAndFeelDecorated(true);
		try{
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}catch (Exception e) {
			System.out.println("UIManager.setLookAndFeel : " + e.getMessage());
		}
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(670, 485);
		frame.setResizable(false);
		frame.setTitle("Serial terminal emulator");
		frame.getContentPane().setLayout(new BorderLayout(0,0));

		/* ~~~ MENU BAR ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		menuBarObj = new MenuBar();
		menuBar = menuBarObj.setUpMenuBar();
		frame.setJMenuBar(menuBar);

		/* ~~~ TOP MOST PANEL TO PUT THINGS IN CENTER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		frame.getContentPane().add(new JLabel("   "), BorderLayout.NORTH);
		frame.getContentPane().add(new JLabel("   "), BorderLayout.WEST);
		centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
		frame.getContentPane().add(new JLabel("   "), BorderLayout.EAST);
		frame.getContentPane().add(new JLabel("   "), BorderLayout.SOUTH);

		/* ~~~ DATA RECEIVE BLOCK ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		datarcvtextfield = new JTextField("DffsF");
		datarcvtextfield.setPreferredSize(new Dimension(625, 40));
		datarcvtextfield.setBackground(Color.WHITE);
		datarcvtextfield.setForeground(Color.WHITE);
		datarcvtextfield.setEditable(false);
		centerPanel.add(datarcvtextfield);

		/* ~~~ SEND DATA BLOCK ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		sendDataPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 15));
		sendDataTextField = new JTextField("-------");
		sendDataTextField.setBackground(Color.WHITE);
		sendDataTextField.setForeground(Color.BLACK);
		sendDataTextField.setEditable(true);
		sendDataTextField.setPreferredSize(new Dimension(520, 40));
		sendDataPanel.add(sendDataTextField);
		sendDataPanel.add(new JLabel("  "));
		sendDataButton = new JButton("Send data");
		sendDataPanel.add(sendDataButton);
		centerPanel.add(sendDataPanel);

		/* ~~~~ TABBED CONTENT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		tabbedPane = new JTabbedPane();

		// serial port tab		
		comTopPanel = new JPanel(new GridLayout(6, 1, 5, 0));

		comportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		comportPanel.add(new JLabel("Com port : "));
		comportComboBox = new JComboBox(new String[] {"   ----select----   "});
		comportComboBox.setPreferredSize(new Dimension(150, 25));
		comportPanel.add(comportComboBox);
		comportRefreshButton = new JButton("Refresh list");
		comportPanel.add(comportRefreshButton);
		comportPanel.add(new JLabel("       Baudrate : "));
		comportComboBox = new JComboBox(new String[] {"4800", "9600", "115200"});
		comportComboBox.setPreferredSize(new Dimension(150, 25));
		comportPanel.add(comportComboBox);
		comTopPanel.add(comportPanel);

		parityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		parityPanel.add(new JLabel("Parity : "));
		parnoneRbutton = new JRadioButton("None");
		parnoneRbutton.setSelected(true);
		paroddRbutton = new JRadioButton("Odd");
		parevenRbutton = new JRadioButton("Even");
		parmarkRbutton = new JRadioButton("Mark");
		parspaceRbutton = new JRadioButton("Space");
		parityButtonGroup = new ButtonGroup();
		parityButtonGroup.add(parnoneRbutton);
		parityButtonGroup.add(paroddRbutton);
		parityButtonGroup.add(parevenRbutton);
		parityButtonGroup.add(parmarkRbutton);
		parityButtonGroup.add(parspaceRbutton);
		parityPanel.add(parnoneRbutton);
		parityPanel.add(paroddRbutton);
		parityPanel.add(parevenRbutton);
		parityPanel.add(parmarkRbutton);
		parityPanel.add(parspaceRbutton);
		comTopPanel.add(parityPanel);

		databitsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		databitsPanel.add(new JLabel("Data bits : "));
		dbits8Rbutton = new JRadioButton("8");
		dbits8Rbutton.setSelected(true);
		dbits7Rbutton = new JRadioButton("7");
		dbits6Rbutton = new JRadioButton("6");
		dbits5Rbutton = new JRadioButton("5");
		dbitsButtonGroup = new ButtonGroup();
		dbitsButtonGroup.add(dbits8Rbutton);
		dbitsButtonGroup.add(dbits7Rbutton);
		dbitsButtonGroup.add(dbits6Rbutton);
		dbitsButtonGroup.add(dbits5Rbutton);
		databitsPanel.add(dbits8Rbutton);
		databitsPanel.add(dbits7Rbutton);
		databitsPanel.add(dbits6Rbutton);
		databitsPanel.add(dbits5Rbutton);
		databitsPanel.add(new JLabel("        Stop bits : "));
		sbits1Rbutton = new JRadioButton("1");
		sbits1Rbutton.setSelected(true);
		sbits2Rbutton = new JRadioButton("2");
		stopbitsButtonGroup = new ButtonGroup();
		stopbitsButtonGroup.add(dbits8Rbutton);
		stopbitsButtonGroup.add(dbits7Rbutton);
		databitsPanel.add(sbits1Rbutton);
		databitsPanel.add(sbits2Rbutton);
		comTopPanel.add(databitsPanel);

		flowctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		flowctrlPanel.add(new JLabel("Flow control : "));
		fctrlnoneRbutton = new JRadioButton("None");
		fctrlnoneRbutton.setSelected(true);
		fctrlswRbutton = new JRadioButton("Software");
		fctrlhwRbutton = new JRadioButton("Hardware");
		fctrlButtonGroup = new ButtonGroup();
		fctrlButtonGroup.add(fctrlnoneRbutton);
		fctrlButtonGroup.add(fctrlswRbutton);
		fctrlButtonGroup.add(fctrlhwRbutton);
		flowctrlPanel.add(fctrlnoneRbutton);
		flowctrlPanel.add(fctrlswRbutton);
		flowctrlPanel.add(fctrlhwRbutton);
		comTopPanel.add(flowctrlPanel);

		flowctrlCharPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
		flowctrlCharPanel.add(new JLabel("Software flow control : "));
		swflctrlrcv = new JCheckBox("Receive");
		flowctrlCharPanel.add(swflctrlrcv);
		flowctrlCharPanel.add(new JLabel("Xon char : "));
		xonCharText = new JTextField();
		xonCharText.setPreferredSize(new Dimension(25, 15));
		xonCharText.setBackground(Color.WHITE);
		xonCharText.setForeground(Color.BLACK);
		xonCharText.setEditable(true);
		flowctrlCharPanel.add(xonCharText);
		flowctrlCharPanel.add(new JLabel("         "));
		swflctrlsnt = new JCheckBox("Transmit");
		flowctrlCharPanel.add(swflctrlsnt);
		flowctrlCharPanel.add(new JLabel("Xoff char : "));
		xoffCharText = new JTextField();
		xoffCharText.setPreferredSize(new Dimension(25, 15));
		xoffCharText.setBackground(Color.WHITE);
		xoffCharText.setForeground(Color.BLACK);
		xoffCharText.setEditable(true);
		flowctrlCharPanel.add(xoffCharText);
		comTopPanel.add(flowctrlCharPanel);

		modemLinesStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		modemLinesStatusPanel.add(new JLabel("Modem lines status : "));
		dsrStatusLabel = new JLabel("DSR", iconRed, JLabel.LEFT);
		modemLinesStatusPanel.add(dsrStatusLabel);
		dcdStatusLabel = new JLabel("DCD", iconRed, JLabel.LEFT);
		modemLinesStatusPanel.add(dcdStatusLabel);
		ctsStatusLabel = new JLabel("CTS", iconRed, JLabel.LEFT);
		modemLinesStatusPanel.add(ctsStatusLabel);
		riStatusLabel = new JLabel("RI", iconRed, JLabel.LEFT);
		modemLinesStatusPanel.add(riStatusLabel);
		openButton = new JButton("Open");
		closeButton = new JButton("Close");
		modemLinesStatusPanel.add(new JLabel("                     "));
		modemLinesStatusPanel.add(openButton);
		modemLinesStatusPanel.add(closeButton);
		comTopPanel.add(modemLinesStatusPanel);

		tabbedPane.addTab("serial port", null, comTopPanel, null);
		tabbedPane.addTab("x/y/z modem", null, null, null);
		centerPanel.add(tabbedPane);

		/* ~~~ PROGRAM STATUS ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
		programStatusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
		programStatusLabel = new JLabel("Program status : ", iconSmiley, JLabel.LEFT);
		programStatusPanel.add(programStatusLabel);
		programStatusText = new JTextField("Initializing......");
		programStatusText.setBackground(new Color(0, 0, 0, 0));
		programStatusText.setPreferredSize(new Dimension(495, 25));
		programStatusText.setEditable(false);
		programStatusText.setBorder(BorderFactory.createEmptyBorder(0,0,1,0));
		programStatusPanel.add(programStatusText);
		centerPanel.add(programStatusPanel);

		// finally show UI On screen
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		serialterm = new SerialTerminalMain();
		//Schedule a job for the event-dispatching thread:
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				serialterm.createAndShowGUI(); 
			}
		});
	}
}
