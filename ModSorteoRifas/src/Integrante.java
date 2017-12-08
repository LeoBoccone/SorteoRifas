import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Integrante {
	
	private Integer id;
	private boolean conAcompanante;
	private ArrayList<Integer> rifas;
	private Integer cantRifasASortear;
	
	public Integrante(Integer id, Integer cantRifasASortear) {
		this.id=id;
		this.conAcompanante=false;
		this.rifas= new ArrayList<Integer>();
		this.cantRifasASortear=cantRifasASortear;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<Integer> getRifas() {
		return rifas;
	}

	public void setRifas(ArrayList<Integer> rifas) {
		this.rifas = rifas;
	}
	
	public void sortRifas() {
		Collections.sort(rifas);
	}
	
	public Integer getSizeRifas() {
		return rifas.size();
	}

	public void addRifa(Integer rifa) {
		this.rifas.add(rifa);
	}
	
	public void removeRifa(Integer rifa) {
		this.rifas.remove(rifa);
	}
	
	public void printRifas2File() {
		try{
			sortRifas();
			List<String> lines;
			Path file;
			file = Paths.get(App.PATH_GENERADOS + this.id + ".txt");
			lines = Arrays.asList("ID Integrante: " + id, "Cantidad Rifas: " + getSizeRifas(), Arrays.toString(rifas.toArray()));
			Files.write(file, lines, Charset.forName("UTF-8"));
		} catch (IOException e) {
		   // do something
		}
	}
	
	public void printRifas() {
		sortRifas();
		System.out.println("*********************");
		System.out.println("ID Integrante: " + id);
		System.out.println("Cantidad Rifas: " + getSizeRifas());
		System.out.println(Arrays.toString(rifas.toArray()));
	}

	/*
	public void printRifas2PDF() throws IOException
    {
        String filename = App.PATH_GENERADOS + this.id + ".pdf";
        PDDocument doc = new PDDocument();
        try
        {
        	boolean isFirstPage = true;
        	Integer firstRifa = 0;
        	Integer lastRifa = 0;
        	Integer cantRifasXPage = 44;
        	while (lastRifa < getSizeRifas()) {
        		if (lastRifa + cantRifasXPage > getSizeRifas()) {
        			lastRifa = getSizeRifas();
        		} else {
        			lastRifa += cantRifasXPage;
        		}
        		printAuxRifas2Page(doc, firstRifa, lastRifa - 1, isFirstPage);
        		isFirstPage = false;
        		firstRifa = lastRifa;
        	}
            
            doc.save(filename);
        }
        finally
        {
            doc.close();
        }
    }

	private void printAuxRifas2Page(PDDocument doc, Integer firstRifa, Integer lastRifa, boolean isFirstPage) throws IOException {
		PDPage page = new PDPage();
		doc.addPage(page);
		PDPageContentStream contents = new PDPageContentStream(doc, page);
		
		PDFont font = PDType1Font.HELVETICA_BOLD;
		float fontSize = 9;
		float leading = 1.5f * fontSize;
		
		PDRectangle mediabox = page.getMediaBox();
		float margin = 72;
		//float width = mediabox.getWidth() - 2*margin;
		float startX = mediabox.getLowerLeftX() + margin;
		float startY = mediabox.getUpperRightY() - margin;
		
		contents.beginText();
		contents.setFont(font, fontSize);
		contents.newLineAtOffset(startX, startY);
		if (isFirstPage) {
			contents.showText("ID Integrante: " + id);
			contents.newLineAtOffset(0, -leading);
			contents.showText("Cantidad Rifas: " + getSizeRifas());
			contents.newLineAtOffset(0, -leading);
		}
		String rifa = "";
		for (Integer i = firstRifa; i <= lastRifa; i++) {
			rifa = rifas.get(i).toString();
			contents.showText(rifa);
			contents.newLineAtOffset(0, -leading);
		}
		contents.endText(); 
		contents.close();
	}
	*/

	public boolean isConAcompanante() {
		return conAcompanante;
	}

	public void setConAcompanante(boolean conAcompanante) {
		this.conAcompanante = conAcompanante;
	}

	public Integer getCantRifasASortear() {
		return cantRifasASortear;
	}

	public void setCantRifasASortear(Integer cantRifasASortear) {
		this.cantRifasASortear = cantRifasASortear;
	}

}
