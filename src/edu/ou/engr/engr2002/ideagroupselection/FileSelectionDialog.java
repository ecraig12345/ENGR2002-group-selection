package edu.ou.engr.engr2002.ideagroupselection;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * Dialog for selecting a file, with text field (to type/paste a path) and a
 * browse button (to choose a path with a file chooser).
 */
public class FileSelectionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private FileSelectionPanel fsp;
	// will be null until the dialog closes
	private String selectedFile = null;
	
	/**
	 * @param owner           parent window (can be null)
	 * @param title           dialog title
	 * @param message         instruction message
	 * @param defaultPath     default path to show in box
	 * @param isSaveDialog    true if this should be a save (not open) dialog
	 * @param directoriesOnly true to only allow selecting directories
	 * @param fileFilter      Optional file filter (can be null)
	 */
	private FileSelectionDialog(Frame owner, String title, String message, 
			String defaultPath, boolean isSaveDialog, boolean directoriesOnly, 
			FileFilter fileFilter) {
		super(owner, title, true);
		
		fsp = new FileSelectionPanel(message, defaultPath, isSaveDialog,
				directoriesOnly, fileFilter);
		fsp.setBorder(new EmptyBorder(10,10,10,10));
		
		JPanel bottomPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelBtn = new JButton("Cancel");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog(false);
			}
		});
		JButton okayBtn = new JButton("OK");
		okayBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog(true);
			}
		});
		JPanel space = new JPanel();
		space.setPreferredSize(new Dimension(5, 5));
		bottomPnl.add(cancelBtn);
		bottomPnl.add(space);
		bottomPnl.add(okayBtn);
		fsp.add(bottomPnl, BorderLayout.SOUTH);
		
		add(fsp);
		getRootPane().setDefaultButton(okayBtn);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void closeDialog(boolean userAccepted) {
		selectedFile = userAccepted ? fsp.getSelectedPath() : null;
		dispose();
	}

	/**
	 * @param owner           parent window (can be null)
	 * @param title           dialog title
	 * @param message         instruction message
	 * @param defaultPath     default path to show in box
	 * @param isSaveDialog    true if this should be a save (not open) dialog
	 * @param directoriesOnly true to only allow selecting directories
	 * @param fileFilter      Optional file filter (can be null)
	 */
	public static String showDialog(Frame owner, String title, String message, 
			String defaultPath, boolean isSaveDialog, boolean directoriesOnly, 
			FileFilter fileFilter) {
		return new FileSelectionDialog(owner, title, message, defaultPath, 
				isSaveDialog, directoriesOnly, fileFilter).selectedFile;
	}
}
