package net.zomis.server.fx;
	
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import net.zomis.custommap.CustomFacade;
import net.zomis.custommap.view.Log4jLog;
import net.zomis.events.EventExecutor;
import net.zomis.server.model.MainServer;

import org.apache.log4j.PropertyConfigurator;


public class ServerMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			PropertyConfigurator.configure("log4j.properties");
			new CustomFacade(new Log4jLog("Main")).setEventFactory(() -> new EventExecutor());
			new MainServer().start();
			
//			BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Sample.fxml"));
//			Scene scene = new Scene(root,400,400);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//			
//			ZomisGWT.setup();
//			HStoneGame game = new HStoneGame(HStoneChars.jaina("Player 1"), HStoneChars.jaina("Player 2"));
//			game.startGame();
//			game.click(game.getActionZone().getBottomCard());
//			
//			ZoneView zone = new ZoneView();
//			zone.setZone(game.getCurrentPlayer().getHand());
//			root.setTop(zone);
			
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
