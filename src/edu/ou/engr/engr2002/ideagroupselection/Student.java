package edu.ou.engr.engr2002.ideagroupselection;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Contains a student's name, the idea the student proposed, and the 
 * idea numbers the student voted for. <p>
 * 
 * <b>REQUIREMENTS:<b>
 * <ul>
 * <li>student names used in spreadsheet are same as ones used on D2L (if using
 *     the constructor that takes filenames)
 * <li>student ideas map should have nulls for students who didn't propose ideas
 * </ul>
 */
public class Student {
	private static final int NUM_RANKED = 10;
	private String name;
	private int[] topIdeas = new int[NUM_RANKED];
	private Idea proposedIdea = null;
	private boolean voted = false;
	
	/**
	 * Constructs a new student.
	 * @param name The student's name
	 * @param topIdeas The idea numbers the student voted for in rank order.
	 * Can be null if the student didn't vote. 
	 * @param proposedIdea The idea the student proposed (can be null)
	 * @param lastFirst If true, use name format "Last, First" (regardless of
	 * the original format of the name)
	 * @throws IllegalArgumentException if name is null or length of topIdeas
	 * is greater than the number of ideas that can be ranked
	 */
	public Student(String name, int[] topIdeas, Idea proposedIdea,
			boolean lastFirst) {
		this.proposedIdea = proposedIdea;
		this.name = nameToOrder(name, lastFirst);
		setVotes(topIdeas);
	}
	
	/**
	 * Reads student vote information from a file.
	 * @param votesFile File pointing to the student's voting .csv file
	 * @param proposedIdea The idea the student proposed
	 * @param lastFirst If true, use name format "Last, First"
	 * @throws IOException if anything goes wrong reading the files
	 * @throws IllegalArgumentException if file is null
	 * @throws NumberFormatException if the student entered an invalid value
	 * (such as an idea name rather than number)
	 */
	public Student(File votesFile, Idea proposedIdea, boolean lastFirst) 
			throws IOException {
		this.proposedIdea = proposedIdea;
		/* Find the student's name from the filename.
		 * D2L makes file names of format:
		 * Elizabeth Craig- Oct 17, 2013 1030 PM - original filename.csv
		 * We want to find the student's name from the file name and reverse
		 * the order to last, first. */
		// Start by getting the filename out of the path
		String filename = votesFile.getName();
		// Then get rid of the junk after the name
		int sepIndex = filename.indexOf("- ");
		if (sepIndex == -1) sepIndex = filename.length();
		String name = filename.substring(0, sepIndex);
		// Set the name in the desired order
		this.name = nameToOrder(name, lastFirst);
		
		CSVReader reader = null;
		try {
			// Read the chosen ideas from a CSV file.
			reader = new CSVReader(new FileReader(votesFile));
			// Skip the header
			reader.readNext();
			// Read the lines of the file
			String[] nextLine;
			int i = 0;
			while (i < NUM_RANKED && (nextLine = reader.readNext()) != null) {
				// The value in the third column should be a number.
				try {
					topIdeas[i] = Integer.parseInt(nextLine[2].trim());
				} catch (ArrayIndexOutOfBoundsException ex) {
					// The second cell was missing--odd but recoverable
					Main.debug("Second cell missing");
				}
				++i;
			}
		} finally {
			if (reader != null) reader.close();
		}
		
		if (Main.LOG) { 
			Main.log("Found student " + this.name);
			if (proposedIdea != null)
				Main.log("(proposed idea: " + proposedIdea.number + ")");
			Main.log(Arrays.toString(topIdeas));
		}
	}
	
	/** Gets the student's name */
	public String getName() {
		return name;
	}
	
	/** Gets the idea the student proposed (could be null) */
	public Idea getProposedIdea() {
		return proposedIdea;
	}
	
	/**
	 * Set the student's proposed idea.
	 * @throws IllegalArgumentException if the student's idea had already
	 * been set to something other than null
	 */
	public void setProposedIdea(Idea proposedIdea) {
		if (proposedIdea != null)
			throw new IllegalArgumentException("Can't set idea twice");
		this.proposedIdea = proposedIdea;
	}

	/** 
	 * Get the idea number that the student gave the given rank.
	 * @throws IllegalArgumentException if rank is < 1 or > 10
	 */
	public int getIdeaRanked(int rank) {
		if (rank < 1 || rank > NUM_RANKED)
			throw new IllegalArgumentException("Invalid idea rank: " + rank);
		return topIdeas[rank - 1];
	}
	
	/**
	 * Gets the rank the student assigned to the given idea number. 
	 * If the student didn't rank the idea or didn't vote, returns 0.
	 */
	public int getRankOfIdea(int ideaNumber) {
		for (int i = 0; i < NUM_RANKED; ++i) {
			if (topIdeas[i] == ideaNumber)
				return i + 1;
		}
		return 0;
	}
	
	/** Returns whether the student voted */
	public boolean didStudentVote() {
		return voted;
	}
	
	/** Gets a string of the idea numbers the student voted for */
	public String getVoteString() {
		return Arrays.toString(topIdeas);
	}

	/**
	 * Sets the ideas the student voted for
	 * @param topIdeas The idea numbers the student voted for
	 * @throws IllegalArgumentException if the student's votes have already
	 * been set or the length of topIdeas is too long
	 */
	public void setVotes(int[] topIdeas) {
		if (voted)
			throw new IllegalArgumentException("Can't set votes twice");
		if (topIdeas == null)
			return;
		if (topIdeas.length > NUM_RANKED)
			throw new IllegalArgumentException("length of topIdeas must "
					+ "be <= " + NUM_RANKED);
	
		for (int i = 0; i < topIdeas.length; ++i)
			this.topIdeas[i] = topIdeas[i];
		// Figure out if the student voted at all
		for (int i = 0; i < this.topIdeas.length; ++i) {
			if (this.topIdeas[i] != 0) {
				voted = true;
				break;
			}
		}
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
		if (name == null && other.name != null)
			return false;
		return name.equals(other.name); 
	}
	
	public String toString() {
		return name 
				+ (proposedIdea == null ? " " : " (" + proposedIdea + ") ") 
				+ Arrays.toString(topIdeas);
	}
	
	/**
	 * Converts a name (first and last) to the given ordering, if it's not
	 * already in that ordering
	 * @param lastFirst If true, use order "Last, First"
	 * @return The modified name
	 * @throws IllegalArgumentException if name is null
	 */
	public static String nameToOrder(String name, boolean lastFirst) {
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");
		if (name.indexOf(',') == -1) {
			if (lastFirst)
				name = name.replaceAll("(.*?) (.*)", "$2, $1");
		} else {
			if (!lastFirst)
				name = name.replaceAll("(.*?), (.*)", "$2 $1");
		}
		return name;
	}
}
