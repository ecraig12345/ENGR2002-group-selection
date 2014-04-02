package edu.ou.engr.engr2002.ideagroupselection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

@SuppressWarnings("serial")
public class OutputDialog extends JDialog {
	private static final int SPACE = 5;
	private static final int SPACE_L = 10;
	private JPanel mainPnl = new JPanel(new BorderLayout(SPACE, SPACE));
	private JEditorPane textPane;
	private JScrollPane scrollPane;
	private JTextField highlightTF = new JTextField(3);
	private DefaultHighlightPainter yellowPainter = 
	        new DefaultHighlightPainter(Color.YELLOW);
	private DefaultHighlightPainter cyanPainter =
			new DefaultHighlightPainter(Color.CYAN);
	private Frame parent;
	
	public OutputDialog(Frame parent, String text) {
		super(parent, "Results", true);
		this.parent = parent;

		add(mainPnl);
		mainPnl.setBorder(new EmptyBorder(SPACE_L, SPACE_L, SPACE_L, SPACE_L));

		text = "<font face=\"sans-serif\">" 
				+ text.replaceAll("\r?\n", "<br>\n");
		text = text.replaceAll("(\nGroup \\d+|\nIdea \\d+[^\\n]*)", "<b>$1</b>");
		
		textPane = new JEditorPane("text/html", text);
		textPane.setCaretPosition(0);
		scrollPane = new JScrollPane(textPane);
		mainPnl.add(scrollPane, BorderLayout.CENTER);
		
		JPanel highlightPnl = new JPanel(
				new FlowLayout(FlowLayout.LEFT, SPACE, SPACE));
		highlightPnl.add(new JLabel("Highlight students who voted for idea #:"));
		highlightPnl.add(highlightTF);
		JButton highlightBtn = new JButton("Highlight");
		highlightPnl.add(highlightBtn);
		highlightBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				highlightIdea();
			}
		});
		JButton clearBtn = new JButton("Clear highlights");
		highlightPnl.add(clearBtn);
		clearBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textPane.getHighlighter().removeAllHighlights();
			}
		});
		mainPnl.add(highlightPnl, BorderLayout.NORTH);
		
		JPanel writePnl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton writeBtn = new JButton("Write file");
		writePnl.add(writeBtn);
		writeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				writeFile();
			}
		});
		mainPnl.add(writePnl, BorderLayout.SOUTH);
		
		setSize(600, 600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	private void highlightIdea() {
		int ideaNum;
		try {
			ideaNum = Integer.parseInt(highlightTF.getText());
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(OutputDialog.this,
					"Invalid idea number", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Highlighter highlighter = textPane.getHighlighter();
		highlighter.removeAllHighlights();
		String text;
		try {
			text = textPane.getDocument().getText(0, 
					textPane.getDocument().getLength());
		} catch (BadLocationException ex) {
			System.err.println(ex);
			return;
		}
		Pattern pattern = Pattern.compile(
				// "* " followed by non-*s followed by " - "
				"\\* [^\\*]*?( - )"
				// ...followed by non-*s followed by [ 
				// (note: (?=\[) is a lookahead assertion--it looks for [ next
				// but doesn't consume it)
				+ "[^\\*]*?(?=\\[)"
				// ...followed by non-[s followed by not a digit and the 
				// requested number and not a digit (lookahead in case it's a ])
				+ "([^\\]]*?(\\D(" + ideaNum + ")(?=\\D))"
				// ...followed by anything followed by ]
				+ ".*?\\])");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			// Highlight each match: the number itself will be cyan, and the
			// rest of the line will be yellow
			try {
				highlighter.addHighlight(
						matcher.start(), matcher.start(4), yellowPainter);
				highlighter.addHighlight(
						matcher.start(4), matcher.end(4), cyanPainter);
				highlighter.addHighlight(
						matcher.end(4), matcher.end(), yellowPainter);
			} catch (BadLocationException ex) {
				System.err.println(ex);
			}
		}
		textPane.setCaretPosition(0);
	}
	
	private void writeFile() {
		String fname = FileSelectionDialog.showDialog(
				OutputDialog.this.parent, "Choose output file", 
				"<html>Choose output file base name. <br>(Two files will be "
				+ "created, with suffixes \" - details\" and \" - groups\")", 
				System.getProperty("user.home") + File.separator + "results", 
				false, null, true);
		if (fname == null)
			return;
		fname = fname.replaceAll("\\.(html|htm|txt)$", "");
		String text = textPane.getText();
		int ndx = text.indexOf("<body>");
		if (ndx != -1) {
			text = text.substring(ndx + 6);
			ndx = text.indexOf("</body>");
			if (ndx != -1)
				text = text.substring(0, ndx);
		}
		text = text.replaceAll("\n(\\S)", "<br>$1");
		text = text.replaceAll("\\s+", " ");
		text = text.replaceAll("<br>", "\n<br>");
		text = "<html>\n<head>\n<title>Results</title>\n</head>\n<body>\n"
				+ text + "\n</body>\n</html>\n";
		
		String shortText = text.replaceAll(
				"(\\* [^\\*]*?)( - [^L]\\S*)( - \\[.*?\\])?", "$1");
		shortText = shortText.replaceAll(
				"( *\\d+ - +\\d+: \".*?\")([^\n]*?)(\n|<br>)", "$1$3");
		FileWriter writer = null;
		try {
			writer = new FileWriter(fname + " - details.html");
			writer.write(text);
			writer.close();
			writer = new FileWriter(fname + " - groups.html");
			writer.write(shortText);
			dispose();
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(this, "Error writing output: " 
					+ ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			try {
				if (writer != null) 
					writer.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}
