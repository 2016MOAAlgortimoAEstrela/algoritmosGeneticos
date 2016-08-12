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
class Main {

    public static Cidade strToCidade(String str) {
        String s[] = Arrays.stream(str.split(" "))
                .filter(e -> !e.equals(""))
                    .toArray(String[]::new);

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
            cidades.adicionar(
                new Cidade(
                    reader.nextInt(),
                    new Coordenadas(
                            Double.valueOf(reader.next()),
                            Double.valueOf(reader.next())
            )));
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
                Util.ARQUIVO_CIDADES
        );
       
        //cidades = lerCidadesPrompt();                
        
        Individuo melhor = ag.executar(cidades);
        //System.out.println(melhor.toString());        
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe AlgoritmoGenetico">
class AlgoritmoGenetico {

    private Mutacao mutacao = new Mutacao();
    private Selecao selecionador = new Selecao();
    private Cruzamento cruzamento = new Cruzamento();
    private BuscaLocal buscaLocal = new BuscaLocal();
    private int repeticaoMelhor;
    private long initialTime;
    private int geracao;

    public Individuo executar(Cidades cidades) {
        this.initialTime = System.currentTimeMillis();
        Individuo melhor;                
        Populacao populacao;                
        populacao = new Populacao(Util.TAMANHO_POPULACAO, cidades);        
        populacao.gerarIndividuos();                      
        this.geracao = 0;
        
        melhor = populacao.getIndividuo(0);
        System.out.println("Geração" + "\tSolução");
        this.printMelhorInvidivuo(populacao);
        
        while (!this.parar()) {                        
            Individuo[] vencedores;            
            Individuo[] filhos;
            
            this.geracao++;
            vencedores = selecionador.executarTorneio(populacao, Util.QUANTIDADE_INDIVIDUOS_TORNEIO);            
            filhos = cruzamento.executar(vencedores, melhor != populacao.getIndividuo(0));
            filhos = mutacao.executar(filhos);                        
            populacao.atualizar(filhos);       
            if (Util.TIPO_BUSCA_LOCAL == 1)
                populacao.setIndividuo(0, buscaLocal.firstFit(populacao.getIndividuo(0)));                                                          
            if (melhor != populacao.getIndividuo(0)){
                if (Util.TIPO_BUSCA_LOCAL == 2)                
                    populacao.setIndividuo(0, buscaLocal.hillClimbing(populacao.getIndividuo(0)));           
                melhor = populacao.getIndividuo(0);
                this.printMelhorInvidivuo(populacao);
                this.repeticaoMelhor = 0;                
            } 
            else repeticaoMelhor++;            
        }

        this.printMelhorInvidivuo(populacao);
        return melhor;
    }        
    
    private void printMelhorInvidivuo(Populacao p){
        System.out.println(this.geracao + "\t" + p.getIndividuo(0));        
    }        
    
    private long tempoExecucaoMillis(){
        return (System.currentTimeMillis() - this.initialTime);
    }

    private boolean parar() {
        return ((this.repeticaoMelhor > Util.QUANTIDADE_LIMITE_REPETICOES_MELHOR)
                || ( this.tempoExecucaoMillis() > Util.TEMPO_LIMITE_EXECUCAO));
    }

}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe BuscaLocal">
class BuscaLocal {
    private Individuo sucessorDeMaiorValor(Individuo pai){
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
    
    private Individuo primeirosucessorDeMaiorValor(Individuo pai){
        int tamanho =  pai.getGenes().size();
        Individuo[] sucessores = new Individuo[tamanho / 2];               
        for (int i = 0; i < tamanho / 2 ; i++){
            sucessores[i] = new Individuo(pai.getCaminhos(), pai.getGenes());            
            sucessores[i].inverteGenes(i, tamanho - (i + 1));       
            if (sucessores[i].getAptidao() < pai.getAptidao()){
                return sucessores[i];
            }            
        }
        return pai;
    }
        
    public Individuo firstFit(Individuo i){
        Individuo vizinho;                
        vizinho = primeirosucessorDeMaiorValor(i);                       
        if (vizinho.getAptidao() < i.getAptidao())
            return vizinho;           
        
        return i;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Selecao">
class Selecao {
       
    public Individuo[] executarTorneio(Populacao populacao, int quantidade) {
        int qtde_populacao = populacao.tamanhoPopulacao;
        int qtde_participantes = qtde_populacao;        
        if (qtde_populacao > quantidade){            
            qtde_participantes = (quantidade + Util.random(qtde_populacao - quantidade));
        }        
                               
        Individuo[] vencedores = new Individuo[quantidade];
        Individuo[] participantes = populacao.getIndividuos(qtde_participantes);
        
        Comparator<Individuo> c = new Individuo();
        Arrays.sort(participantes, c);
        
        System.arraycopy(participantes, 0, vencedores, 0, quantidade);        
        return vencedores;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cruzamento">
class Cruzamento {    
    private Individuo[] cruzar(Individuo x, Individuo y){        
        ArrayList<Integer> genes1 = new ArrayList<>(x.getGenes().size());
        ArrayList<Integer> genes2 = new ArrayList<>(x.getGenes().size());                
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

    public Individuo[] executar(Individuo[] pais, boolean procuraPelosMelhores) {
        Individuo[] filhosArr = new Individuo[pais.length];
        ArrayList<Individuo> filhosList = new ArrayList<>();                
        if (!procuraPelosMelhores) {
            for (int i = 0; i < pais.length / 2; i++){
                filhosArr = this.cruzar(
                        pais[Util.random(pais.length - 1)],
                        pais[Util.random(pais.length - 1)]
                );                                         
                filhosList.add(filhosArr[0]);         
                filhosList.add(filhosArr[1]);         
            }
        } else{
            for (int i = 0; i < pais.length - 2; i += 2){
                filhosArr = this.cruzar(pais[i],  pais[i+1]);      
                filhosList.add(filhosArr[0]);         
                filhosList.add(filhosArr[1]);         
            }                  
        }
        filhosArr = filhosList.toArray(filhosArr);
        return filhosArr;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Mutacao">
class Mutacao {    

    public Individuo[] executar(Individuo[] filhos) {      
        for (Individuo filho : filhos) {
            int quantidadeGenes = filho.getGenes().size() - 1;
            if (Util.random() < Util.TAXA_MUTACAO) {
                filho.inverteGenes(Util.random(quantidadeGenes), Util.random(quantidadeGenes));          
            }
        }
      
      return filhos;
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Populacao">
class Populacao {

    private Individuo[] individuos;
    public int tamanhoPopulacao;
    private Caminhos caminhos;

    public Populacao(int tamanhoPopulacao, Cidades cidades) {
        this.tamanhoPopulacao = tamanhoPopulacao;
        this.caminhos = new Caminhos(cidades);
        this.individuos = new Individuo[tamanhoPopulacao];
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
        
        this.ordenar();
    }

    public Individuo[] getIndividuos() {
        return this.individuos;
    }

    public Individuo getIndividuo(int posicao) {
        return this.individuos[posicao];
    }
    
     public void setIndividuo(int posicao, Individuo i) {
        if (this.individuos[posicao].getAptidao() > i.getAptidao())
            this.individuos[posicao] = i;
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
    
    public boolean podeInserir(Individuo i){
        for (Individuo o : this.individuos) {
            if (i.getAptidao() == o.getAptidao())
                return false;
        }
        return true;
    }

    public void atualizar(Individuo[] novosIndividuos) {
        for (int contador = 0; contador < novosIndividuos.length; contador++) {
            if (podeInserir(novosIndividuos[contador]))
              this.individuos[this.tamanhoPopulacao - (contador + 1)] = novosIndividuos[contador];
            
        }                
	this.ordenar();
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
        this.genes = new ArrayList<>(genes);   
        this.aptidao = 0.0;
    }

    public Individuo(Caminhos caminhos) {
        this.caminhos = caminhos;
        this.gerarGenes(caminhos.getCidades());        
        this.aptidao = 0.0;             
    }

    public void gerarGenes(Cidades cidades) {        
        this.genes = new ArrayList<>(cidades.quantidadeCidades());

        for (int contador = 1; contador <= cidades.quantidadeCidades(); contador++) {
            this.genes.add(contador);                        
        }
        
        Collections.shuffle(this.genes);
    }

    public ArrayList<Integer> getGenes() {
        return genes;
    }

    public Integer getGene(int posicao) {
        return this.genes.get(posicao);
    }

    public double getAptidao() {
        if (this.aptidao == 0.0){
            this.calcularAptidao();
        }
        
        return this.aptidao;
    }

    private void calcularAptidao() {
        this.aptidao = 0.0;
        for (int contador = 0; contador < this.genes.size() - 1; contador++) {
            this.aptidao += this.caminhos.distanciaCaminho(new Tupla(this.genes.get(contador), this.genes.get(contador + 1)));
        }
        this.aptidao += this.caminhos.distanciaCaminho(new Tupla(this.genes.get(this.genes.size() - 1), this.genes.get(0)));
    }
    
    public Caminhos getCaminhos(){
        return this.caminhos;
    }

    @Override
    public String toString() {
        return String.valueOf(this.getAptidao()).replace('.', ',');
//        return '\n' + Arrays.toString(this.genes.toArray()) 
//                + '\n' + String.valueOf(this.getAptidao()); 
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
      this.calcularAptidao();
    }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Caminhos">
class Caminhos {

    private HashMap<Tupla, Double> caminhos;
    private Cidades cidades;

    public Caminhos(Cidades cidades) {
        this.cidades = cidades;
        this.caminhos = new HashMap<>();
    }

    public void adicionarCaminho(Tupla tupla, Double distancia) {
        this.caminhos.put(tupla, distancia);
    }

    public Double distanciaCaminho(Tupla tupla) {
	double distancia = this.caminhos.getOrDefault(tupla, 0.0);
        if (distancia == 0){
	  distancia = cidades.getCidade(tupla.getCidadeA()).distanciaCidade(cidades.getCidade(tupla.getCidadeB()));
          this.adicionarCaminho(tupla, distancia);
	}

	return distancia;	
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
        this.id = id;
        this.coordenadas = coordenadas;
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
    private static final String ARQUIVO_CIDADES_PATH = "C:\\Users\\Duh\\Documents\\algoritmosGeneticos\\algoritmoGenetico\\src\\";
    
    public static final Double DISTANCIA_PADRAO = 0.0;
    public static final int TAMANHO_POPULACAO = 100;
    public static final int QUANTIDADE_INDIVIDUOS_TORNEIO = 51;
    public static final int QUANTIDADE_LIMITE_REPETICOES_MELHOR = 10000000 / TAMANHO_POPULACAO;       
    public static final int TEMPO_LIMITE_EXECUCAO = (int) (30 *60 * 1000);
    public static final int TAXA_MUTACAO = 50;    
    public static final String ARQUIVO_CIDADES = ARQUIVO_CIDADES_PATH + "att532";
    public static final int TIPO_BUSCA_LOCAL = 1;
    public static int random(int limite) {
        return new Random().nextInt(limite);
    }
    
    public static int random() {
        return new Random().nextInt(100);
    }
}
//</editor-fold>
