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

    public boolean isValid() {
        String tempFavorito = this.favorito.replaceAll("\\D", "");
        this.favorito = tempFavorito;
        boolean valid = true;

        if (tempFavorito.isEmpty() || tempFavorito.length() < 3 || tempFavorito.length() > 5) {
            valid = false;
        } else if (Integer.valueOf(tempFavorito) > 80999) {
            valid = false;
        }

        return valid;
    }

}
