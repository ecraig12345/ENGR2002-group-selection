package edu.ou.engr.engr2002.ideagroupselection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * TODO full doc
 * 
 * Equality and hash code are based on idea number.
 * Comparison is in descending order of votes.
 * 
 * @author Elizabeth
 *
 */
public class Idea implements Comparable<Idea> {
	private String name;
	private int number;
	private int votes;
	
	public Idea(String name, int number) {
		this.name = name;
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}
	
	public int getVotes() {
		return votes;
	}
	
	private void addVotes(int votes) {
		this.votes += votes;
	}
	
	public Student findProposer(Collection<Student> students) {
		for (Student s : students) {
			if (this.equals(s.getProposedIdea()))
				return s;
		}
		return null;
	}

	@Override
	public int hashCode() {
		return number;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Idea other = (Idea) obj;
		return other.number == this.number;
	}

	/**
	 * Sorts the ideas in descending order of votes
	 */
	public int compareTo(Idea other) {
		if (other == null || other.votes < votes) return -1;
		if (other.votes > votes) return 1;
		return 0;
	}
	
	public String toString() {
		return number + ": " + name;
	}
	
	public static class IdeaSet {
		private TreeMap<Integer, Idea> ideas = new TreeMap<Integer, Idea>();
		private ArrayList<Idea> ideasSorted = new ArrayList<Idea>();
		boolean ideasModified = false;
		
		public IdeaSet() {
		}
		
		public Idea get(int number) {
			return ideas.get(number);
		}
		
		public void add(Idea idea) {
			ideasModified = true;
			ideas.put(idea.number, idea);
		}
		
		public void addVotesToIdeaNumber(int ideaNum, int numVotes) {
			ideasModified = true;
			Idea idea = ideas.get(ideaNum);
			if (idea != null) idea.addVotes(numVotes);
		}
		
		public List<Idea> getTopIdeas(int number) {
			updateIdeasSorted();
			return ideasSorted.subList(0, number);
		}
		
		public List<Idea> getIdeasSorted() {
			updateIdeasSorted();
			return Collections.unmodifiableList(ideasSorted);
		}
		
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
				sb.append(idea.getVotes() + " - " + idea + "\n");
			return sb.toString();
		}
	}
}
