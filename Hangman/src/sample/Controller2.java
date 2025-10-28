package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Controller2 {
    public TextField tf5;
    public TextField tf4;
    public TextField tf3;
    public TextField tf2;
    @FXML
    ImageView img;
    Image image2 = new Image(getClass().getResourceAsStream("images/2.png"));
    Image image3 = new Image(getClass().getResourceAsStream("images/3.png"));
    Image image4 = new Image(getClass().getResourceAsStream("images/4.png"));
    Image image5 = new Image(getClass().getResourceAsStream("images/5.png"));
    Image image6 = new Image(getClass().getResourceAsStream("images/6.png"));
    Image image7 = new Image(getClass().getResourceAsStream("images/7.png"));

    @FXML
    TextField tf1;
    @FXML
    TextField tf6;
    @FXML
    TextField input;
    @FXML
    private Text timerText;

    List<String> words = new ArrayList<>();
    String currentWord;
    int life = 6;
    private Timeline timeline;
    private int timeSeconds = 30;

    public void initialize() {
        System.out.println("Initializing Controller2...");

        // Αρχικοποίησε τα text fields
        clearTextFields();

        // Φόρτωσε τις λέξεις
        loadWordsFromFile();

        // Διαβεβαίωση ότι υπάρχουν λέξεις πριν την randomizeWord
        if (words.isEmpty()) {
            System.err.println("No words loaded! Using default words.");
            useDefaultWords();
        }

        randomizeWord();
        setHint();
        startTimer();

        System.out.println("Current word: " + currentWord);
    }

    private void loadWordsFromFile() {
        try {
            // Προσπάθεια 1: Από resources
            InputStream inputStream = getClass().getResourceAsStream("words.txt");
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        words.add(trimmedLine.toUpperCase());
                    }
                }
                reader.close();
                System.out.println("Loaded " + words.size() + " words from resources");
            } else {
                // Προσπάθεια 2: Από file system
                String workingDir = System.getProperty("user.dir");
                String filePath = workingDir + File.separator + "src" + File.separator + "sample" + File.separator + "words.txt";
                File file = new File(filePath);

                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String trimmedLine = line.trim();
                        if (!trimmedLine.isEmpty()) {
                            words.add(trimmedLine.toUpperCase());
                        }
                    }
                    reader.close();
                    System.out.println("Loaded " + words.size() + " words from file system: " + filePath);
                } else {
                    // Προσπάθεια 3: Default words
                    System.err.println("Words file not found in both resources and file system");
                    useDefaultWords();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            useDefaultWords();
        }
    }

    private void useDefaultWords() {
        String[] defaultWords = {"TOMATO", "POTATO", "CARROT", "PEPPER", "CHERRY",
                "PAPAYA", "BANANA", "CELERY", "GRAPES", "ORANGE"};
        words.addAll(Arrays.asList(defaultWords));
        System.out.println("Using default words: " + words.size() + " words loaded");
    }

    private void randomizeWord() {
        if (words.isEmpty()) {
            System.err.println("Words list is empty in randomizeWord!");
            useDefaultWords();
        }

        Random random = new Random();
        int randomIndex = random.nextInt(words.size());
        currentWord = words.get(randomIndex);
        System.out.println("Randomized word: " + currentWord);
    }

    private void clearTextFields() {
        tf1.clear();
        tf2.clear();
        tf3.clear();
        tf4.clear();
        tf5.clear();
        tf6.clear();
        input.clear();
    }

    public void setHint() {
        if (currentWord == null || currentWord.length() < 6) {
            System.err.println("Invalid word for hints: " + currentWord);
            return;
        }

        String firstLetter = Character.toString(currentWord.charAt(0));
        String lastLetter = Character.toString(currentWord.charAt(currentWord.length() - 1));
        tf1.setText(firstLetter);
        tf6.setText(lastLetter);
    }

    public void CheckInput() {
        String userInput = input.getText();

        // Έλεγχος για κενή εισαγωγή
        if (userInput == null || userInput.isEmpty()) {
            return;
        }

        // Πάρτε μόνο το πρώτο γράμμα αν ο χρήστης έβαλε περισσότερα
        // Μετατροπή σε κεφαλαία για σύγκριση - ΑΥΤΗ Η ΓΡΑΜΜΗ ΕΙΝΑΙ ΤΟ ΚΛΕΙΔΙ
        String letter = (userInput.length() > 1 ? userInput.substring(0, 1) : userInput).toUpperCase();

        // Έλεγχος αν είναι γράμμα (A-Z ή Ελληνικά)
        if (!letter.matches("[A-ZΑ-Ω]")) {
            // Μήνυμα λάθους (προαιρετικά)
            showErrorDialog("Παρακαλώ εισάγετε μόνο γράμματα (A-Z ή Α-Ω)");
            input.clear();
            return;
        }

        boolean found = false;
        for (int i = 0; i < currentWord.length(); i++) {
            char c = currentWord.charAt(i);
            if (String.valueOf(c).equals(letter)) {
                setLetter(i, letter);
                found = true;
            }
        }

        if (!found) {
            life--;
            setImage();
            if (life <= 0) {
                timeline.stop();
                showGameOverDialog();
            }
        } else if (isWordComplete()) {
            timeline.stop();
            showWinDialog();
        }

        input.clear(); // Καθάρισε το input field μετά από κάθε μάντεψη
    }

    public void setLetter(int index, String str) {
        switch (index) {
            case 0:
                tf1.setText(str);
                break;
            case 1:
                tf2.setText(str);
                break;
            case 2:
                tf3.setText(str);
                break;
            case 3:
                tf4.setText(str);
                break;
            case 4:
                tf5.setText(str);
                break;
            case 5:
                tf6.setText(str);
                break;
        }
    }

    public void setImage() {
        switch (life) {
            case 5:
                img.setImage(image2);
                break;
            case 4:
                img.setImage(image3);
                break;
            case 3:
                img.setImage(image4);
                break;
            case 2:
                img.setImage(image5);
                break;
            case 1:
                img.setImage(image6);
                break;
            case 0:
                img.setImage(image7);
                break;
        }
    }

    private void startTimer() {
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(1),
                        actionEvent -> {
                            timeSeconds--;
                            updateTimerText();
                            if (timeSeconds <= 0) {
                                timeIsUp();
                            }
                        }));
        timeline.playFromStart();
    }

    private void updateTimerText() {
        timerText.setText(Integer.toString(timeSeconds));
    }

    private void timeIsUp() {
        timeline.stop();
        life = 0;
        img.setImage(image7);
        showGameOverDialog();
    }

    private void resetTimer() {
        timeSeconds = 30;
        updateTimerText();
        if (timeline != null) {
            timeline.stop();
        }
        startTimer();
    }

    private boolean isWordComplete() {
        if (currentWord == null || currentWord.length() != 6) {
            return false;
        }

        return tf1.getText().equals(String.valueOf(currentWord.charAt(0))) &&
                tf2.getText().equals(String.valueOf(currentWord.charAt(1))) &&
                tf3.getText().equals(String.valueOf(currentWord.charAt(2))) &&
                tf4.getText().equals(String.valueOf(currentWord.charAt(3))) &&
                tf5.getText().equals(String.valueOf(currentWord.charAt(4))) &&
                tf6.getText().equals(String.valueOf(currentWord.charAt(5)));
    }

    private void showWinDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Νίκη");
        alert.setHeaderText(null);
        alert.setContentText("Συγχαρητήρια! Κέρδισες!\nΗ λέξη ήταν: " + currentWord);
        alert.showAndWait();
    }

    private void showGameOverDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Δυστυχώς έχασες!\nΗ λέξη ήταν: " + currentWord);
        alert.showAndWait();
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Λάθος Εισαγωγή");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void changeScene(ActionEvent event) throws IOException {
        // Επαναφορά του παιχνιδιού
        resetGame();

        Parent parent = FXMLLoader.load(getClass().getResource("gameScene.fxml"));
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
        window.setTitle("Hangman Game");
        window.setScene(new Scene(parent, 800, 650));
        window.show();
    }

    private void resetGame() {
        life = 6;
        timeSeconds = 30;
        clearTextFields();
        words.clear();

        if (timeline != null) {
            timeline.stop();
        }
    }
}