package clients.clients;
import server.models.Course;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;


public class Client {

    private static final String SERVER_HOST = "127.0.0.1"; // Adresse IP
    private static final int SERVER_PORT = 1337; // Port du serveur

    /**
     * ici le code est executer avec le server pour donner les question au client apropos de ses choix de cours
     * @param args argument utiliser
     */
    public static void main(String[] args) {
        try {
            // Se connecter au serveur
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connexion au serveur établie.");

            // Créer les flux de communication avec le serveur
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));

            // Lire l'entrée de l'utilisateur pour choisir la fonctionnalité
            System.out.println("Veuillez choisir une fonctionnalité :");
            System.out.println("1. Récupérer la liste des cours disponibles pour une session donnée");
            System.out.println("2. Faire une demande d'inscription à un cours");
            System.out.print("Choix : ");
            int choix = Integer.parseInt(bufferedReader.readLine());

            // En fonction du choix de l'utilisateur, exécuter la fonctionnalité correspondante
            switch (choix) {
                case 1:
                    // Fonction 1 : Récupérer la liste des cours disponibles pour une session donnée
                    System.out.println("Veuillez entrer la session pour laquelle vous voulez consulter a liste des cours : ");
                    System.out.println("1. Automne");
                    System.out.println("2. Hiver");
                    System.out.println("3. ete");
                    System.out.print("Choix : ");
                    int choixCours = Integer.parseInt(bufferedReader.readLine());
                    switch(choixCours) {
                        case 1:
                            System.out.println("Les cours offerts pendant la session d'automne sont : ");
                            System.out.println("1. IFT1015      Programmation1");
                            System.out.print("Choix : ");
                            break;
                        case 2:
                            System.out.println("Les cours offerts pendant la session d'hiver sont : ");
                            System.out.println("1. IFT2256      Base de donnees");
                            System.out.println("2. IFT1025      Programmation2");
                            System.out.print("Choix : ");
                            break;
                        case 3:
                            System.out.println("Les cours offerts pendant la session d'ete sont : ");
                            System.out.println("1. IFT2256      Base de donnees");
                            System.out.print("Choix : ");

                    }

                    String session = bufferedReader.readLine();
                    // Envoyer la requête au serveur
                    objectOutputStream.writeObject("F1");
                    objectOutputStream.writeObject(session);
                    objectOutputStream.flush();
                    // Recevoir la liste des cours du serveur
                    ArrayList<Course> courses = (ArrayList<Course>) objectInputStream.readObject();
                    // Afficher la liste des cours
                    System.out.println("Liste des cours pour la session " + session + " : ");
                    for (Course course : courses) {
                        System.out.println(course.getCode() + " - " + course.getNom());
                    }
                    break;
                case 2:
                    // Fonction 2: Faire une demande d'inscription à un cours
                    System.out.print("Veuillez entrer le code du cours auquel vous souhaitez vous inscrire : ");
                    String codeCours = bufferedReader.readLine();
                    // Envoyer la requête au serveur
                    objectOutputStream.writeObject("F2");
                    objectOutputStream.writeObject(codeCours);
                    objectOutputStream.flush();
                    // Recevoir la réponse du serveur
                    String reponse = (String) objectInputStream.readObject();
                    // Afficher la réponse
                    System.out.println(reponse);
                    break;
                default:
                    System.out.println("Choix invalide. Veuillez choisir une fonctionnalité valide.");
            }

            // Fermer les flux et la connexion
            objectOutputStream.close();
            objectInputStream.close();
            bufferedReader.close();
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
