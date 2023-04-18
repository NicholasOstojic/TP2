package server;

import javafx.util.Pair;
import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * la classe server qui gere les exception, se qui dirige les input du client et la relation client-server
 */
public class Server {

    /**
     * le string inscrire
     */
    public final static String REGISTER_COMMAND = "INSCRIRE";
    /**
     * le string charger
     */
    public final static String LOAD_COMMAND = "CHARGER";
    private final ServerSocket server;
    private Socket client;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final ArrayList<EventHandler> handlers;

    /**
     * une autre exeption
     * @param port place ou le server est heberger
     * @throws IOException une autre exeption
     */
    public Server(int port) throws IOException {
        this.server = new ServerSocket(port, 1);
        this.handlers = new ArrayList<EventHandler>();
        this.addEventHandler(this::handleEvents);
    }

    /**
     * ajout une commande
     * @param h c'est la commande qui sera ajouter
     */
    public void addEventHandler(EventHandler h) {
        this.handlers.add(h);
    }

    private void alertHandlers(String cmd, String arg) {
        for (EventHandler h : this.handlers) {
            h.handle(cmd, arg);
        }
    }

    /**
     * le sign in, gerer le client
     */
    public void run() {
        while (true) {
            try {
                client = server.accept();
                System.out.println("Connecté au client: " + client);
                objectInputStream = new ObjectInputStream(client.getInputStream());
                objectOutputStream = new ObjectOutputStream(client.getOutputStream());
                listen();
                disconnect();
                System.out.println("Client déconnecté!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * attend pour la prochaine commande
     * @throws IOException les exeption
     * @throws ClassNotFoundException une exeption quand une classe rechercher n'est pas trouver
     */
    public void listen() throws IOException, ClassNotFoundException {
        String line;
        if ((line = this.objectInputStream.readObject().toString()) != null) {
            Pair<String, String> parts = processCommandLine(line);
            String cmd = parts.getKey();
            String arg = parts.getValue();
            this.alertHandlers(cmd, arg);
        }
    }

    /**
     * cree des paire de commande et argument
     * @param line commande total de commande et argument
     * @return retour la paire fonction finale
     */
    public Pair<String, String> processCommandLine(String line) {
        String[] parts = line.split(" ");
        String cmd = parts[0];
        String args = String.join(" ", Arrays.asList(parts).subList(1, parts.length));
        return new Pair<>(cmd, args);
    }

    /**
     * pour deconnecter le client du server
     * @throws IOException declaration de problem avec objectOutputStream ou objectInputStream
     */
    public void disconnect() throws IOException {
        objectOutputStream.close();
        objectInputStream.close();
        client.close();
    }

    /**
     * ici ca gere si handleLoadCourses ou handleRegistration est appeler
     * @param cmd ici ca gere commande dans events
     * @param arg valeur de argument de la command
     */
    public void handleEvents(String cmd, String arg) {
        if (cmd.equals(REGISTER_COMMAND)) {
            handleRegistration();
        } else if (cmd.equals(LOAD_COMMAND)) {
            handleLoadCourses(arg);
        }
    }

    /**
     Lire un fichier texte contenant des informations sur les cours et les transofmer en liste d'objets 'Course'.
     La méthode filtre les cours par la session spécifiée en argument.
     Ensuite, elle renvoie la liste des cours pour une session au client en utilisant l'objet 'objectOutputStream'.
     La méthode gère les exceptions si une erreur se produit lors de la lecture du fichier ou de l'écriture de l'objet dans le flux.
     @param arg la session pour laquelle on veut récupérer la liste des cours
     */
    public void handleLoadCourses(String arg) {
        try {
            // Lire le fichier texte contenant les informations sur les cours
            BufferedReader bufferedReader = new BufferedReader(new FileReader("cours.txt"));
            ArrayList<Course> courses = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Diviser la ligne en différents attributs du cours
                String[] parts = line.split("\t");
                String session = parts[0].trim();
                String code = parts[1].trim();
                String nom = parts[2].trim();
                // Vérifier si la session correspond à celle spécifiée en argument
                if (session.equals(arg)) {
                    // Créer un objet Course et l'ajouter à la liste des cours pour cette session
                    Course course = new Course(session, code, nom);
                    courses.add(course);
                }
            }
            bufferedReader.close();

            // Envoyer la liste des cours pour cette session au client
            objectOutputStream.writeObject(courses);
            objectOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     Récupérer l'objet 'RegistrationForm' envoyé par le client en utilisant 'objectInputStream', l'enregistrer dans un fichier texte
     et renvoyer un message de confirmation au client.
     La méthode gére les exceptions si une erreur se produit lors de la lecture de l'objet, l'écriture dans un fichier ou dans le flux de sortie.
     */
    public void handleRegistration() {
        try {
            // Lire l'objet RegistrationForm du flux d'entrée
            RegistrationForm registrationForm = (RegistrationForm) objectInputStream.readObject();

            // Enregistrer les informations dans un fichier texte
            FileOutputStream fileOutputStream = new FileOutputStream("inscription.txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(registrationForm);
            objectOutputStream.close();

            // Envoyer un message de confirmation au client
            String message = "Inscription enregistrée avec succès!";
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

