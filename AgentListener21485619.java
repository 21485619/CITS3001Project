import java.util.*;


/**
 * @author Wesley Coleman 21485619
 * @author Wen Tan  21503781
 *
 */
public class AgentListener21485619 implements Agent{

	private String players = "";
	private String spies = "";
	private char name;
	private boolean spy = false;
	private Map<Character,Double> spyState;
	private Set<Character> names;
	private Set<Character> givenSpies;
	private String proposed="";
	private String mission="";
	private int traitors=0;
	private int numSpies;
	private int missionNumber;
	private int failures;
	
	private Character lastLeader;
	
	public AgentListener21485619(){
		spyState = new HashMap<Character, Double>();
	}  


	/**
	* Reports the current status, including players name, the name of all players, the names of the spies (if known), the mission number and the number of failed missions
	* If it is the first time this method is called (indicated by spyState being empty), it will initialise the global variables
	* If it isn't the first time this method is calles, then it will only update this.missionNumber and this.failures
	* @param name a string consisting of a single letter, the agent's names.
	* @param players a string consisting of one letter for everyone in the game.
	* @param spies a String consisting of the latter name of each spy, if the agent is a spy, or n questions marks where n is the number of spies allocated; this should be sufficient for the agent to determine if they are a spy or not. 
	* @param mission the next mission to be launched
	* @param failures the number of failed missions
	* */
	public void get_status(String name, String players, String spies, int mission, int failures){
		if(spyState.isEmpty()){
			givenSpies = new HashSet<Character>();
			numSpies = (int) Math.floor((players.length()-1)/3) + 1;//Calculates the number of spies in the game based on the number of players.
			for(int i= 0; i < players.length(); i++){
				spyState.put(players.charAt(i),(double) numSpies/(players.length() - 1));	//Default probability is (number of spies)/(number of players)
			}
			names = spyState.keySet();
			this.name = name.charAt(0);
			this.players = players;
			this.spies = spies;
			if(spies.indexOf(name)!=-1)spy = true;
			if(!spy) spyState.put(this.name, 0.0);
			if(spy) //If a spy, create an list of all spies
			{
				for(int i = 0; i < spies.length(); i++){
					givenSpies.add(spies.charAt(i));
				}		
			}	
		}
		this.failures = failures;
		missionNumber = mission;
	}
  
	/**
	* Nominates a group of agents to go on a mission.
	* If the String does not correspond to a legitimate mission (<i>number</i> of distinct agents, in a String), 
	* a default nomination of the first <i>number</i> agents (in alphabetical order) will be reported, as if this was what the agent nominated.
	* @param number the number of agents to be sent on the mission
	* @return a String containing the names of all the agents in a mission
	* */
	public String do_Nominate(int number){
		String nom = new String();
		nom = nom + name;	//Always nominate self
		Set <Character> nameList = new HashSet<Character>();
		nameList.addAll(spyState.keySet());
		nameList.remove(name);
		nom += likelyInnocent(number - 1, nameList);
		return nom;
	}

	/**
	 * Determines which players are the most likely to be spies based on their probabilities
	 * @param number the number of players to put in the string
	 * @param nameList a list of the names of all players
	 * @return a String containing the names of the players most likely to be spies
	 */
	private String likelySpies(int number, Set <Character> nameList){
		String likelySpies = new String();
		for(int i = 0; i < number; i++){	//Gets the highest value in the set, then removes the highest value from the set
			char maxChar = getHighestProbability(nameList);
			likelySpies += maxChar;
			nameList.remove(maxChar);
		}
		return likelySpies;
	}
	
	/**
	 * Determines which players are the least likely to be spies based on their probabilities
	 * @param number the number of players to put in the string
	 * @param nameList a list of the names of all players
	 * @return a String containing the names of the players most likely to be spies
	 */
	private String likelyInnocent(int number, Set <Character> nameList){
		String likelyInnocent = new String();
		for(int i = 0; i < number; i++){	//Gets the lowest value in the set, then removes the lowest value from the set
			char minChar = getLowestProbability(nameList);
			likelyInnocent += minChar;
			nameList.remove(minChar);
		}
		return likelyInnocent;
	}
	
	/**
	 * Accepts a list of names, and returns the name with the lowest likelyhood of being a spy according to spyState
	 * @param nameList a list of names to be considered. Can be a subset of the list of all players
	 * @return the name of the player from the nameList with the lowest chance of being a spy
	 */
	private char getLowestProbability(Set<Character> nameList){
		double minimum = 10.0;		//Expected values are < 1
		char currentLowest = '\0';	//Default
		for(char c : nameList){
			double currentNum = spyState.get(c);
			if(currentNum < minimum){
				currentLowest = c;
				minimum = currentNum;
			}
		}		
		return currentLowest;
	}
	
	/**
	 * Accepts a list of names, and returns the name with the highest likelyhood of being a spy according to spyState
	 * @param nameList a list of names to be considered. Can be a subset of the list of all players
	 * @return the name of the player from the nameList with the highest chance of being a spy
	 */
	private char getHighestProbability(Set<Character> nameList){
		double maximum = -10.0;		//Expected values are > 0
		char currentLowest = '\0';	//Default
		for(char c : nameList){
			double currentNum = spyState.get(c);
			if(currentNum > maximum){
				currentLowest = c;
				maximum = currentNum;
			}
		}		
		return currentLowest;
	}
	
	/**
	* Provides information of a given mission
	* Stores the name of the player who proposed the mission
	* @param leader the leader who proposed the mission
	* @param mission a String containing the names of all the agents in the mission 
	**/
	public void get_ProposedMission(String leader, String mission){
		this.proposed = mission;
		lastLeader = leader.charAt(0);	//stores the proposer of the last proposed mission
	}

	/**
	* Gets an agents vote on the last reported mission
	* Determines if the agent is a spy or not, and calls the appropriate voting method
	* @return true, if the agent votes for the mission, false, if they vote against it.
	* */
	public boolean do_Vote(){
		boolean vote = false;
		if(this.spy) vote = spyVote();
		else vote = resistanceVote();
		return vote;
	}  
	
	/**
	 * Determines if the agent should vote for or against the current proposal.
	 * To be used if the agent is a spy
	 * @return true, if the agent votes for the mission, false, if they vote against it
	 */
	private boolean spyVote(){
		int spiesOnMission = 0;	//Votes true if the mission has a spy on it
		for(int i = 0; i < proposed.length(); i++){
			for(char s : givenSpies){
				if(proposed.charAt(i) == s) spiesOnMission++;
				break;
			}
		}
		if(missionNumber == 4 && players.length() > 6) return spiesOnMission > 1;
		else return spiesOnMission > 0;
	}
	
	/**
	 * Determines if the agent should vote for or against the current proposal
	 * To be used if the agent is not a spy, and is instead a normal member of the resistance
	 * @return true, if the agent votes for the mission, false, if they vote against it
	 */
	private boolean resistanceVote(){
		boolean vote = true;
		Set<Character> nameList = new HashSet<Character>();
		nameList.addAll(spyState.keySet());
		nameList.remove(name);	//Make sure not to consider ourselves, since we are not a spy
		String mostSpy = likelySpies(numSpies, nameList);
		for(int i = 0; i < mission.length(); i++){
			if(spyState.get(mission.charAt(i)) == 1.0) vote = false; 	//If mission contains confirmed spy, vote no
			for(int j = 0; j < mostSpy.length();){
				if(mission.charAt(i) == mostSpy.charAt(j)) vote = false;	//If a player on the mission is one of the most likely spies, vote no
				break;
			}
			if(!vote) break;
		}		
		return vote;
	}

  /**
   * Reports the votes for the previous mission
   * Checks to see if a leader nominates an agent that is considered a spy according to mostSpy()
   * If they do, for each spy nominated, the leader's probability is moved 10% closer to 1.0
   * @param yays the names of the agents who voted for the mission
   **/
  public void get_Votes(String yays){
	  if (yays.length()<((players.length())/2)){ // if vote didnt pass
		  Set<Character> nameList = new HashSet<Character>();
		  nameList.addAll(spyState.keySet());
		  String mostSpy = likelySpies(numSpies, nameList);
		  for(int i = 0; i < mostSpy.length(); i++){
			if(lastLeader == mostSpy.charAt(i)){	//if leader considerd a spy
			  	double leaderP = spyState.get(lastLeader);	//store leader P
			  	double leaderChange = (double)(1.0 - leaderP)/10;	//Moves probability 10% closer to 1.0
				leaderP += leaderChange;
			  	spyState.put(lastLeader,leaderP);	//store new P
		    }					
		  }
	  }	   
  }
  /**
   * Reports the agents being sent on a mission.
   * Should be able to be infered from tell_ProposedMission and tell_Votes, but incldued for completeness.
   * @param mission the Agents being sent on a mission
   **/
  public void get_Mission(String mission){
		this.mission = mission;
  }

  /**
   * Agent chooses to betray or not.
   * Never betrays if the agent is not a spy
   * @return true if agent betrays, false otherwise
   **/
  public boolean do_Betray(){	  
		if(!spy) return false;
		if(missionNumber == 1) return false;
		if(failures == 2) return true;	//If 1 fail away from winning, always fail
		int spynum = 0;
		for(int i = 0; i < mission.length(); i++){ //get count of spies on mission with agent
			if(spies.contains(String.valueOf(mission.charAt(i)))) spynum++;
		}		
		if(missionNumber == 4 && players.length() > 6){
			if(spynum < 2) return false;	//If not enough spies to fail mission, do not betray
			if(spynum == 2) return true;
			else return Math.random() > spyState.get(name);	//In the case of extra spies, chance to betray is the inverse of the probability of itself being a spy
		}else{
			if(spynum < 2) return true;
			else return Math.random() > spyState.get(name);
		}
   }  

  /**
   * Reports the number of people who betrayed the mission
   * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
   **/
  public void get_Traitors(int traitors){
		this.traitors = traitors;
		if(traitors == mission.length()) //If everyone betrayed on mission, everyone must be a spy
		{
			for(char c : mission.toCharArray()){
				spyState.put(c,1.0);
			}
		}
  }


  /**
   * Reports a string containing a list of any player this agent will accuse of being a spy
   * If not a spy, the agent will only accuse players it is certain are spies
   * If a spy, the agent will accuse the 2 players who aren't spies who have the highest probabilities
   * @return a string containing the name of each accused agent. 
   * */
  public String do_Accuse(){
	  updateWentAgents();
	  String accused = "";
	  if(!spy){
		  for(char c : names){
			  if(spyState.get(c) == 1.0) accused +=c;
	 	}
		  return accused;
	  }else{
		  Set<Character> nameList = new HashSet<Character>();
		  nameList.addAll(spyState.keySet());
		  for(char c : givenSpies){
			  nameList.remove(c);	//Removes the spies from contention
		  }
		  accused = likelySpies(2, nameList);
		  return accused;
	  }

  }

  /**
   * Optional method to process an accusation.
   * Checks each accusation string. If the accuser's probability is higher than the probability of the accused, then their probability is moved 10% closer to 1.0 
   * @param accuser the name of the agent making the accusation.
   * @param accused the names of the Agents being Accused, concatenated in a String.
   * */
  public void get_Accusation(String accuser, String accused){
	  char accuserChar = accuser.charAt(0);
	  for(int i = 0; i < accused.length(); i++){
		  char accusedChar = accused.charAt(i);
		  double accuserP = spyState.get(accuserChar);	//get accuser P
		  double accusedP = spyState.get(accusedChar);	//get accused P
		  if (accuserP > accusedP){	//if the accuser is more likely of being spy than the accused
			  double accuserChange = (double)(1.0 - accuserP)/10;	//Moves probability 10% closer to 1.0
			  accuserP += accuserChange;
			spyState.put(accuserChar,accuserP);		//increase accuser's P
		  }
	  }
  }

  /**
 * A method used to update the probabilities list stored in spyState
 * Each player's probability is updated according to Bayesian Probability
 * To be called after every mission, i.e. during do_Accuse()
 */
private void updateWentAgents(){
 	  char[] went = new char[this.mission.length()];
	  char[] stayed = new char[spyState.size() - this.mission.length()];
	  Set<Character> names = new HashSet<Character>();
	  names.addAll(this.names);
	  int wentI = 0;
	  int stayedJ = 0;
	  for(char name : names){
		  if(mission.indexOf(name) != -1){
			  went[wentI] = name;
			  wentI++;
		  }else{
			  stayed[stayedJ] = name;
			  stayedJ++;
		  }
	  }
	  LinkedList<char[]> allCombos = new LinkedList<char[]>();
	  char[] combination = new char[traitors];
	  getCombinations(allCombos, went, went.length, traitors, 0, combination, 0);
	  double bProbability = findPB(allCombos, went);
	  for(int i = 0; i < went.length; i++){ //Update the probability of all agents that went on the mission
		  double aProbability = spyState.get(went[i]);
		  if(aProbability >= 1) continue;		//If a player is confirmed to be a spy, then their probability stays at 1
		  double bGivenA = pBGivenA(allCombos, went[i], went); 
		  double newProbability = aProbability * bGivenA / bProbability;
		  if(newProbability == 0) newProbability = aProbability/2;		//Causes players to not gain immunity after one successful round
		  spyState.put(went[i], newProbability);
	  }
	  updateNonParticipants(went, traitors);
	  return;
  }
    
  /**
   * Creates a LinkedList containing every possible unique arrangement of spies on a given mission, based on the number of traitors
   * Used recursively
   * @param results the output List containing all of the combinations
   * @param input a char array containing the names of all players who went on the mission
   * @param end an int containing the index of the final element of the input array
   * @param numTraitors the number of players in each combination produced by the algorithm
   * @param index the algorithm's current position in the combination that it is currently creating
   * @param combination an array containing the unique combination to be added to the results list
   * @param start an int referring to the starting position of the algorithm in the input array
   */
private void getCombinations(LinkedList<char[]> results, char[] input, int end, int numTraitors, int index, char[] combination, int start){
	  if(index == numTraitors){
		  char[] comboCopy = new char[combination.length];
		  System.arraycopy(combination, 0, comboCopy, 0, combination.length);
		  results.add(comboCopy);
		  return;
	  }
	  if(start >= end) return;
	  combination[index] = input[start];
	  getCombinations(results, input, end, numTraitors, index + 1, combination, start+1);
	  getCombinations(results, input, end, numTraitors, index, combination, start+1);  
  }
	   
  /**
   * Finds the probability P(B) to be used in the calculation of the Bayesian probability for each player
   * Accepts all possible combinations of spies on the current mission
   * Multiplies the probabilities associated with each player in each combination together
   * Once the product of all players' probabilities in the current combination is found, it is summed with the product of every other possible combination
   * @param combos a list of all possible combinations of spies on the mission
   * @param went, the names of all players who went on the mission
   * @return P(B) as a double < 1
   */
private double findPB(LinkedList<char[]> combos, char[] went){
	  double totalprobability = 0.0;
	  for(char[] i : combos){ //iterate through all rows of combinations to find bProbability
		  double probabilityTotal = 1.0;
		  for(int j = 0; j < went.length; j++){ //find the probability multiplier of each agent
			  boolean inCombo = false;
			  for(int k = 0; k < i.length; k++){		//Check if the agent is a spy in this possible combination
				  if(went[j] == i[k]) inCombo = true;
			  }
			  if(inCombo) probabilityTotal = probabilityTotal * spyState.get(went[j]); 	//If agent is a spy in this combo, multiply by the chance they are a spy 
			  else probabilityTotal = probabilityTotal * (1 - spyState.get(went[j]));	//If agent is not a spy in this combo, multiply by inverse
		  }
		  totalprobability += probabilityTotal;		//The probability of each row occurring is added to the running total		  
	  }
	  
	  return totalprobability;
  }
  
  /**
   * Calculates the probability P(B|A) to be used in the calculation of the Bayesian probability for each player
   * Similar to findPB(), except that it only considers combinations that include the currently examined player
   * @param combos a list of all possible combinations of spies from the current mission
   * @param tested the name of the player whose probability is going to be updated
   * @param went the names of all players who went on the mission
   * @return P(B|A) as a double < 1
   */
private double pBGivenA(LinkedList<char[]> combos, char tested, char[] went){  
	  double totalprobability = 0.0;		//Overall probability
	  for(char[] c : combos){		//Iterate through the list of combinations
		  boolean containsTested = false;
		  double comboProbability = 1.0; 	//Probability for current combination
		  for(int i = 0; i < c.length; i++){	//Scan through the current combination for the tested char
			  if(c[i] == tested) containsTested = true;
		  }
		  if(!containsTested){
			  comboProbability = 0.0;
		  }else{
			  for(int i = 0; i < went.length; i++){		//Calculate probability of all agents that went on the mission
				  double agentProbability = 1.0;
				  if(went[i] != tested){	//tested agent is assumed to be a spy
					  boolean isSpy = false;
					  for(int j = 0; j < c.length;  j++){		//Check if the agent is a spy in this combonation
						  if(c[j] == went [i]) isSpy = true;
					  }
					  if(isSpy) agentProbability = spyState.get(went[i]);	//Gets probability that agent is a spy
					  else agentProbability = 1 - spyState.get(went[i]);	//Gets probability that agent is not a spy
				  }
				  comboProbability = comboProbability * agentProbability;	//
			  }
		  }
		  totalprobability += comboProbability;	//Adds the probability of the current combination to the running total
	  }
	  return totalprobability;
  }
  
  /**
   * Updates the probabilites of all players who did not go on the mission
   * Determines if the probabilities of these players should go up or down based on the number of traitors compared to the number of unaccounted for spies
   * Scales the probabilities of the remaining players by a factor based on the number of unaccounted for spies, and the number of players who did not go on the mission
   * @param went a list of the names of the players who went on the mission
   * @param traitors the number of spies who betrayed the mission
   */
private void updateNonParticipants(char[] went, int traitors){
	  int spiesLeft = numSpies - traitors;
	  if(spiesLeft == 0){
		  for(char c: spyState.keySet()){
			  boolean inWent = false;
			  for(int i = 0; i < went.length; i++){
				  if(c == went[i]){
					  inWent = true; 
					  break;
				  }
			  }
			  if(!inWent)spyState.put(c, 0.0);
			  else spyState.put(c, 1.0); 
		  }
		  return;
	  }
	  int staying = players.length() - went.length;
	  int denom = spiesLeft*staying;
	  double multiplyer = 1.0;
	  double stayingRatio = (double) spiesLeft/staying;
	  double goingRatio = (double) traitors/went.length;
	  if(stayingRatio > goingRatio) multiplyer = (double)(denom + 1)/denom;
	  if(stayingRatio < goingRatio) multiplyer = (double)(denom - 1)/denom;
	  if(multiplyer!=1){
		  for(char c : spyState.keySet()){
			  boolean inWent = false;
			  for(int i = 0; i < went.length; i++){
				  if(c == went[i]){
					  inWent = true;
					  break;
				  }
			  }
			  double oldP = spyState.get(c);
			  if(!inWent && oldP != 1){				  
				  double newP = oldP * multiplyer;
				  spyState.put(c, newP);
			  }
		  }
	  }
	  return;
  }
  
}