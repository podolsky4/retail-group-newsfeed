package com.impltech;

import com.impltech.controller.HomeController;
import com.impltech.exception.TokenExpiredException;
import com.impltech.model.Article;
import com.impltech.service.RestClient;
import com.impltech.service.ViewManager;
import com.impltech.shared.DataStore;
import com.impltech.task.FetchImage;
import com.impltech.utils.ContentTypeChecker;
import com.impltech.utils.MyResourceBundle;
import io.socket.client.IO;
import io.socket.client.Socket;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.*;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Entry point for JavaFX application.
 *
 * <p>Release: 07.11.18</p>
 *
 * @author Yuri Podolsky, 2018-09-15
 *
 */
public class Main extends Application {

  private static final Logger logger = Logger.getLogger(HomeController.class.getName());
  private static final MyResourceBundle bundle = new MyResourceBundle(new Locale("ru", "RU"));

  // SOCKET.IO instance
  private static Socket socket;
  private static final ExecutorService executorService = Executors.newCachedThreadPool();
  // application stage is stored so that it can be shown and hidden based on system tray icon operations.
  private static Stage mainWindow;
  private static DataStore dataStore;


  public static DataStore getDataStore() {
    return dataStore;
  }

  public static void setDataStore(DataStore dataStore) {
    Main.dataStore = dataStore;
  }

  @Override
  public void start(Stage stage) throws Exception {

    // Set proxy settings
    /*System.setProperty("http.proxyHost","proxy.esrf.fr");
    System.setProperty("http.proxyPort","3128");*/
    System.setProperty("java.net.useSystemProxies", "true");
    System.out.println("detecting proxies");
    List l = null;
    try {
      l = ProxySelector.getDefault().select(new URI("http://foo/bar"));
    }
    catch (URISyntaxException e) {
      e.printStackTrace();
    }
    if (l != null) {
      for (Object aL : l) {
        Proxy proxy = (Proxy) aL;
        System.out.println("proxy type: " + proxy.type());

        InetSocketAddress addr = (InetSocketAddress) proxy.address();

        if (addr == null) {
          System.out.println("No Proxy");
        } else {
          String host = addr.getHostName();
          int port = addr.getPort();

          System.setProperty("java.net.useSystemProxies", "false");
          System.setProperty("http.proxyHost", host);
          System.setProperty("http.proxyPort", ""+port);
          System.out.println("proxy hostname: " + addr.getHostName());
          System.setProperty("http.proxyHost", addr.getHostName());
          System.out.println("proxy port: " + addr.getPort());
          System.setProperty("http.proxyPort", Integer.toString(addr.getPort()));
        }
      }
    }

    mainWindow = stage;
    // instructs the javafx system not to exit implicitly when the last application window is shut.
    Platform.setImplicitExit(false);

    // sets up the tray icon (using awt code run on the swing thread).
    javax.swing.SwingUtilities.invokeLater(this::addAppToTray);
    Parent root;
    RestClient client = RestClient.getInstance();
    client.deserializeUser();

    ViewManager viewManager = ViewManager.getInstance();

    if (client.user() != null) {
      try {
        dataStore = DataStore.initialize();
        root = viewManager.loadHomeView();
      } catch(TokenExpiredException e) {
        logger.warning(e.getMessage() + ", redirect to Login page");
        root = FXMLLoader.load(Main.class.getResource("/fxml/LoginView.fxml"));
      }
    } else {
      root = FXMLLoader.load(Main.class.getResource("/fxml/LoginView.fxml"));
    }
    mainWindow.setOnCloseRequest(event -> mainWindow.hide());
    viewManager.rootView(root);

    Platform.runLater(() -> {
      try {
        socket = IO.socket(RestClient.HOST_URL);
      } catch (URISyntaxException e) {
        logger.warning("Cannot initiate socket instance" + e.getMessage());
      }
      socket.on(Socket.EVENT_CONNECT, args -> {
        logger.info("Socket opened id: " + socket.id());
        if (client.user() != null) {
          client.emit(socket);
        }
      }).on("update", args -> {
        JSONObject jsonObject = (JSONObject) args[0];
        logger.info("Received notification from server with object: " + jsonObject);

          String jsonString2 = jsonObject.toString();
          JsonReader jsonReader = Json.createReader(new StringReader(jsonString2));
          JsonObject value = jsonReader.readObject();

          Platform.runLater(() -> {
            try {
              viewManager.showNotificationPopUp(jsonObject);
              Article newArticle = new Article(value);
              String imgUrl = ContentTypeChecker.check(newArticle);
              FetchImage fetchImage = new FetchImage(imgUrl);
              fetchImage.setOnSucceeded(worker -> newArticle.setImageSmallProperty(fetchImage.getValue()));
              executorService().submit(fetchImage);
              ObservableList<Article> newCollection = dataStore.getArticles();
              dataStore.getArticles().add(0, newArticle);
//              dataStore.getArticles().set(0, newArticle);
              dataStore.getArticles().addAll(newCollection.subList(1, newCollection.size()));
            } catch (JSONException e) {
              logger.severe("Cannot parse " + e.getMessage());
            }
          });


      }).on(Socket.EVENT_DISCONNECT, args -> logger.info("Socket closed"));
      socket.connect();
    });

  }
  /**
   * Sets up a system tray icon for the application.
   */
  private void addAppToTray() {
    try {
      // ensure awt toolkit is initialized.
      java.awt.Toolkit.getDefaultToolkit();

      // app requires system tray support, just exit if there is no support.
      if (!java.awt.SystemTray.isSupported()) {
        logger.warning("No system tray support, application exiting.");
        Platform.exit();
      }

      // set up a system tray icon.
      java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
      java.awt.Image image = ImageIO.read(ClassLoader.getSystemResource("img/tray.png"));
      java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);

      // if the user double-clicks on the tray icon, show the main app stage.
      trayIcon.addActionListener(event -> Platform.runLater(this::showStage));

      // if the user selects the default menu item (which includes the app name),
      // show the main app stage.
      java.awt.MenuItem openItem = new java.awt.MenuItem(bundle.getString("tray.menu.open"));
      openItem.addActionListener(event -> Platform.runLater(this::showStage));

      // the convention for tray icons seems to be to set the default icon for opening
      // the application stage in a bold font.
      java.awt.Font defaultFont = java.awt.Font.decode(null);
      java.awt.Font boldFont = defaultFont.deriveFont(java.awt.Font.BOLD);
      openItem.setFont(boldFont);

      // to really exit the application, the user must go to the system tray icon
      // and select the exit option, this will shutdown JavaFX and remove the
      // tray icon (removing the tray icon will also shut down AWT).
      java.awt.MenuItem exitItem = new java.awt.MenuItem(bundle.getString("tray.menu.quit"));
      exitItem.addActionListener(event -> {
        socket.close();
        Platform.exit();
        executorService.shutdownNow();
        System.exit(0);
        tray.remove(trayIcon);
      });

      // setup the popup menu for the application.
      final java.awt.PopupMenu popup = new java.awt.PopupMenu();
      popup.add(openItem);
      popup.addSeparator();
      popup.add(exitItem);
      trayIcon.setPopupMenu(popup);


      // add the application tray icon to the system tray.
      tray.add(trayIcon);
    } catch (java.awt.AWTException | IOException e) {
      System.out.println("Unable to init system tray");
      e.printStackTrace();
    }
  }

  /**
   * Shows the application stage and ensures that it is brought ot the front of all stages.
   */
  private void showStage() {
    if (mainWindow != null) {
      mainWindow.show();
      mainWindow.toFront();
    }
  }

  public static ExecutorService executorService() {
    return executorService;
  }

  // Get main window
  public static Stage getMainWindow() {
    return mainWindow;
  }

  // Get socket
  public static Socket getSocket() {
    return socket;
  }

  public static MyResourceBundle getBundle() {
    return bundle;
  }
  /**
   * @param args the command line arguments
   */
  public static void main(final String[] args)
  {
    try {
      Main.launch(args);
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.getMessage());
      try
      {
        PrintWriter pw = new PrintWriter(new File("exception.txt"));
        e.printStackTrace(pw);
        pw.close();
      }
      catch (IOException e1)
      {
        e1.printStackTrace();
      }
    } finally {
      executorService.shutdownNow();
      System.exit(0);
    }
  }
}

