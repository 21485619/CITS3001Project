import java.util.*;


/**
 * @author Wesley
 *
 */
public class SelfishAgent implements Agent{

	private Scanner scanner;
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
	
	
	public SelfishAgent(){
		spyState = new HashMap<Character, Double>();
	}  


	/**
	* Reports the current status, including players name, the name of all players, the names of the spies (if known), the mission number and the number of failed missions
	* @param name a string consisting of a single letter, the agent's names.
	* @param players a string consisting of one letter for everyone in the game.
	* @param spies a String consisting of the latter name of each spy, if the agent is a spy, or n questions marks where n is the number of spies allocated; this should be sufficient for the agent to determine if they are a spy or not. 
	* @param mission the next mission to be launched
	* @param failures the number of failed missions
	* */
	public void get_status(String name, String players, String spies, int mission, int failures){
		for(int i= 0; i < players.length(); i++){
			spyState.put(players.charAt(i), 0.5);
		}
		names = spyState.keySet();
		this.name = name.charAt(0);
		this.players = players;
		this.spies = spies;
		if(spies.indexOf(name)!=-1)spy = true;
		if(!spy) spyState.put(this.name, 0.0);
		/*for(char c : players.toCharArray())
		{
			spyState.put(c,1.0);
		}*/
		for(int i = 0; i < spies.length(); i++){
			givenSpies.add(spies.charAt(i));
		}
		if(spy) //If a spy, create an list of all spies
		{
			/*for(char c : spies.toCharArray())
				if(!spyState.containsKey(c))
					spyState.put(c,1);*/
		}
		numSpies = (int) Math.floor((players.length()-1)/3) + 1;	//Calculates the number of spies in the game based on the number of players. Trust me.	
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
		Set <Character> nameList = spyState.keySet();
		nameList.remove(name);
		nom += likelyInnocent(number - 1, nameList);
		nameList = spyState.keySet();
		return nom;
	}

	private String likelySpies(int number, Set <Character> nameList){
		String likelySpies = new String();
		for(int i = 0; i < number; i++){	//Gets the highest value in the set, then removes the highest value from the set
			char maxChar = getHighestProbability(nameList);
			likelySpies += maxChar;
			nameList.remove(maxChar);
		}
		return likelySpies;
	}
	
	private String likelyInnocent(int number, Set <Character> nameList){
		String likelyInnocent = new String();
		for(int i = 0; i < number; i++){	//Gets the lowest value in the set, then removes the lowest value from the set
			char minChar = getLowestProbability(nameList);
			likelyInnocent += minChar;
			nameList.remove(minChar);
		}
		return likelyInnocent;
	}
	
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
	* Provides information of a given mission.
	* @param leader the leader who proposed the mission
	* @param mission a String containing the names of all the agents in the mission 
	**/
	public void get_ProposedMission(String leader, String mission){
		this.proposed = mission;
	}

	/**
	* Gets an agents vote on the last reported mission
	* @return true, if the agent votes for the mission, false, if they vote against it.
	* */
	public boolean do_Vote(){
		/*if(proposed.indexOf(name) == -1)	//if mission does not contain self
			return false; 								//disapprove
		int spynum = 0;
		for(char c : proposed.toCharArray()) //Count known spies on proposed mission
		{
			if(spyState.get(c) != 0)
				spynum++;
		}
		if(spy)	//if agent is a spy
		{
			if(spynum == proposed.length()) //Do not approve mission containing all spies
				return false;
			else
				return true;
		}
		else	//if agent is resistance
		{
			return (spynum == 0); //Only approve missions containing no known spies
		}*/
		boolean vote = false;
		if(this.spy) vote = spyVote();
		else vote = resistanceVote();
		return vote;
	}  
	
	private boolean spyVote(){
		int spiesOnMission = 0;	//Votes true if the mission has a spy on it
		for(int i = 0; i < mission.length(); i++){
			for(char s : givenSpies){
				if(mission.charAt(i) == s) spiesOnMission++;
				break;
			}
		}
		if(missionNumber == 4 && players.length() > 6) return spiesOnMission > 1;
		else return spiesOnMission > 0;
	}
	
	private boolean resistanceVote(){
		boolean vote = true;
		Set<Character> nameList = spyState.keySet();
		nameList.remove(name);	//Make sure not to consider ourselves, since we are not a spy
		String mostSpy = likelySpies(numSpies, nameList);
		for(int i = 0; i < mission.length(); i++){
			for(int j = 0; j < mostSpy.length(); j++){
				if(mission.charAt(i) == mostSpy.charAt(j)) vote = false;	//If a player on the mission is one of the most likely spies, vote no
				break;
			}
			if(!vote) break;
		}		
		return vote;
	}

  /**
   * Reports the votes for the previous mission
   * @param yays the names of the agents who voted for the mission
   **/
  public void get_Votes(String yays){
    
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
   * @return true if agent betrays, false otherwise
   **/
  public boolean do_Betray(){
	  
		if(!spy) return false;
		int spynum = 0;
		for(int i = 0; i < mission.length(); i++) //get count of spies on mission with agent
		{
			if(spies.contains(String.valueOf(mission.charAt(i)))) spynum++;
		}
		if(spynum < 2)return true;
		
		return (spynum < mission.length()); //Betray only if the mission does have all spies
   }  

  /**
   * Reports the number of people who betrayed the mission
   * @param traitors the number of people on the mission who chose to betray (0 for success, greater than 0 for failure)
   **/
  public void get_Traitors(int traitors){
		this.traitors = traitors;
		if(traitors == mission.length()) //If everyone betrayed on mission, everyone must be a spy
		{
			for(char c : mission.toCharArray())
			{
				spyState.put(c,1.0);
			}
		}
  }


  /**
   * Optional method to accuse other Agents of being spies. 
   * Default action should return the empty String. 
   * Convention suggests that this method only return a non-empty string when the accuser is sure that the accused is a spy.
   * Of course convention can be ignored.
   * @return a string containing the name of each accused agent. 
   * */
  public String do_Accuse(){
	  String accused = "";
	  for(char c : names){
		  if(spyState.get(c) == 0.0) accused +=c;
	  }
		return accused;
  }

  /**
   * Optional method to process an accusation.
   * @param accuser the name of the agent making the accusation.
   * @param accused the names of the Agents being Accused, concatenated in a String.
   * */
  public void get_Accusation(String accuser, String accused){

  }

  private void updateWentAgents(){
	  /*TODO: 
	   * Update the probabilities for each agent that did not go on the mission
	   * Make sure that it works
	   * Make sure that it works in the event of mission success 
	   */
	  
	  char[] went = new char[this.mission.length()];
	  char[] stayed = new char[spyState.size() - this.mission.length()];
	  for(int i = 0; i < went.length; i++){
		  went[i] = this.mission.charAt(i);
	  }
	  for(int i = 0; i < stayed.length; i++){
		  for(Character agent : this.names){
			  if(mission.indexOf(agent) != -1) stayed[i] = agent;
		  }
	  }

	  char[][] allCombos = new char[nCr(went.length, traitors)][traitors];
	  char[] combination = new char[traitors];
	  getCombinations(went,combination, 0, went.length - 1, 0, traitors, allCombos, 0);
	  double bProbability = findPB(allCombos, went);
	  for(int i = 0; i < went.length; i++){ //Update the probability of all agents that went on the mission
		  double bGivenA = pBGivenA(allCombos, went[i], went); 
		  double aProbability = spyState.get(went[i]);
		  double newProbability = aProbability * bGivenA / bProbability;
		  spyState.put(went[i], newProbability);
	  }
	  /*for(char a : names){
		  aProbability = spyState.get(a);
		  
	  }*/
	  
	  
  }
  private int nCr(int n, int r){
	  if(r == 0) return 1;
	  else return nCr(n, r-1) * (n-r)/(r+1);
  }
  
  private void getCombinations(char[] went, char[] combination, int start, int end, int index, int r, char[][]results, int row){
	  if(index == r){
		  for (int i = 0; i < r; i++){
			  results[row][i] = combination[i];
		  }
		  row++;
		  return;
	  }
	  
	  for(int i = start; i <= end && end-i+1 >= r-index; i++){
		  combination[index] = went[i];
		  getCombinations(went, combination, i+1, end, index +1, r, results, row);
	  }
  }
	   
  private double findPB(char[][] combos, char[] went){
	  double totalprobability = 0.0;
	  for(int i = 0; i < combos.length; i++){ //iterate through all rows of combinations to find bProbability
		  double probabilityTotal = 1.0;
		  for(int j = 0; j < went.length; j++){ //find the probability multiplier of each agent
			  boolean inCombo = false;
			  for(int k = 0; k < combos[i].length; k++){		//Check if the agent is a spy in this possible combination
				  if(went[j] == combos[i][k]) inCombo = true;
			  }
			  if(inCombo) probabilityTotal = probabilityTotal * spyState.get(went[j]); 	//If agent is a spy in this combo, multiply by the chance they are a spy 
			  else probabilityTotal = probabilityTotal * (1 - spyState.get(went[j]));	//If agent is not a spy in this combo, multiply by inverse
		  }
		  totalprobability += probabilityTotal;		//The probability of each row occurring is added to the running total		  
	  }
	  
	  return totalprobability;
  }
  
  private double pBGivenA(char[][] combos, char tested, char[] went){  
	  double totalprobability = 0.0;		//Overall probability
	  for(int row = 0; row < combos.length; row++){		//Iterate through the list of combinations
		  boolean containsTested = false;
		  double comboProbability = 1.0; 	//Probability for current combination
		  for(int i = 0; i < combos[row].length; i++){	//Scan through the current combination for the tested char
			  if(combos[row][i] == tested) containsTested = true;
		  }
		  if(!containsTested){
			  comboProbability = 0.0;
		  }else{
			  for(int i = 0; i < went.length; i++){		//Calculate probability of all agents that went on the mission
				  double agentProbability = 1.0;
				  if(went[i] != tested){	//tested agent is assumed to be a spy
					  boolean isSpy = false;
					  for(int j = 0; j < combos[row].length;  j++){		//Check if the agent is a spy in this combonation
						  if(combos[row][j] == went [i]) isSpy = true;
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
  
  private void updateNonParticipants(char[] went, int traitors){
	  int spiesLeft = numSpies - traitors;
	  if(spiesLeft == 0){
		  for(int i = 0; i < players.length(); i++){
			  boolean inWent = false;
			  for(int j = 0; j < went.length; j++){
				  if(players.charAt(i) == went[j]){
					  inWent = true;
					  break;
				  }
			  }
			  if(!inWent)
				  spyState.put(players.charAt(i), 0.0);
			  else spyState.put(players.charAt(i), 1.0); 
		  }
		  return;
	  }
	  //double remainingSpyProbability = spiesLeft/(players.length() - went.length); 
	  

  }
  
}