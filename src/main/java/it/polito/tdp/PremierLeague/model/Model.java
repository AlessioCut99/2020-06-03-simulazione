package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> grafo;
	private Map<Integer,Player> idMap;
	
	
	//DATI PER PARTE 2
	private List<Player> dreamTeam;
	private Integer bestDegree;

	public Model() {
		this.dao = new PremierLeagueDAO();
	}
	
	public void creaGrafo(Double x) {
		
		grafo = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		idMap = new HashMap<Integer,Player>();
		
		
		//aggiungo i vertici
		dao.getVertici(x, idMap);
		Graphs.addAllVertices(this.grafo, idMap.values());
		
		//aggiungo gli archi
		
		for(Adiacenza a : dao.getAdiacenze(idMap)) {
			if(a.getPeso() > 0) {
				//p1 meglio di p2
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(this.grafo, a.getP1(), a.getP2(), a.getPeso());
				}
			} else if(a.getPeso() < 0) {
				//p2 meglio di p1
				if(grafo.containsVertex(a.getP1()) && grafo.containsVertex(a.getP2())) {
					Graphs.addEdgeWithVertices(this.grafo, a.getP2(), a.getP1(), (-1) * a.getPeso());
				}
			}
		}
	}
	
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public Graph<Player,DefaultWeightedEdge> getGrafo() {
		return this.grafo;
	}
	
	public TopPlayer getTopPlayer() {
		if(grafo == null) {
			return null;
		}
		
		Player best = null;
		Integer maxDegree = Integer.MIN_VALUE;
		
		for(Player p : grafo.vertexSet()) {
			if(grafo.outDegreeOf(p) > maxDegree) {
				maxDegree = grafo.outDegreeOf(p);
				best = p;
			}
				
		}
		
		TopPlayer topPlayer = new TopPlayer();
		topPlayer.setPlayer(best);
		
		List<Opponent> opponents = new ArrayList<Opponent>();
		for(DefaultWeightedEdge edge : grafo.outgoingEdgesOf(topPlayer.getPlayer())) {
			opponents.add(new Opponent(grafo.getEdgeTarget(edge), (int) grafo.getEdgeWeight(edge)));
		}
		Collections.sort(opponents);
		topPlayer.setOpponents(opponents);
		return topPlayer;
	}
	
	
	
	//PARTE 2
	public List<Player> getDreamTeam (int k){
		this.bestDegree = 0;
		this.dreamTeam = new ArrayList<Player>();
		
		List<Player> partial = new ArrayList<Player>();
		
		this.recursive(partial , new ArrayList<Player>(this.grafo.vertexSet()), k);
		
		return dreamTeam;
	}

	private void recursive(List<Player> partial, List<Player> players, int k) {
		if(partial.size() == k) {
			int degree = this.getDegree(partial);
			if(degree > this.bestDegree) {
				dreamTeam = new ArrayList<>(partial);
				bestDegree = degree;
			}
			return;
		}
		
		for(Player p : players) {
			if(!partial.contains(p)) {
				partial.add(p);
				//i "battuti" di p non possono piu' essere considerati
				List<Player> giocatoriRimanenti = new ArrayList<>(players);
				giocatoriRimanenti.removeAll(Graphs.successorListOf(grafo, p));
				recursive(partial, giocatoriRimanenti, k);
				partial.remove(p);
			}
		}
		
		
		
	}

	private int getDegree(List<Player> team) {
		int degree = 0;
		int in;
		int out;
		
		for(Player p : team) {
			in = 0;
			out = 0;
			for(DefaultWeightedEdge edge : this.grafo.outgoingEdgesOf(p))
				out += this.grafo.getEdgeWeight(edge);
			
			for(DefaultWeightedEdge edge : this.grafo.incomingEdgesOf(p))
				in += this.grafo.getEdgeWeight(edge);
			
			degree += (out - in);
		}
		
		return degree;
	}

	public Integer getBestDegree() {
		return bestDegree;
	}
	
	
	
	
}
