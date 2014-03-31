package edu.ou.engr.engr2002.ideagroupselection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group implements Comparable<Group> {
	public static final int MAX_MEMBERS = 5;
	
	/** Keeps track of the next group number to use */
	private static int nextGroupNumber = 1;
	
	/** The group's idea */
	public final Idea idea;
	/** The group number (NOT the idea number) */
	public final int number;
	/** The members of the group */
	private ArrayList<Student> members = new ArrayList<Student>();
	
	/**
	 * Creates a group with the given idea and leader. The group number will
	 * be the next one up from the previous group created.
	 */
	public Group(Student leader, Idea idea) {
		this.idea = idea;
		members.add(leader);
		number = nextGroupNumber++;
	}
	
	/** Gets whether the group is full */
	public boolean isFull() {
		return members.size() == MAX_MEMBERS;
	}
	
	/** Gets the size of the group */
	public int getSize() {
		return members.size();
	}
	
	/** 
	 * Adds a student to the group.
	 * TODO Does not check if the group is full - why?
	 * @return false if the student is already in the group, true if the 
	 * student could be added
	 */
	public boolean addMember(Student student) {
//		if (isFull())
//			throw new IllegalStateException("The group is full.");
		if (members.contains(student))
			return false;
		return members.add(student);
	}
	
	/**
	 * Remove the given student from the group. The leader cannot be removed.
	 * @param student
	 * @return
	 */
	public boolean removeMember(Student student) {
		if (student.equals(members.get(0)))
			return false;
		return members.remove(student);
	}
	
	/** Gets an unmodifiable list view of the members of the group */
	public List<Student> getMemberView() {
		return Collections.unmodifiableList(members);
	}
	
	/**
	 * Gets the "value" of the group, based on the priorities at which group
	 * members other than the leader ranked the group's idea.
	 * Lower values are better. (Note: as much as any computation with n <= 5
	 * can be expensive, this is expensive, and the value is not cached.)
	 * @return A value indicating how "good" the group is (lower values are better)
	 */
	public int getGroupValue() {
		int value = 0;
		for (Student s : members.subList(1, members.size())) {
			value += s.getRankOfIdea(idea.number);
		}
		return value;
	}
	
	public String getBriefString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Group " + number + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("1) " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i)
			sb.append(String.format("%s) %s\n", i+1, members.get(i).getName()));
		return sb.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Group " + number + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("1) " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i) {
			int rank = members.get(i).getRankOfIdea(idea.number);
			sb.append((i + 1) + ") " + members.get(i).getName() + " - "
					+ (rank == 0 ? "n/a" : rank + ""));
			if (Main.DEBUG && members.get(i).didStudentVote())
				sb.append(" - " + members.get(i).getVoteString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public int compareTo(Group other) {
		if (other == null || number < other.number) return -1;
		if (number > other.number) return 1;
		return 0;
	}
}
