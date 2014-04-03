package edu.ou.engr.engr2002.ideagroupselection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Represents an idea.
 * Equality and hash code are based on idea number.
 * Comparison is in descending order of votes.
 */
public class Idea implements Comparable<Idea> {
	public final String name;
	public final int number;
	public int votes;
	
	public Idea(String name, int number) {
		// remove * characters from idea names - could cause problems later
		name = name.replace('*', '.');
		this.name = name.replace('"', '\'');
		this.number = number;
	}
	
	/**
	 * Get the Student who proposed this idea, or null if no student in the
	 * collection proposed this idea (or the collection was null).
	 */
	public Student findProposer(Collection<Student> students) {
		if (students != null) {
			for (Student s : students) {
				if (this.equals(s.getProposedIdea()))
					return s;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return number;
	}

	/** Idea equality is based only on number */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return ((Idea) obj).number == this.number;
	}

	/** Sorts the ideas in descending order of votes */
	public int compareTo(Idea other) {
		if (other == null || other.votes < votes) return -1;
		if (other.votes > votes) return 1;
		return 0;
	}
	
	/** Returns votes - #: "idea name" */
	public String toShortString() {
		return String.format("%3d - %2d: \"%s\"", votes, number, name);
	}
	
	/** Returns votes - #: "idea name" - student name */
	public String toLongString(String studentName) {
		return toShortString() + " - " + studentName;
	}
	
	/** Returns #: idea name */
	@Override
	public String toString() {
		return number + ": " + name;
	}
	
	/**
	 * Represents a set of ideas, with convenience methods to add votes
	 * to an idea with a given number and get the top n ideas.
	 */
	public static class IdeaSet {
		/** Mapping from idea number to idea */
		private TreeMap<Integer, Idea> ideas = new TreeMap<Integer, Idea>();
		/** The ideas sorted by number of votes (this is cached so it doesn't
		 * have to be recalculated every time) */
		private ArrayList<Idea> ideasSorted = new ArrayList<Idea>();
		private boolean ideasModified = false;
		
		public IdeaSet() { }

		/** Gets the idea with the given number, or null if no idea matches. */
		public Idea get(int number) {
			return ideas.get(number);
		}
		
		/** Adds the given idea to the set */
		public void add(Idea idea) {
			// An idea has been added, so ideasSorted must be recalculated
			ideasModified = true;
			ideas.put(idea.number, idea);
		}
		
		/**
		 * Adds the given number of votes to the idea with the given number
		 * (does nothing if there is no idea with that number)
		 */
		public void addVotesToIdeaNumber(int ideaNum, int numVotes) {
			// An idea has been modified, so ideasSorted must be recalculated
			ideasModified = true;
			Idea idea = ideas.get(ideaNum);
			if (idea != null) idea.votes += numVotes;
		}
		
		/** Gets the top <code>number</code> ideas based on votes */
		public List<Idea> getTopIdeas(int number) {
			updateIdeasSorted();
			return ideasSorted.subList(0, number);
		}
		
		/**
		 * Gets an unmodifiable view of the list of ideas sorted by number
		 * of votes
		 */
		public List<Idea> getIdeasSorted() {
			updateIdeasSorted();
			return Collections.unmodifiableList(ideasSorted);
		}
		
		/** Updates the cached ideasSorted, if needed */
		private void updateIdeasSorted() {
			if (ideasModified) {
				ideasSorted = new ArrayList<Idea>(ideas.values());
				Collections.sort(ideasSorted);
				ideasModified = false;
			}
		}
		
		public String toString() {
			updateIdeasSorted();
			StringBuilder sb = new StringBuilder();
			for (Idea idea : ideasSorted)
				sb.append(idea.votes + " - " + idea + "\r\n");
			return sb.toString();
		}
	}
}
