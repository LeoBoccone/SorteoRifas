import java.util.UUID;

public class Favorito {

    public String favorito;
    public String integrante;
    public UUID id;
    public boolean isExtra;

    public Favorito(String favorito, String integrante, boolean isExtra) {
        this.favorito = favorito;
        this.integrante = integrante;
        this.isExtra = isExtra;
    }

}
