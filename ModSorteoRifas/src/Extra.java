public class Extra {

    private Integer integrante;
    private Integer cantExtra;

    public Extra(Integer id ,Integer cantExtra ) {
        this.integrante=id;
        this.cantExtra=cantExtra;
    }

    public Integer getId() {
        return integrante;
    }

    public void setId(Integer integrante) {
        this.integrante = integrante;
    }

    public Integer getCantExtra() {
        return cantExtra;
    }

    public void setCantExtra(Integer cantExtra) {
        this.cantExtra = cantExtra;
    }

}
