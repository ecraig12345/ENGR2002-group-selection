package edu.ou.engr.engr2002.ideagroupselection;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;
import edu.ou.engr.engr2002.ideagroupselection.Idea.IdeaSet;

public class GroupCreator {
	/**
	 * Reads files, creates groups, and returns a string with the results.
	 * @param ideasCsv		path to CSV file with ideas, students, and
	 * 						(optionally) voting data
	 * @param votesDir		optional: path to directory with student voting
	 * 						files (if this is given, won't look for voting data 
	 * 						in ideasCsv)
	 * @param groupsStr		optional: string with idea numbers to use
	 * @param lastFirst		if true, use name format "Last, First" in output
	 * @param numGroups		number of groups to create (<= 0 means as many as
	 * 						given in groupsStr)
	 * @param maxGroupSize	max group size
	 * @param shortVersion	include short (student) version in output
	 * @param longVersion	include long (instructor) version in output
	 * @return A string with the results, or null if an error occurred
	 */
	public static String makeGroupsStr(String ideasCsv, String votesDir, 
			String groupsStr, boolean lastFirst, int numGroups, 
			int maxGroupSize, boolean shortVersion, boolean longVersion) {
		if (ideasCsv == null) {
			JOptionPane.showMessageDialog(null, "Must specify path to "
					+ "CSV file with students and ideas.", "Error", 
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		return new GroupCreator(ideasCsv, votesDir, groupsStr, lastFirst, 
				numGroups, maxGroupSize)
				.getIdeasGroupsStr(shortVersion, longVersion);
	}
	
	private boolean lastFirst;
	private String ideasCsv;
	private String votesDir;
	private int numGroups;
	private int maxGroupSize;
	private HashSet<Student> students = new HashSet<Student>();
	private IdeaSet ideas = new IdeaSet();
	private HashMap<Integer, Group> groups;
	private String warnings = "";
	private String results = null;
	private boolean success = false;
	
	/** 
	 * Make a GroupCreator with parameters as described in makeGroupsStr
	 * and perform the calculations to put students in groups.
	 * @throws IllegalArgumentException if ideasCsv is null
	 */
	private GroupCreator(String ideasCsv, String votesDir, String groupsStr,
			boolean lastFirst, int numGroups, int maxGroupSize) {
		if (ideasCsv == null) 
			throw new IllegalArgumentException("ideasCsv cannot be null");
		this.ideasCsv = ideasCsv;
		this.votesDir = votesDir;
		this.lastFirst = lastFirst;
		this.numGroups = numGroups;
		this.maxGroupSize = maxGroupSize;
		makeGroups(groupsStr);
	}

	/**
	 * Make groups given the class members plus the optional groupsStr.
	 * @param groupsStr if non-null, will determine the idea numbers used
	 */
	private void makeGroups(String groupsStr) {
		try {
			if (votesDir == null) {
				readStudentsIdeasFromFile();
			} else {
				if (!readStudentsIdeasFromVotesDir())
					return;
			}
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Error reading a file: " +
					ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		// Count the votes
		countVotes();
		
		// Start groups for the top ideas, with the student who proposed the
		// idea as the leader of the groups. Remove the leaders from the
		// set of students.
		HashSet<Student> remainingStudents = new HashSet<Student>(students);
		if (groupsStr == null)
			groups = makeGroupsFromVotes(remainingStudents);
		else
			groups = makeGroupsFromString(groupsStr, remainingStudents);
		// Put the remaining students in groups
		groupStudents(groups, remainingStudents);
		success = true;
	}
	
	/**
	 * Reads the file with ideas, students, and votes into ideas and students 
	 * (including determining the number of votes each idea received).
	 * Pre: ideas and students are empty
	 * Post: ideas and students contain the ideas and students read from file
	 * @throws IOException if there is a problem reading the file
	 */
	private void readStudentsIdeasFromFile() throws IOException {
		// Read the ideas, student names, and votes from the file
		CSVReader reader = new CSVReader(new FileReader(ideasCsv));
		try {
			String[] nextLine = reader.readNext(); // skip the first line
			while ((nextLine = reader.readNext()) != null) {
				Student studentRead = parseStudent(nextLine, true);
				if (studentRead != null)
					students.add(studentRead);
			}
		} finally {
			reader.close();
		}
	}
	
	/**
	 * Reads the file with ideas, students, and votes into ideas and students 
	 * (including determining the number of votes each idea received).
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private boolean readStudentsIdeasFromVotesDir() throws IOException {
		if (votesDir == null)
			throw new IllegalArgumentException("votesDir cannot be null");
	
		// Get all the files in the voting sheet directory
		File[] files = new File(votesDir).listFiles();
		if (files == null)
			throw new IOException("Not a directory: " + votesDir);
		String errorContinue = "<p>Continue anyway?<br>(if you continue, this "
				+ "student's results will not be included in the output)";
		for (File file : files) {
			// Try to read the file into a Student object if it's a csv file
			if (file.getName().toLowerCase().endsWith(".csv")) { 
				try {
					students.add(new Student(file, null, lastFirst));
				} catch (IOException ex) {
					// problem reading the file
					int option = JOptionPane.showOptionDialog(null,
							"<html>Problem reading file " + file.getName()
							+ ": " + ex + errorContinue, "Invalid File", 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.ERROR_MESSAGE, null, null, null);
					if (option != JOptionPane.YES_OPTION)
						return false;
				} catch (NumberFormatException ex) {
					// student probably used idea names not numbers
					int option = JOptionPane.showOptionDialog(null, 
							"<html> Non-number found in idea number column in "
							+ file.getName() + errorContinue,
							"Problem Reading File", JOptionPane.YES_NO_OPTION, 
							JOptionPane.ERROR_MESSAGE, null, null, null);
					if (option != JOptionPane.YES_OPTION)
						return false;
				}
			}
		}
		
		// Read the idea names from a separate file
		CSVReader reader = new CSVReader(new FileReader(ideasCsv));
		try {
			String[] nextLine = reader.readNext(); // skip the first line
			while ((nextLine = reader.readNext()) != null) {
				Student studentRead = parseStudent(nextLine, false);
				if (studentRead == null)
					continue;
				
				// Make sure we have a record of this student.
				// Also, figure out which student proposed this idea.
				boolean studentFound = false;
				for (Student s : students) {
					if (s.getName().equals(studentRead.getName())) {
						if (studentRead.getProposedIdea() != null)
							s.setProposedIdea(studentRead.getProposedIdea());
						studentFound = true;
						break;
					}
				}
				if (!studentFound)
					// The student must not have voted
					students.add(studentRead);
			}
		} finally {
			reader.close();
		}
		
		return true;
	}
	
	private Student parseStudent(String[] nextLine, boolean includeVotes) {
		if (nextLine.length < 3) {
			// This might be an empty line or something
			return null;
		} 
		// Get the student's name. If this is empty, that's bad.
		String name = nextLine[2];
		if (name.isEmpty())
			return null;

		// Get the student's idea (if any) from this line
		Idea idea = null;
		try {
			int ideaNumber = Integer.parseInt(nextLine[0].trim());
			// 0 means no idea
			if (ideaNumber > 0) {
				idea = new Idea(nextLine[1].trim(), ideaNumber);
				ideas.add(idea);
			}
		} catch (NumberFormatException ex) {
			// There was not an idea for this student
		}

		int[] votes = null;
		if (includeVotes) {
			// Get the student's votes (if the line is long enough)
			votes = new int[10];
			if (nextLine.length >= 13) {
				for (int i = 0; i < 10; ++i) {
					try {
						votes[i] = Integer.parseInt(nextLine[i+3].trim());
					} catch (NumberFormatException ex) {
					}
				}
			}
		}
		return new Student(name, votes, idea, lastFirst);
	}
	
	private void countVotes() {
		// Update the idea vote totals
		for (Student s : students) {
			if (s.didStudentVote()) 
				for (int i = 0; i < 10; ++i)
					ideas.addVotesToIdeaNumber(s.getIdeaRanked(i+1), 10 - i);
		}
	}

	/**
	 * Choose the idea for each group based on the votes, put the students who
	 * proposed those ideas in the respective groups as leaders, and remove
	 * the leaders from the list of students.
	 * 
	 * @param remainingStudents Set of students (after the method exits, group 
	 * leaders will be removed from this set)
	 * @return Map from idea number to group objects, with only the group
	 * leaders added to the group objects' lists of members
	 */
	private HashMap<Integer, Group> makeGroupsFromVotes(
			HashSet<Student> remainingStudents) {
		// Get the top numGroups ideas
		List<Idea> topIdeas = ideas.getTopIdeas(numGroups);
		HashMap<Integer, Group> groups = new HashMap<Integer, Group>(numGroups);
		HashSet<Student> leaders = new HashSet<Student>();
		// Find the students who proposed the top ideas, and make groups with
		// those students as leaders.
		for (Student s : remainingStudents) {
			Idea idea = s.getProposedIdea();
			if (idea != null && topIdeas.contains(idea)) {
				groups.put(idea.number, new Group(s, idea, maxGroupSize));
				leaders.add(s);
			}
		}
		// Remove all the students who are now in groups
		remainingStudents.removeAll(leaders);
		
		// If the number of groups didn't turn out quite right, that's bad
		if (groups.size() != numGroups) {
			JOptionPane.showMessageDialog(null, "Couldn't find all the "
					+ "students who proposed the top ideas. (number of "
					+ "groups requested = " + numGroups + ", actual = " +
					groups.size() + ")", "Error", JOptionPane.ERROR_MESSAGE);
		}
		return groups;
	}
	
	/**
	 * Make groups for the idea numbers given in a string, put the students
	 * who proposed those ideas in the respective groups as leaders, and remove
	 * the leaders from the list of students.
	 * If numGroups is <= 0, make however many groups there are numbers in input.
	 * 
	 * @param input String containing idea numbers (separated by spaces,
	 * commas, or anything else that's not a number)
	 * @param remainingStudents All the students (this method will remove any 
	 * students who are group leaders)
	 * @return Mapping from idea number to group
	 */
	private HashMap<Integer, Group> makeGroupsFromString(String input, 
			HashSet<Student> remainingStudents) {
		// mapping from idea number to group
		HashMap<Integer, Group> groups = new HashMap<Integer, Group>();
		HashSet<Student> leaders = new HashSet<Student>();

		String error = null;
		// Split the string into numbers (\D means non-digits)
		String[] numbers = input.split("[\\D]");
		error = null;
		// Parse each number into a string and make a group for it
		for (String numStr : numbers) {
			int ideaNumber;
			try { 
				ideaNumber = Integer.parseInt(numStr);
			} catch (NumberFormatException ex) {
				error = "Not a valid idea number: " + numStr;
				break;
			}
			// Make sure the idea wasn't entered multiple times
			if (groups.containsKey((Integer)ideaNumber)) {
				error = "Duplicate idea number: " + ideaNumber;
				break;
			}
			// Find the idea information
			Idea idea = ideas.get(ideaNumber);
			if (idea == null) {
				error = "Idea number not found: " + ideaNumber;
				break;
			}
			// Find the student who proposed the idea
			Student leader = idea.findProposer(remainingStudents);
			if (leader == null) {
				error = "Couldn't find student who proposed idea " + ideaNumber;
				break;
			}
			// Make a group for the idea and add the student who proposed
			// the idea as the leader
			groups.put(ideaNumber, new Group(leader, idea, maxGroupSize));
			leaders.add(leader);
		}
		if (error == null && numGroups > 0 && groups.size() != numGroups) {
			error = "Wrong number of groups (needed " + numGroups
					+ ", found " + groups.size() + ")";
		}
		if (error != null) {
			JOptionPane.showMessageDialog(null, error, 
					"Error creating groups", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// Remove all the students who are now in groups
		remainingStudents.removeAll(leaders);

		return groups;
	}
	
	/**
	 * Put the remaining students in the groups. Tries to put students in 
	 * groups for the ideas that they preferred.
	 * (Updates warnings member with any problems encountered while putting
	 * students in groups.)
	 * @param groups Map from group numbers to group objects
	 * @param remainingStudents Students who are not yet in a group.
	 * When the method is called, this will probably be all the students except
	 * the leaders. When the method exits, this will contain all the students
	 * who still could not be placed in groups because either their preferred
	 * groups filled up (with students who ranked those ideas higher) or they
	 * voted but did not choose any of the ideas that won. 
	 * @return String with information about the students not put in groups
	 */
	private void groupStudents(HashMap<Integer, Group> groups, 
			HashSet<Student> remainingStudents) {
		StringBuilder err = new StringBuilder();
		// Make a list of students who did not vote and remove them from
		// remainingStudents so they can be dealt with separately.
		ArrayList<Student> nonVotingStudents = new ArrayList<Student>();
		for (Student s : remainingStudents) {
			if (!s.didStudentVote()) 
				nonVotingStudents.add(s);
		}
		remainingStudents.removeAll(nonVotingStudents);
		
		// Keep a list of the groups that are not yet full
		HashMap<Integer, Group> nonFullGroups = new HashMap<Integer, Group>(groups);
		int currentRank = 1;
		// Add remaining students to groups. Start by seeing if each student's
		// #1-ranked idea was chosen, and if so, add them to that group.
		// Continue this process with the remaining ranks and students,
		// removing groups from consideration when they fill up.
		while (remainingStudents.size() != 0 && currentRank <= 10) {
			// Keep track of the students added while iterating
			HashSet<Student> addedStudents = new HashSet<Student>();
			for (Student s : remainingStudents) {
				// Get the idea this student has ranked at the current rank
				int idea = s.getIdeaRanked(currentRank);
				// See if the idea was chosen (and its group is not full)
				Group group = nonFullGroups.get((Integer)idea);
				if (group != null) {
					// Add the student to the group and to the list of students
					// to remove from consideration.
					group.addMember(s);
					addedStudents.add(s);
					// If the group is full, remove it from consideration.
					if (group.isFull()) 
						nonFullGroups.remove((Integer)idea);
				}
			}
			remainingStudents.removeAll(addedStudents);
			++currentRank;
		}
		
		// Some students did not get put in groups.
		if (remainingStudents.size() > 0) {
			err.append("Some students did not get put into groups.\n");
			for (Student s : remainingStudents)
				err.append("* " + s.getName() + " - n/a"
						+ (s.didStudentVote() ? " - " + s.getVoteString() : "")
						+ '\n');
			err.append('\n');
		}
		
		// Add non-voting students to groups with size < 4 first.
		for (Group g : nonFullGroups.values()) {
			if (nonVotingStudents.isEmpty()) break;
			if (g.getSize() < 4) {
				g.addMember(nonVotingStudents.get(0));
				nonVotingStudents.remove(0);
			}
		}
		// Add any remaining non-voting students to any group that's left
		for (Group g : nonFullGroups.values()) {
			if (nonVotingStudents.isEmpty()) break;
			g.addMember(nonVotingStudents.get(0));
			nonVotingStudents.remove(0);
		}
		if (!nonVotingStudents.isEmpty()) {
			err.append("WARNING: some non-voting students were not " +
					"put in groups. This should not happen.\n");
			for (Student s : nonVotingStudents)
				err.append(s + "\n");
			err.append('\n');
		}
		
		this.warnings = err.toString();
		
//		// Make sure that no groups with fewer than four students were created.
//		// If they were, resolve the problem by removing students from 
//		// five-student groups.
//		HashSet<Group> smallGroups = new HashSet<Group>();
//		HashSet<Group> largeGroups = new HashSet<Group>();
//		for (Group group : groups.values()) {
//			int size = group.getSize();
//			if (size < 4)
//				smallGroups.add(group);
//			else if (size > 4)
//				largeGroups.add(group);
//		}
//		if (smallGroups.size() > 0) {
//			HashSet<Group> problemGroups = resolveGroupSizes(smallGroups, largeGroups);
//			if (problemGroups.size() != 0) {
////				JOptionPane.showMessageDialog(null, "Some groups could not " +
////						"be made the correct size.", 
////						"Error", JOptionPane.ERROR_MESSAGE);
////				System.exit(1);
//			}
//		}
	}
	
	/**
	 * Attempts to even out group sizes, given the sets of groups that have 
	 * too few members or are completely full.
	 * @param smallGroups Groups with too few members
	 * @param largeGroups Groups that are completely full
	 * @return Set of groups whose sizes could not be fixed
	 */
	private HashSet<Group> resolveGroupSizes(HashSet<Group> smallGroups,
			HashSet<Group> largeGroups) {
		HashSet<Group> problemGroups = new HashSet<Group>();
		// For each group that is too small...
		for (Group small : smallGroups) {
			// First see if students from the other groups ranked that idea.
			int idea = small.idea.number;
			TreeMap<Integer, Pair<Student, Group>> studentsRankingIdea = 
					new TreeMap<Integer, Pair<Student, Group>>();
			for (Group large: largeGroups) {
				List<Student> members = large.getMemberView();
				// For each member of a large group (other than the leader), 
				// see if they ranked the idea
				for (Student s : members.subList(1, members.size())) {
					int ideaRank = s.getRankOfIdea(idea);
					if (ideaRank != 0) {
						// If a student who gave this idea this particular rank
						// hasn't been found yet, add the information.
						if (!studentsRankingIdea.containsKey((Integer)idea))
							studentsRankingIdea.put(ideaRank, 
									new Pair<Student, Group>(s, large));
					}
				}
			}
			
			// If no students could be found who picked this idea, that's 
			// a problem.
			if (studentsRankingIdea.size() == 0) {
				problemGroups.add(small);
			} else {
				// Reassign the student who gave the idea the highest rank
				// to the new group.
				Pair<Student, Group> sg = studentsRankingIdea.firstEntry().getValue();
				sg.second.removeMember(sg.first);
				largeGroups.remove(sg.second);
				small.addMember(sg.first);
			}
		}
		return problemGroups;
	}
	
	/**
	 * Get a string with the results of the group formation.
	 * @param shortv Returned value should include short (student) version
	 * @param longv Returned value should include long (instructor) version
	 * @return the string as specified, or null if there was an error in the
	 * process of forming groups (or both shortv and longv were false)
	 */
	public String getIdeasGroupsStr(boolean shortv, boolean longv) {
		if (!success || !(shortv || longv))
			return null;
		if (results != null)
			return results;

		StringBuilder sb = new StringBuilder(2048);
		sb.append(warnings);
		sb.append("\n\n");
		TreeSet<Group> groupsSorted = new TreeSet<Group>(groups.values());

		if (shortv) {
			for (Group g : groupsSorted) {
				sb.append(g.toShortString() + '\n');
			}
			for (Idea idea : ideas.getIdeasSorted()) {
				sb.append(idea.toShortString() + '\n');
			}
		}

		if (shortv && longv)
			sb.append("\n\n---------- Details ----------\n");
		
		if (longv) {
			for (Group g : groupsSorted) {
				sb.append(g + "\n");
			}
			sb.append("\n\n");
			
			// these structures will be used to look up students and groups
			// by idea
			HashMap<Idea, Student> ideasStudents = new HashMap<Idea, Student>();
			for (Student s : students) {
				if (s.getProposedIdea() != null) 
					ideasStudents.put(s.getProposedIdea(), s);
			}
			HashMap<Idea, Group> ideasGroups = new HashMap<Idea, Group>();
			for (Group g : groups.values()) {
				ideasGroups.put(g.idea, g);
			}

			String groupFmt = " (Group %s)\n";
			for (Idea idea : ideas.getIdeasSorted()) {
				Student student = ideasStudents.get(idea);
				Group group = ideasGroups.get(idea);
				sb.append(idea.toLongString(
						student == null ? "" : student.getName()));
				sb.append(group == null ? "\n" : 
					String.format(groupFmt, group.number));
			}
		}
		results = sb.toString();
		return results;
	}
}
