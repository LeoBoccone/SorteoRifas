public class Rifa {

    private Integer Integrante;
    private Integer Numero;


    public Rifa(Integer integrante, Integer numero) {
        this.Integrante = integrante;
        this.Numero=numero;

    }

    public Integer getNumero() {
        return Numero;
    }


    public Integer getIntegrante() {
        return Integrante;
    }

    public void setIntegrante(Integer integrante) {
        this.Integrante = integrante;
    }
}
