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
	private static final int NUM_GROUPS = 9;
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
		HashMap<Integer, Group> groups = makeGroups(ideas, remainingStudents);
		// Put the remaining students in groups
		groupStudents(groups, remainingStudents);
		
		printIdeasGroups(ideas, students, groups.values());
	}


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
	 * TODO full doc
	 * pre: ideas is sorted
	 * post: leaders are removed from students
	 * @param ideas
	 * @param students
	 * @return
	 */
	private static HashMap<Integer, Group> makeGroups(IdeaSet ideas,
			HashSet<Student> students) {
		List<Idea> topIdeas = ideas.getTopIdeas(NUM_GROUPS);
		HashMap<Integer, Group> groups = new HashMap<Integer, Group>(NUM_GROUPS);
		HashSet<Student> leaders = new HashSet<Student>();
		// Find the students who proposed the top ideas, and make groups with
		// those students as leaders.
		for (Student s : students) {
			Idea idea = s.getProposedIdea();
			if (idea != null && topIdeas.contains(idea)) {
				groups.put(idea.getNumber(), new Group(s, idea));
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
	
	private static HashSet<Group> resolveGroupSizes(HashSet<Group> smallGroups,
			HashSet<Group> largeGroups) {
		HashSet<Group> problemGroups = new HashSet<Group>();
		// For each group that is too small...
		for (Group small : smallGroups) {
			// First see if students from the other groups ranked that idea.
			int idea = small.getIdea().getNumber();
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
			ideasGroups.put(g.getIdea(), g);
		}
		TreeSet<Group> groupsSorted = new TreeSet<Group>(groups);

		for (Group g : groupsSorted) {
			System.out.println(g.getBriefString());
		}
		
		for (Idea idea : ideas.getIdeasSorted()) {
			System.out.printf("%3d - %2d: \"%s\"\n", idea.getVotes(),
					idea.getNumber(), idea.getName());
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
			System.out.printf("%3d - %2d: \"%s\" - %s%s\n", idea.getVotes(),
					idea.getNumber(), idea.getName(), name,
					ideasGroups.containsKey(idea)
							? String.format(groupFormat, ideasGroups.get(idea).getNumber())
							: "");
		}
	}
	
	public static void log(String message) {
		if (LOG) System.out.println(message);
	}
	
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
