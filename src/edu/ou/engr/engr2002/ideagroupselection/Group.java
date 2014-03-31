package edu.ou.engr.engr2002.ideagroupselection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group implements Comparable<Group> {
	
	private static int nextGroupNumber = 1;
	
	private Idea idea;
	private int groupNumber;
	private ArrayList<Student> members = new ArrayList<Student>();
	
	public Group(Student leader, Idea idea) {
		this.idea = idea;
		members.add(leader);
		groupNumber = nextGroupNumber++;
	}
	
	public Idea getIdea() {
		return idea;
	}
	
	public int getNumber() {
		return groupNumber;
	}
	
	public boolean isFull() {
		return members.size() == 5;
	}
	
	public int getSize() {
		return members.size();
	}
	
	public boolean addMember(Student student) {
//		if (isFull())
//			throw new IllegalStateException("The group is full.");
		if (members.contains(student))
			return false;
		members.add(student);
		return true;
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
			value += s.getRankOfIdea(idea.getNumber());
		}
		return value;
	}
	
	public String getBriefString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Group " + groupNumber + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("1) " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i)
			sb.append(String.format("%s) %s\n", i+1, members.get(i).getName()));
		return sb.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Group " + groupNumber + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("1) " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i) {
			int rank = members.get(i).getRankOfIdea(idea.getNumber());
			sb.append((i + 1) + ") " + members.get(i).getName() + " - "
					+ (rank == 0 ? "n/a" : rank + ""));
			if (Main.DEBUG && members.get(i).didStudentVote())
				sb.append(" - " + members.get(i).getVoteString());
			sb.append("\n");
		}
		return sb.toString();
	}

	public int compareTo(Group other) {
		if (other == null || groupNumber < other.groupNumber) return -1;
		if (groupNumber > other.groupNumber) return 1;
		return 0;
	}
}
