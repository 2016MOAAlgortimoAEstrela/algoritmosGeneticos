//<editor-fold defaultstate="collapsed" desc="import">
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Main">
public class Main {
  public static void main(String[] args){
    
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe AlgoritmoGenetico">
class AlgoritmoGenetico{
  
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Selecao">
class Selecao{
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cruzamento">
class Cruzamento {
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Mutacao">
class Mutacao{
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Populacao">
class Populacao{
  private Individuo[] individuos;
  private int tamanhoPopulacao;
  private Cidades cidades;

  public Populacao(int tamanhoPopulacao, Cidades cidades) {
    this.setTamanhoPopulacao(tamanhoPopulacao);
    this.setCidades(cidades);
    this.individuos = new Individuo[tamanhoPopulacao];
    for (int contador = 0; contador < tamanhoPopulacao; contador++){
      this.individuos[contador] = null;
    }
  }
  
  public Populacao(Individuo[] individuos, Cidades cidades){
    this.setCidades(cidades);
    this.setTamanhoPopulacao(individuos.length);
    this.setIndividuos(individuos);
  }
  
  public void gerarIndividuos(){
    for (int contador = 0; contador < this.tamanhoPopulacao; contador++){
      this.individuos[contador] = new Individuo(this.getCidades());
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
  
  public void avaliar(){
    for (int contador = 0; contador < this.tamanhoPopulacao; contador++) {
      Individuo individuo = this.getIndividuo(contador);
      double aptidao = this.cidades.getCidade(individuo.getGene(contador)).distanciaCidade(null);
      individuo.setAptidao(aptidao);
    }
  }

  public Cidades getCidades() {
    return cidades;
  }

  public void setCidades(Cidades cidades) {
    this.cidades = cidades;
  }
  
  public Individuo[] getIndividuos(int quantidadeIndividuos){
    Individuo[] individuosSelecionados = new Individuo[2];
    for (int contador = 0; contador < quantidadeIndividuos; contador++){
      individuosSelecionados[contador] = this.getIndividuo(Util.random(this.tamanhoPopulacao));
    }
    
    return individuosSelecionados;
  }
  
  public Populacao obterSubPopulacao(int quantidadePopulacao){
    return new Populacao(this.getIndividuos(quantidadePopulacao), this.cidades);
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Individuo">
class Individuo{
  private ArrayList<Integer> genes;
  private Caminhos caminhos;
  private double aptidao = 0;

  public Individuo(ArrayList<Integer> genes) {
    this.setGenes(genes);
    this.calcularAptidao();
  }

  public Individuo(Cidades cidades) {
    this.genes = new ArrayList<>(cidades.quantidadeCidades());
    Random random = new Random();    
    int idIndividuo = 0;
    for (int contador = 0; contador < cidades.quantidadeCidades(); contador++){
      do {        
        idIndividuo = random.nextInt(cidades.quantidadeCidades());
      } while (genes.contains(idIndividuo));
      
      this.genes.add(idIndividuo);
    }
    
    caminhos = new Caminhos(cidades);
    calcularAptidao();   
  }

  public ArrayList<Integer> getGenes() {
    return genes;
  }

  public void setGenes(ArrayList<Integer> genes) {
    this.genes = genes;
  }
  
  public Integer getGene(int posicao){
    return this.genes.get(posicao);
  }

  public double getAptidao() {
    return aptidao;
  }

  public void setAptidao(double aptidao) {
    this.aptidao = aptidao;
  }
  
  private void calcularAptidao(){
    double soma = 0.0;
    Tupla tupla;
    for (int contador = 0; contador < this.genes.size(); contador++){
      tupla = new Tupla(this.genes.get(contador), this.genes.get(contador + 1));
      soma += caminhos.distanciaCaminho(tupla);
    }
    
    this.setAptidao(soma);
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Caminhos">
class Caminhos{
  public HashMap<Tupla, Double> caminhos;
  
  public Caminhos(Cidades cidades) {
    this.caminhos = new HashMap<Tupla, Double>();
    calcularCaminhos(cidades);
  }
  
  public void adicionarCaminho(Tupla tupla, Double distancia){
    this.caminhos.put(tupla, distancia);
  }
  
  private void calcularCaminhos(Cidades cidades){
    for (int contador = 1; contador <= cidades.quantidadeCidades(); contador++){
      Cidade cidadeInicial = cidades.getCidade(contador);
      for (int proximo = contador + 1; proximo <= cidades.quantidadeCidades(); proximo++){
        Cidade cidadeProxima = cidades.getCidade(proximo);
        this.adicionarCaminho(new Tupla(cidadeInicial.getId(), cidadeProxima.getId()), cidadeInicial.distanciaCidade(cidadeProxima));
      }
    }
  }
  
  public Double distanciaCaminho(Tupla tupla){
    if (!this.caminhos.containsKey(tupla))
      return Util.DISTANCIA_PADRAO;
    
    return this.caminhos.getOrDefault(tupla, Util.DISTANCIA_PADRAO);
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cidades">
class Cidades{
  private HashMap<Integer, Cidade> listaCidades = new HashMap<Integer, Cidade>();
  
  public Cidade getCidade(int posicao){
    return this.listaCidades.get(posicao);
  }
  
  public void adicionar(Cidade cidade){
    this.listaCidades.put(cidade.getId(), cidade);
  }
  
  public int quantidadeCidades(){
    return this.listaCidades.size();
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cidade">
class Cidade{
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
  
  public double getLatitudeCidade(){
    return this.coordenadas.getLatitude();
  }
  
  public double getLongitudeCidade(){
    return this.coordenadas.getLongitude();
  }
  
  public double distanciaCidade(Cidade cidade){
    double distanciaLatitude = Math.abs(this.getLatitudeCidade()- cidade.getLatitudeCidade());
    double distanciaLongitude = Math.abs(this.getLongitudeCidade()- cidade.getLongitudeCidade());
    return Math.sqrt(Math.pow(distanciaLatitude, 2) + Math.pow(distanciaLongitude, 2));
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Coordenadas">
class Coordenadas{
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
class Tupla{
  private int cidadeA;
  private int cidadeB;
  
  public Tupla(int cidadeA, int cidadeB){
    this.setCidadeA(cidadeA);
    this.setCidadeB(cidadeB);    
  }

  public int getCidadeA() {
    return cidadeA;
  }

  public void setCidadeA(int cidadeA) {
    this.cidadeA = cidadeA;
  }

  public int getCidadeB() {
    return cidadeB;
  }

  public void setCidadeB(int cidadeB) {
    this.cidadeB = cidadeB;
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Util">
class Util{
  public static int numeroDeCidades;
  public static final Double DISTANCIA_PADRAO = 0.0;
  public static int random(int limite){
    return new Random().nextInt(limite);
  }
}
//</editor-fold>