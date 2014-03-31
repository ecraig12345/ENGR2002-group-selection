package edu.ou.engr.engr2002.ideagroupselection;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVReader;

/**
 * TODO full doc
 * 
 * REQUIREMENTS:
 * student names used in spreadsheet are same as ones used on D2L
 * student ideas map should have nulls for students who did not propose ideas
 * 
 * @author Elizabeth
 *
 */
public class Student {
	private String name;
	private int[] topIdeas = new int[10];
	private Idea proposedIdea = null;
	private boolean voted = false;
	
	public Student(String name, int[] topIdeas, Idea proposedIdea) {
		this.name = name;
		this.topIdeas = topIdeas == null ? new int[10] : topIdeas;
		this.proposedIdea = proposedIdea;
		for (int i = 0; i < 10; ++i) {
			if (topIdeas[i] != 0) {
				voted = true;
				break;
			}
		}
	}
	
	public Student(String absoluteFilePath, Idea proposedIdea) 
			throws IOException {
		this.proposedIdea = proposedIdea;
		// Find the student's name from the filename
		setNameFromAbsoluteFilePath(absoluteFilePath);
		
		// Read the chosen ideas from a CSV file.
		CSVReader reader = new CSVReader(new FileReader(absoluteFilePath));
		// Skip the header
		reader.readNext();
		// Read the lines of the file
		String[] nextLine;
		int i = 0;
		while (i < 10 && (nextLine = reader.readNext()) != null) {
			// The value in the second column should be a number.
			try {
				topIdeas[i] = Integer.parseInt(nextLine[1].trim());
			} catch (NumberFormatException ex) {
				// A non-number was in the cell--odd but recoverable
				Main.debug("Non-number found: " + nextLine[1]);
			} catch (ArrayIndexOutOfBoundsException ex) {
				// The second cell was missing--odd but recoverable
				Main.debug("Second cell missing");
			}
			++i;
		}
		
		if (Main.LOG) { 
			Main.log("Found student " + name);
			if (proposedIdea != null)
				Main.log("(proposed idea: " + proposedIdea.getNumber() + ")");
			Main.log(Arrays.toString(topIdeas));
		}
	}
	
	public String getName() {
		return name;
	}

	public Idea getProposedIdea() {
		return proposedIdea;
	}

	public int getIdeaRanked(int rank) {
		if (rank < 1 || rank > 10)
			return 0;
		return topIdeas[rank - 1];
	}
	
	public int getRankOfIdea(int ideaNumber) {
		for (int i = 0; i < 10; ++i) {
			if (topIdeas[i] == ideaNumber)
				return i + 1;
		}
		return 0;
	}
	
	public boolean didStudentVote() {
		return voted;
	}
	
	public String getVoteString() {
		return Arrays.toString(topIdeas);
	}

	private void setNameFromAbsoluteFilePath(String absoluteFilePath) {
		/* D2L makes file names of format:
		 * Elizabeth Craig- Oct 17, 2013 1030 PM - original filename.csv
		 * We want to find the student's name from the file name and reverse
		 * the order to last, first. */
		// Start by getting the filename out of the path
		String filename = absoluteFilePath.substring(
				absoluteFilePath.lastIndexOf(File.separatorChar) + 1);
		// Then split off the junk after the name
		int sepIndex = filename.indexOf("- ");
		if (sepIndex == -1) sepIndex = filename.length();
		String nameFirstLast = filename.substring(0, sepIndex);
		// Now we have the name in first last order. Reverse to last, first.
		name = nameFirstLast.replaceAll("(\\w*) (.*)", "$2, $1");
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Student other = (Student) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} 
		return name.equals(other.name);
	}
	
	public String toString() {
		return name + (proposedIdea == null ? " " : " (" + proposedIdea + ") ") + 
				Arrays.toString(topIdeas);
	}
}
