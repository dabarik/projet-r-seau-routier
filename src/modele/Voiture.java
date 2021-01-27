package modele;

import application.ControleTrafic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Voiture
{
    /**identifiant de la voiture*/
    private int id;
    /**coordonnée x de la voiture*/
    double x;
    /**coordonnée y de la voiture*/
    double y;
    /**liste des noeuds du trajet prevu*/
    private List<Noeud> trajet;
    /**liste des restant a parcourir*/
    public List<Noeud> routeRestante;
    /**noeud de depart*/
    private Noeud origine;
    /**noeud de destination finale*/
    private Noeud destination;
    /**dernier noeud suivi*/
    private Noeud noeudSuivi;
    /**pause pendant le parcours*/
    private boolean pause;
    /**indique si la voiture est arrivee*/
    private boolean arrivee;
    /**accident pendant le parcours*/
    private boolean accident;
    /**indique si la voiture est dans un bouchon*/
    private boolean bouchon;
    /**tps avant remorquage*/
    final static int REMORQUAGE = 10;
    /**temps de panne*/
    private int tpsPanne = 0;

    /**indique si la voiture se deplace de gauche a droite*/
    private boolean ligne;

    private int tpsPriorite =0;


    Voiture(){}
    /**construit une voiture
     * @param id identifiant de la voiture
     * */
    Voiture(int id){
        this.id = id;
        trajet = new ArrayList<>();
        routeRestante = new ArrayList<>();
    }
    /**construit une voiture
     * @param id identifiant de la voiture
     * @param origine noeud de depart
     * @param destination noeud d'arrivee
     * */
    public Voiture(int no, Noeud origine, Noeud destination, boolean ligne) {
        this(no);
        this.origine = origine;
        this.noeudSuivi = this.origine;
        x = origine.x;
        y = origine.y;
        this.destination = destination;
        calculerRoute();
        for(Noeud n:trajet)routeRestante.add(n);
        this.ligne = ligne;
    }
    /**construit une voiture
     * @param id identifiant de la voiture
     * @param xo yo coordonnees du noeud d'origine
     * @param xd yd coordonnees du noeud de destinatin
     * */
    public Voiture(int no, int xo, int yo, int xd, int yd) {
        this(no);
        this.origine = ReseauRoutier.getNoeud(xo, yo);
        this.noeudSuivi = this.origine;
        x = origine.x;
        y = origine.y;
        this.destination = ReseauRoutier.getNoeud(xd, yd);
        calculerRoute();
        for(Noeud n:trajet)routeRestante.add(n);
    }

    /**calcul la route entre origine et destination
     * (suppose que ces points soient aient une meme abscisse ou ordonnee)*/
    public void calculerRoute()
    {
        boolean enLigne = (origine.getY()==destination.getY());
        Noeud suivant = origine;
        while (!suivant.equals(destination))
        {
            List<Arc> arcSortants = suivant.getArcSortants();
            if(!arcSortants.isEmpty())
            {
                for(Arc arc:arcSortants)
                {
                    suivant = arc.getEnd();
                    if(suivant==null) System.err.println("pb de suivant : " + arc);
                    if (enLigne && suivant.getY()==destination.getY()) break;
                    if(!enLigne && suivant.getX()==destination.getX()) break;
                }
                trajet.add(suivant);
            }
            else
            {
                System.err.println("pb avec ce noeud " + suivant);
                break;
            }
        }
    }

    /** retourne le prochain noeud de la route (depile routeRestante)
     * @return le prochain noeud de la route (null si non)*/
    public Noeud prochainNoeud()
    {
        Noeud suivant = null;

        //TODO: modifier ci dessous pour prendre en compte la notion de bouchon, d'arrivee, .
        if(!routeRestante.isEmpty() && !bouchon){
            suivant = routeRestante.get(0);
            this.x= suivant.getX();
            this.y= suivant.getY();
            suivant.addCar(this);
            routeRestante.remove(0);
        }
        if(routeRestante.isEmpty()){
            arrivee = true;
        }

        return suivant;
    }



    /**verifie si la voiture est dans un bouchon*/
    public void verifBouchon()
    {
        Noeud suivant = null;

        //System.out.println(this);

        if(!routeRestante.isEmpty() && !verifPriorite())
        {
            suivant = routeRestante.get(0);
            //TODO: verifier s'il y a un bouchon et changer alors l'etat de la voiture
            if(suivant.cars.size()==1 && suivant.cars.get(0).isPause()){
                this.pause=true;
                this.setBouchon(true);
            }
            else if(suivant.cars.size()>1 && !pause){
                if(suivant.cars.get(0).isBouchon() || suivant.cars.get(0).isAccident()){
                    this.pause=true;
                    this.setBouchon(true);
                }
            }
            else if(suivant.cars.size()==1 && suivant.cars.get(0).ligne == !this.ligne){
                this.pause=true;
                this.setBouchon(true);
            }
            else if(pause && suivant.cars.size()==0 && !isAccident() && bouchon){
                this.pause=false;
                this.setBouchon(false);
            }
        }

    }

    public boolean verifPriorite() {
        Noeud suivant = null;

        if (!routeRestante.isEmpty() && !this.ligne) {
            suivant = routeRestante.get(0);

            double x = suivant.x;
            double y = suivant.y;

            double[] list = {1, 2, 3, 4};

            boolean possede = false;

            //System.out.println(y);

            for (int i = 0; i < list.length; i++) {
                if (y == list[i]) {
                    possede = true;
                }
            }
            if (possede) {
                if ((ReseauRoutier.getNoeud(x - 0.25, y).cars.size() == 1) || (ReseauRoutier.getNoeud(x - 0.5, y).cars.size() == 1)
                        || (ReseauRoutier.getNoeud(x - 0.75, y).cars.size() == 1) || (ReseauRoutier.getNoeud(x, y).cars.size() == 1)) {
                    this.pause = true;
                    this.setBouchon(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**retourne une chaine de caracteres contenant l'identfiant de la voiture et son chemin prevu*/
    public String toString()
    {
        StringBuilder sb = new StringBuilder("voiture ").append(id);
        sb.append(". chemin = ");
        for(Noeud n:trajet) sb.append(n).append("-");
        return sb.toString();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isPause() {return pause; }
    public boolean isArrivee() { return arrivee; }
    public void setPause(boolean pause)
    {
        this.pause = pause;
        if(pause && noeudSuivi!=null) noeudSuivi.addCar(this);
        if(!pause && noeudSuivi!=null) noeudSuivi.removeCar(this);
    }
    public Noeud getNoeudSuivi() { return noeudSuivi; }
    public void setNoeudSuivi(Noeud n){
        noeudSuivi=n;
    }
    public boolean isAccident() { return accident; }
    public void setAccident(boolean accident)
    {
        this.accident = accident;
        if (accident)this.pause = true;
    }
    public int getId() { return id; }
    public boolean isBouchon() { return bouchon; }
    public void setBouchon(boolean bouchon) { this.bouchon = bouchon; }

    public void incrementeTpsPanne() {tpsPanne++; }
    public boolean isARemorquer() {
        //TODO: le vehicule est a remorquer si le tps de panne depasse le seuil
        if(tpsPanne>=10){
            setAccident(false);
            return true;
        }
        return false;
    }
}
