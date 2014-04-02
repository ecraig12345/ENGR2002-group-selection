package edu.ou.engr.engr2002.ideagroupselection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Group implements Comparable<Group> {
	/** Keeps track of the next group number to use */
	private static int nextGroupNumber = 1;
	
	/** The group's idea */
	public final Idea idea;
	/** The group number (NOT the idea number) */
	public final int number;
	/** The maximum number of group members */
	public final int maxMembers;
	/** The members of the group */
	private ArrayList<Student> members = new ArrayList<Student>();
	
	/**
	 * Creates a group with the given idea and leader. The group number will
	 * be the next one up from the previous group created.
	 * @param leader The leader of the group (if null, the "leader" will be
	 * the first member added outside the constructor)
	 * @param idea The group's idea
	 * @param maxMembers The maximum number of members
	 * @throws if maxMembers is < 1 or idea is null
	 */
	public Group(Student leader, Idea idea, int maxMembers) {
		if (maxMembers < 1)
			throw new IllegalArgumentException("maxMembers must be >= 1");
		if (idea == null)
			throw new IllegalArgumentException("idea must not be null");
		this.idea = idea;
		if (leader != null)
			members.add(leader);
		number = nextGroupNumber++;
		this.maxMembers = maxMembers;
	}
	
	/** Gets whether the group is full */
	public boolean isFull() {
		return members.size() == maxMembers;
	}
	
	/** Gets the size of the group */
	public int getSize() {
		return members.size();
	}
	
	/** 
	 * Adds a student to the group.
	 * @return false if the student is already in the group, true if the 
	 * student could be added
	 */
	public boolean addMember(Student student) {
		if (isFull())
			throw new IllegalStateException("The group is full.");
		if (members.contains(student))
			return false;
		return members.add(student);
	}
	
	/**
	 * Remove the given student from the group. The leader cannot be removed.
	 * @return true if the student was in the group and could be removed
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
		StringBuilder sb = new StringBuilder(256);
		sb.append("Group " + number + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("1) " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i)
			sb.append(String.format("%s) %s\n", i+1, members.get(i).getName()));
		return sb.toString();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(512);
		sb.append("Group " + number + "\n");
		sb.append("Idea " + idea + "\n");
		sb.append("* " + members.get(0).getName() + " - Leader\n");
		for (int i = 1; i < members.size(); ++i) {
			int rank = members.get(i).getRankOfIdea(idea.number);
			sb.append("* " + members.get(i).getName() + " - "
					+ (rank == 0 ? "n/a" : rank + ""));
			if (Main.DEBUG && members.get(i).didStudentVote())
				sb.append(" - " + members.get(i).getVoteString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/** Compares groups by group number */
	public int compareTo(Group other) {
		if (other == null || number < other.number) return -1;
		if (number > other.number) return 1;
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idea == null) ? 0 : idea.hashCode());
		result = prime * result + maxMembers;
		result = prime * result + number;
		result = prime * result + members.size();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if ((idea == null && other.idea != null)
				|| (members == null && other.members != null))
			return false;
		return idea.equals(other.idea) && maxMembers == other.maxMembers
				&& number == other.number
				&& members.size() == other.members.size();
	}
}
