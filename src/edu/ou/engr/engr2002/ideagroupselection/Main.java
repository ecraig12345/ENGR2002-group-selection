package edu.ou.engr.engr2002.ideagroupselection;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import au.com.bytecode.opencsv.CSVReader;
import edu.ou.engr.engr2002.ideagroupselection.Idea.IdeaSet;

public class Main {
	public static final int NUM_GROUPS = 9;
	public static final boolean LOG = true;
	public static final boolean DEBUG = true;
	
	public static void main(String[] args) {
		HashSet<Student> students = new HashSet<Student>();
		IdeaSet ideas = new IdeaSet();
		try {
			readStudentsIdeasMethod2(ideas, students);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "Error reading the file: " +
					ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		// Start groups for the top ideas, with the student who proposed the
		// idea as the leader of the groups. Remove the leaders from the
		// set of students.
		HashSet<Student> remainingStudents = new HashSet<Student>(students);
		HashMap<Integer, Group> groups = makeGroupsFromUser(ideas, 
				remainingStudents);
		// Put the remaining students in groups
		groupStudents(groups, remainingStudents);
		
		printIdeasGroups(ideas, students, groups.values());
	}

	/**
	 * Prompts the user to select the file with ideas, students, and votes,
	 * and reads the file into ideas and students (including determining the
	 * number of votes each idea received).
	 * @param ideas Empty IdeaSet
	 * @param students Empty HashSet
	 * @throws IOException if there is a problem reading the file
	 */
	private static void readStudentsIdeasMethod2(IdeaSet ideas, 
			HashSet<Student> students) throws IOException {
		String filePath = FileSelectionDialog.showDialog(
				"Select File",
				"Absolute path to CSV file with ideas, students, and votes:",
				"", false,
				new FileNameExtensionFilter("CSV files (.csv)", "csv"));
		if (filePath == null)
			System.exit(0);
		
		// Read the ideas, student names, and votes from the file
		CSVReader reader = new CSVReader(new FileReader(filePath));
		String[] nextLine = reader.readNext(); // skip the first line
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length < 3) {
				// This might be an empty line or something
				continue;
			} 
			// Get the student's name. If this is empty, that's bad.
			String name = nextLine[2];
			if (name.isEmpty())
				continue;
			
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
			
			// Get the student's votes (if the line is long enough)
			int[] votes = new int[10];
			if (nextLine.length >= 13) {
				for (int i = 0; i < 10; ++i) {
					try {
						votes[i] = Integer.parseInt(nextLine[i+3].trim());
					} catch (NumberFormatException ex) {
					}
				}
			}
			students.add(new Student(name, votes, idea));
		}
		reader.close();
		
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
	 * @param ideas Set of ideas with vote data calculated
	 * @param students Set of students (after the method exits, group leaders
	 * will be removed from this set)
	 * @return Map from group number to group objects, with only the group
	 * leaders added to the group objects' lists of members
	 */
	private static HashMap<Integer, Group> makeGroupsFromVotes(IdeaSet ideas,
			HashSet<Student> students) {
		List<Idea> topIdeas = ideas.getTopIdeas(NUM_GROUPS);
		HashMap<Integer, Group> groups = new HashMap<Integer, Group>(NUM_GROUPS);
		HashSet<Student> leaders = new HashSet<Student>();
		// Find the students who proposed the top ideas, and make groups with
		// those students as leaders.
		for (Student s : students) {
			Idea idea = s.getProposedIdea();
			if (idea != null && topIdeas.contains(idea)) {
				groups.put(idea.number, new Group(s, idea));
				leaders.add(s);
			}
		}
		// Remove all the students who are now in groups
		students.removeAll(leaders);
		
		// If the number of groups didn't turn out quite right, that's bad
		if (groups.size() != NUM_GROUPS) {
			JOptionPane.showMessageDialog(null, "Something went wrong with " +
					"finding the students who proposed the top ideas.", 
					"Error", JOptionPane.ERROR_MESSAGE);
//			System.exit(1);
		}
		return groups;
	}
	
	private static HashMap<Integer, Group> makeGroupsFromUser(IdeaSet ideas,
			HashSet<Student> students) {
		HashMap<Integer, Group> groups = null; 
		HashSet<Student> leaders = null;
		
		// Keep asking for the group numbers as long as the input is invalid
		String input = null;
		String error = null;
		while (true) {
			groups = new HashMap<Integer, Group>();
			leaders = new HashSet<Student>();
			input = JOptionPane.showInputDialog(null,
					"Enter the numbers of the winning ideas.", input);
			if (input == null)
				System.exit(0);
			
			// Split the string into numbers (\D means non-digits)
			String[] numbers = input.split("[\\D]");
			error = null;
			// Parse each number into a string and make a group for it
			for (String numStr : numbers) {
				int number;
				try { 
					number = Integer.parseInt(numStr);
				} catch (NumberFormatException ex) {
					error = "Not a valid group number: " + numStr;
					break;
				}
				// Make sure the idea wasn't entered multiple times
				if (groups.containsKey((Integer)number)) {
					error = "Duplicate group number: " + number;
					break;
				}
				// Find the idea information
				Idea idea = ideas.get(number);
				if (idea == null) {
					error = "Group number not found: " + number;
					break;
				}
				// Find the student who proposed the idea
				Student leader = idea.findProposer(students);
				if (leader == null) {
					error = "Couldn't find student who proposed idea " + number;
					break;
				}
				// Make a group for the idea and add the student who proposed
				// the idea as the leader
				groups.put(number, new Group(leader, idea));
				leaders.add(leader);
			}
//			if (error == null && groups.size() != NUM_GROUPS) {
//				error = "Wrong number of groups (needed " + NUM_GROUPS
//						+ ", found " + groups.size() + ")";
//			}
			// If there were no errors, stop asking the user for input
			if (error == null)
				break;
			JOptionPane.showMessageDialog(null, error, 
					"Error creating groups", JOptionPane.ERROR_MESSAGE);
		}
		// Remove all the students who are now in groups
		students.removeAll(leaders);
		
		return groups;
	}
	
	/**
	 * Put the remaining students in the groups. Tries to put students in 
	 * groups for the ideas that they preferred.
	 * @param groups Map from group numbers to group objects
	 * @param remainingStudents Students who are not yet in a group.
	 * When the method is called, this will probably be all the students except
	 * the leaders. When the method exits, this will contain all the students
	 * who still could not be placed in groups because either their preferred
	 * groups filled up (with students who ranked those ideas higher) or they
	 * voted but did not choose any of the ideas that won. 
	 */
	private static void groupStudents(HashMap<Integer, Group> groups, 
			HashSet<Student> remainingStudents) {
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
			System.out.println("Some students did not get put into groups.");
			for (Student s : remainingStudents)
				System.out.println(s);
			System.out.println();
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
			System.out.println("WARNING: some non-voting students were not " +
					"put in groups. This should not happen.");
			for (Student s : nonVotingStudents)
				System.out.println(s);
			System.out.println();
		}
		
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
	private static HashSet<Group> resolveGroupSizes(HashSet<Group> smallGroups,
			HashSet<Group> largeGroups) {
		HashSet<Group> problemGroups = new HashSet<Group>();
		// For each group that is too small...
		for (Group small : smallGroups) {
			// First see if students from the other groups ranked that idea.
			int idea = small.idea.number;
			TreeMap<Integer, Pair<Student, Group>> stuff = 
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
						if (!stuff.containsKey((Integer)idea))
							stuff.put(ideaRank, new Pair<Student, Group>(s, large));
					}
				}
			}
			
			// If no students could be found who picked this idea, that's 
			// a problem.
			if (stuff.size() == 0) {
				problemGroups.add(small);
			} else {
				// Reassign the student who gave the idea the highest rank
				// to the new group.
				Pair<Student, Group> sg = stuff.firstEntry().getValue();
				sg.second.removeMember(sg.first);
				largeGroups.remove(sg.second);
				small.addMember(sg.first);
			}
		}
		return problemGroups;
	}
	
	private static void printIdeasGroups(IdeaSet ideas, 
			Collection<Student> students, Collection<Group> groups) {
		HashMap<Idea, Student> ideasStudents = new HashMap<Idea, Student>();
		for (Student s : students) {
			if (s.getProposedIdea() != null) 
				ideasStudents.put(s.getProposedIdea(), s);
		}
		HashMap<Idea, Group> ideasGroups = new HashMap<Idea, Group>();
		for (Group g : groups) {
			ideasGroups.put(g.idea, g);
		}
		TreeSet<Group> groupsSorted = new TreeSet<Group>(groups);

		for (Group g : groupsSorted) {
			System.out.println(g.getBriefString());
		}
		
		for (Idea idea : ideas.getIdeasSorted()) {
			System.out.printf("%3d - %2d: \"%s\"\n", idea.votes,
					idea.number, idea.name);
		}

		System.out.println("\n\n---------- Details ----------");
		for (Group g : groupsSorted) {
			System.out.println(g);
		}
		System.out.println("\n");
		String groupFormat = " (Group %s)";
		for (Idea idea : ideas.getIdeasSorted()) {
			String name = ideasStudents.get(idea).getName();
			name = name.replaceAll("([^,]*), (.*)", "$2 $1");
			System.out.printf("%3d - %2d: \"%s\" - %s%s\n", idea.votes,
					idea.number, idea.name, name,
					ideasGroups.containsKey(idea)
							? String.format(groupFormat, ideasGroups.get(idea).number)
							: "");
		}
	}
	
	/** Print log messages to the command line */
	public static void log(String message) {
		if (LOG) System.out.println(message);
	}
	
	/** Print debug messages to the command line */
	public static void debug(String message) {
		if (DEBUG) System.out.println(message);
	}
	
	/*
	private static void readStudentsIdeasMethod1(IdeaSet ideas, 
			HashSet<Student> students) throws IOException {
		String studentIdeaPath = FileSelectionDialog.showDialog(
				"Select File",
				"<html>Absolute path to CSV file with student names and idea names:" +
				"<br>(format must be idea number, student name, idea name)",
				"", false,
				new FileNameExtensionFilter("CSV files (.csv)", "csv"));
		if (studentIdeaPath == null)
			System.exit(0);
		String votingSheetPath = FileSelectionDialog.showDialog(
				"Select Directory", 
				"Absolute path to directory with voting sheets:",
				"", true, null);
		if (votingSheetPath == null)
			System.exit(0);

		CSVReader reader = new CSVReader(new FileReader(studentIdeaPath));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (nextLine.length != 3) {
				debug("Line of wrong length found: " + Arrays.toString(nextLine));
				continue;
			}
//			try {
//				//TODO decide actual ordering
//				int ideaNumber = Integer.parseInt(nextLine[0]);
//				String studentName = nextLine[1];
//				int 
//			}
		}
	}
	*/
}
