public class Main {
  
}

//<editor-fold defaultstate="collapsed" desc="Classe Populacao">
class Populacao{
  private Individuo[] individuos;
  private int tamanho;

  public Populacao(int tamanhoPopulacao) {
    this.tamanho = tamanhoPopulacao;
    inicializaIndividuos();
  }
  
  private void inicializaIndividuos(){
    this.individuos = new Individuo[this.tamanho];
    for (int contador = 0; contador < this.tamanho; contador++) {
        individuos[contador] = null;
    }    
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Individuo">
class Individuo{
  private Cromossomo cromossomos;
  private int aptidao = 0;
  
  public Individuo(){
    this.cromossomos = new Cromossomo();
  }
  
  public Individuo(Cromossomo cromossomos){
    this.cromossomos = cromossomos;
  }
  
  public int getGene(int posicao) {
    return cromossomos.getGene(posicao);
  }
  
  public void setGene(int posicao, int gene){
    this.cromossomos.setGene(posicao, gene);
  }
  
  public Cromossomo getCromossomos() {
    return cromossomos;
  }
  
  public void setCromossomos(Cromossomo cromossomos) {
    this.cromossomos = cromossomos;
  }
  
  public int getAptidao() {
    return aptidao;
  }
  
  public void setAptidao(int aptidao) {
    this.aptidao = aptidao;
  }
}
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="Classe Cromossomo">
class Cromossomo{
  private int[] genes;
  
  public int getGene(int posicao) {
    return this.genes[posicao];
  }
  
  public void setGene(int posicao, int gene){
    this.genes[posicao] = gene;
  }
}
//</editor-fold>
