package edu.ou.engr.engr2002.ideagroupselection;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class GUI extends JFrame {

	public static void main(String[] args) {
		new GUI().setVisible(true);
	}

	private static final int SPACE = 5;
	private static final int SPACE_L = 10;
	
	private CustomJPanel mainPnl = 
			new CustomJPanel(CustomJPanel.VERTICAL, SPACE);
	
	private JPanel ideasPnl = new JPanel(new BorderLayout(SPACE, SPACE));
	private JTextField ideasTF = new JTextField();
	private JButton ideasBtn = new JButton("...");
	private JCheckBox ideasVotesCB = new JCheckBox(
			"This file also includes voting data");
	
	private JPanel votesPnl = new JPanel(new BorderLayout(SPACE, SPACE));
	private JTextField votesDirTF = new JTextField();
	private JButton votesDirBtn = new JButton("...");
	
	private JPanel nameOrderPnl = new JPanel(
			new FlowLayout(FlowLayout.LEFT, SPACE, SPACE));
	private ButtonGroup nameOrderBtns = new ButtonGroup();
	private JRadioButton nameOrderFirstLast = new JRadioButton("First Last");
	private JRadioButton nameOrderLastFirst = new JRadioButton("Last, First");
	
	private JCheckBox votesGroupsCB = new JCheckBox(
			"Determine ideas used based on votes");
	private JPanel ideasOverridePnl = new JPanel(new BorderLayout(SPACE, SPACE));
	private JTextField ideasOverrideTF = new JTextField();
	
	private JPanel numbersPnl = new JPanel(
			new FlowLayout(FlowLayout.LEFT, SPACE, SPACE));
	private JSpinner groupsSpinner = new JSpinner(
			new SpinnerNumberModel(9, 0, 50, 1));
	private JSpinner sizeSpinner = new JSpinner(
			new SpinnerNumberModel(5, 0, 20, 1));
	
	private JButton goBtn = new JButton("Create groups");
	
	public GUI() {
		add(mainPnl);
		mainPnl.setBorder(new EmptyBorder(SPACE_L, SPACE_L, SPACE_L, SPACE_L));
		
		//////// ideas panel //////////////////////////////////////////////////
		ideasPnl.add(new JLabel(
				"<html>Choose the CSV file containing the idea numbers, "
				+ "idea names, and students' names."
				+ "<br>The file must also include the names of students "
				+ "who did not vote (leave their proposed idea blank)."
				+ "<br>The first three columns must be idea #, idea name, "
				+ "student name."),
				BorderLayout.NORTH);
		ideasPnl.add(ideasTF, BorderLayout.CENTER);
		ideasPnl.add(ideasBtn, BorderLayout.EAST);
		ideasBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = ideasTF.getText();
				JFileChooser chooser = new JFileChooser(text == null ? "" : text);
				chooser.addChoosableFileFilter(new FileNameExtensionFilter(
						"CSV files (*.csv)", "csv"));
				int option = chooser.showOpenDialog(GUI.this);
				if (option == JFileChooser.APPROVE_OPTION) 
					ideasTF.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		ideasPnl.add(ideasVotesCB, BorderLayout.SOUTH);
		ideasVotesCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				boolean enabled = e.getStateChange() == ItemEvent.DESELECTED;
				votesDirTF.setEnabled(enabled);
				votesDirBtn.setEnabled(enabled);
			}
		});
		mainPnl.add(ideasPnl);
		mainPnl.add(new JSeparator());
		
		//////// votes panel //////////////////////////////////////////////////
		votesPnl.add(new JLabel(
				"<html>Choose the directory containing the voting files "
				+ "downloaded from D2L. "
				+ "<br>The voting files must contain the students' names."),
				BorderLayout.NORTH);
		votesPnl.add(votesDirTF, BorderLayout.CENTER);
		votesPnl.add(votesDirBtn, BorderLayout.EAST);
		votesDirBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = votesDirTF.getText();
				JFileChooser chooser = new JFileChooser(text == null ? "" : text);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int option = chooser.showOpenDialog(GUI.this);
				if (option == JFileChooser.APPROVE_OPTION) 
					votesDirTF.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});
		mainPnl.add(votesPnl);
		
		///////// name order panel ////////////////////////////////////////////
		nameOrderPnl.add(new JLabel("Name order to use in output:"));
		nameOrderPnl.add(nameOrderFirstLast);
		nameOrderPnl.add(nameOrderLastFirst);
		nameOrderBtns.add(nameOrderFirstLast);
		nameOrderBtns.add(nameOrderLastFirst);
		mainPnl.add(nameOrderPnl);
		
		mainPnl.add(new JSeparator());
		
		//////// ideas override panel /////////////////////////////////////////
		ideasOverridePnl.add(votesGroupsCB, BorderLayout.NORTH);
		votesGroupsCB.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				ideasOverrideTF.setEnabled(
						e.getStateChange() == ItemEvent.DESELECTED);
			}
		});
		ideasOverridePnl.add(new JLabel("Idea numbers to use:"),
				BorderLayout.WEST);
		ideasOverridePnl.add(ideasOverrideTF, BorderLayout.CENTER);
		mainPnl.add(ideasOverridePnl);
		mainPnl.add(new JSeparator());
		
		//////// numbers panel ////////////////////////////////////////////////
		numbersPnl.add(new JLabel("Number of groups:"));
		numbersPnl.add(groupsSpinner);
		numbersPnl.add(new JLabel("Max group size:"));
		numbersPnl.add(sizeSpinner);
		mainPnl.add(numbersPnl);
		mainPnl.add(new JSeparator());
		
		//////// go button ////////////////////////////////////////////////////
		mainPnl.add(goBtn);
		mainPnl.setComponentAnchor(goBtn, GridBagConstraints.CENTER);
		mainPnl.setComponentFill(goBtn, GridBagConstraints.NONE);
		goBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String votesDir = ideasVotesCB.isSelected() ?
						null : votesDirTF.getText();
				String ideasStr = votesGroupsCB.isSelected() ?
						null : ideasOverrideTF.getText();
				Main.makeGroups(GUI.this, ideasTF.getText(), votesDir, 
						ideasStr, nameOrderLastFirst.isSelected(),
						(Integer)groupsSpinner.getValue(),
						(Integer)sizeSpinner.getValue());
			}
		});
		
		//////// other stuff //////////////////////////////////////////////////
		getRootPane().setDefaultButton(goBtn);
		pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE); 
		setResizable(false);
		setLocationRelativeTo(null); // start in middle of screen
		setLocation(getLocation().x, getLocation().y - 150); // now move up 150
		setVisible(true);
	}
}