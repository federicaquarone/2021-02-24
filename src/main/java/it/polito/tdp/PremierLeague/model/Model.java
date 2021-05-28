package it.polito.tdp.PremierLeague.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer,Player> idMap;
	
	public Model() {
		this.dao= new PremierLeagueDAO();
		this.idMap= new HashMap<Integer,Player>();
		this.dao.listAllPlayers(idMap);
	}
	
	public void creaGrafo(Match m) {
		grafo= new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(m, idMap));
		
		//aggiungo archi
		for (Adiacenza a:dao.getArchi(m, idMap)) {
			if(a.getPeso() >= 0) {
				//p1 meglio di p2
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdge(this.grafo, a.getP1(), 
							a.getP2(), a.getPeso());
				}
			} else {
				//p2 meglio di p1
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdge(this.grafo, a.getP2(), 
							a.getP1(), (-1) * a.getPeso());
				}
		}}
	}
	
	public int getVertici() {
		return this.grafo.vertexSet().size();
		
	}
	
	public int getArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Match> getMatches(){
		List<Match> ordine= dao.listAllMatches();
		Collections.sort(ordine, new Comparator<Match>() {

			@Override
			public int compare(Match o1, Match o2) {
				// TODO Auto-generated method stub
				return o1.getMatchID().compareTo(o2.matchID);
			}
			
		});
		return ordine;
	}
	
	//troviamo il giocatore migliore 
	public GiocatoreMigliore getMigliore() {
		if(grafo==null) {
			return null;
		}
		
		Player best= null;
		Double maxDelta=(double) Integer.MIN_VALUE; //deve essere un valore più piccolo
		
		for(Player p: this.grafo.vertexSet()) {
			//calcolo la somma degi pesi degli archi uscenti
			double pesoUsc=0.0;
			for(DefaultWeightedEdge edge: this.grafo.outgoingEdgesOf(p)) {
				pesoUsc+= this.grafo.getEdgeWeight(edge); //il peso di quell'arco
			}
			
			//calcolo somma dei pesi degli archi entranti
			double pesoEntr=0.0;
			for(DefaultWeightedEdge edge: this.grafo.incomingEdgesOf(p)) {
				pesoEntr+= this.grafo.getEdgeWeight(edge); //il peso di quell'arco
			}
			
			double delta= pesoUsc-pesoEntr;
			if(delta>maxDelta) {
				best=p;
				maxDelta=delta;
			}
			
		}
		
		return new GiocatoreMigliore(best,maxDelta);
		
		
	}

	public Graph<Player, DefaultWeightedEdge> getGrafo() {
		// TODO Auto-generated method stub
		return this.grafo;
	}
}
