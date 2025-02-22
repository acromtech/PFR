import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;

/**
 * Auteur : Alexis GIBERT / Elio GENSON (Recherche complexe)
*/
public class Bus{

    //ID
    private static final int MASTER_ID                              = 0x00;
    //private static final int BROADCAST                              = 0xFF;

    //Adresses
    public static final int ADDR_CONNECTED                          = 0x01;
    public static final int ADDR_INDEXATION_TEXTE                   = 0x10;     //(TX_FRAME) Demande l'indexation des données textuelles (+ ACK)
    public static final int ADDR_INDEXATION_IMAGE                   = 0x11;     //(TX_FRAME) Demande l'indexation des données photographique (+ ACK)
    public static final int ADDR_INDEXATION_SON                     = 0x12;     //(TX_FRAME) Demande l'indexation des données sonores (+ ACK)
    public static final int ADDR_INDEXATION_ALL                     = 0x13;     //(TX_FRAME) Demande l'indexation de toutes les données (+ ACK)
    public static final int ADDR_TEXTE_MOTCLE                       = 0x20;     //(TX_FRAME) Demande de réaliser une recherche textuelle par mot clé (+ ACK)
    public static final int ADDR_TEXTE_FICHIER                      = 0x21;     //(TX_FRAME) Demande de réaliser une recherche textuelle par fichier (+ ACK)
    public static final int ADDR_IMAGE_MOTCLE                       = 0x30;     //(TX_FRAME) Demande de réaliser une recherche photographique par mot clé (+ ACK)
    public static final int ADDR_IMAGE_FICHIER                      = 0x31;     //(TX_FRAME) Demande de réaliser une recherche photographique par fichier (+ ACK) 
    public static final int ADDR_SON_FICHIER                        = 0x40;     //(TX_FRAME) Demande de réaliser une recherche sonore par fichier (+ ACK)
    public static final int ADDR_SCORE_CHEMIN                       = 0x50;     //(RX_FRAME) Récupère les données de résulats (+ ACK)
    public static final int ADDR_STOP_BUS                           = 0x60;     //(TX_FRAME) Ordonne l'arret de la connection au bus virtuel (+ ACK)
    public static final int ADDR_OPEN_MODE                          = 0x70;     //(TX_FRAME) Demande l'ouverture ou non de l'indexation (+ ACK)
    public static final int ADDR_SUPPRIMER_DESCRIPTEUR              = 0x23;     //(TX_FRAME) Demande l'ouverture ou non de l'indexation (+ ACK)
   
    //Attributs
   private Ivy bus;
   private boolean affConsole=false;       //Active ou désactive les affichage console

   //Constructeur
   public Bus(String port){
        bus = new Ivy(""+MASTER_ID,"",null);
        try{
            bus.start(port);
            if(affConsole) System.out.println("JAVA\tInitialisation de la communication effectuée avec succès");
        }catch (IvyException e) {
            System.out.println("La connexion au bus a échoué");
        }
   }

   /**
    * Ajoute un moteur au bus : Compile et lance la cible du Makefile dans un thread séparé
    * @param motor
    */
   protected void addMotor(Motor motor){
       System.out.println("motor:"+motor.getId()+",master:"+MASTER_ID);
       try {
           subscribers(motor);
       } catch (IvyException e) {
           e.printStackTrace();
       }
       new Thread(new Runnable() {
           public void run() {
               try {
                   ProcessBuilder pb = new ProcessBuilder("make",""+motor.getMakeTarget());
                   pb.directory(new File(System.getProperty("user.dir") + motor.getPathMakefile()));
                   pb.redirectErrorStream(true);
                   Process p = pb.start();
                   BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                   String line1;
                   while ((line1 = reader.readLine()) != null) {
                       System.out.println(line1);
                   }
                   p.waitFor();
               } catch (IOException | InterruptedException e) {
                   System.out.println("JAVA\tErreur 1 : La cible du makefile ("+motor.getMakeTarget()+") ou le répertoire de travail est incorrect");
               }
           }
       }).start();
       synchronized (bus) { 
           while(!motor.getConnection()){
               try {
                   bus.wait();
               } catch (InterruptedException e) {
                   System.out.println("JAVA\tErreur 3 : L'ajout du moteur "+motor.getId()+" de chemin "+motor.getMakeTarget()+" a échoué");
               }
           }
       }
   }
   
   /**
    * Retire le moteur du bus
    * @param motor
    */
   protected void removeMotor(Motor motor) {
       if(motor.getConnection()) sendFrame(motor,ADDR_STOP_BUS,"",0);
   }

   /**
    * S'abonne aux trames liés au programme JAVA
    * @param motor
    * @throws IvyException
    */
   private void subscribers(Motor motor) throws IvyException{
       bus.bindMsg("^"+MASTER_ID+" "+motor.getId()+" (.*) (.*) (.*)",new IvyMessageListener(){
           public void receive(IvyClient client, String[] args){
               if(Integer.parseInt(args[1])==0xFF && Integer.parseInt(args[2])==0xFF){
                   motor.setAck(true);
                   synchronized (bus) { bus.notify(); }
               } else if(Integer.parseInt(args[1])==0xFF && Integer.parseInt(args[2])==0xFE){
                   motor.setEndProcessing(true);
                   synchronized (bus) { bus.notify(); }
               } else if(Integer.parseInt(args[0])==ADDR_SCORE_CHEMIN) {
                   motor.addScorePaths(new ScorePath(Integer.parseInt(args[1]), args[2]));
                   try {
                       bus.sendMsg(motor.getId()+" "+MASTER_ID+" "+ADDR_SCORE_CHEMIN+" "+0x00+" "+0x00);
                   } catch (IvyException e) {
                       e.printStackTrace();
                   }
               } else if(Integer.parseInt(args[0])==ADDR_CONNECTED){
                   motor.setConnection(true);
                   synchronized (bus) { bus.notify(); }
               }
           }
       });
       motor.clearScorePaths();
   }

   /**
    * Lance un traitement d'indexation
    * @param motor
    * @param addrTypeTraitement
    */
   protected void launchIndexation(Motor motor,final int addrTypeTraitement){
       if(addrTypeTraitement==ADDR_INDEXATION_ALL || addrTypeTraitement==ADDR_INDEXATION_TEXTE || addrTypeTraitement==ADDR_INDEXATION_IMAGE || addrTypeTraitement==ADDR_INDEXATION_SON){
           if(motor.getConnection()) sendFrame(motor,addrTypeTraitement,""+0x00,0x00);
           else System.out.println("JAVA\tErreur : Impossible d'envoyer la requete car le moteur "+motor.getId()+" est déconnecté");
       } else System.out.println("JAVA\tErreur : Le traitement que vous tenter de lancer n'est pas un traitement d'indexation");
   }
   
   /**
    * Envoi une trame sur le bus
    * Forme de la trame : MOTOR ID - MASTER ID - TYPE DE TRAITEMENT - DATA1 - DATA2
    * @param motor
    * @param addrTypeTraitement
    * @param data1
    * @param data2
    */
   protected void sendFrame(Motor motor, final int addrTypeTraitement, String data1, int data2){
       if(affConsole) System.out.println("JAVA\tTraitement "+addrTypeTraitement);
       try {
            motor.setAck(false);
            motor.setEndProcessing(false);
            bus.sendMsg(motor.getId()+" "+MASTER_ID+" "+addrTypeTraitement+" "+data1+" "+data2);
            synchronized (bus) { while(!motor.getAck()) bus.wait(); }
            if(affConsole) System.out.println("JAVA\tACK");
            synchronized (bus) { while(!motor.getEndProcessing()) bus.wait(); }
            if(affConsole) System.out.println("JAVA\tTraitement effectué");
       } catch (IvyException | InterruptedException e) {
           System.out.println("L'envoi de la trame "+motor.getId()+" "+MASTER_ID+" "+addrTypeTraitement+" "+data1+" "+data2+" a échoué");
       }
   }

   /**
    * Lance une requete sur un moteur de recherche
    * @param motor
    * @param addrTypeTraitement
    * @param requete
    * @param nombreResultatMax
    * @return
    */
   private List<ScorePath> sendWord(Motor motor, final int addrTypeTraitement,String requete,int nombreResultatMax){
       motor.clearScorePaths();
       motor.setScorePath();
       System.out.println(motor.getScorePaths().isEmpty());
       if(addrTypeTraitement==ADDR_TEXTE_FICHIER || addrTypeTraitement==ADDR_TEXTE_MOTCLE || addrTypeTraitement==ADDR_IMAGE_FICHIER  || addrTypeTraitement==ADDR_IMAGE_MOTCLE || addrTypeTraitement==ADDR_SON_FICHIER){
           if(motor.getConnection()){
               sendFrame(motor,addrTypeTraitement,requete,nombreResultatMax);
               sendFrame(motor,ADDR_SCORE_CHEMIN,""+0x00,0x00);
           }
           else System.out.println("JAVA\tErreur : Impossible d'envoyer la requete car le moteur "+motor.getId()+" est déconnecté");
       }else System.out.println("JAVA\tErreur : Le traitement que vous tentez de lancer n'est pas un traitement de recherche");
       return motor.getScorePaths();
   }

   /**
    * Détermine le score à partir d'une requete donnée
    * @param motor
    * @param typeTraitement
    * @param recherche
    * @param nbResultMax
    * @return
    */
    protected List<ScorePath> sendRequest(Motor motor, int typeTraitement, String recherche, int nbResultMax) {
        motor.clearScorePaths();
        ArrayList<List<ScorePath>> arrayOfArrayOfScorePath = new ArrayList<>();
        ArrayList<Map<String, Integer>>arrayMapsRes = null;
        Map<String, Double> mapTotalScore = new HashMap<>();
        String[] tabRecherche = null;
        int nbNegatif = 0;
        int nbPositif = 0;
        
        System.out.println(recherche);

        //Découpe tout les mots de la requete et les range dans un tableau
        tabRecherche = recherche.trim().split(" ");

        //Pour tous les mots présent dans le tableau, on retire le signe '-' ou '+' et on lance une requete et on récupère TOUS les résultats dans une liste de liste de résultat
        for (String str : tabRecherche) {
            if (str.charAt(0) == '-' || str.charAt(0) == '+') {
                str = str.substring(1);
            }
            System.out.println(str);
            motor.clearScorePaths();
            arrayOfArrayOfScorePath.add(sendWord(motor, typeTraitement, str, nbResultMax));
            
        }

        //Conversion et prétraitement des données
        arrayMapsRes = Tools.generateMaps(arrayOfArrayOfScorePath);
        if (tabRecherche.length == 1 && !(tabRecherche[0].charAt(0) == '-' || tabRecherche[0].charAt(0) == '+')) {
            for (ScorePath score : arrayOfArrayOfScorePath.get(0)) {
                mapTotalScore.put(score.getPath(), (double) score.getScore());
            }
            return Tools.trieMapOrdreDecroiss(mapTotalScore);
        }

        int i = 0;
        for (String s : tabRecherche) {
            if (s.charAt(0) == '-') {
                arrayMapsRes.set(i, Tools.negateMapValues(arrayMapsRes.get(i)));
                nbNegatif++;
            } else nbPositif++;
            i++;
        }

        mapTotalScore = calculateTotalScores(arrayMapsRes, tabRecherche, nbPositif, nbNegatif);
        motor.clearScorePaths();
        return Tools.trieMapOrdreDecroiss(mapTotalScore);
    }

    /**
     * Calcule les scores finaux a partir des scores des différents mots
     * @param maps
     * @return
     */
    private Map<String, Double> calculateTotalScores(ArrayList<Map<String, Integer>> maps, String[] tabRecherche, int nbPositif, int nbNegatif) {
        Map<String, Double> totalScores = new HashMap<>();
        for (Map<String, Integer> map : maps) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                int i = 0;
                double score = 0;
                if (tabRecherche[i].charAt(0) == '+') score = (double) entry.getValue() / nbPositif;
                else if (tabRecherche[i].charAt(0) == '-') score = (double) entry.getValue() / nbNegatif;
                if (totalScores.containsKey(entry.getKey())) score += totalScores.get(entry.getKey());
                if(score > 0) totalScores.put(entry.getKey(),score);
            }
        }
        return totalScores;
    }

   /**
    * Arrête le bus
    */
   protected void stopBus() {
    bus.stop();
    if(affConsole) System.out.println("JAVA\tBus arrêté avec succès");
}

    /**
     * Affiche les informations critiques sur le terminal
    * @param affConsole
    */
    protected void setAffConsole(boolean affConsole){
        this.affConsole=affConsole;
    }

    /**
     * Renvoi l'état de affConsole
    * @return
    */
    protected boolean getAffConsole(){
        return affConsole;
    }
}
