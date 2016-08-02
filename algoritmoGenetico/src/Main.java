//<editor-fold defaultstate="collapsed" desc="import">

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Stream;
import org.omg.CORBA.portable.IndirectionException;
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Main">
public class Main {

    public static Cidade strToCidade(String str) {
        String s[] = str.split(" ");

        return new Cidade(
                Integer.valueOf(s[0]),
                new Coordenadas(
                        Double.valueOf(s[1]),
                        Double.valueOf(s[2])
                ));
    }

    public static Cidades lerCidadesPrompt() {
        Cidades cidades = new Cidades();
        Scanner reader = new Scanner(System.in);
        int i, n;

        n = reader.nextInt();
        for (i = 0; i < n; i++) {
            reader.nextLine();
            cidades.adicionar(strToCidade(reader.nextLine()));
        }

        return cidades;
    }

    public static Cidades lerCidadesArquivo(String path) {
        try (Stream<String> stream = Files.lines(Paths.get(path))) {
            Cidades cidades = new Cidades();
            stream.forEach((l) -> {
                cidades.adicionar(strToCidade(l));
            });
            return cidades;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        AlgoritmoGenetico ag = new AlgoritmoGenetico();
        Cidades cidades;
            cidades = lerCidadesArquivo(
                "C:\\Users\\Duh\\Documents\\algoritmosGeneticos\\algoritmoGenetico\\src\\input.txt"
        );
        /*
        if (args.length != 0) {
            cidades = lerCidadesArquivo(args[0]);
        } else {
            cidades = lerCidadesPrompt();
        }
         */
        
        Individuo melhor = ag.executar(cidades);
        System.out.println(melhor.getGenes().toString());
        System.out.println(melhor.getAptidao());
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe AlgoritmoGenetico">
class AlgoritmoGenetico {

    private Mutacao mutacao = new Mutacao();
    private Selecao selecionador = new Selecao();
    private Cruzamento cruzamento = new Cruzamento();
    private BuscaLocal buscaLocal = new BuscaLocal();
    private int contator = 0;
    private long initialTime;

    public Individuo executar(Cidades cidades) {
        this.initialTime = System.currentTimeMillis();
        Populacao populacao = new Populacao(Util.TAMANHO_POPULACAO, cidades);
        populacao.gerarIndividuos();        
        while (!this.parar()) {
            populacao.ordenar();
            Individuo[] vencedores = selecionador.executarTorneio(populacao, Util.QUANTIDADE_INDIVIDUOS_TORNEIO);
            Individuo[] filhos = cruzamento.executar(vencedores);
            filhos = mutacao.executar(filhos);            
            buscaLocal.hillClimbing(populacao.getIndividuo(0));
            populacao.atualizar(filhos);
            
        }

        return populacao.getIndividuo(0);
    }

    private boolean parar() {
        return ((++contator > Util.QUANTIDADE_LIMITE_EXECUCAO)
                || ((System.currentTimeMillis() - this.initialTime) > 9000));
    }

}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe BuscaLocal">
class BuscaLocal {
    public Individuo sucessorDeMaiorValor(Individuo pai){
        int tamanho =  pai.getGenes().size();
        Individuo[] sucessores = new Individuo[tamanho / 2];               
        for (int i = 0; i < tamanho / 2 ; i++){
            
            sucessores[i] = new Individuo(pai.getCaminhos(), pai.getGenes());
            sucessores[i].inverteGenes(i, tamanho - (i + 1));            
                                                
        }
        
        Comparator<Individuo> c = new Individuo();
        Arrays.sort(sucessores, c);
        
        return sucessores[0];
    }
    
    public Individuo hillClimbing(Individuo i){
        Individuo vizinho;
        boolean continuar;
        do{
            vizinho = sucessorDeMaiorValor(i);                       
            continuar = vizinho.getAptidao() < i.getAptidao();
            if (continuar)
                i = vizinho;
        } while(continuar);
        return i;        
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Selecao">
class Selecao {
       
    public Individuo[] executarTorneio(Populacao populacao, int quantidade) {
        Individuo[] vencedores = new Individuo[quantidade];
        Individuo[] participantes = populacao.getIndividuos(quantidade);
        for (int i = 0; i < quantidade; i++) {
            vencedores[i] = participantes[i];
        }        
        return vencedores;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cruzamento">
class Cruzamento {    
    private Individuo[] cruzar(Individuo x, Individuo y){        
        ArrayList<Integer> genes1 = new ArrayList<Integer>(x.getGenes().size());
        ArrayList<Integer> genes2 = new ArrayList<Integer>(x.getGenes().size());                
        Individuo[] filhos = new Individuo[2] ;
        int tamanho_heranca = x.getGenes().size() / 3;
        int i;
        
        for (i = 0; i < tamanho_heranca; i++){          
            genes1.add(x.getGene(i));
            genes2.add(y.getGene(i));
        }        
        
        for (i = tamanho_heranca; i < x.getGenes().size(); i++){          
            if (!genes1.contains(y.getGene(i)))
              genes1.add(y.getGene(i));
            
            if (!genes2.contains(x.getGene(i)))
              genes2.add(x.getGene(i));                       
        }
        
        for (i = 0; i < tamanho_heranca; i++){               
            if (!genes1.contains(y.getGene(i)))
              genes1.add(y.getGene(i));
            
            if (!genes2.contains(x.getGene(i)))
              genes2.add(x.getGene(i));                       
        }
               
        filhos[0] = new Individuo(x.getCaminhos(), genes1);
        filhos[1] = new Individuo(x.getCaminhos(), genes2);
        
        return filhos;
    }    

    public Individuo[] executar(Individuo[] pais) {
        return this.cruzar(pais[0], pais[1]);                          
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Mutacao">
class Mutacao {    

    public Individuo[] executar(Individuo[] filhos) {      
      for (int contador = 0; contador < filhos.length; contador++){
        int quantidadeGenes = filhos[contador].getGenes().size() - 1;
        if (Util.random() < Util.TAXA_MUTACAO){
          filhos[contador].inverteGenes(Util.random(quantidadeGenes), Util.random(quantidadeGenes));
        }
      }
      
      return filhos;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Populacao">
class Populacao {

    private Individuo[] individuos;
    private int tamanhoPopulacao;
    private Caminhos caminhos;

    public Populacao(int tamanhoPopulacao, Cidades cidades) {
        this.setTamanhoPopulacao(tamanhoPopulacao);
        this.caminhos = new Caminhos(cidades);
        this.individuos = new Individuo[tamanhoPopulacao];
        /*
        for (int contador = 0; contador < tamanhoPopulacao; contador++) {
            this.individuos[contador] = null;
        }
         */
    }

    public Populacao(Individuo[] individuos, Caminhos caminhos) {
        this.caminhos = caminhos;
        this.tamanhoPopulacao = individuos.length;
        this.individuos = individuos;
    }

    public void gerarIndividuos() {
        for (int contador = 0; contador < this.tamanhoPopulacao; contador++) {
            this.individuos[contador] = new Individuo(this.caminhos);
        }
    }

    public Individuo[] getIndividuos() {
        return individuos;
    }

    public Individuo getIndividuo(int posicao) {
        return individuos[posicao];
    }

    public void setIndividuos(Individuo[] individuos) {
        this.individuos = individuos;
    }

    public int getTamanhoPopulacao() {
        return tamanhoPopulacao;
    }

    public void setTamanhoPopulacao(int tamanhoPopulacao) {
        this.tamanhoPopulacao = tamanhoPopulacao;
    }

    public Individuo[] getIndividuos(int quantidadeIndividuos) {
        Individuo[] individuosSelecionados = new Individuo[quantidadeIndividuos];

        for (int contador = 0; contador < quantidadeIndividuos; contador++) {
            individuosSelecionados[contador] = this.getIndividuo(Util.random(this.tamanhoPopulacao));
        }

        return individuosSelecionados;
    }

    public Populacao obterSubPopulacao(int quantidadePopulacao) {
        return new Populacao(this.getIndividuos(quantidadePopulacao), this.caminhos);
    }

    public void atualizar(Individuo[] novosIndividuos) {
        for (int contador = 0; contador < novosIndividuos.length; contador++) {
            this.individuos[this.tamanhoPopulacao - (contador + 1)] = novosIndividuos[contador];
        }                
    }
    
    public void ordenar(){           
        Comparator<Individuo> c = new Individuo();
        Arrays.sort(this.individuos, c);
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Individuo">
class Individuo implements Comparator<Individuo>{

    private ArrayList<Integer> genes;
    private Caminhos caminhos;
    private double aptidao = 0;
    
    public Individuo(){}

    public Individuo(Caminhos caminhos, ArrayList<Integer> genes) {
        this.caminhos = caminhos;
        this.genes = genes;
        this.calcularAptidao();
    }

    public Individuo(Caminhos caminhos) {
        this.caminhos = caminhos;
        this.genes = this.gerarGenes(caminhos.getCidades());
        this.calcularAptidao();
    }

    public ArrayList<Integer> gerarGenes(Cidades cidades) {
        Random random = new Random();
        genes = new ArrayList<Integer>(cidades.quantidadeCidades());

        int idIndividuo = 0;
        for (int contador = 0; contador < cidades.quantidadeCidades(); contador++) {
            do {
                idIndividuo = random.nextInt(cidades.quantidadeCidades()) + 1;
            } while (genes.contains(idIndividuo));

            genes.add(idIndividuo);
        }

        return genes;
    }

    public ArrayList<Integer> getGenes() {
        return genes;
    }

    public Integer getGene(int posicao) {
        return this.genes.get(posicao);
    }

    public double getAptidao() {
        return this.aptidao;
    }

    private void calcularAptidao() {
        double soma = 0.0;
        Tupla tupla;
        for (int contador = 0; contador < this.genes.size() - 1; contador++) {
            tupla = new Tupla(this.genes.get(contador), this.genes.get(contador + 1));
            soma += this.caminhos.distanciaCaminho(tupla);
        }

        this.aptidao = soma;
    }
    
    public Caminhos getCaminhos(){
        return this.caminhos;
    }

    @Override
    public int compare(Individuo o1, Individuo o2) {
        double diferenca = o1.aptidao - o2.aptidao;
        if (diferenca == 0){
            return 0;
        }
        
        return (int) (diferenca / Math.abs(diferenca));
    }
    
    public void inverteGenes(int indiceGene1, int indiceGene2){
      Integer gene1 = this.getGene(indiceGene1);
      Integer gene2 = this.getGene(indiceGene2);
      this.genes.set(indiceGene1, gene2);
      this.genes.set(indiceGene2, gene1);
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Caminhos">
class Caminhos {

    private HashMap<Tupla, Double> caminhos;
    private Cidades cidades;

    public Caminhos(Cidades cidades) {
        this.cidades = cidades;
        this.caminhos = new HashMap<Tupla, Double>();
        calcularCaminhos(cidades);
    }

    public void adicionarCaminho(Tupla tupla, Double distancia) {
        this.caminhos.put(tupla, distancia);
    }

    private void calcularCaminhos(Cidades cidades) {
        for (int contador = 1; contador <= cidades.quantidadeCidades(); contador++) {
            Cidade cidadeInicial = cidades.getCidade(contador);
            for (int proximo = contador + 1; proximo <= cidades.quantidadeCidades(); proximo++) {
                Cidade cidadeProxima = cidades.getCidade(proximo);
                this.adicionarCaminho(new Tupla(cidadeInicial.getId(), cidadeProxima.getId()), cidadeInicial.distanciaCidade(cidadeProxima));
            }
        }
    }

    public Double distanciaCaminho(Tupla tupla) {
        if (!this.caminhos.containsKey(tupla)) {
            return Util.DISTANCIA_PADRAO;
        }

        return this.caminhos.getOrDefault(tupla, Util.DISTANCIA_PADRAO);
    }

    public Cidades getCidades() {
        return this.cidades;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cidades">
class Cidades {

    private HashMap<Integer, Cidade> listaCidades = new HashMap<Integer, Cidade>();

    public Cidade getCidade(int posicao) {
        return this.listaCidades.get(posicao);
    }

    public void adicionar(Cidade cidade) {
        this.listaCidades.put(cidade.getId(), cidade);
    }

    public int quantidadeCidades() {
        return this.listaCidades.size();
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cidade">
class Cidade {

    private int id;
    private Coordenadas coordenadas;

    public Cidade(int id, Coordenadas coordenadas) {
        this.setId(id);
        this.setCoordenadas(coordenadas);
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Coordenadas getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(Coordenadas coordenadas) {
        this.coordenadas = new Coordenadas(coordenadas.getLatitude(), coordenadas.getLongitude());
    }

    public double getLatitudeCidade() {
        return this.coordenadas.getLatitude();
    }

    public double getLongitudeCidade() {
        return this.coordenadas.getLongitude();
    }

    public double distanciaCidade(Cidade cidade) {
        if (cidade == null) {
            return 0.0;
        }

        double distanciaLatitude = Math.abs(this.getLatitudeCidade() - cidade.getLatitudeCidade());
        double distanciaLongitude = Math.abs(this.getLongitudeCidade() - cidade.getLongitudeCidade());
        return Math.sqrt(Math.pow(distanciaLatitude, 2) + Math.pow(distanciaLongitude, 2));
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Coordenadas">
class Coordenadas {

    private double latitude;
    private double longitude;

    public Coordenadas() {
        Random random = new Random();
        this.setLatitude(random.nextDouble());
        this.setLongitude(random.nextDouble());
    }

    public Coordenadas(double latitude, double longitude) {
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Tupla">
class Tupla {    
    
    private int[] cidades;

    public Tupla(int cidadeA, int cidadeB) {
        this.cidades = new int[2];
        this.cidades[0] = cidadeA;
        this.cidades[1] = cidadeB;        
    }

    public int getCidadeA() {
        return this.cidades[0];
    }

    public void setCidadeA(int cidadeA) {
        this.cidades[0] = cidadeA;
    }

    public int getCidadeB() {
        return this.cidades[1];
    }

    public void setCidadeB(int cidadeB) {
        this.cidades[1] = cidadeB;
    }

    @Override
    public int hashCode() {               
        return Arrays.hashCode(this.cidades); 
    }                            

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tupla){
            return obj.hashCode() == this.hashCode();
        }
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Util">
class Util {

    public static int numeroDeCidades;
    public static final Double DISTANCIA_PADRAO = 0.0;
    public static final int TAMANHO_POPULACAO = 10;
    public static final int QUANTIDADE_INDIVIDUOS_TORNEIO = 2;
    public static final int QUANTIDADE_LIMITE_EXECUCAO = 1000000;
    public static final int TAXA_MUTACAO = 2;
    
    public static int random(int limite) {
        return new Random().nextInt(limite);
    }
    
    public static int random() {
        return new Random().nextInt(100);
    }
}
//</editor-fold>
