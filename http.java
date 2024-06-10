import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
public class http extends JFrame {
    private Timer timer;
    private int currentIndex;
    private DefaultListModel<String> movieListModel;
    private JSONArray results;
    private JList<String> movieList;
    private JLabel imageLabel;

    public http() {
        setTitle("Movie Slideshow");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create input fields and fetch button
        JTextField genreField = new JTextField(20);
        JTextField languageField = new JTextField(20);
        JButton fetchButton = new JButton("Fetch Movies");

        fetchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String genre = genreField.getText();
                String lang = languageField.getText();
                fetchMovies(genre, lang);
                startSlideshow();
            }
        });

        // Create list to display movie details
        movieListModel = new DefaultListModel<>();
        movieList = new JList<>(movieListModel);

        // Create label to display movie images
        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(400, 300));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Create panel to hold input fields and fetch button
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Enter genre:"));
        inputPanel.add(genreField);
        inputPanel.add(new JLabel("Enter language:"));
        inputPanel.add(languageField);
        inputPanel.add(fetchButton);
        // Create panel to hold the movie list and image
        JPanel moviePanel = new JPanel(new BorderLayout());
        moviePanel.add(new JScrollPane(movieList), BorderLayout.WEST);
        moviePanel.add(imageLabel, BorderLayout.CENTER);
        // Add components to the frame
        add(inputPanel, BorderLayout.NORTH);
        add(moviePanel, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
    public void fetchMovies(String genre, String lang) {
        try {
            String apiUrl = "https://ott-details.p.rapidapi.com/advancedsearch?start_year=2020&end_year=2020&min_imdb=6&max_imdb=7.8&genre=" + genre + "&language=" + lang + "&type=movie&sort=latest&page=1";

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("X-RapidAPI-Key", "0c41303e37msh3ca9c5049b62129p1efd89jsndaaf78084778");
            conn.setRequestProperty("X-RapidAPI-Host", "ott-details.p.rapidapi.com");
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            results = (JSONArray) json.get("results");
            currentIndex = 0;

            // Clear the movie list model
            movieListModel.clear();

            if (results != null && !results.isEmpty()) {
                // Iterate over the fetched movies and add their details to the list model
                for (Object obj : results) {
                    JSONObject movie = (JSONObject) obj;
                    String title = (String) movie.get("title");
                    Number rating = (Number) movie.get("imdbrating");
                    movieListModel.addElement(title + " (Rating: " + rating + ")");
                }
            } else {
                // Display a message in the GUI when no movies are found
                movieListModel.addElement("No movies found for the given genre and language.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startSlideshow() {
        timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showNextMovie();
            }
        });
        timer.start();
    }

    public void showNextMovie() {
        if (results != null && !results.isEmpty()) {
            if (currentIndex >= results.size()) {
                currentIndex = 0; // Reset index to loop through the movies
            }

            JSONObject movie = (JSONObject) results.get(currentIndex);
            JSONArray imgUrls = (JSONArray) movie.get("imageurl");
            String imageUrl = "https://img.freepik.com/free-vector/oops-404-error-with-broken-robot-concept-illustration_114360-5529.jpg?w=740&t=st=1690003703~exp=1690004303~hmac=46853b6fe675852fc58699c8329f90a72e08c82a00cc1cf537f9011a6972a46c";
            if (imgUrls != null && !imgUrls.isEmpty()) {
                imageUrl = (String) imgUrls.get(0);
            }
            // Load the image from the URL using ImageIcon
            try {
                ImageIcon imageIcon = new ImageIcon(new URL(imageUrl));
                imageLabel.setIcon(imageIcon);
            } catch (Exception e) {
                // Handle any errors while loading the image
                e.printStackTrace();
            }
            currentIndex++;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            http slideshow = new http();
            slideshow.setVisible(true);
        });
    }
}