
import java.util.ArrayList;
import java.util.List;

public class Integrante {
	
	private Integer id;
	private boolean conAcompanante;
	private ArrayList<Rifa> rifas;
	private Integer cantRifasASortear;
	private Integer cantExtra;
	
	public Integrante(Integer id, Integer cantRifasASortear,Boolean acompanante,Integer cantExtra) {
		this.id=id;
		this.conAcompanante = acompanante;
		this.rifas= new ArrayList<Rifa>();
		this.cantRifasASortear=cantRifasASortear;
		this.cantExtra=0;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Rifa> getRifas() {
		return rifas;
	}

	public void setRifas(ArrayList<Rifa> rifas) {
		this.rifas = rifas;
	}

	public Integer getSizeRifas() {
		return rifas.size();
	}

	public boolean isConAcompanante() {
		return conAcompanante;
	}

	public Integer getCantExtra() {
		return cantExtra;
	}

	public void setCantExtra(Integer cantExtra) {
		this.cantExtra = cantExtra;
	}
}
